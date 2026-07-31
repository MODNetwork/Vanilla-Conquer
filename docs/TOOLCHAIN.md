# TOOLCHAIN.md — cnc-td-android

Pinned at Gate 2. No version changes after Gate 2 without a D-style entry in docs/DECISIONS.md.

Status: **EMPTY — to be filled at Gate 2.**

| Component | Pinned version / ref | Install path | Pinned on | Verified by |
|---|---|---|---|---|
| Android NDK | | | | |
| Android SDK platform | | | | |
| Android build-tools | | | | |
| min API level | | | | |
| target API level | | | | |
| JDK | | | | |
| Gradle | | | | |
| Android Gradle Plugin | | | | |
| CMake (Android) | | | | |
| CMake (desktop host) | | | | |
| Ninja | | | | |
| SDL2 commit SHA | | | | |
| openal-soft version | | | | |
| Vanilla-Conquer upstream SHA at fork | | | | |
| Windows host compiler (MSVC / mingw) | | | | |
| WSL2 distro + clang version | | | | |

## Host audit — 2026-07-31 (pre-Gate-2 baseline)

VERIFIED present:
- git 2.55.0.windows.2 — C:\Program Files\Git\cmd\git.exe

VERIFIED absent from PATH:
- cmake, ninja, java, gradle, adb

VERIFIED absent from disk:
- Android SDK root (%LOCALAPPDATA%\Android — not found; C:\Program Files\Android — not found)
- Android NDK (follows from no SDK root)
- Vanilla-Conquer checkout anywhere on C:\

UNVERIFIED, probe required:
- WSL2 distro. `wsl -l -v` returned usage text rather than a distribution list. Probe: `wsl --status`.
- CMake bundled inside a Visual Studio installation but absent from PATH. Probe: VS Installer
  component check.

## Target device

| Field | Value |
|---|---|
| Model | |
| Android version | |
| ABI | arm64-v8a |
| Recorded on | |
