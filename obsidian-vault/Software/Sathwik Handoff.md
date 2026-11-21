---
tags: [ftc, software, handoff, sathwik]
source: "[[Retro Raiders BIOBUZZ Claude Code handoff.pdf]]"
created: 2026-11-20
---
# Sathwik's handoff (old chat → Claude Code)

Back to [[FTC Home]] · Tasks from it: [[To-Do]] · Original PDF: ![[Retro Raiders BIOBUZZ Claude Code handoff.pdf]]

Sathwik (gopsmonkey) wrote this on 2026-11-20 to carry his robot-software chat into Claude Code. Only facts new to the vault are here.
- Hardware names, servos, viper and odometry pods: [[Hardware Config Names]].
- The "what you see → what to change" tuning table: [[Shooter Tuning Guide]].

> [!warning] Things in this handoff that change tonight's plan
> - **OnBot Java only. No Android Studio, no Gradle.** Every file must be one plain class in `org.firstinspires.ftc.teamcode` that pastes into OnBot Java and compiles. This answers [[Open Questions]] #2. The laptop in the pack list only needs a browser.
> - **The real match TeleOp is `MainTeleOp.java`. It is only on Sathwik's laptop, not in the repo, and has never been tested.** It has flower servos and a shooter at a placeholder 2000 ticks/s. `BioBuzz TeleOp` has the fixes but no flower servos. This answers [[Open Questions]] #1.
> - **Expansion Hub blinks blue once code runs**, so the 4 drive motors aren't detected (reported 2026-11-19, unresolved). See below.
> - **Intake has no encoder.** This answers [[Open Questions]] #7: leave `INTAKE_HAS_ENCODER = false`.

## Working with Sathwik
- He uses casual shorthand and typos. Split answers into categories, not long paragraphs.
- Assume no background. Give a worked example first, then steps.
- **No comments in code he gets back.** He asked for comments to be removed.
- "Don't code yet" means no code. When he asks for code, send the finished file, not a description.
- Use tools (browser, shell, connectors) before asking him to do something. Ask him only for steps that are his alone, such as passwords and approvals.
- **Never type his passwords.** GitHub asks for his password to change repo visibility.
- He sometimes swears. Don't mirror it.

## Robot (per handoff)
- Mecanum drivetrain, all 4 drive motors on the **Expansion Hub**. A new drivetrain is being built (only the right module is finished); the chaining is the same.
- goBILDA 4-bar odometry pods (forward `y_pod`, strafe `x_pod`).
- **Static launcher: one gecko-wheel flywheel, no turret.** Partly answers [[Open Questions]] #6.
- Flower intake dropped and lifted by two servos. An older goBILDA 4-stage viper slide with presets still to be measured.

## Sathwik's local-only files (not in the repo)
Ask him to attach or push these. Only `ShooterVelocityTuner` and `ShooterPIDFTuner` are in the repo.

| File | What it does | State |
| --- | --- | --- |
| `MainTeleOp.java` | Drive + intake (dpad up/down/left), shooter (RB on, LB off, 2000 placeholder), flower servos (A drops, X/Y lift) | **Match TeleOp**, untested |
| `MecanumOdometryRobot.java` | "Retro Reckoning" odometry library: `drive`, `strafe`, `turnTo`, `driveBezier`, pod-offset correction, `resetFieldPose` | Working |
| `ExampleAuto.java` | Example auto on the library | — |
| `PurePursuitAuto.java` | Earlier pure-pursuit example | — |
| `PollenAutoNav.java` | Color-blob vision drive toward POLLEN | Unfinished (HSV placeholders) |
| `ColorSamplerOpMode.java` | Measures POLLEN HSV | Working |
| `DriverTeleOp.java` | Older TeleOp with the viper slide | Old |
| `DrivetrainModuleTest.java`, `RightModuleTest.java` | Wheel-chain tests | Test |
| `retro-reckoning-visualizer.html` | Field path visualizer | Tool |
| `sort-and-launch-concept.html` | 3D concept: color sorter feeding two launchers | Concept |

Superseded, ignore: `DriveAndShooterTeleOp.java`, `ShooterVelocityTest.java`, early `ShooterVelocityTuner` versions.

## Drive math (used in every file)
- Positive strafe = left. Positive turn = left (CCW).
- TeleOp inputs: drive = −left_stick_y, strafe = −left_stick_x, turn = −right_stick_x.
- Mixing: lf = d − s − t, rf = d + s + t, lb = d + s − t, rb = d − s + t. Divide all four by the largest magnitude if it is over 1.
- `STRAFE_SIGN`, `TURN_SIGN` and `SPEED_SCALE` constants flip or slow the drive.

## Odometry constants (`MecanumOdometryRobot`)
- 2000 counts/rev, 32 mm wheel → 0.0019789 in/count.
- `Y_POD_LEFT_OFFSET_IN = -3.0` (y pod 3 in right of center). `X_POD_FORWARD_OFFSET_IN = -2.75` (x pod 2.75 in behind center).
- Left and forward are positive. **Re-measure both for the new drivetrain**, then do a spin test: spin in place and adjust until fieldX/fieldY stop drifting.
- `INVERT_STRAFE_ODOMETRY` had to be flipped on the real robot. `INVERT_DRIVE_ODOMETRY` also exists.

