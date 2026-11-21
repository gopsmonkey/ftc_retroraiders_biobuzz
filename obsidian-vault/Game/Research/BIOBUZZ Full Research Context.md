---
tags: [ftc, research, game, scouting]
created: 2026-11-20
---
# FTC 2026-27 BIOBUZZ: Game + Top-Team Code Context

Back to [[FTC Home]] · Short versions: [[BIOBUZZ Rules and Scoring]], [[Top Teams Shooter Reference]] · Sources and data: [[Research Sources]]

> [!note] File paths
> Paths like `repos/<owner>__<repo>/...` mean `github.com/<owner>/<repo>`. Paths like `raw/...` are local research files; the data files are in this folder.

Collected 2026-11-20. Sources: Competition Manual TU10 (`raw/manual.txt`), FTCScout API (season 2026),
23 cloned GitHub repos (`repos/`). Team: Retro Raiders, likely **26603** (McKinney TX, registered for
USTXNONUPMM1, no matches played yet). Our repo: `repos/gopsmonkey__ftc_retroraiders_biobuzz`.

---

## 1. Game summary (what matters for code)

- **Match**: 30 s AUTO, 8 s transition (no powered motion, G403), 2:00 TELEOP. FLOWER/NECTAR unlock at 1:00 left.
- **Elements**: 40 POLLEN (yellow, ~2.8 in, 0.055 lb), 8 red + 8 blue NECTAR (~3.6 in, 0.091 lb, about 1.65 POLLEN-weights).
- **Staging**: 4 POLLEN preloaded per robot, 4 in each FLOWER (16), 4 in each GARDEN. 3 NECTAR already sit in each upward CELL, and 5 NECTAR wait in each alliance area (human enters 1 per TIP, all remaining at 1:00 left, only via the LOADING ZONE).
- **HIVE**: center frame, one red and one blue HIVE. Each HIVE is a bistable rocker with 2 CELLS about 18.8 in apart. Pivot is 43.95 in up, CELL opening is 20 W × 14 H × 12 D in. Top teams aim at **z ≈ 60 in**.
- **TIP threshold** (Field Setup Guide §12.3 ballast calibration): a CELL tips on **3 NECTAR + 3 POLLEN** and holds on 3+2. It tips on **8 POLLEN** and holds on 7.
  - **First TIP takes only 3 POLLEN** (the CELL starts with 3 NECTAR).
  - **Every later TIP takes about 8 POLLEN-weights**, because the newly raised CELL is empty after spilling. Real fields vary from about 7.0 to 8.0.
  - NECTAR counts as about 1.65 POLLEN, so 5 NECTAR is roughly one TIP.
  - Once a TIP happens, the **other CELL becomes the target**. Code must switch targets (7759 picks the CELL by robot y > 72).
- **No launch zone.** You can LAUNCH from anywhere. LAUNCH is defined as shot, propelled, or thrown forcefully.
- **AprilTags** (36h11, 3.25 in): a 4-tag cluster sits on the **underside of each CELL**. Red far 30–33, red audience 34–37, blue audience 38–41, blue far 42–45.
  - The tags **tilt with the HIVE**, so a raw Limelight pose is biased by HIVE tilt. 16095 solves for this in `subsystems/vision/VisionMath.java`.

### Points (Table 10-2)

| Action | AUTO | TELEOP |
|---|---|---|
| LEAVE | 3 | – |
| PARK (LOADING ZONE) | 5 | 5 |
| **HIVE TIP** | **20** | **20** |
| Piece left in upward CELL (end) | – | 2 |
| Bottom NECTAR bonus (per FLOWER) | – | 5 |
| Piece in owned FLOWER | – | 2 |
| Piece in own GARDEN | – | 1 |

RPs: WIN 3, TIE 1. SWARM RP = LEAVE + PARK ≥ 16. POLLINATOR 1 = ≥ 4 TIPs, POLLINATOR 2 = ≥ 7 TIPs (regionals use the same thresholds; worlds is TBA).
Fouls: MINOR 5, MAJOR 20.

### Rules that hit code and design

- **G407: max 4 pieces controlled.** Intake must be hard-limited to 4, so use beam break or color-sensor counting.
- **G408**: don't control opponent NECTAR. A sorter that spits it back still draws repeated warnings.
- **G409**: no catching pieces spilling from a TIPPED HIVE. Don't park under it.
- **G410**: NECTAR into FLOWER only in the last 60 s (MAJOR per NECTAR).
- **G417**: only launching into the upward CELL may move the HIVE. Don't launch at a tipping HIVE.
- **G418**: FLOWER: put pieces in only through the top, and remove only POLLEN, only from the bottom (3.55 in retrieval opening).
- **G402**: AUTO is split by side (columns A–C red, D–F blue), so stay on your own side.
- **Size**: 18 in cube to start. Max extension is 18 × 24 × 29 in tall, enforced mechanically (R105).
- **Limits**: 8 motors, 8 servos.

---

## 2. Where the points actually come from (FTCScout, through 2026-11-20)

6,595 active teams, 6,610 matches played. Top OPR (non-penalty):

