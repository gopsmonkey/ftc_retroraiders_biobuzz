---
tags: [ftc, research, scouting, shooter, code]
created: 2026-11-20
---
# Top teams: code deep dive

Back to [[FTC Home]] · Summary and gains table: [[Top Teams Shooter Reference]] · Game rules: [[BIOBUZZ Rules and Scoring]] · Repo links and data: [[Research Sources]]

This note covers only per-team code details: file paths, math and constants worth copying later. Rankings, speeds and lessons are in [[Top Teams Shooter Reference]].
Paths are inside each team's repo, listed in [[Research Sources]]. `TeamCode/...` = `TeamCode/src/main/java/org/firstinspires/ftc/teamcode`.

## Launcher code, best first

### A. 7759 Autonomous Prime: full shoot-on-the-move (best reference)
`TeamCode/.../util/ShotCalculator.java`, `systems/Shooter.java`, `systems/Turret.java`

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
`TeamCode/.../opmode/subsystems/Shooter.java`, `Constants.java`

- Separate flywheels for NECTAR and POLLEN (different ball sizes need different speeds).
- Shot tables are dashboard-editable "dist,vel" strings, fit with a **cubic polynomial** (Apache Commons Math `PolynomialCurveFitter`):
  - POLLEN, ticks/s by distance (in): 20→1304, 40→1420, 60→1600, 80→1690, 100→1780
  - NECTAR: 20→1624 … 100→2380
- **Hood from physics**: launch angle = atan(2h/d − tan(entry)), with entry −30°, h = 60 − 14.5 in. Blends to a fixed 70° close shot under 25 in.
- Flywheel: kP 0.0035, kS 0.05–0.06, kV 0.0003. Voltage EMA every 250 ms against 13.2 V nominal. Integral clamp 0.2.
- SOTM constants: latency 0.1 s, time of flight 0.5 s.

### C. 19705 WXYZ (#6 world): simple, no turret
`.../opmodes/teleop/UpsideDownAutoAimModelV21.java`

- Two shooter motors. The drivetrain heading P loop aims (AIM_KP 0.018).
- Fixed shooting band **36–55 in** from the HIVE at about **3750 RPM**, nudged ±75 RPM by angular sector, with a heading fudge on the left side.
- Flywheel: kF 0.00017, kP 0.000446, kD 1e-6, 12.9 V nominal comp, ±35 RPM ready band.
- Lesson: a #6-world robot does **not** need a turret. A consistent spot plus solid RPM control gets there.

### D. 16095 Fat Dragon: vision-heavy
`.../subsystems/`

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
`Sixteen750/.../subsystems/TurretSubsystem.java`, `helpers/SotmCompensator.java`

- Launcher velocity = A·d² + B·d + C, with A −0.066, B 9.58, C 864. Hood also uses a quadratic. Hood also corrects for velocity error (`hoodCompScalar`).
- `SotmCompensator`: splits velocity into radial and tangential parts relative to the Limelight tx.
  - **Bug: it uses `vel.omega` as linear speed.**
  - **Bug: a missing `return` means the low-speed guard never takes effect.** Don't copy it as is.
- Auto `Synced3TipPark`: preload → fire → pull POLLEN from a FLOWER bottom → fire → sweep → fire → park.

### F. 21485 ScrapHeads
- Ported WPILib geometry and `InterpolatingTreeMap` into FTC.
- Has a `CollectionRoutePlanner` / `RouteScorer` that picks which pieces to collect, a FLOWER ramp, and a lift. The flywheel table still looks like placeholders.

### Other useful
- Mona Shores: field sim with a HIVE model calibrated to §12.3 (`src/test/.../HiveCalibration.java`). Also a `HiveTracker` that keeps track of which CELL is up.
- Horizon zenith / simloop / Library-Examples, from the #3 world team:
  - Zenith: JSON auto planner with a time estimator.
  - simloop: runs FTC code on a laptop.
  - Their example auto fires the 4 preloads to TIP from the south, then 4 from the GARDEN from the north, in 8.1 s.
- Quantum Igniters calculator: projectile calculator.

---
