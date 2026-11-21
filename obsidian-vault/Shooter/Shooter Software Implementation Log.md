---
tags: [ftc, shooter, software, log]
branch: shooter-fixes-night-before
commit: 391d8ed
updated: 2026-11-21
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
**BioBuzz TeleOp (match):** the controls table is in the repo `README.md` on `shooter-fixes-night-before` (one place, kept up to date).

**Shooter PIDF Tuner**: same as before, plus RT/LT now *in/out* (in = gated feed), B = CSV log, gamepad2 A = gate on/off, gamepad2 B = scaled F on/off. Starts from `ShooterConfig` gains, Start resets to them.

**Shooter Velocity Tuner**: same as before (B = +preset mid, A = −900, bumpers ±100), plus LT precision drive, positive intake gated while shooting.

## Verification done
- Compiles with javac (`-Xlint:all`, also `--release 8` like the FTC SDK) against FTC SDK API stubs. No Gradle/SDK in the repo, so a real Android Studio build is still needed.
- 18 logic checks on mock motors all pass: PIDF in init + readback, 80% cap, collect vs gated feed, push-through window, re-gate after window, reverse never gated, voltage-scaled F and its clamp, gate disable.
- **Not tested on the robot.** Gamepad mapping and motor directions are copied from the existing tuners.

