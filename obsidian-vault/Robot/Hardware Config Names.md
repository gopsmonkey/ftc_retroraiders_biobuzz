---
tags: [ftc, robot, hardware]
updated: 2026-11-20
---
# Hardware config names

Back to [[FTC Home]]

The Robot Configuration on the Driver Station must use exactly these names (from `ShooterConfig.java`).

| Name | Device | Direction | Mode | Notes |
| --- | --- | --- | --- | --- |
| `shooterMotor` | Flywheel, likely 6000 rpm Yellow Jacket (28 CPR) | FORWARD | RUN_USING_ENCODER, FLOAT | Encoder required |
| `intakeMotor` | Intake + transfer (no separate feeder) | FORWARD | RUN_WITHOUT_ENCODER (or USING if encoder plugged in) | BRAKE |
| `lfMotor` | Drive, left front | REVERSE | RUN_WITHOUT_ENCODER | BRAKE |
| `lbMotor` | Drive, left back | REVERSE | RUN_WITHOUT_ENCODER | BRAKE |
| `rfMotor` | Drive, right front | FORWARD | RUN_WITHOUT_ENCODER | BRAKE |
| `rbMotor` | Drive, right back | FORWARD | RUN_WITHOUT_ENCODER | BRAKE |

- Open question: is the intake motor's encoder cable plugged in? *Shooter MaxV Test* (hold B) answers it.
- Open question: is there a feeder motor or servo gate outside the repo? If so, the gate belongs on it.
