---
tags: [ftc, robot, hardware, cad, index]
updated: 2026-11-20
---
# Robot overview (how our bot is built)

Back to [[FTC Home]] · Names in the config: [[Hardware Config Names]] · Source for most of this: [[Sathwik Handoff]]

**The one note for what our robot physically has and doesn't have.** Check it before writing or running any code. If something here is wrong, fix it here first.

## What it does NOT have (read before running anything)
| Not on the robot | So… |
| --- | --- |
| **No camera** (no webcam, no Limelight) | No vision, no AprilTags, no color-blob tracking. Camera-based ideas from other teams don't apply. Sathwik's `PollenAutoNav` / `ColorSamplerOpMode` can't run on this robot as built (see [[Open Questions]]). |
| **No autonomous/automation code of our own yet** | The team writes AUTO themselves. Claude must not add AUTO or path-following code. The only AUTO file is Shresht's simple [[Preload Shoot + Leave Auto]] (`PreloadShootLeaveAuto.java`), not yet tuned. |
| No turret, no hood adjuster | Aim by turning the robot; distance by preset speed and taped spots. |
| No separate feeder motor or gate servo | The intake motor also pushes balls into the flywheel, so the at-speed gate pauses the intake. |
| No intake encoder | `INTAKE_HAS_ENCODER = false`; feed runs at power 1.0. |
| No ball-count sensor | Driver must not hold more than 4 balls (G407). The intake-current warning can help once `INTAKE_FULL_CURRENT_AMPS` is set. |
| Only one flywheel motor | Top teams use two ([[Top Teams Shooter Reference]]); on [[Post-Competition Redesigns]]. |

## What it has
| Subsystem | Build | Code |
| --- | --- | --- |
| Control | REV Control Hub + Expansion Hub. All 4 drive motors are on the **Expansion Hub** (blinking-blue issue: [[Sathwik Handoff]]). | OnBot Java only, no Android Studio ([[OnBot Java Check]]) |
| Drivetrain | Mecanum, 4 motors. A new drivetrain is being built (right module done), same chaining. | `lfMotor` `lbMotor` (REVERSE), `rfMotor` `rbMotor` (FORWARD) |
| Odometry | goBILDA 4-bar pods: forward `y_pod`, strafe `x_pod`. 2000 counts/rev, 32 mm wheel. Offsets must be re-measured on the new drivetrain. | Sathwik's `MecanumOdometryRobot` (not in repo) |
| Launcher | Static: one gecko-wheel flywheel on a printed motor bracket, fixed printed hood, no turret. Motor likely a 6000 rpm Yellow Jacket, 28 CPR (unconfirmed, [[Open Questions]] #6). | `shooterMotor`, velocity PIDF, at-speed gate ([[Shooter Software Implementation Log]]) |
| Intake | "Flower" intake on one motor (no encoder), dropped and lifted by two servos (servo 2 mirrored). | `intakeMotor`; `flowerServo1/2` (positions 0.0 up / 0.5 down are guesses) |
| Lift | Older goBILDA 4-stage viper slide. Whether it's still mounted is unknown ([[Open Questions]]). | `viperMotor` (old `DriverTeleOp`) |
| Sensors | Encoders only (shooter, odometry). Battery voltage via the hubs. | — |

Budget: 7 of 8 motors (4 drive + shooter + intake + viper), 2 of 8 servos. Size: 18 in cube at the start, 18 × 24 × 29 in extended ([[BIOBUZZ Rules and Scoring]]).

## Known mechanical problems (shooter)
From the 240 fps video ([[Shooter Video Evidence]]):
- The flywheel mount bends 1.5–3 mm on every shot.
- The ball stalls 54–83 ms before the wheel grabs it.
- The hood squeeze isn't locked.

Fixes: [[Shooter Fix List - Night Before Competition]], [[Small Fixes Without Reprinting]].

## CAD
- **Fusion 360**, project **"Admin Project"** (Sathwik's account).
- Chassis: 14-hole U-channel sides, 10-hole back. Mecanum wheels aligned to the 3rd/last hole, spacers, low guard channel.
- Printed parts: flywheel motor bracket, hood (PLA/PETG layer lines on the ball path; tape or polycarbonate strip recommended).
- Belt/pulley rules and the worked example: [[Sathwik Handoff]], "Belts instead of chains".
- Still to add here: CAD screenshots or an export of the current robot, flywheel diameter, gear/belt ratios. Paste images into `Robot/attachments/`.
