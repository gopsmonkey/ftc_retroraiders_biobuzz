---
tags: [ftc, todo, index]
updated: 2026-11-21
---
# To-Do

Back to [[FTC Home]]

The one task list for the team. Each task links to the note with the details; don't copy steps here. Tick the box when done, and add your name if you take a task.
Owners: **Sathwik** (robot, wiring, `MainTeleOp`), **Abraar** (shooter code branch), **Shresht** (night-before review, AUTO), **Prajin** (research, vault, PR #3 on the robot), **Claude** (only the tasks marked for it).

> [!important] Where things stand (2026-11-21, 01:00)
> The robot runs **PR #3** (`intake-gate-at-speed`): the intake stays braked until the flywheel is at −1375 ([[PR 3 - Intake Brake and One-Ball Feed]]).
> The latest build **failed** because OnBot has the new `ShooterController` but the old `ShooterConfig`; fix below.
> The Control Hub is also missing 3 repo files, including the AUTO ([[OnBot Java Check]], "On the Control Hub").

## Right now (blocks the robot)
- [ ] **Re-paste `ShooterConfig.java` from commit `16d2c68`** (https://raw.githubusercontent.com/gopsmonkey/ftc_retroraiders_biobuzz/16d2c68/ShooterConfig.java). It fixes the failed build and slows the feed to 0.5. Cmd+F must find `FEED_TICKS_PER_BALL`, then Build Everything. `ShooterConfig` and `ShooterController` must always come from the same commit. (Prajin)
- [ ] **Add the 3 missing files to OnBot**, all from `7a10048`: `PreloadShootLeaveAuto.java` (the AUTO), `ShooterMaxVelocityTest.java` and `ShooterLogger.java`. **Replace** the old `ShooterPIDFTuner.java` and `ShooterVelocityTuner.java` with the branch versions; the old ones don't use the gate or `ShooterConfig`. ([[OnBot Java Check]])
- [ ] **Robot test** with *BioBuzz TeleOp* ([[Code Check - Night Before]], rows 4–6 updated for PR #3). Confirm the wheel spins the launch way at −1375, otherwise set `SHOOTER_SIGN = 1`.
- [ ] **Measure `FEED_TICKS_PER_BALL`**, so the 2nd ball isn't shot early ([[Shooter Tuning Guide]], "One ball per shot"). `INTAKE_FEED_POWER` is now 0.5 (`16d2c68`). Two balls still go together → 0.4; balls stall → 0.6.
- [ ] **Send every changed number to Claude** to commit. OnBot edits never reach GitHub. Then squash-merge PR #3 and tag `event-2026-11-21`. (Prajin + Claude)


## Decide tonight (blocks other tasks)
- [x] **Which TeleOp runs:** *BioBuzz TeleOp*. It has had the flower servos since `53f96f2` and is the one on the Control Hub now (2026-11-21). `MainTeleOp` is not on the hub.
- [x] **Loaded into OnBot Java; build succeeded (2026-11-21, with PR #3).**  It compiles cleanly on SDK 9.2–12.0 (re-checked on `391d8ed`). Upload all 8 files with **Upload Files** (don't paste), overwrite the old tuners, then Build Everything. If the build fails, it's an old file such as `PollenAutoNav`. (Sathwik; steps in [[OnBot Java Check]])

## Tonight: robot (Sathwik, hands-on)
- [ ] **Fix the Expansion Hub blinking blue.** All 4 drive motors depend on it, so do this first. ([[Sathwik Handoff]], Expansion Hub section)
- [ ] Mechanical fixes: mount, hood, feed guide. ([[Shooter Fix List - Night Before Competition]], [[Small Fixes Without Reprinting]])
- [ ] Measure the flower-servo up/down positions and check intake direction. Put them in `FLOWER_UP/DOWN` in `ShooterConfig`. ([[Hardware Config Names]])
- [ ] While collecting in `BioBuzz TeleOp`, read *Intake current* with 0 and with 4 balls; set `INTAKE_FULL_CURRENT_AMPS` between them. ([[Shooter Tuning Guide]])
- [ ] Charge and label every battery. ([[Competition Day Tips]])

## Tonight: shooter software (Abraar)
- [x] Code polish from the review: RB release mid-ball, stale comment, stray `workspace.json`. Done in `391d8ed`, plus write caching for a faster loop.
- [ ] Run MaxV Test → PIDF Tuner, put real numbers in `ShooterConfig`, commit. ([[Shooter Tuning Guide]])
- [ ] Calibrate presets at tape marks, one spot per CELL. ([[Shooter Tuning Guide]])
- [ ] Power-cycle and confirm `PIDF on hub: OK`. ([[Shooter Tuning Guide]])
- [ ] Merge PR #1, or decide which branch goes on the hub. ([[Repo and Branches]])
- [ ] **Review PR #3** (Abraar): intake brake + one-ball feed. It changes `SHOOTER_SIGN` to −1, `FEED_MIN_PUSH_MS` 300 → 0 and `INTAKE_HAS_ENCODER` to true. **Squash and merge** (one commit carries stray cache files). https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/3 · [[PR 3 - Intake Brake and One-Ball Feed]]

## Tonight: AUTO and code freeze (Shresht)
- [x] Patches A–C (`2e4aaf8`) and D (`4479479`). Night-before notes parts 2–3 uploaded. (Shresht, 2026-11-20)
- [ ] The 20-minute robot test. ([[Code Check - Night Before]])
- [ ] **Put back the hand adjustment from the 30 fps video**, or reset the squeeze: the ball waited twice as long afterwards. ([[Second Video Analysis (30 fps)]])
- [ ] Tune and test the [[Preload Shoot + Leave Auto]]. The 4 preloads TIP the HIVE, about 28 pts.
- [ ] Code freeze: deploy one branch, commit it, tag it `event-2026-11-21`. ([[Competition Day Tips]])

## Tonight: everyone
- [ ] Driver practice: 10 full cycles, and practice the AUTO start placement. ([[Competition Day Tips]])
- [ ] Answer the rest of the [[Open Questions]].

## What Claude can do now without touching anyone's files
Not started. Pick one and tell Claude to go.
- [x] **Check that every file on `shooter-fixes-night-before` will compile in OnBot Java.** Done 2026-11-20 on commit `2e4aaf8`: 0 errors on SDK 9.2–12.0. ([[OnBot Java Check]])
- [x] **Add the handoff answers under [[Open Questions]] #1, #2, #4, #6, #7, #9**, and fix the pack list (Android Studio → browser for OnBot Java). Done 2026-11-20.
- [ ] **Port the shooter fixes into `MainTeleOp`.** Needs: Sathwik pushes `MainTeleOp.java`, and Abraar agrees.
- [ ] **Push Sathwik's local files to the repo** (library, autos, `MainTeleOp`). Needs: the files, attached or pushed.
- ~~Convert POLLEN HSV to `PollenAutoNav`~~ **Dropped: the robot has no camera** ([[Robot Overview]], [[Open Questions]] #14). Measured values kept for reference: hue 43.1–48.5, sat 0.65–0.95, val 0.604–0.620.
- Hold-to-aim with odometry and `driveDiagonal` in the library: **the team will write AUTO/automation code themselves, so Claude doesn't take these.**
- [x] **Repo README** (OnBot Java upload, controls table, tuning steps). Done 2026-11-20 on `53f96f2`.
- [ ] **Belt and pulley calculator.** Needs: vendor, pitch, ratio and shaft distance.
- [x] Verify the NECTAR size and the rules against the manual. NECTAR is ~3.6 in, and crossing over is a foul only in AUTO. ([[Sathwik Handoff]], Corrections section)

## After the meet
- [x] **Decided (Abi, 2026-11-20):** OnBot Java for this competition, then switch to Android Studio afterwards.
- [ ] Switch the team to Android Studio. (after the competition)
- [ ] Then Pedro Pathing: re-enable the parked agent, note the Control Hub's IMU orientation, re-measure pod offsets. ([[Pedro Pathing Plan]])
- [ ] Commit the team's own AUTO to the repo. (team)
- [ ] Re-measure odometry pod offsets for the new drivetrain, spin-test, and check both invert flags. (Sathwik, [[Sathwik Handoff]])
- [ ] Measure the shooter distance → speed table, using odometry for distance (no camera, so no AprilTags). ([[Top Teams Shooter Reference]])
- [ ] Measure viper slide presets. ([[Hardware Config Names]])
- [ ] Fusion chassis CAD in "Admin Project", once the Fusion connector is reachable. ([[Sathwik Handoff]])
- [ ] Bigger shooter redesigns. ([[Post-Competition Redesigns]])
- [ ] Turret, only if added later: needs ticks/rev, gear ratio and zero angle. ([[Sathwik Handoff]])
- [ ] Merge the `team-obsidian-vault` branch into `main`, so the vault is on the default branch. (Prajin)
