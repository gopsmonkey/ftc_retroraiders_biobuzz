---
tags: [ftc, shooter, evidence]
source: https://claude.ai/artifact/2TijvqY1ftJomPvE1RqhEn
created: 2026-11-20
---
# Shooter video evidence

Back to [[Shooter Fix List - Night Before Competition]]

At about one shot a second, **speed held within 2–3% while exit angle swung by about 8°**. The clip is 240 fps slow motion (each frame ≈ 4 ms real time). Seven shots measured from the same camera angle.

| Shot | Real time (s) | Gap before (s) | Stall before grab (ms) | Exit angle vs. average (est.) | Exit speed vs. shot C |
| --- | --- | --- | --- | --- | --- |
| A | 3.7 | — | — | 0° | — |
| B | 4.4 | 0.7 | — | +0.7° | unreliable |
| C | 5.4 | 1.0 | 63 | −1.9° | 100% |
| D | 6.2 | 0.8 | 71 | +3.6° | ~98% |
| E | 7.1 | 0.9 | 83 | +2.8° | ~99% |
| F | 10.6 | 3.5 | 54 | −4.7° | ~96% |
| G | 13.6 | 3.0 | 58 | −0.5° | ~88% |

## Notes
- A and B were filmed at normal speed, so only direction could be measured. B's speed falls in the iPhone's ramp into slow motion, so it was excluded.
- Exit speed ≈ 6 m/s, ±15%, scaled from the 3-inch Pollen ball.
- Angle spread is 9°–22° in the camera's view, about 8° of real launch angle. It's in the wheel's plane, so it changes **arc height**, not left-right aim. D and E (4th and 5th in a row) both went the same way.
- Stall grew through the burst (63 → 71 → 83 ms) and was shorter after rests (54, 58 ms). Longer stalls lined up with steeper shots, ~0.8 correlation over 5 shots. A strong hint, not proof.
- G was 12% slower after a 3 s rest, so it wasn't a recovery problem. Points to grip/squeeze changing ball to ball.
- Motor moved 1.5–3 mm on every shot and wobbled ~12 Hz for >150 ms. D bent the most (~3 mm) and left at the steepest angle.
- **Not measured:** flywheel rpm, hood flex, whether shots scored (goal not in frame). Shots 8–10 left out (robot adjusted, camera moved).
- Timing: video recorded 5:05 pm, about an hour before `ShooterPIDFTuner` was committed (6:00 pm). It most likely ran Shooter Velocity Tuner at 1,450 ticks/s with whatever PIDF the hub held.
- Cross-check: 1,450 ticks/s on a 28-count motor ≈ 3,100 rpm, which fits ~6 m/s exit speed.
