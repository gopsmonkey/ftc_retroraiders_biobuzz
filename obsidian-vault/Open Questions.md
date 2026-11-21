---
tags: [ftc, questions]
created: 2026-11-20
---
# Open questions

Back to [[FTC Home]] · Context: [[Code Check - Night Before]], [[Second Video Analysis (30 fps)]]

Answer these tonight if you can. Write the answer under each one.

## Code
1. **Which TeleOp and AUTO will we actually run tomorrow, and are they in the repo?** If it's a different file, none of tonight's fixes are in it.
    - **Answer:** TeleOp is still undecided. `MainTeleOp` (Sathwik's laptop only) has no fixes. `BioBuzz TeleOp` has the fixes and, since 2026-11-20 9 pm, the flower servos on Y. AUTO: only Shresht's `PreloadShootLeaveAuto` ([[Sathwik Handoff]], [[To-Do]]).
2. Android Studio or OnBot Java? That decides how the branch gets onto the Control Hub.
    - **Answer:** OnBot Java only ([[OnBot Java Check]]).
3. Has *BioBuzz TeleOp* run on the robot even once? Did `PIDF on hub` say OK?
4. Is PR #1 merged, and which branch is on the Control Hub right now?
    - **Answer:** PR #1 (code) and PR #2 (vault) are both open, not merged, as of 2026-11-20 9 pm. Which files are on the hub is unknown.
5. Did `At speed` turn YES with the placeholder PIDF? If not, what did *Shooter MaxV Test* measure?

## Robot
6. Exactly which shooter motor (6000 rpm Yellow Jacket 1:1?) and what flywheel diameter? That sets maxV and the presets.
    - **Answer:** Partly. One gecko-wheel flywheel, no turret ([[Robot Overview]]). Motor model and wheel diameter still unconfirmed.
7. Is the intake motor's encoder cable plugged in?
    - **Answer:** No encoder (handoff). `INTAKE_HAS_ENCODER = false`.
8. Can the hood gap be adjusted, or is it fixed by the print?
9. Is anything besides the intake pushing balls into the flywheel (a feeder servo or a second motor)?
    - **Answer:** None listed in the handoff; the flower servos only drop and lift the intake.
14. **Camera:** the robot has no camera (Abi, 2026-11-20), but the handoff lists `ColorSamplerOpMode` as "working" and `PollenAutoNav` as color-blob vision. Was a camera used for testing and then removed? Until it's answered, treat the robot as camera-free ([[Robot Overview]]).
15. Is the viper slide still mounted on the robot? It decides whether `viperMotor` belongs in the config.

## Video
10. The 240 fps clip ([[Shooter Video Evidence]]) and the 30 fps clip ([[Second Video Analysis (30 fps)]]): same shooter speed (1450 ticks/s) and same hood setting? Was anything changed between them? Someone adjusts the shooter by hand at 1:38 in the 30 fps clip.

## Match plan
11. Where does the robot start in AUTO, and can it hit the up CELL from there?
12. How many charged batteries do we have?
13. Who drives, and who is coach / human player?
