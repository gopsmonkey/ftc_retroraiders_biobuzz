---
tags: [ftc, software, git]
updated: 2026-11-20
---
# Repo and branches

Back to [[FTC Home]]

- Repo: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz (owner: gopsmonkey; Abi has push access)
- `main`: the two original tuners (commit `1af7759`)
- `shooter-fixes-night-before`: all shooter software fixes (commit `ac9be61`), PR: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/1, see [[Shooter Software Implementation Log]]

## Using it
Files go in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` of the FTC SDK project (the repo holds only the OpMode files, not the whole SDK).

## Rules from tonight
- Keep **every** OpMode in the repo, including the match TeleOp and Autonomous.
- After tuning, commit the `ShooterConfig.java` values.
- Tag the commit brought to each event (e.g. `git tag event-2026-11-21`).
