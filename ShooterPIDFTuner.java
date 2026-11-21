package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Live PIDF tuning under real ball load.
 *
 * Starts from the gains in ShooterConfig (NOT whatever the hub happens to hold),
 * feeds through the same at-speed gate the match code uses, and can log a burst to CSV.
 * Gains changed here are NOT saved: copy the final P/I/D/F into ShooterConfig.
 *
 * gamepad1:
 *   A = start shooter, X = stop, bumpers = target +/-50 (capped at 80% of maxV)
 *   dpad left/right = pick F/P/I/D, dpad up/down = change, stick presses = step size
 *   Back = reset stats, Start = reset PIDF to ShooterConfig
 *   right trigger = intake IN (gated feed while shooter runs), left trigger = OUT, Y = intake off
 *   B = start/stop CSV log
 * gamepad2:
 *   A = toggle at-speed gate (compare gated vs ungated), B = toggle battery-scaled F
 */
@TeleOp(name = "Shooter PIDF Tuner", group = "Diagnostic")
public class ShooterPIDFTuner extends LinearOpMode {

    private static final double VELOCITY_STEP = 50;

    private static final double SHOT_DIP_THRESHOLD = 0.93;
    private static final double RECOVERED_THRESHOLD = 0.98;

    private static final String[] PARAM_NAMES = {"F", "P", "I", "D"};
    private static final double[] BASE_STEPS = {0.5, 1.0, 0.1, 0.1};
    private static final double[] MULTIPLIERS = {0.1, 1.0, 10.0};

    private final double[] pidf = new double[4];
    private final double[] configPidf = {
            ShooterConfig.SHOOTER_F, ShooterConfig.SHOOTER_P, ShooterConfig.SHOOTER_I, ShooterConfig.SHOOTER_D};

    private double targetVelocity = ShooterConfig.PRESET_MID;

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

    private boolean lastRightBumper, lastLeftBumper, lastUp, lastDown, lastLeft, lastRight;
    private boolean lastStart, lastBack, lastLeftStick, lastRightStick, lastB, lastG2A, lastG2B;

    @Override
    public void runOpMode() {
        ShooterController.enableBulkCaching(hardwareMap);
        ShooterController shooter = new ShooterController(hardwareMap);
        ShooterLogger logger = new ShooterLogger();
        System.arraycopy(configPidf, 0, pidf, 0, 4);
        shooter.setTarget(targetVelocity);

        ElapsedTime timer = new ElapsedTime();
        ElapsedTime loopTimer = new ElapsedTime();
        int param = 0;
        int multIndex = 1;
        boolean gateOn = true;
        boolean voltageComp = ShooterConfig.VOLTAGE_COMPENSATE_F;
        ShooterController.IntakeRequest intakeRequest = ShooterController.IntakeRequest.OFF;

        telemetry.addLine("Ready. PIDF loaded from ShooterConfig.");
        telemetry.addLine("SHOOTER: A = start, X = stop, bumpers = target +/-50");
        telemetry.addLine("PIDF: dpad left/right = pick F/P/I/D, dpad up/down = change");
        telemetry.addLine("Stick presses = step size, Back = reset stats, Start = reset PIDF");
        telemetry.addLine("INTAKE: RT = in (gated feed), LT = out, Y = stop | B = CSV log");
        telemetry.addLine("gamepad2: A = gate on/off, B = battery-scaled F on/off");
        telemetry.update();
        waitForStart();
        timer.reset();
        loopTimer.reset();

        while (opModeIsActive()) {
            double loopMs = loopTimer.milliseconds();
            loopTimer.reset();

            boolean rb = gamepad1.right_bumper;
            boolean lb = gamepad1.left_bumper;
            boolean targetChanged = false;
            if (rb && !lastRightBumper) {
                targetVelocity = Math.min(ShooterConfig.maxTarget(), targetVelocity + VELOCITY_STEP);
                targetChanged = true;
            }
            if (lb && !lastLeftBumper) {
                targetVelocity = Math.max(0, targetVelocity - VELOCITY_STEP);
                targetChanged = true;
            }
            lastRightBumper = rb;
            lastLeftBumper = lb;
            shooter.setTarget(targetVelocity);

            if (gamepad1.a) {
                shooter.spinUp();
            } else if (gamepad1.x) {
                shooter.stop();
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
            boolean pidfChanged = false;
            if (up && !lastUp) {
                pidf[param] = Math.max(0, pidf[param] + delta);
                pidfChanged = true;
            }
            if (down && !lastDown) {
                pidf[param] = Math.max(0, pidf[param] - delta);
                pidfChanged = true;
            }
            if (start && !lastStart) {
                System.arraycopy(configPidf, 0, pidf, 0, 4);
                pidfChanged = true;
            }
            if (pidfChanged) {
                shooter.setPidf(pidf[1], pidf[2], pidf[3], pidf[0]);
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
                intakeRequest = ShooterController.IntakeRequest.IN;
            } else if (gamepad1.left_trigger > 0.5) {
                intakeRequest = ShooterController.IntakeRequest.OUT;
            } else if (gamepad1.y) {
                intakeRequest = ShooterController.IntakeRequest.OFF;
            }
            shooter.setIntakeRequest(intakeRequest);

            if (gamepad2.a && !lastG2A) {
                gateOn = !gateOn;
                shooter.setGateEnabled(gateOn);
                resetStats();
            }
            if (gamepad2.b && !lastG2B) {
                voltageComp = !voltageComp;
                shooter.setVoltageCompensation(voltageComp);
                resetStats();
            }
            lastG2A = gamepad2.a;
            lastG2B = gamepad2.b;

            if (gamepad1.b && !lastB) {
                if (logger.isOpen()) logger.stop(); else logger.start();
            }
            lastB = gamepad1.b;

            shooter.update();
            logger.log(timer.milliseconds(), shooter);

            double speed = shooter.getVelocity();
            if (shooter.isRunning() && targetVelocity > 0) {
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
            for (int k = 0; k < 4; k++) {
                String mark = (k == param) ? ">> " : "   ";
                telemetry.addData(mark + PARAM_NAMES[k], "%.3f (config %.3f)", pidf[k], configPidf[k]);
            }
            telemetry.addLine("NOT SAVED: copy final values into ShooterConfig.java");
            telemetry.addData("Gate / scaled F", "%s / %s", gateOn ? "ON" : "OFF", voltageComp ? "ON" : "OFF");
            telemetry.addData("Target cap (80% maxV)", "%.0f", ShooterConfig.maxTarget());

            shooter.addTelemetry(telemetry);

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
            telemetry.addLine("(hub velocity is ~50 ms averaged: compare runs, don't trust exact ms)");

            telemetry.addData("Log", logger.isOpen() ? "REC " + logger.getRows() + " rows -> " + logger.getPath()
                    : (logger.getError().isEmpty() ? "off (B to start)" : "ERROR " + logger.getError()));
            telemetry.addData("Loop", "%.1f ms", loopMs);
            telemetry.update();
        }

        logger.stop();
        shooter.shutdown();
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
}
