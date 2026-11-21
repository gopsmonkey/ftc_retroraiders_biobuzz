---
tags: [ftc, shooter, design, later]
created: 2026-11-20
---
# After competition: bigger redesigns

Back to [[FTC Home]]

These fix the causes instead of managing them. Don't start them the night before.

- Give the feeder its own motor, with a stop that holds each ball in the same spot until you fire. Ends the shared intake-and-feed problem and the stall.
- Support the flywheel shaft on both sides on a stiff plate, and add mass to the shaft (a second wheel or steel weights) so each shot takes a smaller bite of speed.
- Build a stiffer, adjustable hood, so distance changes with hood angle instead of speed alone.
- Write our own velocity loop at a fixed rate, with a feedforward that adjusts for battery voltage.
- Add a second flywheel motor and a heavier wheel. Every top team runs two ([[Top Teams Shooter Reference]]).
- Track which CELL is up and how many balls are in it: 3 POLLEN to the first TIP, then ~8. Switch the aim after each TIP ([[BIOBUZZ Rules and Scoring]]).
- Count balls in the robot (beam break or color sensor) and stop the intake at 4 (G407).
- Odometry (Pinpoint) + auto-aim to the CELL, then a turret and shoot-while-moving. See [[Top Teams Code Deep Dive]].
- Keep every OpMode in the repo and tag the commit brought to each event. See [[Repo and Branches]].
