---
tags: [ftc, shooter, evidence]
source: IMG_2083.mov (30 fps, 1920×1080, 2:45, overhead phone, recorded 2026-11-20 4:41 pm)
updated: 2026-11-20
---
# Second video analysis (30 fps)

Back to [[FTC Home]] · 240 fps clip: [[Shooter Video Evidence]] · Fixes: [[Shooter Fix List - Night Before Competition]] · Robot: [[Robot Overview]]

Measured frame by frame (1 frame = 33 ms). Shots were found automatically by tracking the yellow ball, then checked by eye. Balls are hand-loaded into the clear roller intake. The robot fires straight up toward the camera at the wooden backstop.

## Shots in the clip
| Shot | Time | Gap before | Ball sits in the hood before the wheel grabs it | Grab → exit | Exit direction (top view) | Exit speed (image) |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0:09.5 | — | ~0.50 s | ~0.2 s | 76° | 9.9 px/frame |
| 2 | 0:15.8 | 6 s | ~0.53 s | ~0.2 s | 68° | 10.1 |
| 3 | 0:23.2 | 7 s | ~0.67 s (longest) | ~0.2 s | 69° | 10.2 |
| 4 | 0:51.2 | 28 s | ~0.50 s | ~0.2 s | 82° | 10.9 |
| 5 | 1:15.6 | 24 s | ~0.50 s | ~0.2 s | 75° | 9.8 |
| *Hand adjustment of the flywheel/hood area, 1:36–1:57; camera moved* | | | | | | |
| 6 | 2:12.7 | — | **~0.93 s** | ~0.2 s | (new camera angle) | — |
| 7 | 2:26.2 | 13 s | **~1.13 s** | ~0.2 s | (new camera angle) | — |

![[30fps-shot-at-9s.jpg]]
*Shot 1, every frame from 8.90 s to 10.20 s. The ball sits still in the purple hood with its holes sharp (not spinning) until about 9.33 s, then the wheel grabs it and it's gone by about 9.53 s.*

![[30fps-shot-at-132s-after-adjustment.jpg]]
*Shot 6, after the hand adjustment. The ball creeps around the inside of the purple ring for about 0.9 s before the wheel grabs it.*

## What it means
1. **The feed is the problem, not flywheel speed.**
    - Shots were 6–28 s apart, so the wheel was always back at full speed.
    - Exit speed was nearly the same every time (±5%).
    - What changes is how long the ball waits in the hood before the wheel grabs it: 0.5–0.67 s, and the wheel only touches it for the last ~0.2 s.
2. **Longer waits went with a different launch direction.** The two longest waits (shots 2–3) left at 68–69°, the short ones at 75–82°. That's a 14° spread seen from above. With only 5 shots this is a strong hint, not proof. It matches the 240 fps clip ([[Shooter Video Evidence]]).
3. **The adjustment at 1:36 made the creep about twice as long** (0.93–1.13 s). Whatever was changed (gap or position) should be **put back**, or the squeeze set again per [[Small Fixes Without Reprinting]]. This answers [[Open Questions]] #10.
4. **The flywheel mount moves on every shot.** The motor can shifts 2.5–4 px against the printed frame, about 2 mm (the ball is ~112 px for 2.8 in). Before each shot it stays still (≤0.3 px). This matches the 1.5–3 mm measured at 240 fps.
5. **Why the ball creeps:** the flywheel is mounted directly on the motor shaft with nothing supporting the other side, and the ball enters the purple ring far from where the wheel touches it. The orange transfer wheels push it slowly around the ring until it reaches the wheel. A short guide that steers the ball straight to the wheel, plus a locked squeeze, attacks this directly ([[Small Fixes Without Reprinting]]).

## What the code can and can't do about it
- Feeding at power 1.0 while shooting (`INTAKE_FEED_POWER`) pushes harder than the old 0.8, which should shorten the creep.
- The push hold (`FEED_MIN_PUSH_MS`, fixed in `4479479`) keeps the intake pushing once a ball touches the wheel.
- No code can fix the geometry. If the ball still waits more than ~0.3 s after the mechanical fixes, the next step is a feed roller right at the ring entry ([[Post-Competition Redesigns]]).

## Not measured
Flywheel rpm (there's no telemetry in the clip), whether shots would score (no HIVE), and hood flex (the ball hides it).

## Next video
Side view at wheel height, slow motion, on a tripod, 5–10 shots in a row at a real target. Film before **and** after each mechanical change.
