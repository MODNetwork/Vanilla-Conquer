# docs/reference — IMPORTED DOCTRINE, READ-ONLY

Per CLAUDE.md: this directory is read-only imported doctrine. Nothing here is authored by this
project and nothing here is edited by it. Read before touch-control work or silent-failure work.

## Contents

| File | Source | License | What it is |
|---|---|---|---|
| `PORTING_PLAYBOOK.md` | `ammaarreshi/Generals-Mac-iOS-iPad` `docs/port/` | see source repo | Case study: C&C Generals Zero Hour (Win32/DX8) to iOS, June 2026. Every decision, problem and fix. |
| `PORTING_PATTERNS.md` | same | see source repo | Generalized from-scratch porting methodology: strategy selection, compat-shim craft, portability bug taxonomy, determinism gates. |
| `RELEASE_CHECKLIST.md` | same | see source repo | Release gate checklist from that project. |
| `android-port-map.diff` | `TheAssemblyArmada/Vanilla-Conquer` `vanilla` → `sandstranger/Vanilla-Conquer` `android` | **GPL v3** | **The route.** 31 commits, 17 files, 32KB. An existing, shipping Android port of this exact engine. |

## android-port-map.diff — how it is used, and the attribution owed

This is the Option B artifact. We do **not** merge these commits. We read them as a map: they
show exactly which 17 files resist Android and roughly what each needed. Our implementation is
authored on our own tree against true upstream, which keeps `platform-layer-only diffs vs
upstream` intact per CLAUDE.md.

**Attribution is owed regardless.** That diff is GPL v3 work by the `sandstranger` project. Where
our implementation follows it closely, credit belongs in the fork README and in the Gate 7
third-party notices. Reading a GPL work as a guide and then shipping something materially derived
from it without credit is not a position this project takes. See `docs/LICENSING.md`.

Files touched by that diff, for orientation:

```
CMakeLists.txt              common/soundio_openal.cpp
common/connect.cpp          common/straw.cpp
common/debugstring.cpp      common/video.h
common/load.cpp             common/video_sdl2.cpp
common/paths_posix.cpp      common/wwkeyboard_sdl2.cpp
common/rawfile.cpp          redalert/CMakeLists.txt
common/settings.cpp         redalert/options.cpp
                            redalert/startup.cpp
                            tiberiandawn/CMakeLists.txt
                            tiberiandawn/startup.cpp
```

Note what is **absent** from that list: no `android/` directory, no `wwmouse_*` changes, no
gesture or touch code anywhere. The touch layer is unbuilt. That is our project.
