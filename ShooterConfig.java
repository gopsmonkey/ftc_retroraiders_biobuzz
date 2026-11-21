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
    public static final double SHOOTER_SIGN = -1;   // team: launch velocity is -1375
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
     * Keep feeding this long after the wheel was last at speed (or the RB override was last held),
     * so a ball's own contact dip doesn't cut the push mid-grab. Covers the ~0.2 s grab-to-exit
     * seen in the 30 fps video. Was 120; Shresht raised it to 300. 0 = pure gate.
     */
    public static final double FEED_MIN_PUSH_MS = 0;   // team: brake the intake the moment the wheel is off target (was 300)

    // ---------------- Presets (ticks/s) ----------------
    // TODO(tonight): calibrate at taped floor spots. All must be <= maxTarget() (1960 at maxV 2450).
    public static final double PRESET_CLOSE = 1300;
    public static final double PRESET_MID = 1375;   // commanded as -1375 (SHOOTER_SIGN = -1)
    public static final double PRESET_FAR = 1600;
    /** Reverse speed for clearing a jam (Velocity Tuner "A"). */
    public static final double UNJAM_VELOCITY = -900;

    // ---------------- Intake / feed ----------------
    /**
     * true = the intake (which also drives the two mini wheels under the flywheel) never pushes
     * balls up until the flywheel is at launch speed, even with the shooter off. So RT does nothing
     * until A has spun the wheel up. false = old behavior: RT collects at INTAKE_COLLECT_POWER
     * while the shooter is off (balls get pushed into the stopped wheel).
     */
    public static final boolean FEED_ONLY_AT_SPEED = true;
    /**
     * One ball per shot: once the wheel is at speed, the intake moves only this many encoder
     * ticks, then brakes for FEED_GAP_MS. Stops the next ball BEFORE it reaches the wheel,
     * instead of waiting for the speed dip (which the hub reports ~50 ms late). 0 = off.
     * To set it: leave it 0, spin up, hold RT and shoot 4 balls, read "Intake ticks fed" on
     * telemetry, divide by 4, and start at about 70% of that. Raise it if a ball doesn't reach
     * the wheel; lower it if two balls still go out together.
     */
    public static final int FEED_TICKS_PER_BALL = 0;
    /** Brake time between balls (ms), so the shot clears and the wheel recovers. Try 150-300. */
    public static final double FEED_GAP_MS = 200;
    /** Power while collecting balls (shooter off, only when FEED_ONLY_AT_SPEED = false). */
    public static final double INTAKE_COLLECT_POWER = 0.8;
    /**
     * True only if the intake motor's encoder cable is plugged into the hub.
     * Then feeding uses closed-loop velocity, so feed speed doesn't sag with the battery.
     */
    public static final boolean INTAKE_HAS_ENCODER = true;   // encoder cable added to intakeMotor
    /**
     * Feed velocity in ticks/s when INTAKE_HAS_ENCODER. Set to ~1.2x the intake ticks/s
     * that "Shooter MaxV Test" measures at power 0.8 on a fresh battery.
     * TODO(tonight): measure. 0 means "not measured" and falls back to power feed.
     */
    public static final double INTAKE_FEED_VELOCITY = 0;
    /**
     * Feed speed while shooting (fraction of max; with the encoder in, RUN_USING_ENCODER holds it
     * steady as the battery drains). Was 1.0: two stacked balls went up together and the second
     * fell short. Slower feed spaces them so the at-speed brake catches the second one.
     * Two still go together -> 0.4. Balls stall before the wheel or shots are too slow -> 0.6.
     */
    public static final double INTAKE_FEED_POWER = 0.5;

    /**
     * Intake current (amps) that means "intake stalled = full", so the driver gets a rumble
     * before picking up a 5th ball (G407: max 4). 0 = off. To set it: watch "Intake current"
     * on BioBuzz TeleOp telemetry while collecting, note the normal amps and the amps when
     * 4 balls are packed, and put a value between them. Idea from team 23849.
     */
    public static final double INTAKE_FULL_CURRENT_AMPS = 0;

    // ---------------- Flower intake servos (BioBuzz TeleOp) ----------------
    /** Optional: if these names aren't in the Robot Configuration, the TeleOp just skips the servos. */
    public static final String FLOWER_SERVO_1_NAME = "flowerServo1";
    public static final String FLOWER_SERVO_2_NAME = "flowerServo2";
    /** TODO(tonight): measure. 0.0 / 0.5 are the guesses from MainTeleOp. Servo 2 is mirrored (reversed). */
    public static final double FLOWER_UP = 0.0;
    public static final double FLOWER_DOWN = 0.5;

    // ---------------- Live speed trim (BioBuzz TeleOp) ----------------
    /** gamepad2 d-pad up/down adds/removes this much from every preset during a match. Idea from team 7759. */
    public static final double TRIM_STEP = 25;
    public static final double TRIM_LIMIT = 200;

    // ---------------- Driving ----------------
    public static final double DRIVE_SPEED = 1.0;
    /** Drive speed while the precision trigger is held, for lining up a shot. */
    public static final double PRECISION_DRIVE_SPEED = 0.35;
}
