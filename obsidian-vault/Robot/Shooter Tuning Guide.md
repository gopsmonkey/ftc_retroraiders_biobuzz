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
    - Formula ([REV](https://docs.revrobotics.com/first-global/fgc-pid)): F = 32767 ÷ maxV, P = 0.1F, I = 0.1P. Expect maxV ≈ 2,400–2,500.
    - Hold **B**: if the intake reads ticks/s, set `INTAKE_HAS_ENCODER = true` and `INTAKE_FEED_VELOCITY` to the shown 1.2× value. Reads 0 → leave false (feeds at power 1.0).
3. **Deploy** the new config (build + install).
4. **Tune P under load**: run *Shooter PIDF Tuner*. A = spin up, RT = feed (gated). Press **B** to log.
    - Select P (d-pad left/right), raise with d-pad up. Watch *Last recovery* and *Worst recovery*.
    - Stop when recovery is fastest without overshoot that keeps ringing. Back resets stats.
    - Hub velocity is ~50 ms averaged: compare runs to each other.
5. **Copy final P/I/D/F into `ShooterConfig`.** The tuner does NOT save them.
6. **Presets**: drive to each taped spot, set `PRESET_CLOSE/MID/FAR` (all ≤ 80% of maxV).
7. **Power-cycle the hub**, run *BioBuzz TeleOp*, check init says `PIDF on hub: OK`.
8. Commit to the repo (see [[Repo and Branches]]).

## Gate knobs (ShooterConfig)
| Setting | Default | Change if… |
| --- | --- | --- |
| `AT_SPEED_TOLERANCE` | 0.03 (3%) | Shots come too slowly → 0.04. Shots still inconsistent → 0.02. |
| `FEED_MIN_PUSH_MS` | 120 ms | Ball still stalls in gap → raise to 150–200. Two balls go in together → lower. |
| `VOLTAGE_COMPENSATE_F` | false | Late-match shots fall short → test true. |
