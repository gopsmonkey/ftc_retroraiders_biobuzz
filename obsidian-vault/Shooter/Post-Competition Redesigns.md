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
- Keep every OpMode in the repo and tag the commit brought to each event. See [[Repo and Branches]].
