# RetroRaiders / BioBuzz FTC code

Drop these files into `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` of the FTC SDK project.

| File | What it is |
| --- | --- |
| `ShooterConfig.java` | **Every tuned number.** PIDF, maxV, presets, gate tolerance, intake feed. Edit this, nothing else. |
| `ShooterController.java` | Shared shooter + intake logic: PIDF in init, 80% cap, at-speed gate, feed control, battery-scaled F. |
| `BioBuzzTeleOp.java` | Reference match TeleOp ("Match" group) using the controller. |
| `ShooterMaxVelocityTest.java` | Step 1: measures maxV and intake ticks/s, prints starting PIDF. |
| `ShooterPIDFTuner.java` | Step 2: live PIDF tuning under ball load, gated feed, CSV logging (B). |
| `ShooterVelocityTuner.java` | Drive + shooter velocity + intake power sweep, now gated with precision drive. |
| `ShooterLogger.java` | CSV logs to `/sdcard/FIRST/shooter_logs/` (`adb pull /sdcard/FIRST/shooter_logs`). |

## Tuning order (after the mechanical fixes)
1. Fresh battery. Run **Shooter MaxV Test**, hold A. Copy `MAX_VELOCITY`, `SHOOTER_F/P/I` into `ShooterConfig`. Hold B to check the intake encoder.
2. Run **Shooter PIDF Tuner**. Feed balls at match pace (RT). Raise P until the dip recovers fastest without ringing. Copy the final P/I/D/F into `ShooterConfig`.
3. Calibrate `PRESET_CLOSE/MID/FAR` at taped floor spots (all ≤ 80% of maxV).
4. **Power-cycle the hub**, run **BioBuzz TeleOp**, and check init telemetry says `PIDF on hub: OK`.

PIDF set in a tuner is lost on power cycle. Only `ShooterConfig` sticks.
