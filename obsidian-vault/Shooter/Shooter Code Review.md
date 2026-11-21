---
tags: [ftc, shooter, software, code-review]
repo: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz
commit: 1af7759
created: 2026-11-20
---
# Shooter code review (commit `1af7759`)

Back to [[Shooter Fix List - Night Before Competition]] · Fixes: [[Shooter Software Implementation Log]]

The [repo](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz) holds two diagnostic OpModes, `ShooterPIDFTuner.java` and `ShooterVelocityTuner.java`. Nothing in it feeds based on flywheel speed, and nothing saves the tuning.

| Finding | Where (at 1af7759) | Why it matters | Fix | Status |
| --- | --- | --- | --- | --- |
| No match TeleOp or Autonomous in the repo | whole repo | The code that runs tomorrow can't be reviewed or recovered | Commit it tonight | Reference `BioBuzzTeleOp` added; real match code still needs committing |
| Tuned PIDF is never saved | PIDF Tuner 72–77, 252–254 | Hub forgets it on power cycle | Hardcode in init | Done: `ShooterConfig` + `ShooterController.init()` |
| Feeding never waits for the flywheel | PIDF Tuner 158–172, Velocity Tuner 138–156 | Next ball can hit a slowed wheel | At-speed gate | Done |
| Intake runs open loop at 0.8 | PIDF Tuner 21, 68–70 | Feed slows as battery drains, so stall varies | Velocity control, or 1.0 while shooting | Done (config switch) |
| Max target 6,000 ticks/s | PIDF Tuner 19, Velocity Tuner 31 | Motor tops out ~2,500, no headroom | Cap at 80% of maxV | Done |
| No battery voltage reading | both files | F term weakens as battery drains | Fresh battery; optional scaled F | Done (telemetry + optional scaled F) |
| Shot counted only on a 7% dip | PIDF Tuner 23–24, 182 | Smaller dips missed; hub smooths ~50 ms | Compare runs; log to file | Done (CSV logging) |
| Drive speed fixed at 1.0 | Velocity Tuner 26 | Hard to line up a shot | Precision mode | Done |
| No bulk caching or loop timer | both files | Loop rate unknown and slower | Bulk caching AUTO | Done |

- Checked and fine: mecanum mixing math, FLOAT zero-power on the shooter, encoder reset sequence.
- Assumption: one intake motor and no separate feeder, so the intake also drives the transfer. If a feeder motor or servo gate exists outside this repo, the gate goes on that instead.
