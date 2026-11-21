package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
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

        int preset = 1;
        boolean wasUnjamming = false;
        ElapsedTime loopTimer = new ElapsedTime();

        // Pre-match check: these lines must say OK before you queue.
        while (!isStarted() && !isStopRequested()) {
            telemetry.addLine("PRE-MATCH CHECK");
            telemetry.addData("PIDF on hub", shooter.hubPidfMatches() ? "OK (matches ShooterConfig)" : "MISMATCH, re-init!");
            telemetry.addData("Battery", "%.2f V %s", shooter.getVoltage(),
                    shooter.getVoltage() < 12.5 ? "<- swap to a fresh battery" : "OK");
            telemetry.addData("Presets", "%.0f / %.0f / %.0f (cap %.0f)",
                    PRESETS[0], PRESETS[1], PRESETS[2], ShooterConfig.maxTarget());
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

            boolean unjam = gamepad1.b;
            if (unjam) {
                shooter.setTarget(ShooterConfig.UNJAM_VELOCITY);
                shooter.spinUp();
            } else {
                if (wasUnjamming) shooter.stop();   // releasing B stops; press A to spin up again
                shooter.setTarget(PRESETS[preset]);
            }
            wasUnjamming = unjam;

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

            telemetry.addData("Preset", "%s (%.0f ticks/s)", PRESET_NAMES[preset], PRESETS[preset]);
            telemetry.addData("Drive", precision ? "PRECISION" : "normal");
            shooter.addTelemetry(telemetry);
            telemetry.addData("Loop", "%.1f ms", loopMs);
            telemetry.update();
        }

        for (DcMotor m : driveMotors) m.setPower(0);
        shooter.shutdown();
    }
}