| # | Team | OPR | Auto | TIPs/OPR | Public code? |
|---|---|---|---|---|---|
| 1 | 19666 Forever Knight (CN) | 162.8 | 28.5 | 7.21 | no |
| 2 | 16010 Astra Machina (NE) | 157.3 | 45.4 | 7.20 | no |
| 3 | 36596 Horizon (OR) | 155.0 | 37.6 | 6.99 | tools only (zenith, simloop) |
| 4 | 21229 Quality Control (WA) | 151.1 | 41.8 | 6.57 | no |
| 5 | 19564 ITKAN Solar (TX) | 136.0 | 40.2 | 6.27 | sibling ITKAN repos |
| 6 | 19705 WXYZ (CN) | 135.0 | 32.8 | 5.84 | **yes**, `WXY7050__UpsideDown-FTC-2027` |
| 10 | 16750 Technototes (WA) | 122.6 | 25.0 | 5.56 | **yes**, `technototes__BioBuzz2026` |
| 13 | 23849 Droid Force (WA) | 120.2 | 23.0 | 5.33 | **yes**, `FTC-23849__FTC-23849-BioBuzzV2` |
| 16 | 7759 Autonomous Prime (WA) | 112.8 | 23.7 | 4.79 | **yes**, `Stealth-Robotics__AutonomousPrime2026` (+ Smelt2026) |
| 31 | 14906 Leviathan (OK) | 101.9 | 29.4 | 4.34 | partial |
| 36 | 21485 ScrapHeads (MO) | 99.5 | 27.8 | 4.31 | **yes**, `ScrapHeads__2026-2027-BIOBUZZ` |
| 60 | 16095 Fat Dragon (CN) | – | – | – | **yes**, code + knowledge base |

Full list (193 teams): `raw/topteams.tsv`. Per-team breakdown: `raw/top.json`.

**Takeaways**
- **HIVE TIPs are the game right now.** FLOWER OPR is about 0 for every top team, and GARDEN is negligible. 7 TIPs ≈ 140 pts.
- A strong auto is 2 TIPs (≈ 40 + LEAVE). 16010 and 21229 average 42–45 auto points.
- FLOWER ownership (endgame NECTAR) is untapped. It could be the tiebreaker once TIP rates saturate.

---

## 3. Consensus software stack (top public repos)

| Piece | Used by |
|---|---|
| **Pedro Pathing** (v2.x / v3) | essentially all |
| **goBILDA Pinpoint** odometry | all |
| **Limelight 3A** (MegaTag on CELL tags) | all |
| **Panels** (bylazar) / FTC Dashboard for live tuning | most |
| FTCLib / **SolversLib** command-based | 7759, 16095, 21485, 16750 (TechnoLib) |
| Zenith (JSON auto planner, from 36596 Horizon) | emerging |

---

## 4. Launcher patterns, best first

### A. 7759 Autonomous Prime: full shoot-on-the-move (best reference)
`Stealth-Robotics__AutonomousPrime2026/TeamCode/.../util/ShotCalculator.java`, `systems/Shooter.java`, `systems/Turret.java`

- Two flywheel motors at 28 ticks/rev (bare goBILDA 6000), driven by **custom PIDF in RPM**: P 0.001, D 1e-5, F 0.00019.
- **Voltage compensation**: `power *= 12.0 / voltage`. Ready when within ±50 RPM. Gate servo feeds the shot.
- **Hood servo**: angle 3.8°–15.8° mapped linearly to servo positions 0.55–0.76.
- **InterpLUT by distance (in)**:
  - RPM: 24→2950, 30→3000, 40→3050, 47→3100, 50→3200, 60→3300, 70→3450, 80→3550
  - Hood: 24→6, 40→10, 60→14, 80→15
  - Flight time: 0.45–0.7 s
- **SOTM** (shoot on the move): turret velocity = robot v + ω × turret offset. The aim point is shifted by v × flight time, iterated 5×. The output includes a turret feed-forward velocity.
- **CELL targets** (Pedro field inches, z = 60): red (57.5, 55.5) / (57.5, 86), blue (84, 55.5) / (84, 86). The CELL is chosen by robot y > 72.
- **Turret**: 384.5-tick motor, 100:30 gear ratio, range −90° to 270°. Picks the in-range equivalent angle nearest the current one (wrap handling). Control is PID + kS + kV·targetVel.

### B. 23849 Droid Force: two launchers (NECTAR + POLLEN), each with turret + hood
`FTC-23849__FTC-23849-BioBuzzV2/TeamCode/.../opmode/subsystems/Shooter.java`, `Constants.java`

- Separate flywheels for NECTAR and POLLEN (different ball sizes need different speeds).
- Shot tables are dashboard-editable "dist,vel" strings, fit with a **cubic polynomial** (Apache Commons Math `PolynomialCurveFitter`):
  - POLLEN, ticks/s by distance (in): 20→1304, 40→1420, 60→1600, 80→1690, 100→1780
  - NECTAR: 20→1624 … 100→2380