## Changes after the first commit
| Commit | Who | What | Review (Claude, 2026-11-20) |
| --- | --- | --- | --- |
| `2e4aaf8` | Shresht | RB + RT feed override in TeleOp | OK. RB was free. Use only if *At speed* never turns YES. |
| `2e4aaf8` | Shresht | `FEED_MIN_PUSH_MS` 120 → 300 | OK. Shots are ~0.7–1 s apart, so a 300 ms push can't send two balls at once. Lower toward 150 if two do go in together. |
| `2e4aaf8` | Shresht | PIDF Tuner: `armed = false` when the shooter stops | **Real bug fix.** Before, the next spin-up after a stop counted as a "shot". |
| `2e4aaf8` | Shresht | `PreloadShootLeaveAuto` (shoot 4 preloads, LEAVE) | Logic checked, no bugs found. All `TUNE` numbers still need the practice field. No other AUTO will be added by Claude ([[Robot Overview]]). |
| `53f96f2` | Claude | BioBuzz TeleOp: flower servos on Y (optional via `tryGet`, no motion during INIT for G403) | Closes the "BioBuzz TeleOp has no flower servos" gap in [[To-Do]]. Positions are guesses. |
| `53f96f2` | Claude | gamepad2 d-pad speed trim ±25 (7759's idea), X resets | Fixes all-short/all-long at the event without a rebuild. |
| `53f96f2` | Claude | Rumble: blip at speed, buzz when intake current says "full" (23849's idea) | Full warning is off until `INTAKE_FULL_CURRENT_AMPS` is measured. Current is read every 100 ms, only while collecting. |
| `53f96f2` | Claude | README: OnBot Java upload + controls table | Replaces the Android Studio instructions. |
| `391d8ed` | Claude | Final pass: releasing RB mid-ball keeps the push; motor commands written only when they change (forced re-send every 500 ms in case a hub resets); flower servos written only on press; stray `workspace.json` removed | Fewer hub round trips per loop. 25 mock checks pass; compiles on SDK 12.0/9.2. |
| `4479479` | Claude | **Push-hold bug fix**: the hold now counts from the last moment the wheel was at speed, so every ball gets the push, not just the first after the gate opens. Same fix in the Velocity Tuner. | Bug found by the code review in the [fix-list doc](https://claude.ai/artifact/2TijvqY1ftJomPvE1RqhEn). The original 18 tests missed it; there are now 21 checks, including a second-ball regression. |

Checks on `391d8ed`: 25 mock-motor logic checks pass; compiles with 0 errors against the real FTC SDK **12.0.0 and 9.2.0** libraries at Java 8 (`Software/attachments/onbot-compile-check.sh`). Still not run on the robot.

## PR #3 (branch `intake-gate-at-speed`, on top of `391d8ed`)
The robot runs this now. Full design, settings, tests and on-robot status: [[PR 3 - Intake Brake and One-Ball Feed]].

| Commit | Who | What |
| --- | --- | --- |
| `997a408` | Claude (for Prajin) | `intakeMotor` braked until the flywheel is at −1375 (`FEED_ONLY_AT_SPEED`). `SHOOTER_SIGN` −1, `PRESET_MID` 1375, `INTAKE_HAS_ENCODER` true, `FEED_MIN_PUSH_MS` 0. RB can't push into a stopped wheel. |
| `17504b8`, `6fc84c7` | Claude | Removed plugin cache files committed by mistake; restored Abraar's `.gitignore` line. |
| `5609301` | Claude (for Prajin) | Hold LT = intake at collect power, no gate (precision drive removed). |
| `cbb8a8a` | Claude (for Prajin) | Tap B = shooter reverse 900 on/off (was hold). |
| `3a79aba` | Claude (for Prajin) | Telemetry: intake actual/target speed; "ticks fed" renamed "distance fed". |
| `8038bf2` | Claude (for Prajin) | Intake feed by velocity: `INTAKE_FEED_VELOCITY` 600 ticks/s (was power ~2000 ticks/s). |
| `e140571` | Claude (for Prajin) | Back-off: reverse 0.3 for 120 ms whenever feeding stops mid-shooting. 01:57 OnBot build failed: old `ShooterConfig` (missing the 2 new lines). |
| `bb88475` | Claude (for Prajin) | `INTAKE_FEED_POWER` 0.5 → 0.3 (team request). |
| `16d2c68` | Claude (for Prajin) | `INTAKE_FEED_POWER` 1.0 → 0.5: stacked balls went up together and the 2nd fell short. |
| `7a10048` | Claude (for Prajin) | One ball per shot: `FEED_TICKS_PER_BALL` + `FEED_GAP_MS`, plus "Intake distance fed" telemetry. |

Checks on `7a10048`: compiles on SDK 9.2/10.1/11.2/12.0; mock tests 10/10 (gate) and 5/5 (pulse) in `Software/attachments/tests/`.

## Still to do (people)
Tracked in [[To-Do]].

## Log
- 18:25 Read fix list + repo (commit 1af7759). Branch `shooter-fixes-night-before`.
- 18:40 Added ShooterConfig + ShooterController (PIDF, gate, cap, feed, voltage).
- 18:50 Added MaxV Test, ShooterLogger; rewrote both tuners on the controller.
- 19:00 Added BioBuzz TeleOp. Compile + 18 logic checks pass. Committed `ac9be61`.
- 19:10 Pushed branch, opened PR #1: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/1
- 21:00 Reviewed Shresht's `2e4aaf8`. Added flower servos, trim, rumble, intake-full warning, README. Pushed `53f96f2`.
- 21:30 Fixed push-hold bug (`4479479`), doc status table and review updated to match.
- 22:30 Final pass `391d8ed` (Shresht's review items 1–3, write caching). Vault de-duplicated.
- 2026-11-20 22:00 Prajin: the two small wheels under the flywheel are on `intakeMotor` (one chain); an encoder was added; launch velocity is −1375. PR #3 opened with `997a408`.
- 2026-11-21 ~00:20 Prajin pasted the PR #3 files into OnBot; **Build succeeded**. The brake works, but the 2nd ball still gets shot (the brake is late).
- 2026-11-21 00:47 `7a10048`: one-ball pulse using the intake encoder.
- 2026-11-21 00:54 OnBot build **failed**: `cannot find symbol FEED_TICKS_PER_BALL / FEED_GAP_MS`. The new `ShooterController` was in, but the old `ShooterConfig` was still there. Fix: re-paste `ShooterConfig` from `7a10048`.
- 2026-11-21 01:00 The Control Hub file list shows `PreloadShootLeaveAuto`, `ShooterMaxVelocityTest` and `ShooterLogger` were never uploaded, and the tuners are Sathwik's old versions ([[OnBot Java Check]]).
