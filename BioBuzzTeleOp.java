package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Reference match TeleOp with every software fix from the night-before list.
 *
 * If your real match TeleOp lives somewhere else, either run this one, or copy
 * the ShooterController lines (marked "FIX") into yours. Same hardware names as
 * the tuners.
 *
 * gamepad1:
 *   left stick = drive + strafe, right stick = turn, hold LT = precision drive (0.35)
 *   A = spin up at the selected preset, X = stop shooter
 *   dpad left / up / right = CLOSE / MID / FAR preset (also changes speed live if spinning)
 *   hold RT = intake: collects when shooter is off, gated feed when shooter is on
 *   hold LB = reverse intake (unjam), hold B = reverse shooter slowly (unjam)
 *   hold RB + RT = feed even if the flywheel is not at speed (override if "At speed" never turns YES)
 *   Y = flower intake down / up (toggle; only if flowerServo1/2 are in the config)
 *   Rumble: one blip when the shooter reaches speed, a long buzz when the intake looks full (4 balls)
 *
 * gamepad2 (optional):
 *   d-pad up / down = speed trim +/-25 on every preset, X = trim back to 0
 *
 * Match habits: spin up while driving to the taped spot, stop, then hold RT to shoot.
 */
@TeleOp(name = "BioBuzz TeleOp", group = "Match")
public class BioBuzzTeleOp extends LinearOpMode {

    private static final DcMotorSimple.Direction LF_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction LB_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction RF_DIRECTION = DcMotorSimple.Direction.FORWARD;
    private static final DcMotorSimple.Direction RB_DIRECTION = DcMotorSimple.Direction.FORWARD;
    private static final double STRAFE_SIGN = 1;
    private static final double TURN_SIGN = 1;

    private static final String[] PRESET_NAMES = {"CLOSE", "MID", "FAR"};
    private static final double[] PRESETS = {
            ShooterConfig.PRESET_CLOSE, ShooterConfig.PRESET_MID, ShooterConfig.PRESET_FAR};

