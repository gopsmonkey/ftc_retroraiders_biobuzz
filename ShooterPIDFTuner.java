package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Shooter PIDF Tuner", group = "Diagnostic")
public class ShooterPIDFTuner extends LinearOpMode {

    private static final String SHOOTER_MOTOR_NAME = "shooterMotor";
    private static final String INTAKE_MOTOR_NAME = "intakeMotor";

    private static final double SHOOTER_SIGN = 1;
    private static final double START_TARGET_VELOCITY = 1400;
    private static final double VELOCITY_STEP = 50;
    private static final double MAX_TARGET_VELOCITY = 6000;

    private static final double INTAKE_POWER = 0.8;

    private static final double SHOT_DIP_THRESHOLD = 0.93;
    private static final double RECOVERED_THRESHOLD = 0.98;

    private static final String[] PARAM_NAMES = {"F", "P", "I", "D"};
    private static final double[] BASE_STEPS = {0.5, 1.0, 0.1, 0.1};
    private static final double[] MULTIPLIERS = {0.1, 1.0, 10.0};

    private final double[] pidf = new double[4];
    private final double[] defaults = new double[4];

    private double targetVelocity = START_TARGET_VELOCITY;
    private boolean shooterRunning = false;

    private boolean armed = false;
    private double lowest = Double.MAX_VALUE;
    private double highest = 0;
    private boolean inDip = false;
    private double currentDipLow = 0;
    private double dipStartMs = 0;
    private int shotCount = 0;
    private double lastDipPercent = 0;
    private double lastRecoveryMs = 0;
    private double worstRecoveryMs = 0;

    private boolean lastRightBumper = false;
    private boolean lastLeftBumper = false;
    private boolean lastUp = false;
    private boolean lastDown = false;
    private boolean lastLeft = false;
    private boolean lastRight = false;
    private boolean lastStart = false;
    private boolean lastBack = false;
    private boolean lastLeftStick = false;
    private boolean lastRightStick = false;

