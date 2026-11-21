---
tags: [ftc, robot, hardware]
updated: 2026-11-20
---
# Hardware config names

Back to [[FTC Home]] · What the robot is built from: [[Robot Overview]]

The Robot Configuration on the Driver Station must use exactly these names (from `ShooterConfig.java`).

| Name | Device | Direction | Mode | Notes |
| --- | --- | --- | --- | --- |
| `shooterMotor` | Flywheel, likely 6000 rpm Yellow Jacket (28 CPR) | FORWARD | RUN_USING_ENCODER, FLOAT | Encoder required. Launch velocity **−1375** (`SHOOTER_SIGN = -1`, PR #3) |
| `intakeMotor` | **One motor, all rollers chained** (big intake + the two mini wheels under the flywheel) | FORWARD | RUN_USING_ENCODER (encoder added 2026-11-20) | BRAKE |
| `lfMotor` | Drive, left front | REVERSE | RUN_WITHOUT_ENCODER | BRAKE |
| `lbMotor` | Drive, left back | REVERSE | RUN_WITHOUT_ENCODER | BRAKE |
| `rfMotor` | Drive, right front | FORWARD | RUN_WITHOUT_ENCODER | BRAKE |
| `rbMotor` | Drive, right back | FORWARD | RUN_WITHOUT_ENCODER | BRAKE |
| `flowerServo1`, `flowerServo2` | Flower intake drop/lift (in `MainTeleOp`, and in `BioBuzz TeleOp` via Y; optional there) | servo 2 REVERSED | Servo | Up 0.0 / down 0.5 are guesses, measure |
| `viperMotor` | goBILDA 4-stage viper slide (older `DriverTeleOp`) | – | DC motor | Presets LOW 0 / MID 900 / HIGH 2100 / max 2200 are placeholders |
| `y_pod`, `x_pod` | goBILDA 4-bar odometry pods (forward / strafe) | see `INVERT_*_ODOMETRY` | encoder | Constants in [[Sathwik Handoff]] |

- All 4 drive motors are on the **Expansion Hub**. If they aren't detected, see the blinking-blue section of [[Sathwik Handoff]].
- Shooter wheel is a single **gecko wheel**, no turret.
- If the Driver Station config literally uses names like "RB motor", change the name constants at the top of each file.

- **Encoder cable added to `intakeMotor` (2026-11-20).** `INTAKE_HAS_ENCODER = true` on branch `intake-gate-at-speed` (PR #3), which keeps `intakeMotor` braked until the flywheel is at launch speed (-1375).
- Open question: is there a feeder motor or servo gate outside the repo? The handoff lists none; the flower servos only drop and lift the intake.
