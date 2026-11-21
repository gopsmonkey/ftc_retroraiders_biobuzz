---
tags: [ftc, software, pedro-pathing, autonomous]
created: 2026-11-20
---
# Pedro Pathing plan (for our robot)

Back to [[FTC Home]] · Robot: [[Robot Overview]] · Odometry details: [[Sathwik Handoff]] · Rules: [[BIOBUZZ Rules and Scoring]]

**Blocker first: Pedro Pathing needs Android Studio.** Its docs say "you **must** use **Android Studio**. Pedro Pathing does **not** work with OnBot Java or Blocks" ([prerequisites](https://pedropathing.com/docs/pathing/prerequisites)). We use OnBot Java only, so using Pedro means moving the team to Android Studio first.

Claude has a **`pedro-pathing` subagent** for this. Ask Claude Code, e.g. "use the pedro-pathing agent to set up the two-wheel localizer". It reads this note and [[Robot Overview]] first, checks the Pedro docs live, and only writes the AUTO code we ask for.

## Decision to make
| Option | What it takes | Good | Bad |
| --- | --- | --- | --- |
| **A. Android Studio + Pedro Quickstart** | Install Android Studio. Clone [Pedro Quickstart](https://github.com/Pedro-Pathing/Quickstart.git), copy all our `.java` files into `TeamCode`, deploy over USB or Wi-Fi. | What top teams use; AutoTune; smooth curved paths | New toolchain for everyone; not for the night before an event |
| **B. Stay on OnBot Java** with Sathwik's `MecanumOdometryRobot` | Push the library to the repo, re-measure pod offsets, spin test | Already works with our pods; no new tools | Less tooling, we maintain it ourselves |

Our shooter code is plain FTC SDK Java, so it works the same under either option.

## Our Pedro setup (if option A)
| Setting | Our value | Status |
| --- | --- | --- |
| Pedro version | 3.x (v3.0.2, Oct 2026). The API changed in 3.0, so old 2.x tutorials are outdated. | Check the Quickstart's Gradle files |
| Gradle | repo `https://repo.dairy.foundation/releases/`; `com.pedropathing:revhub:<version>`, `com.pedropathing:tuning:1.0.0` ([install](https://pedropathing.com/docs/pathing/installation)) | — |
| Localizer | **Two-Wheel** (2 dead wheels + IMU) ([docs](https://pedropathing.com/docs/pathing/tuning/localization/two-wheel)) | — |
| Forward pod | `y_pod`, offset **−3.0 in** (3 in right of center) | **Re-measure on new drivetrain** |
| Strafe pod | `x_pod`, offset **−2.75 in** (2.75 in behind center); direction probably REVERSE (`INVERT_STRAFE_ODOMETRY` was flipped) | **Re-measure** |
| Ticks → inches | 2000 counts/rev, 32 mm wheel → 0.0019789 in/tick (both pods) | Fine-tune with the localization test |
| IMU | Control Hub built-in, name `imu` | **Orientation unknown**: note logo and USB directions |
| Drive motors | `lfMotor` `lbMotor` REVERSE, `rfMotor` `rbMotor` FORWARD, all on the Expansion Hub | Fix the blinking-blue hub first |
| Tuning | Drivetrain → Localization → Foresight, all via AutoTune at `http://192.168.43.1:10158` ([tuning](https://pedropathing.com/docs/pathing/tuning)) | — |

## Field coordinates
- Pedro: origin bottom-left, 144 × 144 in, 0 rad faces +x, CCW positive ([coordinates](https://pedropathing.com/docs/pathing/reference/coordinates)).
- From FTC coordinates: x = FTC y + 72, y = 72 − FTC x, heading = FTC heading − π/2.
- CELL aim points (from 7759's code, verify on a real field): red (57.5, 55.5) / (57.5, 86), blue (84, 55.5) / (84, 86). See [[Top Teams Code Deep Dive]].
- No camera, so no AprilTag correction. Start every AUTO from a measured start pose and keep paths short.

## AUTO plan ideas (the team decides and writes these)
1. Preloads → TIP → LEAVE → PARK (~28 pts). Shresht's `PreloadShootLeaveAuto` already does this without odometry.
2. With Pedro: drive to a FLOWER bottom or the GARDEN for 4 more POLLEN → 2nd TIP. Stay on our half (G402).

## Log
- 2026-11-20: Researched Pedro 3.x; confirmed it does not support OnBot Java. Created the `pedro-pathing` agent and this note. No Pedro code written.
