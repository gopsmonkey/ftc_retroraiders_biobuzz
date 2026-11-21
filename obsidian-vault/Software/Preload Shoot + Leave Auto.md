---
tags: [ftc, software, auto]
file: PreloadShootLeaveAuto.java
created: 2026-11-20
---
# Preload Shoot + Leave Auto

Back to [[FTC Home]] · Test it: [[Code Check - Night Before]] · Points: [[BIOBUZZ Rules and Scoring]]

`PreloadShootLeaveAuto.java` on `shooter-fixes-night-before`. On the Driver Station it's *Preload Shoot + Leave* (group Match), and it preselects *BioBuzz TeleOp*.

## What it does
1. Spins up to `SHOOT_VELOCITY` and waits until at speed (max 3 s; if it never gets there, it shoots anyway).
2. Feeds for 6 s through the same gate as TeleOp.
3. Stops the shooter, drives straight for 1 s at power 0.4, and stops. All done by about 10 s, so nothing is moving in the transition (G403).

The up CELL starts with 3 NECTAR, so our 4 preloads should TIP it: about 20 + 3 points (+5 if we also PARK).

## Tune before the first match (top of the file)
| Setting | Default | Set it to |
| --- | --- | --- |
| `SHOOT_VELOCITY` | `PRESET_MID` (1450) | The speed that hits the up CELL from our start tile |
| `FEED_TIME_S` | 6.0 | Just long enough for all 4 to leave |
| `LEAVE_POWER` | 0.4 | Sign picks the direction: + forward, − backward |
| `LEAVE_TIME_S` | 1.0 | Just off the wall; stay on our side (G402) |
| `SHOOT_FIRST` | true | **false = LEAVE only** (3 pts, can't go wrong) if shooting isn't tested |

## Limits
- No odometry and no aiming: it shoots from wherever it's placed. Start placement must be the same every match (mark it with tape).
- Drives on time, not distance, so it's affected by battery. Any better AUTO is the team's to write ([[Robot Overview]]).
- Tell the partner which way we LEAVE.
