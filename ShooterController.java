package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * Shooter + intake logic shared by every OpMode (TeleOp and Autonomous).
 *
 * What it fixes, from the night-before fix list:
 *  - PIDF is hardcoded and re-applied in init (hub forgets it on power cycle).
 *  - Targets are clamped to 80% of measured max velocity.
 *  - The intake only feeds while the flywheel is at speed (at-speed gate).
 *  - Feeding uses velocity control (encoder) or full power, not 0.8 open loop.
 *  - Optional battery-scaled F on spin-up.
 *
 * Usage, every loop:
 *   shooter.setIntakeRequest(...);   // what the driver wants the intake to do
 *   shooter.update();                // commands both motors
 */
public class ShooterController {

    public enum IntakeRequest {
        /** Intake off. */
        OFF,
        /** Shooter off: collect at INTAKE_COLLECT_POWER. Shooter on: gated feed into the flywheel. */
        IN,
        /** Reverse at collect power (clearing jams). Never gated. */
        OUT
    }

    private final DcMotorEx shooter;
    private final DcMotorEx intake;
    private final VoltageSensor[] voltageSensors;
    private final ElapsedTime clock = new ElapsedTime();

    private double p = ShooterConfig.SHOOTER_P;
    private double i = ShooterConfig.SHOOTER_I;
    private double d = ShooterConfig.SHOOTER_D;
    private double f = ShooterConfig.SHOOTER_F;
    private double appliedF = f;
    private PIDFCoefficients hubPidf;

    private double target = ShooterConfig.PRESET_MID;
    private boolean running = false;
    private boolean wasRunning = false;
    private boolean gateEnabled = true;
    private boolean voltageCompensate = ShooterConfig.VOLTAGE_COMPENSATE_F;

    private IntakeRequest intakeRequest = IntakeRequest.OFF;
    private boolean intakeManaged = true;
    private double velocity = 0;
    private boolean atSpeed = false;
    private boolean feeding = false;
    private double feedStartMs = -1;
    private double intakeOutput = 0;
    private String intakeMode = "power";

    private double voltage = 0;
    private double lastVoltageReadMs = -1e9;

    private double intakeCurrent = 0;
    private double lastCurrentReadMs = -1e9;
    private boolean intakeFull = false;

