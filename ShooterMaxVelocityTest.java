package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Step 1 of the tuning procedure: measure the real top speed (maxV), per REV's PIDF guide.
 *
 * Use a FULLY CHARGED battery and NO ball in the shooter.
 *  A (hold) = shooter at power 1.0, RUN_WITHOUT_ENCODER. Peak is recorded after 1.5 s of spin-up.
 *  B (hold) = intake at power 0.8, to measure its ticks/s (only meaningful if its encoder is plugged in).
 *  Back     = clear the peaks.
 * Copy the numbers into ShooterConfig.
 */
@TeleOp(name = "Shooter MaxV Test", group = "Diagnostic")
public class ShooterMaxVelocityTest extends LinearOpMode {

    private static final double SETTLE_MS = 1500;

    @Override
    public void runOpMode() {
        DcMotorEx shooter = hardwareMap.get(DcMotorEx.class, ShooterConfig.SHOOTER_MOTOR_NAME);
        DcMotorEx intake = hardwareMap.get(DcMotorEx.class, ShooterConfig.INTAKE_MOTOR_NAME);

        shooter.setDirection(DcMotorEx.Direction.FORWARD);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        intake.setDirection(DcMotorEx.Direction.FORWARD);
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        ElapsedTime shooterOnFor = new ElapsedTime();
        ElapsedTime intakeOnFor = new ElapsedTime();
        boolean shooterWasOn = false;
        boolean intakeWasOn = false;
        double shooterPeak = 0;
        double intakePeak = 0;
        double voltageAtPeak = 0;

        telemetry.addLine("FULL battery, no ball loaded.");
        telemetry.addLine("Hold A = shooter power 1.0 | Hold B = intake power 0.8 | Back = clear");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            boolean shooterOn = gamepad1.a;
            boolean intakeOn = gamepad1.b;
            if (gamepad1.back) {
                shooterPeak = 0;
                intakePeak = 0;
                voltageAtPeak = 0;
            }

            if (shooterOn && !shooterWasOn) shooterOnFor.reset();
            if (intakeOn && !intakeWasOn) intakeOnFor.reset();
            shooterWasOn = shooterOn;
            intakeWasOn = intakeOn;

            shooter.setPower(shooterOn ? ShooterConfig.SHOOTER_SIGN * 1.0 : 0);
            intake.setPower(intakeOn ? ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_COLLECT_POWER : 0);

            double shooterV = Math.abs(shooter.getVelocity());
            double intakeV = Math.abs(intake.getVelocity());
            double voltage = batteryVoltage();
            if (shooterOn && shooterOnFor.milliseconds() > SETTLE_MS && shooterV > shooterPeak) {
                shooterPeak = shooterV;
                voltageAtPeak = voltage;
            }
            if (intakeOn && intakeOnFor.milliseconds() > SETTLE_MS) intakePeak = Math.max(intakePeak, intakeV);

            telemetry.addData("Battery", "%.2f V %s", voltage, voltage < 12.8 ? "(charge it for a true maxV!)" : "");
            telemetry.addLine("--- SHOOTER @ power 1.0 ---");
            telemetry.addData("Now", "%.0f ticks/s", shooterV);
            telemetry.addData("Peak (maxV)", "%.0f ticks/s at %.2f V", shooterPeak, voltageAtPeak);
            if (shooterPeak > 0) {
                double f = 32767.0 / shooterPeak;
                double p = 0.1 * f;
                double i = 0.1 * p;
                telemetry.addLine("Put in ShooterConfig:");
                telemetry.addData("  MAX_VELOCITY", "%.0f", shooterPeak);
                telemetry.addData("  SHOOTER_F", "%.2f", f);
                telemetry.addData("  SHOOTER_P", "%.2f (starting point, tune it)", p);
                telemetry.addData("  SHOOTER_I", "%.3f", i);
                telemetry.addData("  SHOOTER_D", "0");
                telemetry.addData("  Max preset (80%)", "%.0f ticks/s", 0.8 * shooterPeak);
            }
            telemetry.addLine("--- INTAKE @ power 0.8 ---");
            telemetry.addData("Now", "%.0f ticks/s", intakeV);
            telemetry.addData("Peak", "%.0f ticks/s", intakePeak);
            if (intakeOn && intakeOnFor.milliseconds() > SETTLE_MS && intakePeak < 1) {
                telemetry.addLine("Intake reads 0: encoder not plugged in. Keep INTAKE_HAS_ENCODER = false.");
            } else if (intakePeak > 0) {
                telemetry.addData("  INTAKE_HAS_ENCODER", "true");
                telemetry.addData("  INTAKE_FEED_VELOCITY", "%.0f (1.2x peak)", 1.2 * intakePeak);
            }
            telemetry.update();
        }

        shooter.setPower(0);
        intake.setPower(0);
    }

    private double batteryVoltage() {
        double min = Double.POSITIVE_INFINITY;
        for (VoltageSensor s : hardwareMap.voltageSensor) {
            double v = s.getVoltage();
            if (v > 0) min = Math.min(min, v);
        }
        return min == Double.POSITIVE_INFINITY ? 0 : min;
    }
}
