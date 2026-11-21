package org.firstinspires.ftc.teamcode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Writes one CSV row per loop to /sdcard/FIRST/shooter_logs/ on the Control Hub.
 * The hub averages velocity over ~50 ms, so telemetry dips look smoother than reality;
 * a log lets you compare burst-to-burst. Pull files with: adb pull /sdcard/FIRST/shooter_logs
 */
public class ShooterLogger {

    public static final String LOG_DIR = "/sdcard/FIRST/shooter_logs";

    private BufferedWriter out;
    private String path = "";
    private String error = "";
    private int rows = 0;

    public boolean isOpen() { return out != null; }
    public String getPath() { return path; }
    public String getError() { return error; }
    public int getRows() { return rows; }

    public void start() {
        stop();
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists() && !dir.mkdirs()) throw new IOException("can't create " + LOG_DIR);
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File file = new File(dir, "shooter_" + stamp + ".csv");
            out = new BufferedWriter(new FileWriter(file));
            out.write("t_ms,target,velocity,at_speed,feeding,intake_out,voltage,p,i,d,f_applied\n");
            path = file.getPath();
            error = "";
            rows = 0;
        } catch (IOException e) {
            out = null;
            error = e.getMessage();
        }
    }

    public void log(double tMs, ShooterController s) {
        if (out == null) return;
        try {
            out.write(String.format(Locale.US, "%.1f,%.0f,%.1f,%d,%d,%.3f,%.2f,%.4f,%.4f,%.4f,%.4f\n",
                    tMs, s.isRunning() ? s.getTarget() : 0.0, s.getVelocity(),
                    s.isAtSpeed() ? 1 : 0, s.isFeeding() ? 1 : 0, s.getIntakeOutput(), s.getVoltage(),
                    s.getP(), s.getI(), s.getD(), s.getAppliedF()));
            rows++;
        } catch (IOException e) {
            error = e.getMessage();
            stop();
        }
    }

    public void stop() {
        if (out == null) return;
        try {
            out.close();
        } catch (IOException e) {
            error = e.getMessage();
        }
        out = null;
    }
}