    @Override
    public void runOpMode() {
        // FIX: bulk caching (faster loop, gate reacts sooner).
        ShooterController.enableBulkCaching(hardwareMap);
        // FIX: hardcoded PIDF applied here in init, every time, so a power cycle can't lose it.
        ShooterController shooter = new ShooterController(hardwareMap);

        DcMotor lf = hardwareMap.get(DcMotor.class, ShooterConfig.LF_MOTOR_NAME);
        DcMotor lb = hardwareMap.get(DcMotor.class, ShooterConfig.LB_MOTOR_NAME);
        DcMotor rf = hardwareMap.get(DcMotor.class, ShooterConfig.RF_MOTOR_NAME);
        DcMotor rb = hardwareMap.get(DcMotor.class, ShooterConfig.RB_MOTOR_NAME);
        lf.setDirection(LF_DIRECTION);
        lb.setDirection(LB_DIRECTION);
        rf.setDirection(RF_DIRECTION);
        rb.setDirection(RB_DIRECTION);
        DcMotor[] driveMotors = {lf, lb, rf, rb};
        for (DcMotor m : driveMotors) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        Servo flower1 = hardwareMap.tryGet(Servo.class, ShooterConfig.FLOWER_SERVO_1_NAME);
        Servo flower2 = hardwareMap.tryGet(Servo.class, ShooterConfig.FLOWER_SERVO_2_NAME);
        if (flower2 != null) flower2.setDirection(Servo.Direction.REVERSE);
        // No setPosition() here: nothing may move during INIT / the AUTO-to-TELEOP transition (G403).

        int preset = 1;
        boolean wasUnjamming = false;
        double trim = 0;
        boolean flowerDown = false;
        boolean flowerCommanded = false;
        boolean lastY = false, lastTrimUp = false, lastTrimDown = false;
        boolean wasAtSpeed = false, wasFull = false;
        ElapsedTime loopTimer = new ElapsedTime();

        // Pre-match check: these lines must say OK before you queue.
        while (!isStarted() && !isStopRequested()) {
            telemetry.addLine("PRE-MATCH CHECK");
            telemetry.addData("PIDF on hub", shooter.hubPidfMatches() ? "OK (matches ShooterConfig)" : "MISMATCH, re-init!");
            telemetry.addData("Battery", "%.2f V %s", shooter.getVoltage(),
                    shooter.getVoltage() < 12.5 ? "<- swap to a fresh battery" : "OK");
            telemetry.addData("Presets", "%.0f / %.0f / %.0f (cap %.0f)",
                    PRESETS[0], PRESETS[1], PRESETS[2], ShooterConfig.maxTarget());
            telemetry.addData("Flower servos", flower1 != null || flower2 != null ? "found" : "not in config (Y does nothing)");
            telemetry.update();
            idle();
        }
        loopTimer.reset();

        while (opModeIsActive()) {
            double loopMs = loopTimer.milliseconds();
            loopTimer.reset();

            // ---- Drive (FIX: precision mode for lining up shots) ----
            boolean precision = gamepad1.left_trigger > 0.5;
            double speedScale = precision ? ShooterConfig.PRECISION_DRIVE_SPEED : ShooterConfig.DRIVE_SPEED;
            double drive = -gamepad1.left_stick_y;
            double strafe = STRAFE_SIGN * -gamepad1.left_stick_x;
            double turn = TURN_SIGN * -gamepad1.right_stick_x;
            double lfP = drive - strafe - turn;
            double rfP = drive + strafe + turn;
            double lbP = drive + strafe - turn;
            double rbP = drive - strafe + turn;
            double max = Math.max(1.0, Math.max(Math.max(Math.abs(lfP), Math.abs(rfP)),
                    Math.max(Math.abs(lbP), Math.abs(rbP))));
            lf.setPower(lfP / max * speedScale);
            rf.setPower(rfP / max * speedScale);
            lb.setPower(lbP / max * speedScale);
            rb.setPower(rbP / max * speedScale);

            // ---- Shooter (FIX: presets, capped at 80% of maxV inside setTarget) ----
            if (gamepad1.dpad_left) preset = 0;
            else if (gamepad1.dpad_up) preset = 1;
            else if (gamepad1.dpad_right) preset = 2;

            if (gamepad1.a) shooter.spinUp();
            else if (gamepad1.x) shooter.stop();

            // Live trim (gamepad2): fix "all shots short/long" at the event without editing code.
            if (gamepad2.dpad_up && !lastTrimUp) trim = Math.min(ShooterConfig.TRIM_LIMIT, trim + ShooterConfig.TRIM_STEP);
            if (gamepad2.dpad_down && !lastTrimDown) trim = Math.max(-ShooterConfig.TRIM_LIMIT, trim - ShooterConfig.TRIM_STEP);
            if (gamepad2.x) trim = 0;
            lastTrimUp = gamepad2.dpad_up;
            lastTrimDown = gamepad2.dpad_down;

            boolean unjam = gamepad1.b;
            if (unjam) {
                shooter.setTarget(ShooterConfig.UNJAM_VELOCITY);
                shooter.spinUp();
            } else {
                if (wasUnjamming) shooter.stop();   // releasing B stops; press A to spin up again
                shooter.setTarget(PRESETS[preset] + trim);
            }
            wasUnjamming = unjam;

            // ---- Flower intake servos ----
            if (gamepad1.y && !lastY) {
                flowerDown = !flowerDown;
                flowerCommanded = true;
            }
            lastY = gamepad1.y;
            if (flowerCommanded) {
                double pos = flowerDown ? ShooterConfig.FLOWER_DOWN : ShooterConfig.FLOWER_UP;
                if (flower1 != null) flower1.setPosition(pos);
                if (flower2 != null) flower2.setPosition(pos);
            }

            // ---- Intake (FIX: at-speed gate + full-power / velocity feed) ----
            if (gamepad1.left_bumper) {
                shooter.setIntakeRequest(ShooterController.IntakeRequest.OUT);
            } else if (gamepad1.right_trigger > 0.5) {
                shooter.setIntakeRequest(ShooterController.IntakeRequest.IN);
            } else {
                shooter.setIntakeRequest(ShooterController.IntakeRequest.OFF);
            }

            // FIX: emergency override. Hold RB (with RT) to feed even if the gate never opens.
            shooter.setGateEnabled(!gamepad1.right_bumper);

            shooter.update();

            // Driver feedback without looking at telemetry.
            boolean atSpeed = shooter.isAtSpeed();
            boolean full = shooter.isIntakeFull();
            if (atSpeed && !wasAtSpeed) gamepad1.rumbleBlips(1);
            if (full && !wasFull) gamepad1.rumble(600);
            wasAtSpeed = atSpeed;
            wasFull = full;

            telemetry.addData("Preset", "%s (%.0f ticks/s, trim %+.0f)", PRESET_NAMES[preset], PRESETS[preset], trim);
            telemetry.addData("Flower", !flowerCommanded ? "not moved yet (Y)" : flowerDown ? "DOWN" : "UP");
            telemetry.addData("Drive", precision ? "PRECISION" : "normal");
            shooter.addTelemetry(telemetry);
            telemetry.addData("Loop", "%.1f ms", loopMs);
            telemetry.update();
        }

        for (DcMotor m : driveMotors) m.setPower(0);
        shooter.shutdown();
    }
}
