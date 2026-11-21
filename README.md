# RetroRaiders / BioBuzz FTC code

The team uses **OnBot Java** (no Android Studio). Upload every `.java` file with OnBot Java's **Upload Files** button (don't paste), overwrite old copies with the same name, then **Build Everything**. Details and build risks: `obsidian-vault/Software/OnBot Java Check.md`.

| File | What it is |
| --- | --- |
| `ShooterConfig.java` | **Every tuned number.** PIDF, maxV, presets, gate tolerance, intake feed. Edit this, nothing else. |
| `ShooterController.java` | Shared shooter + intake logic: PIDF in init, 80% cap, at-speed gate, feed control, battery-scaled F. |
| `BioBuzzTeleOp.java` | Match TeleOp ("Match" group): drive, presets, gated feed, flower servos, live trim, rumble. |
| `ShooterMaxVelocityTest.java` | Step 1: measures maxV and intake ticks/s, prints starting PIDF. |
| `ShooterPIDFTuner.java` | Step 2: live PIDF tuning under ball load, gated feed, CSV logging (B). |
| `ShooterVelocityTuner.java` | Drive + shooter velocity + intake power sweep, now gated with precision drive. |
| `PreloadShootLeaveAuto.java` | Simple AUTO: shoot the 4 preloads, then LEAVE. Tune the `TUNE` numbers first. |
| `ShooterLogger.java` | CSV logs to `/sdcard/FIRST/shooter_logs/` (`adb pull /sdcard/FIRST/shooter_logs`). |

## Tuning order (after the mechanical fixes)
1. Fresh battery. Run **Shooter MaxV Test**, hold A. Copy `MAX_VELOCITY`, `SHOOTER_F/P/I` into `ShooterConfig`. Hold B to check the intake encoder.
2. Run **Shooter PIDF Tuner**. Feed balls at match pace (RT). Raise P until the dip recovers fastest without ringing. Copy the final P/I/D/F into `ShooterConfig`.
3. Calibrate `PRESET_CLOSE/MID/FAR` at taped floor spots (all ≤ 80% of maxV).
4. **Power-cycle the hub**, run **BioBuzz TeleOp**, and check init telemetry says `PIDF on hub: OK`.

PIDF set in a tuner is lost on power cycle. Only `ShooterConfig` sticks.

## BioBuzz TeleOp controls
| Input | Action |
| --- | --- |
| gamepad1 left stick / right stick | Drive + strafe / turn |
| hold LT | Precision drive (0.35) |
| A / X | Spin up at preset / stop shooter |
| d-pad left / up / right | CLOSE / MID / FAR preset |
| hold RT | Intake: collects when shooter is off, gated feed when it's on |
| hold RB + RT | Feed even if not at speed (override) |
| hold LB | Reverse intake (unjam) |
| hold B | Reverse shooter (unjam); releasing stops it |
| Y | Flower intake down / up (if `flowerServo1/2` are in the config) |
| gamepad2 d-pad up / down, X | Speed trim ±25 on every preset / reset trim |
| Rumble | 1 blip = shooter at speed; long buzz = intake looks full (needs `INTAKE_FULL_CURRENT_AMPS`) |
