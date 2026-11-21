---
tags: [ftc, game, scouting, shooter, reference]
source: FTCScout API (season 2026), public GitHub repos
created: 2026-11-20
---
# Top teams: how they shoot

Back to [[FTC Home]] · Rules: [[BIOBUZZ Rules and Scoring]] · Our plan: [[Shooter Fix List - Night Before Competition]] · Later: [[Post-Competition Redesigns]]

As of 2026-11-20: 6,595 active teams, 6,610 matches played.

## Where points come from
- **TIPs are almost everything.** The best teams average about 7 TIPs per match by OPR (~140 pts).
- **FLOWER points are about 0 for every top team**, so nobody has figured out endgame NECTAR yet.
- The best AUTOs score 40–45 (2 TIPs + LEAVE).

| World rank (OPR) | Team | OPR | TIPs | Shooter |
| --- | --- | --- | --- | --- |
| 1 | 19666 Forever Knight (CN) | 163 | 7.2 | not public |
| 2 | 16010 Astra Machina (NE) | 157 | 7.2 | not public, best AUTO (45) |
| 3 | 36596 Horizon (OR) | 155 | 7.0 | not public (they publish tools: zenith, simloop) |
| 4 | 21229 Quality Control (WA) | 151 | 6.6 | not public, AUTO 42 |
| 5 | 19564 ITKAN Solar (TX) | 136 | 6.3 | not public (TX, likely at state) |
| 6 | 19705 WXYZ (CN) | 135 | 5.8 | **no turret**, fixed spot, 2 motors |
| 10 | 16750 Technototes (WA) | 123 | 5.6 | turret + hood + Limelight |
| 13 | 23849 Droid Force (WA) | 120 | 5.3 | 2 launchers (POLLEN + NECTAR), each with turret + hood |
| 16 | 7759 Autonomous Prime (WA) | 113 | 4.8 | turret + hood, shoots while driving |

Full top-193 list: `Game/Research/ftcscout-top-opr-2026-11-20.tsv`. Per-team code details: [[Top Teams Code Deep Dive]].

## What they all do
- **Two flywheel motors**, not one. More inertia means a smaller speed drop per ball and faster recovery.
- **A custom velocity loop with battery-voltage compensation**: power × (12–13.2 V ÷ battery voltage). This is the same idea as our `VOLTAGE_COMPENSATE_F`. Every top team has it on.
- **A "ready" check before feeding**, the same as our at-speed gate:
  - 7759: ±50 RPM
  - WXYZ: ±35 RPM
  - 16095: within 6% for 50 ms
- **Distance → speed table** (and usually hood angle), live-editable from the dashboard.
- Stack: Pedro Pathing, goBILDA Pinpoint odometry, Limelight 3A, Panels/FTC Dashboard.

## Their speeds vs. ours (good sanity check)
Our MID preset is 1450 ticks/s (~3,100 rpm). That sits right in their range:

| Team | Units | Close | Far |
| --- | --- | --- | --- |
| 23849 (POLLEN launcher) | ticks/s | 1304 @ 20 in | 1780 @ 100 in |
| 16095 Fat Dragon | ticks/s | 1560 @ 24 in (61 cm) | 1840 @ 64 in (163 cm) |
| 7759 | RPM | 2950 @ 24 in | 3550 @ 80 in |
| WXYZ | RPM | ~3750 flat, 36–55 in from the HIVE | |

So close, mid and far presets roughly in the **1300–1800 ticks/s** range are what other robots use. Our 80% cap (1960 at maxV 2450) leaves room.

## Their gains (for when we write our own loop, see [[Post-Competition Redesigns]])
Not hub units. These are custom loops where power = kV·target + kP·error.

| Team | kV / kF | kP | Notes |
| --- | --- | --- | --- |
| 23849 | 0.0003 per tick/s | 0.0035 | kS 0.05, 13.2 V nominal, voltage averaged every 250 ms |
| 16095 | 0.0004 per tick/s | 0.004 | |
| 7759 | 0.00019 per RPM | 0.001 | 12 V nominal |
| WXYZ | 0.00017 per RPM | 0.000446 | 12.9 V nominal |

## Lessons for us
1. **A #6-in-the-world robot has no turret.** WXYZ turns the drivetrain to aim and shoots from a fixed zone at one speed. Our presets + tape marks plan is the right one for now.
2. **Two shooting spots, one per CELL** (why: [[BIOBUZZ Rules and Scoring]]).
3. **The easiest AUTO is worth ~28.** The 4 preloads TIP the HIVE (the first TIP needs only 3), then LEAVE and PARK. Top teams get a 2nd TIP by collecting from the bottom of a FLOWER or from the GARDEN.
4. **Biggest hardware gap: one flywheel motor vs. their two.** It is on [[Post-Competition Redesigns]].
5. **Shooting while moving** (7759, Technototes, 23849) needs a turret and odometry. That's a season-long project, not a next-week one.

## Public code worth reading
- 7759: `Stealth-Robotics/AutonomousPrime2026` (`util/ShotCalculator.java`, `systems/Shooter.java`)
- 23849: `FTC-23849/FTC-23849-BioBuzzV2` (`opmode/subsystems/Shooter.java`)
- WXYZ: `WXY7050/UpsideDown-FTC-2027` (`UpsideDownAutoAimModelV21.java`)
- 16095: `rdfz-robotics/FTC-16095-FatDragon-2027-Public` (vision corrected for HIVE tilt)
- Mona Shores: `Mona-Shores-FTC-Robotics/biobuzz` (HIVE simulator)
- 36596 Horizon (#3): `Horizon-36596/zenith` (auto planner)
