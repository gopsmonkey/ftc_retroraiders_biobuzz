package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Drive + shooter velocity + intake power sweep.
 *
 * Changes vs. the original (night-before fix list):
 *  - Hardcoded PIDF from ShooterConfig is applied in init (no more "whatever the hub held").
 *  - Shooter targets are capped at 80% of measured maxV (was 6000).
 *  - Positive (feed-direction) intake is GATED: it pauses while the shooter runs
 *    forward and is not at speed. Reverse is never gated.
 *  - Left trigger held = precision drive (0.35) for lining up shots.
 *  - Bulk caching, battery voltage and loop time on telemetry.
 */
@TeleOp(name = "Shooter Velocity Tuner", group = "Diagnostic")
public class ShooterVelocityTuner extends LinearOpMode {

    private static final DcMotorSimple.Direction LF_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction LB_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction RF_DIRECTION = DcMotorSimple.Direction.FORWARD;
    private static final DcMotorSimple.Direction RB_DIRECTION = DcMotorSimple.Direction.FORWARD;

    private static final double STRAFE_SIGN = 1;
    private static final double TURN_SIGN = 1;

    private static final double VELOCITY_STEP = 100;
    private static final double INTAKE_POWER_STEP = 0.05;

    private double shooterTarget = 0;

    private double intakePower = 0;
    private int intakeDirection = 1;
    private boolean intakeRunning = false;

    private boolean lastRightBumper = false;
    private boolean lastLeftBumper = false;
    private boolean lastDpadUp = false;
    private boolean lastDpadDown = false;

    @Override
    public void runOpMode() {
        ShooterController.enableBulkCaching(hardwareMap);
        ShooterController shooter = new ShooterController(hardwareMap);
        shooter.setIntakeManaged(false);
        DcMotor intakeMotor = hardwareMap.get(DcMotor.class, ShooterConfig.INTAKE_MOTOR_NAME);

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
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        // The tuner sweeps raw power, so the intake runs open loop here regardless of ShooterConfig.
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        ElapsedTime loopTimer = new ElapsedTime();
        ElapsedTime runTimer = new ElapsedTime();
        boolean feedOpen = false;
        double feedOpenSinceMs = 0;

        telemetry.addLine("Ready. PIDF loaded from ShooterConfig.");
        telemetry.addLine("DRIVE: left stick = drive + strafe, right stick = turn, hold LT = precision");
        telemetry.addLine(String.format("SHOOTER (velocity): B = +%.0f, A = %.0f, X = stop, bumpers = +/-100 live",
                ShooterConfig.PRESET_MID, ShooterConfig.UNJAM_VELOCITY));
        telemetry.addLine("INTAKE (power): dpad up/down +/-0.05, left = positive (gated), right = negative, Y = stop");
        telemetry.update();
        waitForStart();
        loopTimer.reset();

        while (opModeIsActive()) {
            double loopMs = loopTimer.milliseconds();
            loopTimer.reset();

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

            if (gamepad1.b) {
                shooterTarget = ShooterConfig.PRESET_MID;
                shooter.spinUp();
            } else if (gamepad1.a) {
                shooterTarget = ShooterConfig.UNJAM_VELOCITY;
                shooter.spinUp();
            } else if (gamepad1.x) {
                shooter.stop();
            }

            boolean rbBumper = gamepad1.right_bumper;
            boolean lbBumper = gamepad1.left_bumper;
            if (shooter.isRunning() && shooterTarget != 0) {
                double sign = Math.signum(shooterTarget);
                double magnitude = Math.abs(shooterTarget);
                if (rbBumper && !lastRightBumper) {
                    magnitude = Math.min(ShooterConfig.maxTarget(), magnitude + VELOCITY_STEP);
                }
                if (lbBumper && !lastLeftBumper) {
                    magnitude = Math.max(0, magnitude - VELOCITY_STEP);
                }
                shooterTarget = sign * magnitude;
            }
            lastRightBumper = rbBumper;
            lastLeftBumper = lbBumper;
            shooter.setTarget(shooterTarget);
            shooter.update();

            boolean up = gamepad1.dpad_up;
            boolean down = gamepad1.dpad_down;
            if (up && !lastDpadUp) {
                intakePower = Math.min(1.0, Math.round((intakePower + INTAKE_POWER_STEP) * 100) / 100.0);
            }
            if (down && !lastDpadDown) {
                intakePower = Math.max(0.0, Math.round((intakePower - INTAKE_POWER_STEP) * 100) / 100.0);
            }
            lastDpadUp = up;
            lastDpadDown = down;

            if (gamepad1.dpad_left) {
                intakeDirection = 1;
                intakeRunning = true;
            } else if (gamepad1.dpad_right) {
                intakeDirection = -1;
                intakeRunning = true;
            } else if (gamepad1.y) {
                intakeRunning = false;
            }

            // At-speed gate: don't push a ball into a wheel that hasn't recovered.
            boolean shootingForward = shooter.isRunning() && shooterTarget > 0;
            // Same push-through hold as ShooterController: once open, keep pushing FEED_MIN_PUSH_MS.
            boolean wantFeed = intakeRunning && intakeDirection > 0 && shootingForward;
            double nowMs = runTimer.milliseconds();
            boolean open = shooter.isAtSpeed() || (feedOpen && nowMs - feedOpenSinceMs < ShooterConfig.FEED_MIN_PUSH_MS);
            if (wantFeed && open && !feedOpen) feedOpenSinceMs = nowMs;
            feedOpen = wantFeed && open;
            boolean gated = wantFeed && !open;
            double intakeCommanded = intakeRunning && !gated ? intakeDirection * intakePower : 0.0;
            intakeMotor.setPower(ShooterConfig.INTAKE_FEED_SIGN * intakeCommanded);

            telemetry.addData("Drive / Strafe / Turn", "%.2f / %.2f / %.2f", drive, strafe, turn);
            telemetry.addData("Drive speed", "%.2f %s", speedScale, precision ? "(PRECISION)" : "");

            telemetry.addData("Shooter mode", shooter.isRunning()
                    ? (shooterTarget > 0 ? "POSITIVE (B)" : "NEGATIVE (A)") : "STOPPED");
            telemetry.addData("Target cap (80% maxV)", "%.0f", ShooterConfig.maxTarget());
            shooter.addTelemetry(telemetry);

            telemetry.addLine("--- INTAKE (power sweep) ---");
            telemetry.addData("State", !intakeRunning ? "STOPPED" : gated ? "GATED (waiting for shooter)"
                    : (intakeDirection > 0 ? "POSITIVE (LEFT)" : "NEGATIVE (RIGHT)"));
            telemetry.addData("Power setting", "%.2f", intakePower);
            telemetry.addData("Power output", "%.2f", intakeCommanded);
            telemetry.addData("Loop", "%.1f ms", loopMs);
            telemetry.update();
        }

        for (DcMotor m : driveMotors) m.setPower(0);
        shooter.shutdown();
    }
}
