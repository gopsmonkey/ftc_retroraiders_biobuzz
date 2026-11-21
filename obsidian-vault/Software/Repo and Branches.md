---
tags: [ftc, software, git]
updated: 2026-11-21
---
# Repo and branches

Back to [[FTC Home]]

- Repo: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz (public; owner Sathwik / gopsmonkey). Collaborators: Abraar24, CodingPraj2307 (Prajin), shreshtmeti2021 (Shresht). Prithu still needs adding (owner only: Settings → Collaborators).

| Branch | What's on it | Latest | PR |
| --- | --- | --- | --- |
| `main` | Sathwik's two original tuners | `1af7759` | — |
| `shooter-fixes-night-before` | All robot code: `ShooterConfig`, `ShooterController`, `ShooterLogger`, `BioBuzzTeleOp`, `PreloadShootLeaveAuto`, `ShooterMaxVelocityTest`, both tuners, README | `391d8ed` | [#1](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/1), open |
| `intake-gate-at-speed` | **What the robot runs:** `shooter-fixes-night-before` + intake brake until −1375 + one-ball feed ([[PR 3 - Intake Brake and One-Ball Feed]]) | `7a10048` | [#3](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/3) → `shooter-fixes-night-before`, open. **Squash and merge.** |
| `team-obsidian-vault` | This vault (`obsidian-vault/`) | see git log | [#2](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/2), open |

- Merge order: PR #3 into `shooter-fixes-night-before` (squash), then PR #1 and PR #2 into `main`.
- The code and vault branches don't touch the same files, so both PRs can merge into `main` with no conflicts. Then everyone works on `main`.
- Code history: [[Shooter Software Implementation Log]]. Compile check: [[OnBot Java Check]].

## Using it
- **OnBot Java only**: upload the `.java` files with *Upload Files*, then *Build Everything* ([[OnBot Java Check]]). The repo holds only the OpMode files, not the SDK.
- Local clones: Abi's vault is the Windows clone `C:\Users\Owner\Documents\ftc_retroraiders_biobuzz`. Pull before editing; commit and push after ([[Sharing This Vault]]).

## Rules
- Keep **every** OpMode in the repo, including `MainTeleOp` and Sathwik's other local files ([[Sathwik Handoff]]).
- After tuning, commit the `ShooterConfig.java` values.
- Code freeze: tag the commit brought to each event (`git tag event-2026-11-21`).
- **OnBot edits never reach GitHub.** After changing numbers on the robot, send them to whoever commits, so the repo matches the hub.
- **Commit only the files you changed** (`git add <file>`, not `git add -A`). Plugin caches (`.claude-flow/`, `.swarm/`, `ruvector.db`) got into `997a408` that way; they are now in `.gitignore`.
