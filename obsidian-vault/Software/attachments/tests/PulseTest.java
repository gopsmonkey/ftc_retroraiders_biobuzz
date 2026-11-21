package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import java.lang.reflect.Proxy;

public class PulseTest {
    static double shooterVel = -1375, intakePower = 0;
    static int intakePos = 0;
    static int failures = 0;

    static DcMotorEx motor(boolean isShooter) {
        return (DcMotorEx) Proxy.newProxyInstance(PulseTest.class.getClassLoader(), new Class[]{DcMotorEx.class},
            (p, m, a) -> {
                switch (m.getName()) {
                    case "getVelocity": return isShooter ? shooterVel : 0.0;
                    case "getCurrentPosition": return intakePos;
                    case "setPower": if (!isShooter) intakePower = (Double) a[0]; return null;
                    case "setVelocity": if (!isShooter) intakePower = Math.signum((Double) a[0]); return null;
                    case "getPIDFCoefficients": return new PIDFCoefficients(ShooterConfig.SHOOTER_P, ShooterConfig.SHOOTER_I, ShooterConfig.SHOOTER_D, ShooterConfig.SHOOTER_F);
                    case "getCurrent": return 0.0;
                    case "hashCode": return System.identityHashCode(p);
                    case "equals": return p == a[0];
                    default: return null;
                }
            });
    }

    static void check(boolean ok, String what) {
        System.out.println((ok ? "PASS " : "FAIL ") + what);
        if (!ok) failures++;
    }

    public static void main(String[] args) throws Exception {
        final DcMotorEx sm = motor(true), im = motor(false);
        HardwareMap hw = new HardwareMap(null, null) {
            @Override @SuppressWarnings("unchecked")
            public <T> T get(Class<? extends T> c, String n) { return (T) (n.equals(ShooterConfig.SHOOTER_MOTOR_NAME) ? sm : im); }
        };
        ShooterController s = new ShooterController(hw);
        s.setTarget(ShooterConfig.PRESET_MID);
        s.spinUp();
        s.setIntakeRequest(ShooterController.IntakeRequest.IN);

        int fedLoops = 0, posAtFirstStop = -1;
        long stopAt = -1, resumeAt = -1;
        String firstStopMode = "";
        long t0 = System.currentTimeMillis();
        while (resumeAt < 0 && System.currentTimeMillis() - t0 < 2000) {
            s.update();
            boolean on = intakePower > 0;
            if (on) intakePos += 20;
            if (on && stopAt < 0) fedLoops++;
            if (!on && fedLoops > 0 && stopAt < 0) { stopAt = System.currentTimeMillis(); posAtFirstStop = intakePos; firstStopMode = s.getIntakeMode(); }
            if (on && stopAt >= 0) resumeAt = System.currentTimeMillis();
            Thread.sleep(5);
        }
        check(posAtFirstStop == 100, "first pulse stops after 100 ticks (stopped at " + posAtFirstStop + ")");
        check(firstStopMode.contains("gap") || firstStopMode.startsWith("BACKOFF"), "telemetry shows backoff/gap while waiting (" + firstStopMode + ")");
        check(resumeAt > 0, "second pulse starts after the gap");
        long gap = resumeAt - stopAt;
        check(gap >= ShooterConfig.FEED_GAP_MS - 10 && gap < ShooterConfig.FEED_GAP_MS + 100, "gap ~FEED_GAP_MS=" + ShooterConfig.FEED_GAP_MS + " (measured " + gap + " ms)");

        shooterVel = -1000; s.update();
        check(intakePower <= 0, "wheel dips mid-pulse: no forward push");

        System.out.println(failures == 0 ? "ALL PASS" : failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
