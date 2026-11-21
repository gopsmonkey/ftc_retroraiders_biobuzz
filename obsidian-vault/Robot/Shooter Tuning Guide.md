---
tags: [ftc, shooter, tuning, how-to]
updated: 2026-11-20
---
# Shooter tuning guide

Back to [[FTC Home]] · Why: [[Shooter Fix List - Night Before Competition]] · Code: [[Shooter Software Implementation Log]]

**Do the mechanical fixes first.** They change the load the PIDF handles.

1. **Fresh battery** (≥ 12.8 V on telemetry).
2. **Measure maxV**: run *Shooter MaxV Test*, no ball loaded, hold **A** for ~3 s.
    - Copy into `ShooterConfig.java`: `MAX_VELOCITY`, `SHOOTER_F`, `SHOOTER_P`, `SHOOTER_I` (D = 0).
    - Formula ([REV](https://docs.revrobotics.com/first-global/fgc-pid)): F = 32767 ÷ maxV, P = 0.1F, I = 0.1P. Expect maxV ≈ 2,400–2,520: a 6000 rpm Yellow Jacket at 28 CPR runs ~5,400 rpm unloaded (= 2,520 ticks/s; a bit less with the flywheel on) ([ServoCity](https://www.servocity.com/5202-series-yellow-jacket-motor-1-1-ratio-24mm-length-6mm-d-shaft-6000-rpm-3-3-5v-encoder)). For maxV 2,450: F ≈ 13.4, P ≈ 1.3, I ≈ 0.13.
    - Hold **B**: if the intake reads ticks/s, set `INTAKE_HAS_ENCODER = true` and `INTAKE_FEED_VELOCITY` to the shown 1.2× value. Reads 0 → leave false (feeds at power 1.0).
    - Why: open-loop power 0.8 is 0.8 of whatever the battery gives ([gm0](https://gm0-ja.readthedocs.io/ja/latest/docs/software/adv-control-system/sdk-motors.html)), so feed speed drops as the battery drains.
3. **Deploy** the new config (build + install).
4. **Tune P under load**: run *Shooter PIDF Tuner*. A = spin up, RT = feed (gated). Press **B** to log.
    - Select P (d-pad left/right), raise with d-pad up. Watch *Last recovery* and *Worst recovery*.
    - **Step size:** at the default 1× a P step is 1.0 and an I step is 0.1. That's huge next to P ≈ 1.3 and I ≈ 0.13, so press the **left stick** for 0.1× steps when tuning P and I.
    - Stop when recovery is fastest without overshoot that keeps ringing. Back resets stats.
    - Hub velocity is ~50 ms averaged: compare runs to each other.
5. **Copy final P/I/D/F into `ShooterConfig`.** The tuner does NOT save them, and the hub [forgets PIDF on power cycle](https://ftc-docs.firstinspires.org/programming_resources/shared/pidf_coefficients/pidf-coefficients.html).
    - **Trap:** the tuner's values stay loaded until the hub powers off. Practice right after tuning looks better than the first match tomorrow.
6. **Presets**: drive to each taped spot, set `PRESET_CLOSE/MID/FAR` (all ≤ 80% of maxV, which leaves the loop headroom to recover after a shot). Tape one spot **per CELL**, because the up CELL swaps ends after every TIP.
7. **Power-cycle the hub**, run *BioBuzz TeleOp*, check init says `PIDF on hub: OK`.
8. Commit to the repo (see [[Repo and Branches]]).

## Gate knobs (ShooterConfig)
| Setting | Default | Change if… |
| --- | --- | --- |
| `AT_SPEED_TOLERANCE` | 0.03 (3%, ~45 ticks/s at 1450) | Shots come too slowly → 0.04. Shots still inconsistent → 0.02. |
| `FEED_MIN_PUSH_MS` | **0** on PR #3 (team: brake the instant the wheel is off target; was 300, covering the ~0.2 s grab → exit in [[Second Video Analysis (30 fps)]]) | Ball stalls halfway into the hood → raise toward 300. Two balls go in together → lower. |
| `FEED_ONLY_AT_SPEED` | true (PR #3) | false = RT collects with the shooter off again (balls get pushed into the stopped wheel). |
| `FEED_TICKS_PER_BALL` | 0 = off (PR #3) | Measure it, see "One ball per shot" below. |
| `FEED_GAP_MS` | 200 ms (PR #3) | Second shot falls short → raise toward 300 (more recovery). Shots too slow → lower. |
| `VOLTAGE_COMPENSATE_F` | false | Late-match shots fall short → test true. Every top team runs voltage compensation ([[Top Teams Shooter Reference]]). |
| `INTAKE_FULL_CURRENT_AMPS` | 0 (off) | Watch *Intake current* while collecting; set between normal amps and amps with 4 balls packed. Then the gamepad buzzes when full. |
| `TRIM_STEP` / `TRIM_LIMIT` | 25 / 200 ticks/s | gamepad2 d-pad trim during matches. Copy a trim that works into the presets afterwards. |
| `FLOWER_UP` / `FLOWER_DOWN` | 0.0 / 0.5 (guesses) | Measure the real servo positions. |

## One ball per shot (`FEED_TICKS_PER_BALL`, PR #3)
The speed-dip brake reacts ~50 ms late (the hub averages velocity), so the next ball is already in the wheel. This setting stops it earlier ([[PR 3 - Intake Brake and One-Ball Feed]]).
1. Leave `FEED_TICKS_PER_BALL = 0`. Press A, wait for `At speed: YES`, hold RT, shoot all 4.
2. Read **Intake ticks fed (this RT hold)** on telemetry and divide by 4.
3. Set `FEED_TICKS_PER_BALL` to ~70% of that, then build. Telemetry shows `BRAKED (gap between balls)` between shots.
4. Two balls still go out → lower it. A ball stops short of the wheel → raise it.
5. Feed speed: `INTAKE_FEED_POWER` is 0.5 since `16d2c68` (was 1.0). Two balls still go → 0.4; balls stall → 0.6.

## What you see → what to change (from [[Sathwik Handoff]])
Each round: press A and wait for speed to settle, press Back, fire 4 POLLEN fast, look where they landed, change **one** value, then repeat. A shot counts when speed drops below 93% of target; it is recovered at 98%. Leave I and D at 0 unless a small error remains.

| What you see | Meaning | Change |
| --- | --- | --- |
| First goes in, the rest fall short | Wheel doesn't recover in time | P up 1–2 steps, and/or target +50 |
| 4 shots scattered long and short | Speed bounces around target | P down 1 step |
| First too long, rest fine | Overshoot while recovering | P down 1 step, or target −50 |
| All 4 short, even the first | Wheel doesn't reach target | Error > ~30 before firing → F up 1–2 steps; error ≈ 0 → raise target |
| All 4 too long | Target too high | Lower target (left bumper) |
| Fine at first, worse later | Battery draining or motor warm | Fresh battery, let it cool |
| Surging/buzzing, numbers jumping | P too strong | P down 2 steps |
| All 4 land together | Done | Write down F, P, I, D and the target |

Over time: repeat the 4-shot burst 5–10 times (Back between runs). If *Worst recovery* climbs, suspect the battery or motor heat. Never feed faster than *Last recovery*.
