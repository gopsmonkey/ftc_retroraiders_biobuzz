---
tags: [ftc, index]
updated: 2026-11-21
---
# FTC Home: RetroRaiders / BioBuzz

Start here. Everything about the robot, the code and the competition prep links from this page.

**Tasks: [[To-Do]]** (one list, with owners)

**Now (2026-11-21):** the robot runs [[PR 3 - Intake Brake and One-Ball Feed]]. The intake stays braked until the wheel is at −1375. The next build needs `ShooterConfig` re-pasted ([[To-Do]], "Right now").

**Map:** press **Cmd+G** (Ctrl+G) for the graph. Colors: amber = Home/To-Do, green = Game, orange = Shooter, yellow = Robot, teal = Software, grey = everything else.

## Competition prep (2026-11-21)
- [[Shooter Fix List - Night Before Competition]]: the prioritised fix list (do this first)
- [[Pre-Match Checklist]]: run it before every match
- [[Shooter Video Evidence]]: what the 240 fps video showed
- [[Shooter Code Review]]: what's wrong in the repo code, with line refs
- [[Shooter Software Implementation Log]]: what was changed in code tonight, and why

## Night-before review (added 2026-11-20, 7:30 pm)
- [[Small Fixes Without Reprinting]]: mechanical fixes with kit metal, washers and tape, no big prints
- [[Code Check - Night Before]]: patches A–D, what could still be better in the code, 20-minute robot test, backup plan
- [[Preload Shoot + Leave Auto]]: Shresht's simple AUTO (`PreloadShootLeaveAuto.java`): what it does and the numbers to tune
- [[Second Video Analysis (30 fps)]]: the 30 fps clip, frame by frame: the ball waits 0.5–0.67 s in the hood (0.9–1.1 s after the hand adjustment), and the mount moves ~2 mm per shot
- [[Competition Day Tips]]: tonight, pack list, at the event, judging
- [[Open Questions]]: what we still need answered

## Game
- [[BIOBUZZ Rules and Scoring]]: scoring, how a TIP works (3 then ~8 POLLEN), rules that cost points
- [[Top Teams Shooter Reference]]: what the best teams in the world do, and how our speeds compare
- [[Top Teams Code Deep Dive]]: per-team code details (file paths, math, constants), plus [[Research Sources]] (FTCScout data, repo links)

## Robot
- **[[Robot Overview]]: how the bot is built, CAD, and what it does NOT have (no camera, no own AUTO yet). Read before running code.**
- [[Hardware Config Names]]: motor names and directions the code expects
- [[Shooter Tuning Guide]]: how to measure maxV, set PIDF, and tune P

## Software
- [[PR 3 - Intake Brake and One-Ball Feed]]: **what the robot runs now**. Intake brake + one-ball feed: settings, tests, on-robot status
- [[OnBot Java Check]]: the branch compiles for OnBot Java; how to upload it, and what can still break the build
- [[Sathwik Handoff]]: Sathwik's old-chat handoff (OnBot Java only, local-only files, odometry, Expansion Hub issue, solved bugs)
- [[Repo and Branches]]: where the code lives and which branch to run
- [[Pedro Pathing Plan]]: **on hold until after the competition.** Then: switch to Android Studio, then Pedro. Has our localizer values and a parked Claude agent.

## Team
- [[Sharing This Vault]]: how to get the vault from GitHub and contribute

## Later
- [[Post-Competition Redesigns]]

## Sources
- Fix list doc (Claude Docs): https://claude.ai/artifact/2TijvqY1ftJomPvE1RqhEn
- Repo: https://github.com/gopsmonkey/ftc_retroraiders_biobuzz
