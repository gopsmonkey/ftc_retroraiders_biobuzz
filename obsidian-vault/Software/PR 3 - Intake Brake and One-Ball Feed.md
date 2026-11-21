---
tags: [ftc, software, shooter, intake, pr]
branch: intake-gate-at-speed
pr: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/3
base: shooter-fixes-night-before @ 391d8ed
latest: 7a10048
updated: 2026-11-21
---
# PR #3: intake brake and one-ball feed

Back to [[FTC Home]] · Tasks: [[To-Do]] · Tuning: [[Shooter Tuning Guide]] · Earlier code history: [[Shooter Software Implementation Log]]

**This is the code the robot runs now.** Branch `intake-gate-at-speed` is built on Abraar's `391d8ed`, and its PR #3 targets `shooter-fixes-night-before`. As of 2026-11-21 ~00:30, Prajin pasted its files into OnBot Java and the build succeeded.

## Why it exists (the team's words, 2026-11-20 night)
1. The rollers pushed every ball up to touch the flywheel before it was at speed. The team asked that the intake not move **until the wheel is at launch velocity**.
2. With the brake in place, the brake stopped **too late**. After one shot, the next pollen was already in the wheel and got shot anyway.

## Hardware facts this depends on (confirmed by Prajin)
- **`intakeMotor` is ONE motor driving every roller through one chain**: the big intake wheel *and* the two small wheels under the flywheel. Software can't stop the small wheels separately. No `feederMotor` exists; never invent config names (see [[Hardware Config Names]]).
- **An encoder cable was added to `intakeMotor`** on 2026-11-20, so `INTAKE_HAS_ENCODER = true`.
- **Launch velocity is −1375 ticks/s**, set by `SHOOTER_SIGN = -1` with `PRESET_MID = 1375`. Sathwik's old tuners shot at +1450, so if the wheel spins backward, set `SHOOTER_SIGN = 1`.

## What the intake does now (`BioBuzz TeleOp`)
| Driver input | Wheel state | `intakeMotor` | Telemetry |
| --- | --- | --- | --- |
| RT, shooter off | — | Braked | `BRAKED (press A)` |
| RT | Spinning up, or > 3% off −1375 | Braked | `BRAKED (wheel not at speed)` |
| RT | Within 3% of −1375 | Feeds at `INTAKE_FEED_POWER` | `feed pwr` |
| RT, `FEED_TICKS_PER_BALL > 0` | At speed | Feeds that many encoder ticks, then brakes `FEED_GAP_MS`, then repeats | `BRAKED (gap between balls)` |
| RB + RT | Shooter running | Feeds even if not at speed (override, skips the pulse) | `feed pwr` |
| RB + RT | Shooter off | Braked (can't push into a stopped wheel) | `BRAKED (press A)` |
| LB | Any | Reverse (unjam), always allowed | `reverse` |

**Braked** means power 0 + `ZeroPowerBehavior.BRAKE`. With the encoder and `RUN_USING_ENCODER`, power 0 is a velocity-0 command, so the motor actively holds.

**Driver habit:** press A, wait for the rumble / `At speed: YES`, then hold RT. RT alone with the shooter off does nothing; that's by design.

## Settings it added or changed (`ShooterConfig.java`)
| Setting | Value | Was | Meaning |
| --- | --- | --- | --- |
| `FEED_ONLY_AT_SPEED` | `true` | (new) | Intake never pushes up unless the wheel is at speed. `false` = old "collect with shooter off". |
| `FEED_TICKS_PER_BALL` | `0` (off) | (new) | Encoder ticks to push one ball. **Must be measured** ([[Shooter Tuning Guide]]). |
| `FEED_GAP_MS` | `200` | (new) | Brake time between balls. |
| `FEED_MIN_PUSH_MS` | `0` | 300 | Team asked to brake the instant the wheel is off target. Raise toward 300 if a ball stalls halfway into the hood. |
| `INTAKE_HAS_ENCODER` | `true` | false | Encoder cable added. If it comes loose, set `false`. |
| `SHOOTER_SIGN` | `-1` | 1 | Launch command is −1375. |
| `PRESET_MID` | `1375` | 1450 | |

## Why the brake was late, and how the pulse fixes it
- The hub reports flywheel speed averaged over ~50 ms, plus one loop of delay. By the time the code *sees* the dip from ball 1, ball 2 (packed right behind) is already touching the wheel.
- The fix is to stop ball 2 **before** it gets there. With the encoder, the intake moves a measured number of ticks per ball, then brakes `FEED_GAP_MS`. The dip-brake still runs as a backup.
- No-code stopgap: lower `INTAKE_FEED_POWER` (1.0 → ~0.6), so balls arrive slower and the dip-brake can catch up.

## Commits on the branch
| Commit | What |
| --- | --- |
| `997a408` | Brake until at speed, `FEED_ONLY_AT_SPEED`, −1375, encoder on, push 0, RB can't push into a stopped wheel, `BRAKED` telemetry, README + TeleOp header |
| `17504b8` | Removed plugin cache files that `997a408` accidentally included (`.claude-flow/`, `.swarm/`, `ruvector.db`) |
| `6fc84c7` | Restored Abraar's `.gitignore` line and kept the cache ignores |
| `7a10048` | One-ball pulse: `FEED_TICKS_PER_BALL`, `FEED_GAP_MS`, "Intake ticks fed" telemetry |

> [!warning] Merge with **Squash and merge**
> `997a408` still holds the plugin cache files in its history (the repo is public). A squash merge keeps them out of `shooter-fixes-night-before`. Scrubbing them from GitHub completely needs a force-push, and nobody has approved that.

## Checked
- **OnBot Java compile:** 0 errors on SDK 9.2.0, 10.1.1, 11.2.1, 12.0.0 at Java 8 (`Software/attachments/onbot-compile-check.sh`).
- **Mock-motor tests (laptop, real SDK classes):**
    - `GateTest`, 10/10: braked when off, spinning up, dipped or reversed; feeds at −1375; RB override can't push into a stopped wheel; LB reverse works.
    - `PulseTest`, 5/5: stops at exactly `FEED_TICKS_PER_BALL`, `gap` telemetry, gap ≈ `FEED_GAP_MS`, resumes, dip brakes mid-pulse.
    - Both are in `Software/attachments/tests/`, so re-run them after any change.
- **On the robot:** the brake build ran. The pulse build (`7a10048`) is **not yet confirmed**; see below.

## On the robot so far (2026-11-21)
- ~00:30: first PR #3 build (`997a408` files) succeeded in OnBot. The brake works, but the next ball still got shot, which led to `7a10048`.
- 00:54: the `7a10048` build **failed** with `cannot find symbol FEED_TICKS_PER_BALL / FEED_GAP_MS` in `ShooterController`. Cause: the new `ShooterController.java` was pasted, but OnBot still had the old `ShooterConfig.java`. **Fix: paste `ShooterConfig.java` from `7a10048`**, then check that Cmd+F finds `FEED_TICKS_PER_BALL` (~line 100) before Build Everything.
- **Rule: `ShooterConfig` and `ShooterController` must always come from the same commit.**

## Open items (owners in [[To-Do]])
- Re-paste `ShooterConfig.java` (`7a10048`), build, run the robot test.
- Measure `FEED_TICKS_PER_BALL` ([[Shooter Tuning Guide]]).
- Confirm the wheel spins the launch way at −1375.
- Send the final numbers to Claude to commit (OnBot edits don't reach GitHub), then squash-merge PR #3 and tag `event-2026-11-21`.
