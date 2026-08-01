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

### WSL2 canary — INSTALLED AND BUILDING 2026-07-31

Distro: **Ubuntu** (WSL2), x86_64, 8 cores. `wsl -u root` is passwordless.
WSLg is active (`WSL2_GUI_APPS_ENABLED=1`, `WAYLAND_DISPLAY=wayland-0`, PulseServer present).

| Component | Version |
|---|---|
| g++ | 15.2.0 (Ubuntu 15.2.0-16ubuntu1) |
| cmake | 4.2.3 |
| ninja | 1.13.2 |
| git | 2.53.0 |
| SDL2 (libsdl2-dev) | **2.32.10** |
| OpenAL (libopenal-dev) | **1.25.1** |

**Canary clone location: `~/cnc-td-android` on ext4 — NOT `/mnt/c`.**

Reason, VERIFIED 2026-07-31: `ls /mnt/c/DEV/cnc-td-android/CMAKELISTS.TXT` **succeeds** against a
file actually named `CMakeLists.txt`. DrvFs (`/mnt/c`) is case-INSENSITIVE, so a canary built
there would catch **zero** case bugs and D-1's stated purpose would be silently defeated. The
canary clone is sourced from the local Windows repo (`git clone /mnt/c/DEV/cnc-td-android`) so it
carries our scaffold commits rather than the unpushed-fork state on GitHub.

### GATE 1 (canary half) — PASS 2026-07-31

Configure + build succeeded. 236 object files. Artifact verified, not merely "it compiled":

```
build/vanillatd: ELF 64-bit LSB pie executable, x86-64, dynamically linked,
                 BuildID d91fa28ca0cb154b5ab056e7deeb352ca7e02650, with debug_info   (14 MB)

ldd: libSDL2-2.0.so.0  => /usr/lib/x86_64-linux-gnu/libSDL2-2.0.so.0
     libopenal.so.1    => /usr/lib/x86_64-linux-gnu/libopenal.so.1

CMakeCache: BUILD_VANILLATD=ON  BUILD_VANILLARA=OFF  NETWORKING=OFF
            SDL2=ON  OPENAL=ON  CMAKE_BUILD_TYPE=RelWithDebInfo
nm: 0 networking symbols (NETWORKING=OFF confirmed at the binary level)
```

Launched with no assets present: reached OpenAL device init (ALSOFT messages emitted), did **not**
segfault. Full no-asset code path is NOT yet characterised — output was piped through `head` and
no audio device exists in this headless context, so `EXIT_CODE=0` reflects the pipe, not the game.
Asset-missing behaviour is properly a Gate 3 criterion and is re-tested there.

### Windows native (Gate 1 playtest loop) — BUILDS 2026-08-01

| Component | Version / path |
|---|---|
| MSVC | **19.44.35228.0** (Build Tools 2022, VC\Tools\MSVC\14.44.35207) |
| Windows SDK | 10.0.26100.0 |
| CMake | 4.4.1 (scoop) |
| Generator | **Visual Studio 17 2022, -A x64** |
| SDL2 | 2.x, `C:/Users/mprit/scoop/apps/sdl2/current` (MSVC import libs) |
| OpenAL | **NOT INSTALLED — built with `-DOPENAL=OFF`** |

Working configure line:

```
cmake -S C:\DEV\cnc-td-android -B C:\DEV\cnc-td-android\build-win ^
  -G "Visual Studio 17 2022" -A x64 ^
  -DBUILD_VANILLATD=ON -DBUILD_VANILLARA=OFF -DNETWORKING=OFF ^
  -DSDL2=ON -DOPENAL=OFF ^
  -DSDL2_ROOT_DIR=C:/Users/mprit/scoop/apps/sdl2/current ^
  -DImageMagick_magick_EXECUTABLE=IGNORE -DImageMagick_convert_EXECUTABLE=IGNORE
```

Artifact: `build-win/RelWithDebInfo/vanillatd.exe` — 1,314,816 bytes, **machine x64**, Windows GUI
subsystem, with `vanillatd.pdb`. `SDL2.dll` staged alongside. Launches without a missing-DLL
failure and exits cleanly; behaviour beyond that is uncharacterised until game data is present.

**The Visual Studio generator is used deliberately instead of Ninja.** It lets CMake locate MSVC
itself, so no `vcvars64.bat` call and no `%PATH%` expansion is required — see the host defect below.

### HOST DEFECT — malformed Windows PATH entry

`%PATH%` on this machine contains an entry with a stray trailing double-quote:

```
C:\Program Files\GitHub CLI"
```

Any script doing `set "PATH=...;%PATH%"` unbalances its quoting and cmd then fails parsing with
`\Microsoft was unexpected at this time.` This burned three build-script attempts. Not fixed
(out of project scope, and it is Michael's environment) — worked around by never expanding
`%PATH%` and by calling `cmake.exe` at its absolute path. **Worth Michael repairing separately.**

### Known-good workarounds recorded

- `.cmd` wrapper scripts written by tooling did not execute (exit 255, no output). Build commands
  are issued as single command lines instead. `.sh` scripts for WSL work fine after
  `sed -i 's/\r$//'`.

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
