---
tags: [ftc, software, testing, competition-prep]
branch: shooter-fixes-night-before
commit: 391d8ed
updated: 2026-11-20
---
# Code check: night before

Back to [[FTC Home]] · Changes: [[Shooter Software Implementation Log]] · Upload + build: [[OnBot Java Check]] · AUTO: [[Preload Shoot + Leave Auto]] · Tasks: [[To-Do]]

## Status (commit `391d8ed`)
- **Compiles:** all 8 `.java` files, 0 errors against the real FTC SDK 11.0.0 libraries (Prajin also checked SDK 9.2–12.0, see [[OnBot Java Check]]).
- **OnBot-safe:** one public class per file, no lambdas or streams.
- **Logic:** 13 fake-motor checks pass (PIDF in init, 80% cap, collect vs gated feed, RB override, push hold after every ball, reverse never gated).
- **Never run on the robot.**

## Patches
| | What | Commit |
| --- | --- | --- |
| A | Hold **RB + RT** in *BioBuzz TeleOp* to feed even if the flywheel never reaches speed | `2e4aaf8` (Shresht) |
| B | `FEED_MIN_PUSH_MS` 120 → 300 ms ([[Second Video Analysis (30 fps)]]) | `2e4aaf8` (Shresht) |
| C | PIDF Tuner no longer counts a restart as a shot | `2e4aaf8` (Shresht) |
| D | Push hold counts from the last at-speed moment, so **every** ball gets pushed, not just the first | `4479479` (Abraar) |

Shresht's review found D independently. The version on GitHub is the team's (Abraar's); nothing else was changed.

## Could be better (items 1–3 done in `391d8ed`, 2026-11-20)
1. ~~**Releasing RB mid-ball stops the push instantly.**~~ **Done:** holding RB now counts as at speed for the hold. During the override the wheel isn't "at speed", so the hold timer never starts, and letting go of RB cuts the intake mid-grab. Possible fix: in `ShooterController.updateIntake`, change `if (atSpeed)` to `if (atSpeed || !gateEnabled)`. Low priority: RB is an emergency button.
2. ~~**Stale comment.**~~ **Done.** It used to say "once the gate opens". The code now counts from the last at-speed moment.
3. ~~**Stray file on the code branch.**~~ **Done:** removed, and `.gitignore` keeps it out. It was `obsidian-vault/.obsidian/workspace.json` (a personal Obsidian layout file) was committed in `53f96f2`. Harmless; delete it before merging PR #1.
4. **The 300 ms hold can't fix the creep.** The video shows the ball waits 0.5–1.1 s *before* touching the wheel ([[Second Video Analysis (30 fps)]]). That's a feed-geometry problem: fix it mechanically first ([[Small Fixes Without Reprinting]]).

## Biggest risk: the robot never shoots
The intake only feeds when the flywheel is within 3% of target. If PIDF is off, `At speed` may never say YES.
- **In a match:** hold **RB + RT**.
- **In the pit:** run *Shooter MaxV Test*, put the real numbers in `ShooterConfig`, or raise `AT_SPEED_TOLERANCE` to 0.05.
- AUTO already handles it: if not at speed after 3 s, it shoots anyway.

## 20-minute robot test (after uploading per [[OnBot Java Check]])
Fix the Expansion Hub blinking blue first, or the drive won't work ([[Sathwik Handoff]]).

| # | Do | Pass | If it fails |
| --- | --- | --- | --- |
| 1 | Init *BioBuzz TeleOp* | `PIDF on hub: OK`, battery ≥ 12.5 V | MISMATCH → stop, init again. "Device not found" → [[Hardware Config Names]]. |
| 2 | Drive: stick forward, strafe right, turn right | Robot does what the stick says | Strafe wrong → `STRAFE_SIGN = -1`; turn wrong → `TURN_SIGN = -1` (top of the TeleOp). |
| 3 | Y, then Y again | Flower intake drops, then lifts | Wrong positions → `FLOWER_UP/DOWN` in `ShooterConfig`. |
| 4 | RT with shooter **off** | Intake stays still, telemetry `BRAKED (press A)` (PR #3) | Rollers move → `FEED_ONLY_AT_SPEED` isn't `true`, so the old file is still loaded. |
| 5 | Press A | Wheel spins the shooting way at −1375; rumble blip and `At speed: YES` within ~2 s | Wrong way → flip `SHOOTER_SIGN` (PR #3 set it to −1). Never YES → see the risk above. |
| 6 | Hold RT with shooter on, 4 balls | `BRAKED (wheel not at speed)`, then `feed pwr`; intake pulls **in** and each ball goes through | Pushes out → `INTAKE_FEED_SIGN = -1`. Ball stops halfway into the hood → raise `FEED_MIN_PUSH_MS` (0 now) toward 300. Two at once → lower it. |
| 7 | Hold LB, then B | Intake reverses; wheel reverses slowly, stops when released | — |
| 8 | *Shooter MaxV Test*, hold A (no ball, fresh battery) | Peak ≈ 2,400–2,520 | Very different → copy its numbers into `ShooterConfig`, re-upload, rebuild. |
| 9 | *Preload Shoot + Leave* from the real start tile | Shoots 4, drives off the wall, stops on our side | [[Preload Shoot + Leave Auto]] |

Then commit the changed `ShooterConfig` numbers and tag the commit (`event-2026-11-21`), see [[Repo and Branches]].

## Backup plan
- **TeleOp:** if *BioBuzz TeleOp* misbehaves at the event, switch to Sathwik's `MainTeleOp` (no shooter fixes, and also untested). Keep both on the hub, with different OpMode names.
- **AUTO:** if it misbehaves, set `SHOOT_FIRST = false` for LEAVE only (3 pts).
- **Shooter:** gate never opens → RB + RT for the whole match.
