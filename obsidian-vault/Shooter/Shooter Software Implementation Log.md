---
tags: [ftc, shooter, software, log]
branch: shooter-fixes-night-before
commit: ac9be61
updated: 2026-11-20
---
# Shooter software implementation log

Back to [[Shooter Fix List - Night Before Competition]] · Review: [[Shooter Code Review]] · How to tune: [[Shooter Tuning Guide]]

All software fixes from the fix list are in branch `shooter-fixes-night-before` (commit `ac9be61`) of the [repo](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz). See [[Repo and Branches]].

> [!important] Before it works on the robot
> The numbers in `ShooterConfig.java` are **placeholders** (maxV 2450, P 1.3, I 0.13, F 13.4, presets 1300/1450/1600). They must be measured and tuned tonight: [[Shooter Tuning Guide]].

## What changed (2026-11-20)
| Fix from the list | Where in code | How it works |
| --- | --- | --- |
| Hardcode PIDF (hub forgets on power cycle) | `ShooterConfig`, `ShooterController` constructor | Every OpMode re-sends P/I/D/F right after `RUN_USING_ENCODER`, then reads it back. Init telemetry shows `PIDF on hub: OK` or `MISMATCH`. |
| Only feed when flywheel is at speed | `ShooterController.update()` | Intake feeds only when error < 3% of target. Shows `GATED` on telemetry while waiting. |
| (new) Push-through hold | `FEED_MIN_PUSH_MS = 120` | Once the gate opens, feeding continues ≥120 ms even if speed dips. Without it, the ball's own contact dip would cut the push mid-grab and make the stall worse. Set to 0 for a pure gate. |
| Intake velocity control / 1.0 while shooting | `INTAKE_HAS_ENCODER`, `INTAKE_FEED_VELOCITY`, `INTAKE_FEED_POWER` | No encoder (default): feed at power 1.0. With encoder + measured velocity: closed-loop `setVelocity()`. Collecting (shooter off) stays 0.8. |
| Cap at 80% of maxV | `ShooterConfig.maxTarget()`, `setTarget()` | All targets clamped (was 6000 in tuners). |
| Presets | `PRESET_CLOSE/MID/FAR`, d-pad in BioBuzz TeleOp | d-pad left/up/right. Calibrate at tape marks. |
| Precision drive | `PRECISION_DRIVE_SPEED = 0.35` | Hold left trigger (TeleOp + Velocity Tuner). |
| Battery voltage + scaled F | `VOLTAGE_COMPENSATE_F` (off by default) | Voltage on telemetry always. When on: F × 12.5 / V on spin-up and every 2 s, clamped to ±25%. gamepad2 B toggles it in PIDF Tuner. |
| Bulk caching + loop time | `ShooterController.enableBulkCaching()` | LynxModule AUTO in every OpMode; loop ms on telemetry. |
| Log flywheel speed to file | `ShooterLogger`, B in PIDF Tuner | CSV in `/sdcard/FIRST/shooter_logs/`. Pull with `adb pull /sdcard/FIRST/shooter_logs`. |
| Measure maxV | `Shooter MaxV Test` OpMode | Hold A: power 1.0 peak after 1.5 s → prints MAX_VELOCITY, F, P, I, 80% cap. Hold B: intake ticks/s → feed velocity. |
| No match TeleOp in repo | `BioBuzz TeleOp` (group "Match") | Reference TeleOp with all fixes. If the real match TeleOp is elsewhere, copy the `// FIX` lines into it **and commit it**. |

## OpMode controls
**BioBuzz TeleOp (match)**
- Left stick drive/strafe, right stick turn, hold LT = precision
- A = spin up at preset, X = stop shooter, d-pad L/U/R = CLOSE/MID/FAR
- Hold RT = intake (collects when shooter off, gated feed when on)
- Hold LB = reverse intake, hold B = reverse shooter (releasing B stops it)

**Shooter PIDF Tuner**: same as before, plus RT/LT now *in/out* (in = gated feed), B = CSV log, gamepad2 A = gate on/off, gamepad2 B = scaled F on/off. Starts from `ShooterConfig` gains, Start resets to them.

**Shooter Velocity Tuner**: same as before (B = +preset mid, A = −900, bumpers ±100), plus LT precision drive, positive intake gated while shooting.

## Verification done
- Compiles with javac (`-Xlint:all`, also `--release 8` like the FTC SDK) against FTC SDK API stubs. No Gradle/SDK in the repo, so a real Android Studio build is still needed.
- 18 logic checks on mock motors all pass: PIDF in init + readback, 80% cap, collect vs gated feed, push-through window, re-gate after window, reverse never gated, voltage-scaled F and its clamp, gate disable.
- **Not tested on the robot.** Gamepad mapping and motor directions are copied from the existing tuners.

## Still to do (people)
Tracked in [[To-Do]].

## Log
- 18:25 Read fix list + repo (commit 1af7759). Branch `shooter-fixes-night-before`.
- 18:40 Added ShooterConfig + ShooterController (PIDF, gate, cap, feed, voltage).
- 18:50 Added MaxV Test, ShooterLogger; rewrote both tuners on the controller.
- 19:00 Added BioBuzz TeleOp. Compile + 18 logic checks pass. Committed `ac9be61`.
- 19:10 Pushed branch, opened PR #1: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/1
