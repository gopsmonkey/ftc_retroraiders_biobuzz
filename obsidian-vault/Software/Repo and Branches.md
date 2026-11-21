---
tags: [ftc, software, git]
updated: 2026-11-20
---
# Repo and branches

Back to [[FTC Home]]

- Repo: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz (public; owner Sathwik / gopsmonkey). Collaborators: Abraar24, CodingPraj2307 (Prajin), shreshtmeti2021 (Shresht). Prithu still needs adding (owner only: Settings → Collaborators).

| Branch | What's on it | Latest | PR |
| --- | --- | --- | --- |
| `main` | Sathwik's two original tuners | `1af7759` | — |
| `shooter-fixes-night-before` | All robot code: `ShooterConfig`, `ShooterController`, `ShooterLogger`, `BioBuzzTeleOp`, `PreloadShootLeaveAuto`, `ShooterMaxVelocityTest`, both tuners, README | `4479479` | [#1](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/1), open |
| `team-obsidian-vault` | This vault (`obsidian-vault/`) | see git log | [#2](https://github.com/gopsmonkey/ftc_retroraiders_biobuzz/pull/2), open |

- The two branches don't touch the same files, so both PRs can merge into `main` with no conflicts. Then everyone works on `main`.
- Code history: [[Shooter Software Implementation Log]]. Compile check: [[OnBot Java Check]].

## Using it
- **OnBot Java only**: upload the `.java` files with *Upload Files*, then *Build Everything* ([[OnBot Java Check]]). The repo holds only the OpMode files, not the SDK.
- Local clones: Abi's vault is the Windows clone `C:\Users\Owner\Documents\ftc_retroraiders_biobuzz`. Pull before editing; commit and push after ([[Sharing This Vault]]).

## Rules
- Keep **every** OpMode in the repo, including `MainTeleOp` and Sathwik's other local files ([[Sathwik Handoff]]).
- After tuning, commit the `ShooterConfig.java` values.
- Code freeze: tag the commit brought to each event (`git tag event-2026-11-21`).
