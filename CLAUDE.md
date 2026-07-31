# CNC-TD-ANDROID — Claude Code Law
(Global ~/.claude/CLAUDE.md rules 1-18 inherit; never restated.)
<!-- v2.1 - the six amendments from the 2026-07-13 review are APPLIED here. Supersedes v2. -->

## Stack
C++ (Vanilla-Conquer engine, ~504K LOC) · CMake · SDL2 (Android template + JNI bootstrap) ·
OpenAL/openal-soft · Gradle externalNativeBuild · NDK r26+ · arm64-v8a only.
CMake law: -DNETWORKING=OFF -DBUILD_VANILLARA=OFF for all builds through Gate 6.
Re-enabling networking is a post-Gate-7 decision, logged D-style if taken.

## Critical paths
Engine tree = fork root. Android layer = android/ (new). Platform seams: common/video_*,
wwkeyboard_*, wwmouse_*. Writable runtime paths -> SDL_AndroidGetInternalStoragePath().
Reference doctrine: docs/reference/ (Reshi playbook — read before touch-control or
silent-failure work). Plan of record: BUILD_PLAN.md. Decisions: docs/DECISIONS.md.
Toolchain pins: docs/TOOLCHAIN.md records exact NDK, SDL2 commit, Gradle/AGP,
CMake, and min-API decision at Gate 2; no version changes after Gate 2 without
a D-style decision entry.

## Hard boundaries
- GAME ASSETS NEVER ENTER THE REPO: no commit, no bundle, no distribution. Asset dirs and
extraction outputs are gitignored; user supplies at runtime (procedure per docs/RECON.md).
- NEVER MODIFY GAME LOGIC. Diff vs upstream stays minimal and platform-layer only.
- NO MOD OS / TCG IDENTIFIERS anywhere — package id, app name, signing identity included.
- SIGNING KEYSTORE + PASSWORDS: never in repo, never in chat (names only). Gitignore:
*.keystore, *.jks, local.properties, .gradle/, build outputs, asset dirs.
- docs/reference/ is READ-ONLY imported doctrine.
- REPO HYGIENE (Windows host): .gitattributes enforces LF on *.sh, *.cmake,
gradlew; core.autocrlf=false for this repo. Case-collision and include-case
audit runs at Gate 3 step 4 alongside readelf/nm (Windows FS hides case bugs
that Android exposes).

## Verification definition (what done means)
Phase-gated per BUILD_PLAN.md — the gate IS the verification.
Desktop phases: engine builds and TD runs on the Windows host from this tree
(Gate 1 tag baseline-desktop is the playtest reference; reproduce bugs there
FIRST). A parallel WSL2 Linux build is the porting canary: clang + POSIX +
case-sensitive FS, the closest desktop analog to the NDK target. Any bug that
reproduces in WSL2 but not Windows is treated as an Android-class bug and
fixed before Phase 3.
Android phases: apk builds via Gradle, installs via adb install, and the gate's on-device
criterion holds — logcat evidence for boot paths, Michael's playtest for gameplay
(HUMAN-GATE; agents cannot see the screen). "Compiles" is never "done";
readelf/nm artifact checks per Phase 3 where specified.

## Evidence gate (operating law)
- One directive at a time; no advance without verification output and a PASS ruling.
- Every step: verification command + binary PASS + failure branches. SUPERSEDED labeling
mandatory. Reports >15 lines -> temp file + Notepad.
- Installers/SDK setup -> plain PowerShell window, never a pane.
- All android/ layer logging uses fixed tag VCTD. Gate evidence capture is one
command: adb logcat -s VCTD SDL -d > gate<N>_evidence.txt (temp file rule
applies).

## Delegation map
Single-stream through Phase 4 (one pane or legacy quad). If parallelized at Phases 5-6:
W1 engine/platform (C++/CMake/NDK) · W2 touch overlay + input translation (android/, synthesized
SDL events into wwmouse/wwkeyboard — game logic untouched; translation layer includes a
raw-passthrough debug toggle for input-bug bisection) · W3 QA: builds, adb install,
logcat capture, case-sensitivity audits; product source read-only, findings filed ·
W4 integrator at main: merges, upstream syncs, APK signing (Phase 7), release evidence.

## Entity rules
Entity: personal. GPL v3 code / EA-copyright assets — the license wall is absolute.
Never conflate MOD OS LLC and The CoCreate Group; neither appears in this project at all.
