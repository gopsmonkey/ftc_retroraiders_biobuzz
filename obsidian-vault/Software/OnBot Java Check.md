---
tags: [ftc, software, onbot-java, check]
branch: shooter-fixes-night-before
commit: 2e4aaf8
created: 2026-11-20
---
# OnBot Java check (branch `shooter-fixes-night-before`)

Back to [[FTC Home]] · Tasks: [[To-Do]] · Why it matters: [[Sathwik Handoff]] (the team uses OnBot Java only)

**Result: all 8 Java files compile for OnBot Java. No code changes needed.**
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

## Not compile problems, but worth knowing
- **Sathwik prefers no comments in code** ([[Sathwik Handoff]]). These files are full of comments. They compile fine; strip them only if he asks.
- Compiling isn't the same as working on the robot. Nothing on this branch has run on the robot yet ([[Shooter Software Implementation Log]]).
