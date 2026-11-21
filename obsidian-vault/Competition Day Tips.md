---
tags: [ftc, competition, tips]
created: 2026-11-20
---
# Competition day tips (2026-11-21)

Back to [[FTC Home]] · Every match: [[Pre-Match Checklist]] · Questions to answer: [[Open Questions]]

**Goal for a first league meet: a reliable robot that scores one TIP a match plus LEAVE and PARK beats a fast robot that breaks.**

## Tonight
- [ ] Small mechanical fixes: [[Small Fixes Without Reprinting]].
- [ ] Code: patches A–C and the test run in [[Code Check - Night Before]]. Add the [[Preload Shoot + Leave Auto]].
- [ ] **Code freeze.** Deploy one branch, commit it, tag it (`git tag event-2026-11-21`). Tomorrow, change only `ShooterConfig` numbers.
- [ ] Charge every battery and label them. Use the freshest one for each match.
- [ ] Driver practice: 10 full cycles with the match TeleOp. Collect 4 with the shooter **off** → press A while driving → stop on the tape → hold RT → X.
- [ ] Practice the AUTO start placement until anyone on the team can do it the same way.

## Pack list
Charged batteries + charger, extension cord, laptop with Android Studio, the repo and a USB-C cable, Driver Station phone/controller, spare gamepad, hex keys and screwdrivers, spare screws and nylock nuts, spare small printed parts, CA glue, packing tape, paint pen, zip ties (wires only), measuring tape, floor tape.

## At the event
- **Inspection first.** The robot must fit the 18 in. sizing box at the start. Battery and wires secure. Robot Controller and Driver Station apps up to date and matching.
- **Practice field:** check each preset from the taped spots on the real HIVE, for **both** CELL ends (the up CELL swaps after every TIP). Adjust `PRESET_CLOSE/MID/FAR` if needed. See [[BIOBUZZ Rules and Scoring]].
- **Talk to every alliance partner:** both LEAVE in AUTO and both PARK at the end (SWARM RP), who shoots when, and stay out of each other's lane at the HIVE.
- **In matches:**
    - Shoot only while stopped. Wait for the HIVE to settle after a TIP.
    - Never hold more than 4 balls (G407).
    - Head back to PARK with about 10 s left (5 points).
- **Between matches:**
    - Check the paint marks (flywheel hub, hood) and bracket bolts, and look for cracks.
    - Swap the battery.
    - Write one line per match here or in a new note: shots taken, shots in, what broke.

## Judging
The shooter story is a strong engineering story. You found the problem with frame-by-frame video (stall time, mount movement) and tuner data (speed dip and recovery). Then you fixed it with mechanical changes plus an at-speed gate. Show the video frames and the fix list, and make sure everyone can explain one part.
