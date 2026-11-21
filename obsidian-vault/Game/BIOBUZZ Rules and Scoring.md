---
tags: [ftc, game, rules, strategy]
source: Competition Manual TU10 (ftc.game/manual), Event Field Setup Guide V1.0 §12.3
created: 2026-11-20
---
# BIOBUZZ rules and scoring (what matters for us)

Back to [[FTC Home]] · Competitors: [[Top Teams Shooter Reference]]

## Tomorrow's event
- **NTX U-League Meet #1, PM session**, Sat 2026-11-21, UME Preparatory Academy (Dallas). 25 teams. We are 26603.

## Match
- 30 s AUTO → **8 s transition** → 2:00 TELEOP.
  - During the transition **nothing may move under power** (G403). Pressing INIT is fine because `BioBuzz TeleOp` init only writes PIDF.
  - Don't spin the flywheel before TELEOP starts.
- At **1:00 left** (chime): humans may enter all remaining NECTAR, and FLOWER scoring opens.

## Points
| Action | AUTO | TELEOP |
| --- | --- | --- |
| LEAVE (off the wall) | 3 | – |
| PARK (partly in our LOADING ZONE) | 5 | 5 |
| **HIVE TIP** | **20** | **20** |
| Ball left in the up CELL at the end | – | 2 |
| Piece in a FLOWER we own (top-most NECTAR is ours) | – | 2 |
| Bottom NECTAR bonus per FLOWER | – | 5 |
| Piece in our GARDEN | – | 1 |

RPs: WIN 3, TIE 1, plus three bonus RPs:
- **SWARM**: LEAVE + PARK ≥ 16.
- **POLLINATOR 1**: ≥ 4 TIPs.
- **POLLINATOR 2**: ≥ 7 TIPs.

Fouls: MINOR 5, MAJOR 20 (to the other alliance).

> [!tip] SWARM RP is nearly free
> Both robots LEAVE in AUTO (3 + 3) and both PARK at the end (5 + 5) = 16. Agree on this with the partner every match.

## How a TIP works (the important part)
- Each HIVE is a seesaw with 2 CELLS, one up and one down. Launching into the **up** CELL is the only legal way to tip it (G417).
- Field staff ballast every HIVE so a CELL **tips on 3 NECTAR + 3 POLLEN** and **tips on 8 POLLEN** (Field Setup Guide §12.3). Real fields land between about 7 and 8.
- Each up CELL **starts the match holding 3 NECTAR**, so **the first TIP takes only 3 POLLEN**.
  - **Our 4 preloads tip the HIVE in AUTO.** Shoot preloads, LEAVE and PARK: about 20 + 3 + 5 = 28 points.
- After a TIP, the other CELL comes up **empty**, so every later TIP takes about **8 POLLEN** (two full 4-ball loads).
  - A NECTAR weighs about 1.65 POLLEN, so 5 NECTAR is about one TIP.
- **The target moves after every TIP.** The up CELL alternates between the two ends of the HIVE.
  - One taped shooting spot only works for one CELL, so we need a spot (or a preset) for **each** end.
- Don't shoot while the HIVE is moving. A ball hitting a tipping HIVE can stop the TIP (manual §10.5.1). Wait for it to settle.

## Rules that can cost us
| Rule | What it says | What it means for us |
| --- | --- | --- |
| **G407** | Max **4** pieces controlled at once | The video's "5-ball burst" is fine on the practice field but illegal in a match. The robot must never hold 5. |
| G409 | No catching or deflecting balls spilling from a TIPPED HIVE until they hit something else | Don't sit under the HIVE waiting for spills. |
| G408 | Don't control the opponent's NECTAR | If we pick one up, spit it out right away. Repeats get a YELLOW. |
| G410 | NECTAR into a FLOWER only in the last 60 s | MAJOR (20) per NECTAR if early. |
| G418 | FLOWER: put pieces in only through the top; take only POLLEN, only from the bottom opening | The bottom of each FLOWER holds 4 POLLEN at the start, a good pickup source. |
| G402 | In AUTO, stay on our side (columns A–C red, D–F blue) | Our AUTO must not cross the middle. |
| G405 | Don't deliberately shoot balls out of the field | MAJOR per ball. |
| G417 | Don't ram the HIVE or launch at its outside faces | Missed shots that hit it are fine. |

## Where POLLEN is at the start
- 4 in each robot (preload)
- 4 in each of the 4 FLOWERS, taken from the bottom opening
- 4 lined up in each GARDEN, in the corner nearest the alliance area
- 40 total. Balls that leave the field are put back in by field staff.

## Field facts for aiming
- **No launch zone.** We can launch from anywhere on the field.
- HIVE pivot is 43.95 in up. Top teams aim at **about 60 in high**. The CELL opening is about 20 in wide × 14 in tall.
- AprilTags (36h11) sit on the **underside** of each CELL and tilt when the HIVE tips.
  - IDs: red far 30–33, red audience 34–37, blue audience 38–41, blue far 42–45.
- Robot max size: 18 in cube at the start; extensions within 18 × 24 × 29 in tall. Limits: 8 motors, 8 servos.