    @Override
    public void runOpMode() {
        DcMotorEx shooterMotor = hardwareMap.get(DcMotorEx.class, SHOOTER_MOTOR_NAME);
        DcMotor intakeMotor = hardwareMap.get(DcMotor.class, INTAKE_MOTOR_NAME);

        shooterMotor.setDirection(DcMotorEx.Direction.FORWARD);
        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        PIDFCoefficients c = shooterMotor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
        pidf[0] = c.f;
        pidf[1] = c.p;
        pidf[2] = c.i;
        pidf[3] = c.d;
        System.arraycopy(pidf, 0, defaults, 0, 4);

        ElapsedTime timer = new ElapsedTime();
        int param = 0;
        int multIndex = 1;
        double intakeOutput = 0;

        telemetry.addLine("Ready.");
        telemetry.addLine("SHOOTER: A = start, X = stop, bumpers = target +/-50");
        telemetry.addLine("PIDF: dpad left/right = pick F/P/I/D, dpad up/down = change");
        telemetry.addLine("Stick presses = step size, Back = reset stats, Start = reset PIDF");
        telemetry.addLine("INTAKE: right trigger = forward 0.8, left trigger = reverse 0.8, Y = stop");
        telemetry.update();
        waitForStart();
        timer.reset();

        while (opModeIsActive()) {

            boolean rb = gamepad1.right_bumper;
            boolean lb = gamepad1.left_bumper;
            boolean targetChanged = false;
            if (rb && !lastRightBumper) {
                targetVelocity = Math.min(MAX_TARGET_VELOCITY, targetVelocity + VELOCITY_STEP);
                targetChanged = true;
            }
            if (lb && !lastLeftBumper) {
                targetVelocity = Math.max(0, targetVelocity - VELOCITY_STEP);
                targetChanged = true;
            }
            lastRightBumper = rb;
            lastLeftBumper = lb;

            if (gamepad1.a) {
                shooterRunning = true;
            } else if (gamepad1.x) {
                shooterRunning = false;
            }

            boolean up = gamepad1.dpad_up;
            boolean down = gamepad1.dpad_down;
            boolean left = gamepad1.dpad_left;
            boolean right = gamepad1.dpad_right;
            boolean start = gamepad1.start;
            boolean back = gamepad1.back;
            boolean leftStick = gamepad1.left_stick_button;
            boolean rightStick = gamepad1.right_stick_button;

            if (left && !lastLeft) param = (param + 3) % 4;
            if (right && !lastRight) param = (param + 1) % 4;
            if (rightStick && !lastRightStick) multIndex = Math.min(MULTIPLIERS.length - 1, multIndex + 1);
            if (leftStick && !lastLeftStick) multIndex = Math.max(0, multIndex - 1);

            double delta = BASE_STEPS[param] * MULTIPLIERS[multIndex];
            if (up && !lastUp) {
                pidf[param] = Math.max(0, pidf[param] + delta);
                applyPidf(shooterMotor);
                targetChanged = true;
            }
            if (down && !lastDown) {
                pidf[param] = Math.max(0, pidf[param] - delta);
                applyPidf(shooterMotor);
                targetChanged = true;
            }
            if (start && !lastStart) {
                System.arraycopy(defaults, 0, pidf, 0, 4);
                applyPidf(shooterMotor);
                targetChanged = true;
            }
            if ((back && !lastBack) || targetChanged) {
                resetStats();
            }

            lastUp = up;
            lastDown = down;
            lastLeft = left;
            lastRight = right;
            lastStart = start;
            lastBack = back;
            lastLeftStick = leftStick;
            lastRightStick = rightStick;

            if (gamepad1.right_trigger > 0.5) {
                intakeOutput = INTAKE_POWER;
            } else if (gamepad1.left_trigger > 0.5) {
                intakeOutput = -INTAKE_POWER;
            } else if (gamepad1.y) {
                intakeOutput = 0;
            }
            intakeMotor.setPower(intakeOutput);

            double commanded = shooterRunning ? SHOOTER_SIGN * targetVelocity : 0.0;
            if (shooterRunning) {
                shooterMotor.setVelocity(commanded);
            } else {
                shooterMotor.setPower(0);
            }

            double actual = shooterMotor.getVelocity();
            double speed = Math.abs(actual);

            if (shooterRunning && targetVelocity > 0) {
                if (!armed && speed >= 0.95 * targetVelocity) armed = true;
                if (armed) {
                    lowest = Math.min(lowest, speed);
                    highest = Math.max(highest, speed);
                    if (!inDip && speed < SHOT_DIP_THRESHOLD * targetVelocity) {
                        inDip = true;
                        shotCount++;
                        dipStartMs = timer.milliseconds();
                        currentDipLow = speed;
                    }
                    if (inDip) {
                        currentDipLow = Math.min(currentDipLow, speed);
                        if (speed >= RECOVERED_THRESHOLD * targetVelocity) {
                            inDip = false;
                            lastRecoveryMs = timer.milliseconds() - dipStartMs;
                            worstRecoveryMs = Math.max(worstRecoveryMs, lastRecoveryMs);
                            lastDipPercent = (targetVelocity - currentDipLow) / targetVelocity * 100.0;
                        }
                    }
                }
            } else {
                inDip = false;
            }

            telemetry.addLine("=== PIDF | selected: " + PARAM_NAMES[param]
                    + " | step: " + String.format("%.2f", delta) + " ===");
            for (int i = 0; i < 4; i++) {
                String mark = (i == param) ? ">> " : "   ";
                telemetry.addData(mark + PARAM_NAMES[i], "%.3f (default %.3f)", pidf[i], defaults[i]);
            }

            telemetry.addLine("--- SHOOTER ---");
            telemetry.addData("State", shooterRunning ? "RUNNING" : "STOPPED");
            telemetry.addData("Target", "%.0f ticks/sec", commanded);
            telemetry.addData("Actual", "%.0f ticks/sec", actual);
            telemetry.addData("Error", "%.0f", commanded - actual);

            telemetry.addLine("--- TEST RESULTS (since reset) ---");
            if (armed) {
                telemetry.addData("Lowest speed", "%.0f (dip %.1f%%)", lowest,
                        (targetVelocity - lowest) / targetVelocity * 100.0);
                telemetry.addData("Highest speed", "%.0f (over %.1f%%)", highest,
                        (highest - targetVelocity) / targetVelocity * 100.0);
            } else {
                telemetry.addData("Lowest / Highest", "waiting for target speed");
            }
            telemetry.addData("Shots detected", shotCount);
            telemetry.addData("Last shot dip", "%.1f%%", lastDipPercent);
            telemetry.addData("Last recovery", "%.0f ms", lastRecoveryMs);
            telemetry.addData("Worst recovery", "%.0f ms", worstRecoveryMs);
            telemetry.addData("Recovering now", inDip ? "YES" : "no");

            telemetry.addLine("--- INTAKE ---");
            telemetry.addData("Power", "%.2f", intakeOutput);
            telemetry.update();
        }

        shooterMotor.setPower(0);
        intakeMotor.setPower(0);
    }

    private void resetStats() {
        armed = false;
        lowest = Double.MAX_VALUE;
        highest = 0;
        inDip = false;
        currentDipLow = 0;
        dipStartMs = 0;
        shotCount = 0;
        lastDipPercent = 0;
        lastRecoveryMs = 0;
        worstRecoveryMs = 0;
    }

    private void applyPidf(DcMotorEx motor) {
        motor.setVelocityPIDFCoefficients(pidf[1], pidf[2], pidf[3], pidf[0]);
    }
}