## Problems already solved (don't re-debug)
| Symptom | Real cause → fix |
| --- | --- |
| Strafe never stopped or stalled short | Strafe odometry sign inverted (distance went negative). Flip `INVERT_STRAFE_ODOMETRY`. Gain, deadband and minPower changes were red herrings, so **check signs before tuning gains**. |
| Yaw wiggles during moves | A global `minPower` leaked into the turn controller. Use per-controller values: drive 0.0, strafe ~0.1–0.2, yaw 0.0. |
| Bezier following turned erratically | Forward-only lookahead index, plus damping on heading error and path curvature. |
| Robot drifted left driving forward | Added pod-offset correction. The spin test is still to do. |
| TeleOp strafe and turn reversed | Negative signs, now `STRAFE_SIGN` / `TURN_SIGN`. |
| `ColorSamplerOpMode` compile errors | Use `android.graphics.Color.red/green/blue` on the packed RGB and `Color.RGBToHSV`. |
| Vision picked up brown objects | Color range too wide. Blue box = contour, orange = box-fit blob (both are POLLEN candidates). |

## Expansion Hub blinking blue (open, reported 2026-11-19)
- **Symptom:** the hub is green after making a new config but turns blinking blue once code starts. "Address Change" turns it green again, but it still blinks blue on run.
- **Meaning** (REV LED guide): blinking blue is a keep-alive timeout, so the hub lost contact with the Control Hub. Normal is solid green with a blue blink every 5 s; the blink count is the address (default 2).
- **Causes to check, in order:**
    1. Loose or weak XT30 power plug, or a low battery resetting the hub when the motors start.
    2. A loose or bad RS485 cable between the hubs.
    3. A shorted or damaged motor or encoder cable on the new drivetrain.
    4. A saved config that still has the old hub address.
    5. Mismatched firmware.
- **Test:** unplug all 4 drive motors and encoders, run any OpMode, then plug them back one at a time. Delete the old config, scan a new one, name every device to match the code, activate it, and rerun.
- **Why a new config works and an old one doesn't:** a scan reads the hub's current address, but a saved config points at the old one.
- **Still unknown:** does the LED stay green after pressing play?

## Shooter (Sathwik's view)
- **Symptom:** the first shot is good and the next three fall short, because the wheel hasn't recovered. There's no steel flywheel.
- **Fix order he was given:**
    1. Feed only when back near target (now the at-speed gate).
    2. Tune F, then P.
    3. Raise the target slightly.
    4. Add inertia: stacked aluminum hubs, evenly spaced rim weights, a bigger wheel (inertia ∝ r²) or a second wheel.
    5. Less squeeze, or a stronger motor.
- Ramping speed during a burst helps only a little, because it doesn't store more energy.

## Design ideas (V1 is POLLEN only)
- **Aim without a camera:** turn the robot with odometry. Target heading = atan2(hiveY − y, hiveX − x), plus an offset for whichever side the launcher faces.
    - A hold-to-aim TeleOp button was offered but not built.
    - CELL coordinates exist in [[Top Teams Code Deep Dive]] (7759's Pedro coords).
- Later: a distance → speed table using the same odometry.
- Other ideas discussed:
    - catapult with a fabric sling
    - counter-roller to cut backspin
    - pivoting rubber-band intake
    - color sorter feeding two launchers
    - lazy-susan turret (≥ 3 in inner diameter) with a REV Through Bore Encoder for angle
- **Chassis CAD** is in Fusion 360, project "Admin Project": 14-hole U-channel sides, 10-hole back, mecanum aligned to the 3rd/last hole, spacers, low guard channel.
    - The Fusion connector kept timing out. Retry only when Sathwik confirms Fusion and the desktop app are open.
    - They looked into CNC options in the Frisco area.

## Belts instead of chains (CAD rules)
- Ratio = output teeth ÷ input teeth. Pitch diameter = teeth × pitch ÷ π.
- Belt length ≈ 2C + π(D + d)/2 + (D − d)² / (4C). Snap to a belt that exists, then solve back for the center distance C.
- Example: 3 mm pitch, 20T/40T, target 100 mm → pitch diameters 19.10/38.20 mm → ~290.9 mm ≈ 97T → use 96T, which gives C = 98.5 mm. Wrap is ~169°, with ~9 teeth engaged.
- Need ≥ 120° wrap and ≥ 6 teeth engaged. Slot motor holes ~2 mm each way, or add a tensioner.
- Meshed gears need no belt: C = module × (N1 + N2) ÷ 2, and they reverse direction.

## GitHub history
- The repo is public; Sathwik made it public himself.
- **The web editor adds indentation to typed code.** Set the editor text with `document.querySelector('.cm-content').cmTile.view.dispatch(...)`, then compare character counts.
- The messy commit messages on `main` are GitHub auto-text. Harmless.
- To give write access, add collaborators by username. A public repo has no invite link.
- For Sathwik's own Claude to push: GitHub → Settings → Applications → Installed GitHub Apps → Claude → add `ftc_retroraiders_biobuzz`.

## Corrections to the handoff's game facts
The handoff says to recheck its game facts. Checked against the manual ([[BIOBUZZ Rules and Scoring]]):
- **NECTAR is ~3.6 in** (POLLEN ~2.8 in).
- **Crossing into the other half is a MAJOR foul (20) only during AUTO** (G402), not all match.
- The 29 in height limit and the 4-ball start are correct.
- "Tipping takes ~3.5 s" is not in the manual. Time a real TIP.