    public ShooterController(HardwareMap hardwareMap) {
        shooter = hardwareMap.get(DcMotorEx.class, ShooterConfig.SHOOTER_MOTOR_NAME);
        intake = hardwareMap.get(DcMotorEx.class, ShooterConfig.INTAKE_MOTOR_NAME);
        int n = 0;
        for (VoltageSensor ignored : hardwareMap.voltageSensor) n++;
        voltageSensors = new VoltageSensor[n];
        n = 0;
        for (VoltageSensor s : hardwareMap.voltageSensor) voltageSensors[n++] = s;

        shooter.setDirection(DcMotorEx.Direction.FORWARD);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intake.setDirection(DcMotorEx.Direction.FORWARD);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        if (ShooterConfig.INTAKE_HAS_ENCODER) {
            intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            intake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        } else {
            intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        // Hardcoded PIDF goes on in init, right after RUN_USING_ENCODER.
        readVoltage();
        applyPidf();
        clock.reset();
    }

    /** Bulk caching AUTO: one hub read per loop instead of one per sensor call. Call once in init. */
    public static void enableBulkCaching(HardwareMap hardwareMap) {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
    }

    // ---------------- Commands ----------------

    /** Sets the flywheel target in ticks/s (magnitude; sign comes from ShooterConfig). Clamped to 80% of maxV. */
    public void setTarget(double ticksPerSecond) {
        double max = ShooterConfig.maxTarget();
        target = Math.max(-max, Math.min(max, ticksPerSecond));
    }

    public void spinUp() { running = true; }
    public void spinUp(double ticksPerSecond) { setTarget(ticksPerSecond); running = true; }
    public void stop() { running = false; }
    public void setIntakeRequest(IntakeRequest request) { intakeRequest = request; }
    /** Tuners can turn the gate off to compare. Matches should always leave it on. */
    public void setGateEnabled(boolean enabled) { gateEnabled = enabled; }
    public void setVoltageCompensation(boolean enabled) {
        voltageCompensate = enabled;
        readVoltage();
        applyPidf();
    }

    /** For the PIDF tuner: change gains live (they are NOT saved; copy them into ShooterConfig). */
    public void setPidf(double p, double i, double d, double f) {
        this.p = p;
        this.i = i;
        this.d = d;
        this.f = f;
        applyPidf();
    }

    /** Call once per loop, after setting the requests. */
    public void update() {
        double now = clock.milliseconds();

        // Re-read voltage on spin-up (and every 2 s while running) so scaled F tracks the battery.
        boolean spinUpEdge = running && !wasRunning;
        if (spinUpEdge || now - lastVoltageReadMs > 2000) {
            readVoltage();
            if (voltageCompensate && running) applyPidf();
        }
        wasRunning = running;

        if (running && target != 0) {
            shooter.setVelocity(ShooterConfig.SHOOTER_SIGN * target);
        } else {
            shooter.setPower(0);
        }

        velocity = shooter.getVelocity() * ShooterConfig.SHOOTER_SIGN;
        double goal = Math.abs(target);
        atSpeed = running && target > 0
                && Math.abs(goal - velocity) < ShooterConfig.AT_SPEED_TOLERANCE * goal;

        updateIntake(now);
        updateIntakeCurrent(now);
    }

    /**
     * Current isn't part of the bulk read (it costs its own hub call), so read it at most
     * every 100 ms, and only while collecting, which is when "full" matters.
     */
    private void updateIntakeCurrent(double now) {
        boolean collecting = intakeManaged && "collect".equals(intakeMode);
        if (!collecting) {
            intakeFull = false;
            return;
        }
        if (now - lastCurrentReadMs < 100) return;
        lastCurrentReadMs = now;
        intakeCurrent = intake.getCurrent(CurrentUnit.AMPS);
        intakeFull = ShooterConfig.INTAKE_FULL_CURRENT_AMPS > 0
                && intakeCurrent >= ShooterConfig.INTAKE_FULL_CURRENT_AMPS;
    }

    /**
     * false = update() leaves the intake motor alone, for OpModes that drive it
     * themselves (the Velocity Tuner's power sweep). They should still check isAtSpeed().
     */
    public void setIntakeManaged(boolean managed) { intakeManaged = managed; }

    private void updateIntake(double now) {
        if (!intakeManaged) return;
        boolean wantFeed = intakeRequest == IntakeRequest.IN && running && target > 0;

        if (wantFeed) {
            boolean inPushWindow = feeding && now - feedStartMs < ShooterConfig.FEED_MIN_PUSH_MS;
            boolean open = !gateEnabled || atSpeed || inPushWindow;
            if (open && !feeding) feedStartMs = now;
            feeding = open;
        } else {
            feeding = false;
        }

        if (feeding) {
            if (ShooterConfig.INTAKE_HAS_ENCODER && ShooterConfig.INTAKE_FEED_VELOCITY > 0) {
                intake.setVelocity(ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_FEED_VELOCITY);
                intakeOutput = ShooterConfig.INTAKE_FEED_VELOCITY;
                intakeMode = "feed vel";
            } else {
                setIntakePower(ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_FEED_POWER, "feed pwr");
            }
        } else if (wantFeed) {
            setIntakePower(0, "GATED");
        } else if (intakeRequest == IntakeRequest.IN) {
            setIntakePower(ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_COLLECT_POWER, "collect");
        } else if (intakeRequest == IntakeRequest.OUT) {
            setIntakePower(-ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_COLLECT_POWER, "reverse");
        } else {
            setIntakePower(0, "off");
        }
    }

    private void setIntakePower(double power, String mode) {
        intake.setPower(power);
        intakeOutput = power;
        intakeMode = mode;
    }

    /** Stops both motors. Call at the end of every OpMode. */
    public void shutdown() {
        running = false;
        shooter.setPower(0);
        intake.setPower(0);
    }

    // ---------------- PIDF + voltage ----------------

    private void applyPidf() {
        appliedF = f;
        if (voltageCompensate && voltage > 1) {
            double scale = Math.min(ShooterConfig.MAX_VOLTAGE_SCALE, ShooterConfig.NOMINAL_VOLTAGE / voltage);
            appliedF = f * Math.max(1.0 / ShooterConfig.MAX_VOLTAGE_SCALE, scale);
        }
        shooter.setVelocityPIDFCoefficients(p, i, d, appliedF);
        // Read back what the hub actually holds, so telemetry proves the values loaded.
        hubPidf = shooter.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void readVoltage() {
        double min = Double.POSITIVE_INFINITY;
        for (VoltageSensor s : voltageSensors) {
            double v = s.getVoltage();
            if (v > 0) min = Math.min(min, v);
        }
        voltage = min == Double.POSITIVE_INFINITY ? 0 : min;
        lastVoltageReadMs = clock.milliseconds();
    }

    // ---------------- Readouts ----------------

    public boolean isRunning() { return running; }
    public boolean isAtSpeed() { return atSpeed; }
    public boolean isFeeding() { return feeding; }
    public double getTarget() { return target; }
    /** Flywheel speed in ticks/s, positive = shooting direction. */
    public double getVelocity() { return velocity; }
    public double getVoltage() { return voltage; }
    public double getIntakeOutput() { return intakeOutput; }
    /** Amps, refreshed every 100 ms while collecting (0 otherwise until the next read). */
    public double getIntakeCurrent() { return intakeCurrent; }
    /** True while collecting and the current is over INTAKE_FULL_CURRENT_AMPS (off when that is 0). */
    public boolean isIntakeFull() { return intakeFull; }
    public String getIntakeMode() { return intakeMode; }
    public double getP() { return p; }
    public double getI() { return i; }
    public double getD() { return d; }
    public double getF() { return f; }
    public double getAppliedF() { return appliedF; }
    public PIDFCoefficients getHubPidf() { return hubPidf; }

    /** True if the hub holds the gains this controller sent (the pre-match "PIDF matches" check). */
    public boolean hubPidfMatches() {
        if (hubPidf == null) return false;
        return Math.abs(hubPidf.p - p) < 1e-3 && Math.abs(hubPidf.i - i) < 1e-3
                && Math.abs(hubPidf.d - d) < 1e-3 && Math.abs(hubPidf.f - appliedF) < 1e-3;
    }

    /** Standard shooter telemetry block. */
    public void addTelemetry(org.firstinspires.ftc.robotcore.external.Telemetry telemetry) {
        telemetry.addLine("--- SHOOTER ---");
        telemetry.addData("State", running ? "RUNNING" : "STOPPED");
        telemetry.addData("At speed", atSpeed ? "YES" : "no");
        telemetry.addData("Target / Actual", "%.0f / %.0f ticks/s", running ? target : 0.0, velocity);
        telemetry.addData("Error", "%.0f (gate %.0f)", (running ? Math.abs(target) : 0) - velocity,
                ShooterConfig.AT_SPEED_TOLERANCE * Math.abs(target));
        telemetry.addData("Battery", "%.2f V", voltage);
        telemetry.addData("PIDF sent", "P %.3f I %.3f D %.3f F %.3f", p, i, d, appliedF);
        if (hubPidf != null) {
            telemetry.addData("PIDF on hub", "P %.3f I %.3f D %.3f F %.3f %s",
                    hubPidf.p, hubPidf.i, hubPidf.d, hubPidf.f, hubPidfMatches() ? "(OK)" : "(MISMATCH!)");
        }
        telemetry.addLine("--- INTAKE ---");
        telemetry.addData("Intake", "%s %.2f", intakeMode, intakeOutput);
        telemetry.addData("Intake current", "%.2f A%s", intakeCurrent, intakeFull ? "  FULL (4 balls?)" : "");
    }
}
