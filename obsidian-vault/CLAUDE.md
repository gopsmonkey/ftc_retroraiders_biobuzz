# Claude: how to use this vault

FTC team Retro Raiders (26603), season BIOBUZZ 2026-27. Start at `FTC Home.md`, which links every note.

## Before writing or running anything (robot constraints)
- **Read `Robot/Robot Overview.md` first.** It lists what the robot has and doesn't.
- **No camera.** Don't suggest or write vision, AprilTag, Limelight or color-blob code.
- **Don't add AUTO or automation code.** The team has its own AUTO (mostly done by hand, not in the repo yet) and will add it themselves. Shresht's `PreloadShootLeaveAuto` is the only AUTO in the repo.
- **OnBot Java for the 2026-11-21 competition. Android Studio and Pedro Pathing are ON HOLD until after it.** Don't start either. The plan and a parked Pedro agent are in `Software/Pedro Pathing Plan.md`.
- **OnBot Java only.** One plain class per file, package `org.firstinspires.ftc.teamcode`, FTC SDK + standard Java only. Re-run `Software/attachments/onbot-compile-check.sh` after code changes.
- **The robot runs branch `intake-gate-at-speed` (PR #3, latest `e140571`)**, built on `shooter-fixes-night-before` (PR #1, `391d8ed`). Make code changes on top of PR #3 unless told otherwise. The vault lives on `team-obsidian-vault` (PR #2).
- **Hardware is fixed and known.** `intakeMotor` is one motor driving every roller (one chain), and it has an encoder. Launch is −1375. Use only the config names in `Robot/Hardware Config Names.md`; **never invent device names** like `feederMotor`. If a name truly isn't known, ask once, naming the note you checked.
- **The team pastes code into OnBot by hand.** Hand them raw GitHub links pinned to a commit SHA, and name every file that changed. Always ship `ShooterConfig` and `ShooterController` together. **When `ShooterConfig` gains settings, also give the exact lines to add to the team's existing OnBot `ShooterConfig`**, because they keep their tuned numbers there; a whole-file replace has failed twice (00:54, 01:57). Before handing code over, run `Software/attachments/onbot-compile-check.sh` and `Software/attachments/tests/run-tests.sh`.
- **Numbers changed in OnBot aren't in git.** Ask for them and commit them. Stage files by name, never `git add -A` (it swept plugin caches into `997a408`).
- **The team is under time pressure.** When they ask for a change, make it and verify it; don't send them a list of options. Keep answers short and concrete: which file, which setting, which button.

## Where each fact lives (one fact, one note: link to it, don't copy it)
| Topic | Note |
| --- | --- |
| **All open tasks, with owners** | `To-Do.md` |
| **What the robot runs now: intake brake + one-ball feed, settings, tests, on-robot status** | `Software/PR 3 - Intake Brake and One-Ball Feed.md` |
| Laptop tests for the intake gate (mock motors, real SDK classes) | `Software/attachments/tests/run-tests.sh` |
| OnBot Java compile result, upload steps, build risks (re-run `Software/attachments/onbot-compile-check.sh` after code changes) | `Software/OnBot Java Check.md` |
| Sathwik's handoff: OnBot Java, local files, odometry, hub issue, how he likes to work | `Software/Sathwik Handoff.md` |
| Game rules, scoring, TIP threshold, fouls | `Game/BIOBUZZ Rules and Scoring.md` |
| Other teams: rankings, speeds, gains, lessons | `Game/Top Teams Shooter Reference.md` |
| Other teams: code details | `Game/Research/Top Teams Code Deep Dive.md` |
| FTCScout data, repo links, official docs | `Game/Research/Research Sources.md` |
| What's wrong with our shooter and the fix order | `Shooter/Shooter Fix List - Night Before Competition.md` |
| Video measurements | `Shooter/Shooter Video Evidence.md` |
| Code problems found (with line refs) | `Shooter/Shooter Code Review.md` |
| What changed in code and how the OpModes work | `Shooter/Shooter Software Implementation Log.md` |
| Tuning steps and numbers (maxV, PIDF, presets) | `Robot/Shooter Tuning Guide.md` |
| **What the robot is built from, CAD, what it lacks (no camera)** | `Robot/Robot Overview.md` |
| Motor names and directions | `Robot/Hardware Config Names.md` |
| Match-day checklist | `Shooter/Pre-Match Checklist.md` |
| Future work | `Shooter/Post-Competition Redesigns.md` |
| Branches and commits | `Software/Repo and Branches.md` |
| Pedro Pathing: OnBot blocker, our localizer constants, coordinates, AUTO ideas | `Software/Pedro Pathing Plan.md` |
| Night-before code check: compile result, risks, patches, backup plan | `Software/Code Check - Night Before.md` |
| Simple AUTO (shoot preloads + LEAVE) | `Software/Preload Shoot + Leave Auto.md` |
| 30 fps video measurements | `Shooter/Second Video Analysis (30 fps).md` |
| Mechanical fixes that need no big prints | `Shooter/Small Fixes Without Reprinting.md` |
| Competition-day plan, pack list, judging | `Competition Day Tips.md` |
| Unanswered questions | `Open Questions.md` |

## Rules for editing
- Before adding a fact, search the vault. If it exists, link to it with `[[Note Name]]`.
- New topic → new note in the right folder, linked from `FTC Home.md`.
- Keep the frontmatter (`tags`, `created`/`updated`) and the "Back to [[FTC Home]]" line.
- Pull before editing. Commit and push after every change. The vault lives on branch `team-obsidian-vault`.
