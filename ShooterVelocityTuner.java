package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Shooter Velocity Tuner", group = "Diagnostic")
public class ShooterVelocityTuner extends LinearOpMode {

    private static final String LF_MOTOR_NAME = "lfMotor";
    private static final String LB_MOTOR_NAME = "lbMotor";
    private static final String RF_MOTOR_NAME = "rfMotor";
    private static final String RB_MOTOR_NAME = "rbMotor";
    private static final String SHOOTER_MOTOR_NAME = "shooterMotor";
    private static final String INTAKE_MOTOR_NAME = "intakeMotor";

    private static final DcMotorSimple.Direction LF_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction LB_DIRECTION = DcMotorSimple.Direction.REVERSE;
    private static final DcMotorSimple.Direction RF_DIRECTION = DcMotorSimple.Direction.FORWARD;
    private static final DcMotorSimple.Direction RB_DIRECTION = DcMotorSimple.Direction.FORWARD;

    private static final double STRAFE_SIGN = 1;
    private static final double TURN_SIGN = 1;
    private static final double SPEED_SCALE = 1.0;

    private static final double B_VELOCITY = 1450;
    private static final double A_VELOCITY = -900;
    private static final double VELOCITY_STEP = 100;
    private static final double MAX_TARGET_VELOCITY = 6000;
    private static final double INTAKE_POWER_STEP = 0.05;

    private double shooterTarget = 0;
    private boolean shooterRunning = false;

    private double intakePower = 0;
    private int intakeDirection = 1;
    private boolean intakeRunning = false;

    private boolean lastRightBumper = false;
    private boolean lastLeftBumper = false;
    private boolean lastDpadUp = false;
    private boolean lastDpadDown = false;

    @Override
    public void runOpMode() {
        DcMotor lf = hardwareMap.get(DcMotor.class, LF_MOTOR_NAME);
        DcMotor lb = hardwareMap.get(DcMotor.class, LB_MOTOR_NAME);
        DcMotor rf = hardwareMap.get(DcMotor.class, RF_MOTOR_NAME);
        DcMotor rb = hardwareMap.get(DcMotor.class, RB_MOTOR_NAME);
        DcMotorEx shooterMotor = hardwareMap.get(DcMotorEx.class, SHOOTER_MOTOR_NAME);
        DcMotor intakeMotor = hardwareMap.get(DcMotor.class, INTAKE_MOTOR_NAME);

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

        shooterMotor.setDirection(DcMotorEx.Direction.FORWARD);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        telemetry.addLine("Ready.");
        telemetry.addLine("DRIVE: left stick = drive + strafe, right stick = turn");
        telemetry.addLine("SHOOTER (velocity): B = +1450, A = -900, X = stop, bumpers = +/-100 live");
        telemetry.addLine("INTAKE (power): dpad up/down +/-0.05, left = positive, right = negative, Y = stop");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            double drive = -gamepad1.left_stick_y;
            double strafe = STRAFE_SIGN * -gamepad1.left_stick_x;
            double turn = TURN_SIGN * -gamepad1.right_stick_x;

            double lfP = drive - strafe - turn;
            double rfP = drive + strafe + turn;
            double lbP = drive + strafe - turn;
            double rbP = drive - strafe + turn;

            double max = Math.max(1.0, Math.max(Math.max(Math.abs(lfP), Math.abs(rfP)),
                    Math.max(Math.abs(lbP), Math.abs(rbP))));
            lf.setPower(lfP / max * SPEED_SCALE);
            rf.setPower(rfP / max * SPEED_SCALE);
            lb.setPower(lbP / max * SPEED_SCALE);
            rb.setPower(rbP / max * SPEED_SCALE);

            if (gamepad1.b) {
                shooterTarget = B_VELOCITY;
                shooterRunning = true;
            } else if (gamepad1.a) {
                shooterTarget = A_VELOCITY;
                shooterRunning = true;
            } else if (gamepad1.x) {
                shooterRunning = false;
            }

            boolean rbBumper = gamepad1.right_bumper;
            boolean lbBumper = gamepad1.left_bumper;
            if (shooterRunning && shooterTarget != 0) {
                double sign = Math.signum(shooterTarget);
                double magnitude = Math.abs(shooterTarget);
                if (rbBumper && !lastRightBumper) {
                    magnitude = Math.min(MAX_TARGET_VELOCITY, magnitude + VELOCITY_STEP);
                }
                if (lbBumper && !lastLeftBumper) {
                    magnitude = Math.max(0, magnitude - VELOCITY_STEP);
                }
                shooterTarget = sign * magnitude;
            }
            lastRightBumper = rbBumper;
            lastLeftBumper = lbBumper;

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

            double shooterCommanded = shooterRunning ? shooterTarget : 0.0;
            if (shooterRunning) {
                shooterMotor.setVelocity(shooterCommanded);
            } else {
                shooterMotor.setPower(0);
            }

            double intakeCommanded = intakeRunning ? intakeDirection * intakePower : 0.0;
            intakeMotor.setPower(intakeCommanded);

            double actual = shooterMotor.getVelocity();
            telemetry.addData("Drive / Strafe / Turn", "%.2f / %.2f / %.2f", drive, strafe, turn);

            telemetry.addLine("--- SHOOTER (velocity) ---");
            telemetry.addData("State", shooterRunning
                    ? (shooterTarget > 0 ? "POSITIVE (B)" : "NEGATIVE (A)") : "STOPPED");
            telemetry.addData("Target velocity", "%.0f ticks/sec", shooterCommanded);
            telemetry.addData("Actual velocity", "%.0f ticks/sec", actual);
            telemetry.addData("Error", "%.0f", shooterCommanded - actual);
            telemetry.addData("Encoder position", shooterMotor.getCurrentPosition());

            telemetry.addLine("--- INTAKE (power) ---");
            telemetry.addData("State", intakeRunning
                    ? (intakeDirection > 0 ? "POSITIVE (LEFT)" : "NEGATIVE (RIGHT)") : "STOPPED");
            telemetry.addData("Power setting", "%.2f", intakePower);
            telemetry.addData("Power output", "%.2f", intakeCommanded);
            telemetry.update();
        }

        for (DcMotor m : driveMotors) m.setPower(0);
        shooterMotor.setPower(0);
        intakeMotor.setPower(0);
    }
}