- **Hood from physics**: launch angle = atan(2h/d − tan(entry)), with entry −30°, h = 60 − 14.5 in. Blends to a fixed 70° close shot under 25 in.
- Flywheel: kP 0.0035, kS 0.05–0.06, kV 0.0003. Voltage EMA every 250 ms against 13.2 V nominal. Integral clamp 0.2.
- SOTM constants: latency 0.1 s, time of flight 0.5 s.

### C. 19705 WXYZ (#6 world): simple, no turret
`WXY7050__UpsideDown-FTC-2027/.../opmodes/teleop/UpsideDownAutoAimModelV21.java`

- Two shooter motors. The drivetrain heading P loop aims (AIM_KP 0.018).
- Fixed shooting band **36–55 in** from the HIVE at about **3750 RPM**, nudged ±75 RPM by angular sector, with a heading fudge on the left side.
- Flywheel: kF 0.00017, kP 0.000446, kD 1e-6, 12.9 V nominal comp, ±35 RPM ready band.
- Lesson: a #6-world robot does **not** need a turret. A consistent spot plus solid RPM control gets there.

### D. 16095 Fat Dragon: vision-heavy
`rdfz-robotics__FTC-16095-FatDragon-2027-Public/.../subsystems/`

- Two motors, ticks/s. Distance measured to the 4-tag centroid (cm):
  - 60.6 cm → 1560 ticks/s, pitch 0.54
  - 75 cm → 1580, 0.46
  - 99 cm → 1660, 0.41
  - 129 cm → 1680, 0.34
  - 163.5 cm → 1840, 0.26
- kP 0.004, kF 0.0004. Ready = within 6% for 50 ms. PID resets when the target jumps by more than 30.
- `VisionMath.solveHiveFlatPose`: corrects the Limelight MT1 pose for HIVE tilt. **Needed if you localize off CELL tags.**
- `ShootAssistCommand`: holds position while shooting, auto-aims, and handles jam/reverse. Also `PollenChaseController` (drive to POLLEN by vision).
- The `ftc-16095-knowledge` repo (Chinese) has a full test and procedure plan aimed at a 2-TIP auto.

### E. 16750 Technototes: turret + Limelight, quadratic regressions
`technototes__BioBuzz2026/Sixteen750/.../subsystems/TurretSubsystem.java`, `helpers/SotmCompensator.java`

- Launcher velocity = A·d² + B·d + C, with A −0.066, B 9.58, C 864. Hood also uses a quadratic. Hood also corrects for velocity error (`hoodCompScalar`).
- `SotmCompensator`: splits velocity into radial and tangential parts relative to the Limelight tx.
  - **Bug: it uses `vel.omega` as linear speed.**
  - **Bug: a missing `return` means the low-speed guard never takes effect.** Don't copy it as is.
- Auto `Synced3TipPark`: preload → fire → pull POLLEN from a FLOWER bottom → fire → sweep → fire → park.

### F. 21485 ScrapHeads
- Ported WPILib geometry and `InterpolatingTreeMap` into FTC.
- Has a `CollectionRoutePlanner` / `RouteScorer` that picks which pieces to collect, a FLOWER ramp, and a lift. The flywheel table still looks like placeholders.

### Other useful
- `Mona-Shores-FTC-Robotics__biobuzz`: field sim with a HIVE model calibrated to §12.3 (`src/test/.../HiveCalibration.java`). Also a `HiveTracker` that keeps track of which CELL is up.
- `Horizon-36596__zenith` / `__simloop` / `__Horizon-Library-Examples`, from the #3 world team:
  - Zenith: JSON auto planner with a time estimator.
  - simloop: runs FTC code on a laptop.
  - Their example auto fires the 4 preloads to TIP from the south, then 4 from the GARDEN from the north, in 8.1 s.
- `Quantum-Igniters__FTC-BioBuzz-Velocity-Calculator`: projectile calculator.

---

## 5. Our code vs. the field

`ShooterVelocityTuner.java`, `ShooterPIDFTuner.java`:
- One `shooterMotor` on the SDK's built-in `RUN_USING_ENCODER` velocity PIDF. Target about 1400–1450 ticks/s. Intake at 0.8.
- There is already a shot-dip/recovery detector, which is good for tuning.

Gaps compared to the top teams:
1. **No voltage compensation.** Every top team scales by nominal/battery voltage (12–13.2 V). The SDK PIDF partly hides this, but shots drift as the battery sags.
2. **Single flywheel motor.** Every top launcher uses 2, for faster recovery between 4-ball bursts.
3. **No distance → velocity (and hood) map yet.** All top teams have one: LUT or poly fit, editable from the dashboard. Our 1450 ticks/s sits in the same range as 16095 (1560 @ 60 cm) and 23849 POLLEN (1304–1420 @ 20–40 in).
4. **No aiming or localization yet.** Minimum viable is the WXYZ approach: Pinpoint + heading-aim to the CELL plus a fixed shooting band. Next step up is a 7759-style turret + SOTM.
5. **TIP-aware logic.** Track which CELL is up (switch after each TIP) and how many pieces are in it (3 to the first TIP, then about 8). Don't fire into a tipping HIVE.
6. **4-piece limit** sensing on the intake (G407).

Discord fix log: not accessible (Discord). It is not in the GitHub repo, which has only 1 commit with the two tuners.
