package org.firstinspires.ftc.teamcode;

/**
 * Every shooter number lives here, so a tuned value only has to be typed once.
 *
 * The hub FORGETS PIDF on power cycle. Whatever is in this file is what every
 * OpMode re-applies in init, so this file is the only place tuning "sticks".
 *
 * Workflow (see Obsidian: Shooter Tuning Guide):
 *  1. Run "Shooter MaxV Test" on a fresh battery -> put the result in MAX_VELOCITY
 *     and the suggested F/P/I below.
 *  2. Run "Shooter PIDF Tuner", feed balls at match pace, raise P until the dip
 *     recovers fastest without ringing -> copy the final P/I/D/F here.
 *  3. Calibrate PRESETS at the taped floor spots.
 */
public final class ShooterConfig {

    private ShooterConfig() {}

    // ---------------- Hardware names (must match the Robot Configuration) ----------------
    public static final String SHOOTER_MOTOR_NAME = "shooterMotor";
    public static final String INTAKE_MOTOR_NAME = "intakeMotor";
    public static final String LF_MOTOR_NAME = "lfMotor";
    public static final String LB_MOTOR_NAME = "lbMotor";
    public static final String RF_MOTOR_NAME = "rfMotor";
    public static final String RB_MOTOR_NAME = "rbMotor";

    /** +1 if a positive velocity shoots the ball out. Flip to -1 if the wheel spins backward. */
    public static final double SHOOTER_SIGN = 1;
    /** +1 if positive intake power moves balls toward the flywheel. */
    public static final double INTAKE_FEED_SIGN = 1;

    // ---------------- Flywheel limits ----------------
    /**
     * Measured top speed in ticks/s at power 1.0, RUN_WITHOUT_ENCODER, fresh battery.
     * TODO(tonight): replace with the number from "Shooter MaxV Test".
     * 2450 is the estimate for a 6000 rpm Yellow Jacket (28 CPR, ~5400 rpm loaded-free).
     */
    public static final double MAX_VELOCITY = 2450;
    /** Never command more than this fraction of MAX_VELOCITY, so the loop has headroom to recover after a shot. */
    public static final double MAX_TARGET_FRACTION = 0.80;

    public static double maxTarget() {
        return MAX_VELOCITY * MAX_TARGET_FRACTION;
    }

    // ---------------- Velocity PIDF (hub units) ----------------
    // Starting point from REV's formula with MAX_VELOCITY = 2450:
    //   F = 32767 / maxV, P = 0.1 * F, I = 0.1 * P, D = 0
    // TODO(tonight): replace with the values tuned in "Shooter PIDF Tuner".
    public static final double SHOOTER_P = 1.3;
    public static final double SHOOTER_I = 0.13;
    public static final double SHOOTER_D = 0.0;
    public static final double SHOOTER_F = 13.4;

    /**
     * Battery-scaled feedforward. When true, F is re-sent on every spin-up as
     * F * (NOMINAL_VOLTAGE / measured voltage). Off by default: turn it on only
     * after testing, it is the lowest-priority fix on the list.
     */
    public static final boolean VOLTAGE_COMPENSATE_F = false;
    public static final double NOMINAL_VOLTAGE = 12.5;
    /** Voltage scaling is clamped so a bad reading can't send a crazy F. */
    public static final double MAX_VOLTAGE_SCALE = 1.25;

    // ---------------- At-speed gate ----------------
    /** Feed only while |target - actual| is under this fraction of target (0.03 = 3%, ~45 ticks/s at 1450). */
    public static final double AT_SPEED_TOLERANCE = 0.03;
    /**
     * Once the gate opens, keep feeding at least this long even if speed dips.
     * The ball's contact dips the wheel immediately, and cutting the push at that
     * moment would make the "ball stalls in the gap" problem worse. 0 = pure gate.
     */
    public static final double FEED_MIN_PUSH_MS = 120;

    // ---------------- Presets (ticks/s) ----------------
    // TODO(tonight): calibrate at taped floor spots. All must be <= maxTarget() (1960 at maxV 2450).
    public static final double PRESET_CLOSE = 1300;
    public static final double PRESET_MID = 1450;   // the speed used in the video
    public static final double PRESET_FAR = 1600;
    /** Reverse speed for clearing a jam (Velocity Tuner "A"). */
    public static final double UNJAM_VELOCITY = -900;

    // ---------------- Intake / feed ----------------
    /** Power while collecting balls (shooter off). */
    public static final double INTAKE_COLLECT_POWER = 0.8;
    /**
     * True only if the intake motor's encoder cable is plugged into the hub.
     * Then feeding uses closed-loop velocity, so feed speed doesn't sag with the battery.
     */
    public static final boolean INTAKE_HAS_ENCODER = false;
    /**
     * Feed velocity in ticks/s when INTAKE_HAS_ENCODER. Set to ~1.2x the intake ticks/s
     * that "Shooter MaxV Test" measures at power 0.8 on a fresh battery.
     * TODO(tonight): measure. 0 means "not measured" and falls back to power feed.
     */
    public static final double INTAKE_FEED_VELOCITY = 0;
    /** Feed power when there is no encoder (or no measured velocity): full power so it's as consistent as possible. */
    public static final double INTAKE_FEED_POWER = 1.0;

    // ---------------- Driving ----------------
    public static final double DRIVE_SPEED = 1.0;
    /** Drive speed while the precision trigger is held, for lining up a shot. */
    public static final double PRECISION_DRIVE_SPEED = 0.35;
}
