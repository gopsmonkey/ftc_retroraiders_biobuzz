package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import java.lang.reflect.Proxy;

public class GateTest {
    static double shooterVel = 0, intakePower = Double.NaN, intakeVelCmd = Double.NaN, shooterCmd = Double.NaN;
    static String intakeMode = "";

    static DcMotorEx motor(boolean isShooter) {
        return (DcMotorEx) Proxy.newProxyInstance(GateTest.class.getClassLoader(), new Class[]{DcMotorEx.class},
            (p, m, a) -> {
                switch (m.getName()) {
                    case "getVelocity": return isShooter ? shooterVel : 0.0;
                    case "setPower": if (isShooter) shooterCmd = 0; else { intakePower = (Double) a[0]; intakeVelCmd = Double.NaN; } return null;
                    case "setVelocity": if (isShooter) shooterCmd = (Double) a[0]; else { intakeVelCmd = (Double) a[0]; intakePower = Double.NaN; } return null;
                    case "getPIDFCoefficients": return new PIDFCoefficients(ShooterConfig.SHOOTER_P, ShooterConfig.SHOOTER_I, ShooterConfig.SHOOTER_D, ShooterConfig.SHOOTER_F);
                    case "getCurrent": return 0.0;
                    case "getCurrentPosition": return 0;
                    case "hashCode": return System.identityHashCode(p);
                    case "equals": return p == a[0];
                    case "toString": return isShooter ? "shooter" : "intake";
                    default: return null;
                }
            });
    }

    static void check(boolean ok, String what) {
        System.out.println((ok ? "PASS " : "FAIL ") + what + "  [intake power=" + intakePower + " vel=" + intakeVelCmd + " mode=" + intakeMode + "]");
        if (!ok) failures++;
    }
    static int failures = 0;
    static boolean feedingForward() {
        double cmd = !Double.isNaN(intakeVelCmd) ? intakeVelCmd : intakePower;
        return !Double.isNaN(cmd) && cmd != 0 && (cmd > 0) == (ShooterConfig.INTAKE_FEED_SIGN > 0)
                && (ShooterConfig.INTAKE_FEED_VELOCITY <= 0 || intakeVelCmd == ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_FEED_VELOCITY);
    }

    static void step(ShooterController s) { s.update(); intakeMode = s.getIntakeMode(); }

    public static void main(String[] args) {
        final DcMotorEx shooterM = motor(true), intakeM = motor(false);
        HardwareMap hw = new HardwareMap(null, null) {
            @Override @SuppressWarnings("unchecked")
            public <T> T get(Class<? extends T> c, String name) {
                return (T) (name.equals(ShooterConfig.SHOOTER_MOTOR_NAME) ? shooterM : intakeM);
            }
        };
        ShooterController s = new ShooterController(hw);
        double target = ShooterConfig.PRESET_MID;

        s.setTarget(target);
        s.setIntakeRequest(ShooterController.IntakeRequest.IN);
        step(s);
        check(intakePower == 0 && intakeMode.startsWith("BRAKED"), "shooter off + RT: intake braked");

        s.spinUp();
        step(s);
        check(shooterCmd == -1375, "spinUp commands -1375 (got " + shooterCmd + ")");
        shooterVel = -700; step(s);
        check(intakePower == 0 && intakeMode.startsWith("BRAKED"), "spinning up at -700: intake braked");

        shooterVel = -1375; step(s);
        check(feedingForward(), "at -1375: intake feeds (power or velocity)");

        shooterVel = -1300; step(s);
        check(intakePower < 0 && intakeMode.startsWith("BACKOFF"), "ball dips wheel to -1300 (5.5% off): intake backs off at once (pulls next ball off the wheel)");
        long t0 = System.currentTimeMillis();
        while (System.currentTimeMillis() - t0 < ShooterConfig.FEED_BACKOFF_MS + 30) { step(s); try { Thread.sleep(5); } catch (InterruptedException e) {} }
        check(intakePower == 0 && intakeMode.startsWith("BRAKED"), "after FEED_BACKOFF_MS: braked until the wheel recovers");

        shooterVel = -1360; step(s);
        check(feedingForward(), "back within 3% (-1360): intake feeds again");

        shooterVel = 1375; step(s);
        check(intakePower <= 0, "wheel spinning the wrong way (+1375): intake never pushes forward");

        shooterVel = -1375;
        s.setGateEnabled(false); s.stop(); step(s);
        check(intakePower == 0, "RB override with shooter off: still braked (no push into stopped wheel)");
        s.setGateEnabled(true);

        s.setIntakeRequest(ShooterController.IntakeRequest.OUT); step(s);
        check(intakePower < 0, "LB reverse (unjam) still works with shooter off");

        s.setIntakeRequest(ShooterController.IntakeRequest.COLLECT); step(s);
        check(intakePower == ShooterConfig.INTAKE_FEED_SIGN * ShooterConfig.INTAKE_COLLECT_POWER && intakeMode.equals("collect"), "LT collect with shooter off: runs at collect power, no gate");

        s.setIntakeRequest(ShooterController.IntakeRequest.OFF); step(s);
        check(intakePower == 0, "no request: intake stopped");

        System.out.println(failures == 0 ? "ALL PASS" : failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
