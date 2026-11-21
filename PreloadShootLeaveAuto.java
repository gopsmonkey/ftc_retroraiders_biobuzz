package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Simplest useful AUTO: shoot the 4 preloads from the start spot, then LEAVE.
 * Every number marked TUNE must be set on the practice field before the first match.
 * Set SHOOT_FIRST = false for a LEAVE-only auto (3 pts, can't go wrong).
 */
@Autonomous(name = "Preload Shoot + Leave", group = "Match", preselectTeleOp = "BioBuzz TeleOp")
public class PreloadShootLeaveAuto extends LinearOpMode {

    private static final boolean SHOOT_FIRST = true;
    private static final double SHOOT_VELOCITY = ShooterConfig.PRESET_MID; // TUNE: speed that hits from the start spot
    private static final double SPINUP_TIMEOUT_S = 3.0;  // if not at speed by then, feed anyway
    private static final double FEED_TIME_S = 6.0;       // TUNE: long enough for all 4 preloads
    private static final double LEAVE_POWER = 0.4;       // TUNE: + = forward, - = backward
    private static final double LEAVE_TIME_S = 1.0;      // TUNE: just enough to get off the wall; stay on our side (G402)

    @Override
    public void runOpMode() {
        ShooterController.enableBulkCaching(hardwareMap);
        ShooterController shooter = new ShooterController(hardwareMap);

        DcMotor lf = hardwareMap.get(DcMotor.class, ShooterConfig.LF_MOTOR_NAME);
        DcMotor lb = hardwareMap.get(DcMotor.class, ShooterConfig.LB_MOTOR_NAME);
        DcMotor rf = hardwareMap.get(DcMotor.class, ShooterConfig.RF_MOTOR_NAME);
        DcMotor rb = hardwareMap.get(DcMotor.class, ShooterConfig.RB_MOTOR_NAME);
        lf.setDirection(DcMotorSimple.Direction.REVERSE);
        lb.setDirection(DcMotorSimple.Direction.REVERSE);
        rf.setDirection(DcMotorSimple.Direction.FORWARD);
        rb.setDirection(DcMotorSimple.Direction.FORWARD);
        DcMotor[] drive = {lf, lb, rf, rb};
        for (DcMotor m : drive) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }

        while (!isStarted() && !isStopRequested()) {
            telemetry.addData("PIDF on hub", shooter.hubPidfMatches() ? "OK" : "MISMATCH, re-init!");
            telemetry.addData("Battery", "%.2f V", shooter.getVoltage());
            telemetry.addData("Plan", SHOOT_FIRST ? "shoot preloads, then leave" : "leave only");
            telemetry.update();
            idle();
        }

        ElapsedTime t = new ElapsedTime();
        if (SHOOT_FIRST && opModeIsActive()) {
            shooter.spinUp(SHOOT_VELOCITY);
            t.reset();
            while (opModeIsActive() && !shooter.isAtSpeed() && t.seconds() < SPINUP_TIMEOUT_S) {
                shooter.update();
                telemetry.addData("Step", "spinning up");
                shooter.addTelemetry(telemetry);
                telemetry.update();
            }
            if (!shooter.isAtSpeed()) shooter.setGateEnabled(false); // never reached speed: shoot anyway
            shooter.setIntakeRequest(ShooterController.IntakeRequest.IN);
            t.reset();
            while (opModeIsActive() && t.seconds() < FEED_TIME_S) {
                shooter.update();
                telemetry.addData("Step", "shooting preloads");
                shooter.addTelemetry(telemetry);
                telemetry.update();
            }
            shooter.setIntakeRequest(ShooterController.IntakeRequest.OFF);
            shooter.stop();
            shooter.update();
        }

        for (DcMotor m : drive) m.setPower(LEAVE_POWER);
        t.reset();
        while (opModeIsActive() && t.seconds() < LEAVE_TIME_S) idle();
        for (DcMotor m : drive) m.setPower(0);
        shooter.shutdown();
    }
}
