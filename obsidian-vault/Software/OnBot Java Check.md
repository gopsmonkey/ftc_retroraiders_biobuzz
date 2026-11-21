---
tags: [ftc, software, onbot-java, check]
branch: shooter-fixes-night-before
commit: 391d8ed
created: 2026-11-20
---
# OnBot Java check (branch `shooter-fixes-night-before`)

Back to [[FTC Home]] · Tasks: [[To-Do]] · Why it matters: [[Sathwik Handoff]] (the team uses OnBot Java only)

**Result: all 8 Java files compile for OnBot Java. No code changes needed.** Re-checked on `391d8ed` (flower servos, trim, rumble, push-hold fix, write caching) against SDK 12.0.0 and 9.2.0: 0 errors.
- Compiled against the real FTC SDK libraries at Java 8 on SDK **9.2.0, 10.1.1, 11.2.1 and 12.0.0**: 0 errors, 0 warnings.
- The check can fail: with the SDK libraries removed, the same files give 344 errors.
- Script: `Software/attachments/onbot-compile-check.sh`. Re-run it after any code change.

## What was checked
| Check | Result |
| --- | --- |
| Every file is `package org.firstinspires.ftc.teamcode` | OK |
| One public class per file, file name = class name | OK |
| Only FTC SDK + standard Java (no FTCLib, Dashboard, `@Config`, Gradle libraries) | OK |
| Java features OnBot supports (no lambdas, streams or try-with-resources; Java 7-level code) | OK |
| CSV log writes to `/sdcard/FIRST/shooter_logs` (allowed on the Control Hub) | OK |
| `preselectTeleOp` in the AUTO (needs SDK 6.1+) | OK |
| INIT moves no motors (G403) | OK |

## How to load it into OnBot Java
1. Upload all 8 files from the branch: **`ShooterConfig`, `ShooterController`, `ShooterLogger`** (helpers, no OpMode), plus `BioBuzzTeleOp`, `PreloadShootLeaveAuto`, `ShooterMaxVelocityTest`, `ShooterPIDFTuner`, `ShooterVelocityTuner`. Don't upload `README.md`.
2. Upload them with OnBot Java's **Upload Files** button, not by pasting. Pasting can mangle indentation, as it does in the GitHub web editor ([[Sathwik Handoff]]).
3. Press **Build Everything** and wait for "Build succeeded". Then the new OpModes appear on the Driver Station.

## Things that can still break the build on the robot
These are problems with the robot's existing OnBot files, not with this code.
- **OnBot builds every file together, so one broken file means no OpModes at all.** Sathwik's unfinished files are the risk, especially `PollenAutoNav` with its placeholder HSV values. If the build fails, read the error's file name, then fix or delete that file.
- **Old copies with the same names.** OnBot already has Sathwik's original `ShooterVelocityTuner` and `ShooterPIDFTuner`. Overwrite them; don't keep both. Two files with the same class give a "duplicate class" error.
- **Missing helpers.** Every OpMode here needs `ShooterConfig` and `ShooterController`, and the PIDF Tuner also needs `ShooterLogger`. Upload all three.
- **Duplicate OpMode names.** If `MainTeleOp` or an old file also uses the name "BioBuzz TeleOp", "Shooter PIDF Tuner" or similar, only one shows up. Rename one.
- **Config names.** These must match the Driver Station config exactly: `shooterMotor`, `intakeMotor`, `lfMotor`, `lbMotor`, `rfMotor`, `rbMotor` ([[Hardware Config Names]]).

## On the Control Hub (file list from OnBot, 2026-11-21 00:54)
45 files: `April_Tag`, `ArmServoTest`, `BioBuzzTeleOp`, `ColorFinalTest`, `ColorSamplerOpMode`, `ColorTest`, `DecemberDecodeAuto`, `DecodeAutoB`, `DecodeAutoF`, `DecodeTeleOp`, `DriverTeleOp`, `DrivetrainModuleTest`, `ExampleAuto`, `FinalAuto`, `FinalAutoRefactored`, `FinalAutoSamples`, `FinalAutoV2`, `FinalDecodeTeleOp`, `IMUTest`, `IntakeTest`, `intaketestcode`, `launchTest`, `launchTest2`, `launchTest3`, `MecanumOdometryRobot`, `Meet3auto`, `motortest`, `nishsteleop`, `odometry`, `PollenAutoNav`, `RightModuleTest`, `Robot`, `Robot26`, `RobotConstants`, `RobotNew`, `RRTeleop`, `ShooterConfig`, `ShooterController`, `ShooterPIDFTuner`, `ShooterVelocityTest`, `ShooterVelocityTuner`, `spinTest`, `TeleOpActual`, `TeleOpFinal`, `TeleOpFinalV2ControlsOverhaul`.

- **Missing from the repo branch:** `PreloadShootLeaveAuto` (the AUTO), `ShooterMaxVelocityTest`, `ShooterLogger`.
- **Old versions:** `ShooterPIDFTuner` and `ShooterVelocityTuner` must be Sathwik's originals. The new tuner needs `ShooterLogger`, which isn't there, yet the build passed.
- Most of the rest is last season (DECODE) and test code. It builds today, so leave it alone at the event. Delete it after the meet to cut build risk and duplicate OpMode names.
- **`ShooterConfig` and `ShooterController` must come from the same commit.** Mixing them gave `cannot find symbol FEED_TICKS_PER_BALL` (00:54).
- To copy files from GitHub for pasting, use raw links **pinned to a commit**, e.g. `https://raw.githubusercontent.com/gopsmonkey/ftc_retroraiders_biobuzz/7a10048/ShooterConfig.java`. Branch links can serve a cached old version.

## Not compile problems, but worth knowing
- **Sathwik prefers no comments in code** ([[Sathwik Handoff]]). These files are full of comments. They compile fine; strip them only if he asks.
- Compiling isn't the same as working on the robot. Nothing on this branch has run on the robot yet ([[Shooter Software Implementation Log]]).
