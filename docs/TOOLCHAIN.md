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

## Host audit — 2026-07-31 (updated during Phase 1)

**Host:** Windows 10 Pro 22H2, build 19045.6466, x64.

### INSTALLED this session (scoop, no admin required)

| Tool | Version | Path |
|---|---|---|
| CMake | **4.4.1** | `C:\Users\mprit\scoop\shims\cmake.exe` |
| Ninja | **1.13.2** | `C:\Users\mprit\scoop\shims\ninja.exe` |
| g++ (MinGW-w64, nuwen) | **15.2.0** | `C:\Users\mprit\scoop\apps\gcc\current\bin\g++.exe` |
| SDL2 | scoop `extras` | `C:\Users\mprit\scoop\apps\sdl2\current` |
| 7zip | 26.02 | scoop dependency |

CMake 4.4.1 compatibility checked: tree declares `cmake_minimum_required(VERSION 3.25)`.
CMake 4.x only drops support for `<3.5`, so **no conflict**. VERIFIED.

### BLOCKER — SDL2 library format does not match the installed compiler

scoop's SDL2 ships **MSVC import libraries only**:

```
lib\SDL2.lib   lib\SDL2main.lib   lib\SDL2test.lib   lib\SDL2.dll   lib\SDL2.pdb
```

No mingw `.a` archives. Linking MSVC import libs from MinGW is fragile and is a workaround, not
a fix (CLAUDE.md: do not route around problems). Two clean resolutions, both requiring elevation:

- **Install MSVC Build Tools** → matches the SDL2 we already have. Preferred for Gate 1.
- **Install WSL2 + apt** → matched g++/SDL2/OpenAL in one command. Required anyway as the D-1 canary.

### STILL ABSENT

| Component | State | Notes |
|---|---|---|
| MSVC / Windows SDK | absent | `vswhere.exe` not present; no Visual Studio installed |
| OpenAL dev libs | absent | Not available in scoop. openal-soft ships MSVC binaries. |
| WSL2 | **absent** | `wsl.exe` is the System32 stub; `--version` and `-l -q` both print usage. Needs `wsl --install` (admin + reboot). |
| Android SDK / NDK / JDK / Gradle / adb | absent | Phase 2 concern (D-3) |

### WSLg note (Win10 22H2)

WSLg GUI support requires the Microsoft Store WSL package on Windows 10 build 19044+. This host is
19045, so WSLg is available in principle, but GUI-plus-audio playtesting under WSLg on Windows 10
is less reliable than on Windows 11. **This is why Gate 1's playtest baseline stays Windows-native
(D-1) and WSL2 stays the compile canary.** Do not move the playtest loop into WSL2.

### Package managers available

winget (present), scoop (present, in use), choco (absent).

---

## Target device

| Field | Value |
|---|---|
| Model | |
| Android version | |
| ABI | arm64-v8a |
| Recorded on | |
