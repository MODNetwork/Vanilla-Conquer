# DECISIONS — cnc-td-android

Format: D-<n> · date · decision · rationale · what it supersedes.

---

## D-1 · 2026-07-13 · Desktop baseline is Windows native, with WSL2 as porting canary

**Decision.** Gate 1 is satisfied by TD running on the Windows host from this tree. A parallel
WSL2 Linux build runs alongside as the porting canary. Any bug that reproduces in WSL2 but not
Windows is treated as an Android-class bug and fixed before Phase 3.

**Rationale.** The original law said "desktop-Linux-first debugging" and defined Gate 1 as "TD
runs on Linux from this tree." The host is Windows (C:\DEV, PowerShell). As written, Phase 1 was
unexecutable. Swapping Linux for Windows outright would have discarded something valuable: Android
is clang + POSIX paths + case-sensitive filesystem, and Windows is none of those three. WSL2
catches that whole bug class for roughly one extra hour at Phase 1, versus discovering it
on-device in Phase 3 where debugging is an order of magnitude slower.

**Supersedes.** CLAUDE.md "Verification definition" Linux sentence; GENESIS_PACK v2 §4 execution
law line ("desktop-Linux-first debugging").

---

## D-2 · 2026-07-13 · Networking and Red Alert target OFF through Gate 6

**Decision.** All builds through Gate 6 carry `-DNETWORKING=OFF -DBUILD_VANILLARA=OFF`.
Re-enabling either is a post-Gate-7 decision, logged D-style if taken.

**Rationale.** NETWORKING defaults ON in upstream CMakeLists. First-gen C&C networking is
WinSock-era code and a classic porting pain surface, and it is irrelevant to the success
definition (single-player GDI mission 1 by touch). Carrying it through the Phase 3 cross-compile
buys breakage for a feature no gate tests.

**Note on Red Alert.** The host machine carries licensed RA legacy data alongside TD (verified
2026-07-31). RA is therefore a cheap post-Gate-7 extension, not a scope expansion of this plan.
Nothing in Phases 0–7 changes.

---

## D-3 · 2026-07-31 · Gate 2 is install-then-pin, not pin

**Decision.** Phase 2 step 1 is expanded: the Android toolchain does not exist on this host and
must be installed before it can be pinned.

**Rationale.** Host audit 2026-07-31 (VERIFIED): git 2.55.0.windows.2 present. CMake, Ninja, JDK,
Gradle, adb absent from PATH. No Android SDK root at %LOCALAPPDATA%\Android or
C:\Program Files\Android. No NDK. WSL2 distro presence unconfirmed (`wsl -l -v` returned usage
text; probe `wsl --status`). CMake may exist bundled inside a Visual Studio install without being
on PATH — ASSUMPTION, probe is a VS Installer component check.

**Effect.** Phase 2 gains an install step ahead of the template import. No gate criteria change.

---

## D-4 · 2026-07-31 · Fork owner is the MODNetwork GitHub org · RATIFIED BY MICHAEL

**Decision.** The fork lives at `MODNetwork/Vanilla-Conquer`. Michael ratified 2026-07-31.

**Rationale.** MODNetwork is the personal umbrella org and already hosts personal-entity projects
(fo4-mod, pricharda-gallery). It is not a MOD OS LLC identifier. The entity wall in CLAUDE.md
governs package id, app name and signing identity, all of which remain clean.

**Also ruled.** Push is held to last. The local scaffold commit is not published until Michael
authorizes it. GPL v3 distribution obligations and the EA additional terms (see docs/RECON.md)
trigger on conveyance, not on local commits.

---

## D-5 · 2026-07-31 · Upstream license VERIFIED as GPL v3 + EA additional terms

**Decision.** The GPL v3 assumption carried by CLAUDE.md and the Genesis Pack is confirmed
correct. GitHub reporting `NOASSERTION` was a classifier failure on the EA preamble, not a
missing grant.

**Rationale.** `License.txt` (714 lines) read directly in the clone. Header: *"Electronic Arts
Inc. released only TiberianDawn.dll and RedAlert.dll and their corresponding source code under
the GPL V3 below, with additional terms at the bottom."* The additional terms are recorded in
docs/RECON.md.

**Consequence that changes build inputs:** the EA trademark clause forbids distributing any
modification using an EA trademark. The app name, package id and icon cannot use "Command &
Conquer", "C&C", "Tiberian Dawn" or "Red Alert". Phase 2 step 4 (package id) and Phase 7
(packaging) inherit this constraint. Modified versions must additionally be marked as modified.

---

## D-6 · 2026-07-31 · Branding scrub happens at Phase 2, not Phase 7 · attribution is never scrubbed

**Decision.** Two rulings, recorded together because they are routinely confused.

**(a) Timing.** The EA-mark-free app name and package id are chosen at **Phase 2 step 4**, not
during a late "branding scrub" at Phase 7. Package id binds to the signing identity and to
`SDL_AndroidGetInternalStoragePath()`, which is where saves and config live. Renaming after
Gate 6 invalidates installs and save paths and forces a Gate 6 re-test. Phase 7 branding work is
then cosmetic and additive only: icon, credits screen, README, third-party notices.

**(b) Scope.** The scrub removes EA **trademarks used as product identity** (app name, package
id, icon, listing, any affiliation claim). It never touches **copyright notices or provenance**
(License.txt, EA copyright headers, the GPL-source-release statement, Vanilla-Conquer contributor
credit). Stripping a copyright notice to avoid EA would trade a trademark exposure for a
copyright violation, which is strictly worse.

**Rationale.** Michael asked whether GPL attribution is absolute in this build. It is, and it
becomes fully binding on conveyance rather than on private use. Full doctrine in docs/LICENSING.md.

**Also recorded.** SDL2 and OpenAL resolve via `find_package` (verified in CMakeLists.txt) and are
not vendored in this tree, so on Android we bundle them and their licenses attach to our APK.
openal-soft is believed LGPL, which carries a relinking obligation distinct from GPL —
**NEEDS VERIFICATION before Gate 7**; dynamic linking is the normal way to satisfy it.

---

## D-7 · 2026-07-31 · Option B ruled — read the existing port as a map, do not merge it

**Decision.** Michael ruled Option B. We keep the clean fork of true upstream
(`TheAssemblyArmada/Vanilla-Conquer`) and treat `sandstranger:android` as reference doctrine
only. Its 31-commit / 17-file diff is imported to `docs/reference/android-port-map.diff` and is
never merged.

**Rejected alternatives.** *Option A* (re-base on `sandstranger:android`) was faster to a running
build but made this a fork of a fork, strained the platform-layer-only-diff law, put upstream
merges behind a middleman, and inherited bugs their own commit messages acknowledge. *Option C*
(install their APK, build nothing) was recorded as the honest zero-effort baseline and declined.

**Rationale.** The project's schedule risk lived in Gates 2–3 (does a 1995 DOS-era C++ tree
cross-compile for ARM64). A shipping third-party arm64-v8a APK proves it does, and a 32KB diff
names the 17 files that resist. That risk is dead either way. Option B additionally preserves
one-command upstream merges and full authorship of our own tree, which matters more on a project
intended to be kept than the few hours Option A saves.

**Consequence.** BUILD_PLAN.md v2.0: Phase 0 closed, Phase 3 derisked 3–6 hrs → 2–4 hrs, Phase 5
raised 3–5 hrs → 4–7 hrs and named as the differentiated work. Total 13–25 hrs authoring.

**Attribution obligation.** `android-port-map.diff` is GPL v3 work by the sandstranger project.
Where our implementation follows it closely, credit is owed in the fork README and Gate 7
third-party notices. Recorded in docs/LICENSING.md and docs/reference/README.md.

---

## D-8 · 2026-07-31 · Gate 1 baseline uses freeware Gold CD data, not the Remastered install

**Decision.** The desktop baseline and all gate testing use the **freeware C&C Gold CD release**
(GDI + NOD ISOs) for Tiberian Dawn. The host's C&C Remastered Collection is NOT the data source.

**Supersedes.** `docs/RECON-SEED.md` asset pre-clearance, which assumed the Remastered legacy
`.MIX` trees were usable. That assumption was labelled ASSUMPTION, probed, and **found false.**

**Evidence.** Upstream README in-tree: *"While it is possible to use the game data from the
Remastered Collection, The Ultimate Collection or The First Decade they are currently not
supported."* And: *"Any repackaged version ... is not supported."* Supported sources named are the
freeware Gold CD release for TD and the freeware CD release or official demo for RA.

**Rationale.** "Unsupported" here means the documented failure mode is data-related bugs —
invisible objects, unit-specific crashes. Gate 1 exists to be the reference implementation every
later bug is compared against. Building it on unsupported data makes every Android bug ambiguous
between "our port" and "the data", which destroys the baseline's diagnostic purpose before Phase 3
begins.

**Michael's action.** Obtain the freeware TD Gold GDI + NOD ISOs (links in upstream README).
Agent does not download game data.

---

## D-9 · 2026-07-31 · Desktop toolchain: MSVC preferred over MinGW for Gate 1

**Decision.** Gate 1's Windows-native build targets **MSVC**, not the MinGW g++ installed this
session.

**Rationale.** Installed without admin this session: CMake 4.4.1, Ninja 1.13.2, g++ 15.2.0
(MinGW-w64), SDL2 (scoop). But scoop's SDL2 ships **MSVC import libraries only** (`SDL2.lib`,
`SDL2main.lib`, no mingw `.a`). Linking MSVC import libs from MinGW is fragile and would be a
workaround rather than a fix, which CLAUDE.md forbids. Installing MSVC Build Tools makes the
already-installed SDL2 match natively.

**Also ruled.** WSL2 remains the **compile canary only** (D-1), not the playtest loop. This host is
Windows 10 22H2 build 19045; WSLg exists on 19044+ but GUI-plus-audio playtesting under WSLg on
Windows 10 is less reliable than on Windows 11. The playtest loop stays Windows-native.

**Blocked on.** Both MSVC Build Tools and WSL2 require elevation. Per CLAUDE.md, installer and SDK
setup runs in a plain PowerShell window, never a pane. Handed to Michael.

---

## D-10 · 2026-07-31 · The WSL2 canary must live on ext4, never on /mnt/c

**Decision.** The WSL2 canary clone is `~/cnc-td-android` inside the WSL ext4 filesystem. Building
the canary from `/mnt/c/DEV/cnc-td-android` is forbidden.

**Evidence.** `ls /mnt/c/DEV/cnc-td-android/CMAKELISTS.TXT` **succeeds** against a file actually
named `CMakeLists.txt`. DrvFs is case-insensitive.

**Why this matters.** D-1 justifies the WSL2 canary as "clang + POSIX + case-sensitive FS, the
closest desktop analog to the NDK target." Two of those three hold on `/mnt/c`, but the
case-sensitivity — the property that catches the wrongly-cased `#include` or asset path that
builds clean on Windows and dies on Android — does not. A canary built on `/mnt/c` would have
looked like it was working while silently providing none of its stated value, and the first
symptom would have appeared on-device in Phase 3 where debugging is an order of magnitude slower.

**Also ruled.** The canary clones from the local Windows repo, not from GitHub, so it tracks our
scaffold commits (currently unpushed) rather than a stale fork state.

---

## D-11 · 2026-08-01 · Windows icon generation disabled — upstream bug, cosmetic scope

**Decision.** The Windows build configures with
`-DImageMagick_magick_EXECUTABLE=IGNORE -DImageMagick_convert_EXECUTABLE=IGNORE`, which makes
`make_icon()` no-op. `vanillatd.exe` ships with a generic program icon on this host.

**Upstream bug found.** `cmake/BuildIcons.cmake` does:

```cmake
find_program(ImageMagick_magick_EXECUTABLE magick)
if(ImageMagick_magick_EXECUTABLE) ... else()
    find_program(ImageMagick_convert_EXECUTABLE convert)   # <-- unqualified "convert"
```

On any Windows machine without ImageMagick, `convert` resolves to **`C:\Windows\System32\convert.exe`**,
the NTFS filesystem conversion utility. CMake then treats it as ImageMagick and feeds it image
arguments, producing `Invalid Parameter - none` and build failure with exit code 4. This is a real
upstream defect worth reporting to TheAssemblyArmada; it is not caused by anything we changed.

**Why not simply install ImageMagick.** It was installed (scoop, 7.1.2-29) and still failed:
first `rsvg-convert` delegate missing, then `RegistryKeyLookupFailed 'CoderModulesPath'` (portable
build, no registry), then after pointing `MAGICK_CODER_MODULE_PATH` at the real `modules\coders`
directory, `unable to load module IM_MOD_RL_svg_.dll: The specified module could not be found`
(the coder's own dependent DLLs do not resolve). Four attempts, same class of failure.

**Stall declared per Agent Law 3.8** and approach switched rather than attempting a fifth fix.

**Scope justification.** The icon is cosmetic. Upstream's own README states icons are optional
("otherwise you will end up with generic 'program' icons"). It has no bearing on Gate 1
("TD runs on the Windows host"), and Android launcher icons are a separate Phase 7 asset entirely.
`make_icon` sets `VANILLATD_ICON` only when ImageMagick is found, and `add_executable` simply
receives one fewer argument when it is unset — so this produces **zero diff against upstream**.

**Revisit at.** Phase 7, only if a Windows release build with a proper icon is wanted. The Android
icon path does not depend on this.

---

## D-12 · 2026-08-01 · App identity — package id pinned now, display name deferred

**Michael's request:** two titles "CCTD" and "CCRA", ideally selectable inside one app called "C&C".

**Conflict.** All three strings are Electronic Arts trademark references. `docs/LICENSING.md` and D-6
record the EA GPL §7 additional term: *"You may not distribute any modification of this program
using any Electronic Arts trademark."* That binds app name, package id, icon and listing. This was
ratified before the request, and recorded state outranks request wording.

**The resolution that unblocks Phase 2 without deciding branding today:** package id and display
name are **separable**.

- **Package id is permanent.** It binds the signing identity and
  `SDL_AndroidGetInternalStoragePath()`, so it determines where saves live. Changing it after
  Gate 6 invalidates installs and forces a Gate 6 re-test (D-6).
- **Display name is cosmetic** and can change at any point up to Gate 7 at zero cost.

**Ruled now (agent decision, reversible only before Gate 2 completes):**

| Field | Value | Rationale |
|---|---|---|
| Package id | `dev.pricharda.vc95` | **SUPERSEDED by D-22 on 2026-08-02.** Reverse-DNS under Michael's personal namespace. No EA mark. No MOD OS / TCG identifier (entity wall). |
| Display name (placeholder) | `VC95` | **SUPERSEDED by D-22 on 2026-08-02.** Neutral placeholder. |
| Internal build target names | `vanillatd`, `vanillara` | Upstream's own names, already public and GPL-attributed. Internal target names are not product identity and carry materially lower risk than app name or package id. |

**Deferred to Michael — display name, three options, lowest risk first:**

1. **Fully original name, no lineage in the title.** Lineage credited in the README and the credits
   screen where GPL requires it, not in the product mark. Lowest risk; also the only option that
   scales if the app is ever listed publicly.
2. **Upstream-family name** (e.g. something built around "Vanilla Conquer"). Not an EA mark, but
   risks implying affiliation with the upstream project, which is a courtesy problem rather than a
   legal one. Would warrant asking them.
3. **Keep "CCTD"/"CCRA" as in-app mode labels only**, with a neutral app name. Still carries
   residual risk: they are recognisable contractions of the marks. **[COUNSEL]** if pursued.

**Not recommended:** "C&C" as the app name in any form. It is the mark.

---

## D-13 · 2026-08-01 · Two-title shell designed now, Red Alert enabled after Gate 7

**Decision.** The Android app shell is built from the start to host **two** game targets with a
mode selector. Only Tiberian Dawn is wired and gated through Gate 6. Red Alert is enabled as a
post-Gate-7 extension.

**Rationale.** Michael wants both titles in one app. D-2 holds `-DBUILD_VANILLARA=OFF` through
Gate 6 to shrink the Phase 3 cross-compile breakage surface, and that reasoning is unchanged.
These are reconcilable: the cost of *designing* for two titles now is near zero (do not hardcode a
single-game assumption into the activity or the asset-path layout), whereas retrofitting a mode
selector after the fact would touch the launcher, asset paths and save paths — the same fields
D-6 says are expensive to change late.

**Concrete effect on Phase 2:**
- Activity and asset layout assume `<game>/` subdirectories, not a flat single-game data dir.
- Save path scheme includes the game key from the start.
- Native build wires one target now; the second slot exists and is empty.

**Unchanged:** Gate 5 (touch controls) is validated against TD only. RA inherits it.

**Asset note:** both titles' legacy data is already confirmed present on this machine — the RA
discs are the same freeware family (D-8, RECON.md). RA is therefore an enablement task, not a
sourcing task.

---

## D-14 · 2026-08-01 · Gate 6 gains a fold/unfold case — target device is a foldable

**Decision.** Gate 6's criterion is extended. Current text: *"Sleep the phone mid-mission, resume,
save, force-kill, relaunch, load — all clean."* Add: **fold and unfold mid-mission, both
directions, without crash or rendering corruption.**

**Rationale.** The target device is a Google Pixel 9 Pro Fold (`comet`), verified 2026-08-01. A
fold event changes display geometry at runtime and destroys/recreates the drawing surface — the
same code path as sleep/resume, but reachable at any moment during gameplay rather than only at a
lifecycle boundary. A normal phone never exercises this. Discovering it at Gate 6 with no criterion
covering it would mean either shipping a crash or reopening a closed gate.

**Supporting evidence.** `docs/reference/android-port-map.diff` contains a commit titled
*"reload screen resolution after every window resizing on Android"*. The existing port already hit
window-resize handling on ordinary devices; a foldable exercises that path far harder.

**Effect on Phase 5.** Touch geometry (drag-box, sidebar hit regions, integer scaling) must be
recomputed on surface change, not cached at startup. Recorded now so Phase 5 does not bake in a
fixed-geometry assumption that Phase 6 then has to unpick.

---

## D-15 · 2026-08-01 · NDK r26d pin VERIFIED safe against the 16 KB page hazard

**Decision.** The NDK 26.3.11579264 (r26d) pin stands.

**Risk that was checked, not assumed.** Android 15+ permits devices with a 16 KB kernel page size.
**NDK r26 does not align shared libraries to 16 KB by default; r27+ does.** On a 16 KB device, an
r26-built `.so` fails to load. That would have surfaced in Phase 3 as an opaque native-library load
failure with no obvious connection to the toolchain pin — an expensive false trail.

**Probe and result.** `adb shell getconf PAGE_SIZE` on the target device returns **4096**. The
hazard does not apply. VERIFIED, not assumed.

**Standing rule.** Re-run this probe whenever the target device changes. If any future target
reports 16384, the NDK pin must move to r27+ and `docs/TOOLCHAIN.md` updated by D-entry.

---

## D-16 · 2026-08-01 · Aspect handling — FILL is the shipping default, NATIVE stays switchable

**Decision.** `BoxingAspectRatio=auto` (fill the panel) is the default. `16:10` (true native
proportions, letterboxed) remains available and is switchable on-device with no rebuild.

**Rationale.** Michael reviewed both live on the target device, folded and unfolded, and chose
fill. Recorded rather than assumed: this is an aesthetic ruling, not a technical one, and the
trade-off is real — the game is natively 640x400 (exactly 16:10), so filling a 2.24:1 panel
stretches roughly 40% horizontally.

**Implementation.** `auto` is resolved inside `Update_HWCursor_Settings()`, which already runs on
every `SDL_WINDOWEVENT_SIZE_CHANGED`. The image therefore refits whenever the panel changes rather
than only at launch. Verified across a live fold with no relaunch:

```
folded    renderer output 2424x1080   dst 2424x1080 at 0,0
unfolded  renderer output 2152x1940   dst 2152x1940 at 0,0
```

**Reversibility is permanent and deliberate.** `CONQUER.INI` is read from the writable user path,
so `_cnc-scripts\aspect.cmd fill|native` swaps the file and relaunches in about two seconds. No
rebuild, no reinstall. This survives to Gate 7 and costs nothing to keep.

**Why this was done now rather than at Phase 7.** Michael asked for it before it became expensive,
and that instinct was correct. It cost ~20 lines because the scaling path was already centralised.
After Phase 5 it would have meant unpicking every touch hit-test built on a fixed-geometry
assumption.

---

## D-17 · 2026-08-01 · Cover-screen orientation defect WITHDRAWN — never independently verified

**Withdrawn.** The reported "landscape fails on the cover screen" is closed as **not a distinct
defect**.

**Provenance, stated plainly.** The claim originated in Michael's playtest report, not in any
measurement by the agent. The agent recorded it as an open item and offered `resizeableActivity`
as a **suspicion**, explicitly not a finding. No probe was ever run against it.

**Resolution.** Michael reports the cover screen honours landscape correctly after the
`SDL_RenderSetLogicalSize` revert (F-7). The manifest already carried `screenOrientation`
`sensorLandscape` **before** the failing playtest, so orientation was being applied throughout.
The regression rendered the game as a small stranded rectangle; on the narrow cover panel a
mis-scaled sliver is readily misread as an orientation fault.

**One root cause, two reported symptoms.** The agent should have connected them rather than
opening a second line of investigation off an unverified report.

**Standing rule.** A symptom reported once and not reproduced by measurement is a lead, not a
defect. Do not carry it as an open work item without a probe that confirms it, and never fix
against it — that is how effort gets spent on problems that do not exist.

---

## D-19 · 2026-08-02 · Touch pan direction is inverted (grab-the-map) by default

**Decision.** `Video.TouchPanInvert` defaults to `true` on Android. Sliding drags the map with the
finger. Desktop is unaffected.

**Rationale.** Settled by live A/B on the target device rather than by agent preference. The agent
had originally hardcoded slide-right-scrolls-right without surfacing that a choice existed; making
it a setting first, then having Michael feel both, is what produced the answer.

**Reversible.** `TouchPanInvert=false` in `CONQUER.INI`, no rebuild. Verified by deleting
`CONQUER.INI` from the device entirely so the compiled-in default is what runs — testing a pushed
override would only have proven the INI parser works.

---

## D-20 · 2026-08-02 · Gate 5 closed on input completeness; soak testing moved to Gates 6 and 7

**Decision.** Gate 5 closes on verified input completeness. The "play mission 1 to a win"
requirement is struck as a Phase 5 criterion.

**Rationale.** The criterion bundled an input test with a stability test. Every touch interaction
is verified; a full playthrough would additionally have exercised long-session stability and
many-unit performance, neither of which is an input concern. Keeping the gate open would have
gated a finished input layer on evidence about something else.

**Where the deferred coverage lands.** Gate 6 already tests lifecycle under real play. Gate 7
already requires a performance pass. Both are strengthened by absorbing this rather than weakened.

**Recorded risk.** If a crash or frame-rate collapse appears during extended play, it will surface
at Gate 6 or 7 rather than Gate 5. That is a sequencing change, not a reduction in coverage — but
it does mean nobody should read "Gate 5 passed" as "the port was soak-tested."

---

## D-21 · 2026-08-02 · Audio backend is openal-soft as a pinned submodule, built before the engine

**Decision.** `openal-soft` 1.24.3 is pinned as a submodule at `android/app/jni/openal-soft`
(commit `dc7d705`) and `OPENAL` is flipped `ON` for the Android build. The submodule is added to
CMake **before** the engine subdirectory, and `OPENAL_LIBRARY` / `OPENAL_INCLUDE_DIR` are seeded
as cache variables.

**Rationale for the ordering.** openal-soft defines `OpenAL::OpenAL` as an `ALIAS` target
(`openal-soft/CMakeLists.txt:1643`). The engine's `cmake/FindOpenAL.cmake` guards its own target
creation with `if(NOT TARGET OpenAL::OpenAL)` (line 142). Building openal-soft first means the
finder sees the real target and leaves it alone. Reverse the order and the finder wins, creating
an `UNKNOWN IMPORTED` target pointing at a library file that does not exist at configure time.

**Rationale for the seeded cache variables.** `FIND_PACKAGE_HANDLE_STANDARD_ARGS` still demands
both variables even when the target already exists, and `find_library()` cannot resolve a target
built in the same configure run. This is the identical pattern already used for
`SDL2_LIBRARY` / `SDL2_SDLMAIN_LIBRARY` — not a new mechanism.

**Verified this session (build evidence, not runtime evidence).**

| Claim | How it was checked | Result |
|---|---|---|
| openal-soft compiles for arm64-v8a | `libopenal.so` in `stripped_native_libs` | 3,623,976 bytes |
| It ships in the APK | zip listing of `app-debug.apk` | `lib/arm64-v8a/libopenal.so` present |
| The engine links against it | `llvm-readelf -d libvanillatd.so` | `NEEDED libopenal.so` |
| The OpenSL backend is compiled in | object listing under `alc/backends/` | `opensl.cpp.o`, 892,856 bytes |
| AL entry points are exported | `llvm-nm -D --defined-only` | `alcOpenDevice`, `alSourcePlay`, `alGenBuffers` |

**A false alarm worth recording.** `libopenal.so` carries no `NEEDED` entry for `libOpenSLES.so`,
which on its face looks like the null backend and silence. It is not: openal-soft `dlopen`s
`libOpenSLES.so` by name at runtime. The embedded string `libOpenSLES.so` and `dlopen@LIBC` as
the only related undefined symbol are the evidence. The lesson is the check, not the outcome —
"it compiled and packaged" would not have distinguished the two cases.

**Not yet verified.** No sound has been heard. Linking is not audio. Runtime device init,
`SOUNDS.MIX` effects, `SPEECH.MIX` EVA voice and `SCORES.MIX` music are all open until Michael
reports from the device.

**Asset dependency.** `SCORES.MIX` (37.3 MB) is still not on the device, so music cannot be judged
until it is pushed. Sound effects and EVA speech do not depend on it and can be judged first.

**No Java change.** `getLibraries()` still returns `{"SDL2","vanillatd"}`. The `NEEDED` entry makes
the dynamic linker load `libopenal.so` automatically; adding it to the list would be an invented
requirement (Law 1.10).

---

## D-22 · 2026-08-02 · Product identity settled: Command Post, with Dawn and Red · SUPERSEDES D-12

**Michael's ruling:** *"I do not want dev.pricharda.vc95 at all - fix it to something we just
discussed.... option 1 for me dawg!"*

| Field | Old (D-12) | New | Where |
|---|---|---|---|
| Package id | `dev.pricharda.vc95` | `dev.pricharda.commandpost` | `build.gradle` namespace + applicationId |
| Activity | `VC95Activity` | `CommandPostActivity` | Java class, package dir, manifest |
| Display name | `VC95` | **Command Post** | `strings.xml` `app_name` |
| Title 1 (TD) | — | **Dawn** | `strings.xml` `title_td` |
| Title 2 (RA) | — | **Red** | `strings.xml` `title_ra` |
| CMake project | `VC95` | `CommandPost` | `android/app/jni/CMakeLists.txt` |

**Trademark reasoning.** The distinctive half of EA's mark is **"Conquer," not "Command."**
"Command" alone is an ordinary English word EA cannot monopolise, and "Command Post" is a generic
military term in common usage. Neither the app name nor the package id contains an EA mark, which
is what EA's GPL v3 §7 additional terms require. The two titles are labelled **Dawn** and **Red** —
bare common words — not "Tiberian Dawn" and "Red Alert," which are EA marks.

**Rejected: "Com&Conq" and "CAC."** `Com&Conq` retains both initial syllables *and* the ampersand
of the EA mark, on a product that is that game; under a sight-sound-meaning comparison that is
close, and the intent to evoke is self-evident. `CAC` is less similar visually but in context still
functions as a pointer to the mark, and is weak branding besides. Neither bought anything that
"Command Post" does not.

**Timing.** D-12 warned that a late package-id change is expensive: it binds the signing identity
and `SDL_AndroidGetInternalStoragePath()`, so it moves save and config paths and forces a Gate 6
re-test. Doing it **now** — after Gate 6 part 1 (audio) and *before* Gate 6 part 2 (lifecycle) —
costs almost nothing, because no lifecycle result exists yet to invalidate and no saves exist
(Michael cannot save yet; see the open soft-keyboard defect). A week later this would have been
genuinely painful.

**Assets were moved, not re-pushed.** `cp -a` on device from the old external files directory to
the new one preserved all 18 MIX files including `MOVIES.MIX` at 449,080,410 bytes — byte-exact,
no 449 MB re-transfer. Old package uninstalled and old asset directory removed only *after* the
app was confirmed running under the new id.

**Verified end to end on device:**

```
Android: game data path is '/storage/emulated/0/Android/data/dev.pricharda.commandpost/files'
Android: user data path is '/data/data/dev.pricharda.commandpost/files'
OpenAL: device open, context current. Renderer 'OpenAL Soft', 16 bit, mono, 22050 Hz.
C&C95 - About to register MOVIES.MIX
C&C95 - About to play the intro movie
pm list packages | grep pricharda  ->  package:dev.pricharda.commandpost
```

No `FATAL` and no `AndroidRuntime` exception — which matters, because F-3 was a
`ClassNotFoundException` caused by exactly this kind of activity-name change.

**Historical records were not rewritten.** `docs/RECON.md` still contains `dev.pricharda.vc95` in
the F-3 crash log and the Gate 6 `dumpsys` capture. Those are verbatim captured evidence from the
time they were taken; editing them would falsify the record. D-12's table is marked SUPERSEDED
rather than overwritten, for the same reason.

**Now permanent.** A Play Store package id can never be changed after first publication. If this
ships, `dev.pricharda.commandpost` is forever.

---

## D-23 · 2026-08-03 · Red Alert enabled before any harder title is attempted

**Decision.** `BUILD_VANILLARA=ON` for Android. Red Alert now cross-compiles and ships in the APK
alongside Tiberian Dawn.

**Why this and not Generals.** Michael identified a real, working native Android port of Generals
Zero Hour (`tarek369/GeneralsZH-Android`, GPL v3) and proposed adopting it. It is genuine, but it
is a second project rather than an integration — see D-24. The multi-title shell had never hosted
more than one title, and Red Alert is the cheapest possible test of it: same engine family, so the
entire platform layer applies unchanged. **Law 3.5 — prove the pipe with a trivial artifact before
building the real one.** If the shell cannot host Dawn plus Red, it certainly cannot host a
DXVK/Vulkan/SDL3 stack, and that is far cheaper to learn now than after weeks of Generals work.

**Changes, all mirroring what `tiberiandawn/CMakeLists.txt` already does.** Three Android branches
added to `redalert/CMakeLists.txt`, no new mechanism invented:

| Branch | Reason |
|---|---|
| Skip `BuildIcons` / `make_icon` on Android | Android supplies its launcher icon from `res/mipmap` |
| `add_library(VanillaRA SHARED ...)` instead of `add_executable` | SDLActivity `dlopen()`s the engine; it is not an executable |
| Link `log` and `android` | `__android_log_print`, used by the `VCTD` evidence tag |

**Verified in the built binary, not merely that it compiled:**

| Claim | Probe | Result |
|---|---|---|
| Ships in the APK | zip listing | `lib/arm64-v8a/libvanillara.so`, 3,347,248 bytes |
| Entry point is correct | `llvm-nm -D --defined-only` | `T SDL_main` — **not** `T main` |
| Links the same stack as TD | `llvm-readelf -d` | `libSDL2.so`, `libopenal.so`, `liblog.so`, `libandroid.so` |
| Our platform layer is present | `llvm-strings` | `VCTD`, `SDL_StartTextInput`, `TOUCH_TWOFINGER` |

**The `SDL_main` check is not ceremonial.** F-6 was precisely this fault in Tiberian Dawn — the
symbol came out as `T main` and the app failed at launch with "Couldn't find function SDL_main".
Checking it on the second engine costs one command and would have caught a repeat.

**The load-bearing result.** `SDL_StartTextInput` and `TOUCH_TWOFINGER` appearing in
`libvanillara.so` means the touch gesture layer, the soft-keyboard hook, the audio backend and the
Android logging all apply to Red Alert **with no additional work**. Every platform-layer decision
from D-16 through D-21 was made in `common/`, and this is the proof that it paid off. D-13's claim
that the shell was designed for two titles is now demonstrated rather than asserted.

**NOT verified — Red Alert has never been run.** Two things still block it:

1. `CommandPostActivity.getLibraries()` still returns `{"SDL2", "vanillatd"}`. Nothing selects
   between engines yet. That is the title picker, the next step.
2. Red Alert's own MIX files are not on the device. Michael must supply them from his copy, as he
   did for Tiberian Dawn.

**Expansions come free.** Covert Operations (`CD_COVERTOPS`, `COVERTMOVIES.MIX` in
`tiberiandawn/conquer.cpp`), Counterstrike (`EXPAND.MIX`) and The Aftermath (`EXPAND2.MIX`, both
handled in `redalert/conquer.cpp:4642` and `:4665`) are data-only. They require no porting work at
all — only the user's files.

---

## D-24 · 2026-08-03 · Generals Zero Hour is a separate project, gated on a one-day spike

**Finding.** `tarek369/GeneralsZH-Android` is real: GPL v3, native arm64, DirectX 8 → DXVK →
Vulkan, and its README reports a full gameplay session working. Michael is correct that it exists
and that it is free to use. It is **not**, however, something that can be copied in.

**Why it is a second project, not an integration:**

| Fact | Consequence |
|---|---|
| v0.1 Alpha, released 2026-07-07 | Four weeks old. 4 stars, 0 forks, 1 open issue. |
| Tested only on OnePlus Pad 2 / **Adreno 830** | Target device is a Pixel 9 Pro Fold / **Mali-G715**. DXVK on Mali is untested by anyone. |
| Uses **SDL3** | This project uses SDL2. |
| DXVK requires Meson cross-compilation | An entire second build system. |
| `.big` archives | Different asset system from `.MIX`. |
| FFmpeg stubbed | No cutscenes. |
| Forked from an iOS port, from a macOS port, from TheSuperHackers | Four layers of attribution owed. |

**The hard blocker, which the README does not surface.** SDL2 and SDL3 both ship a Java class
named `org.libsdl.app.SDLActivity`. Same package, same class name, two incompatible versions.
**Two classes with one fully-qualified name cannot coexist in a single APK's dex.** This is a
collision, not a difficulty. It must be resolved before any Generals work begins, by one of:

1. Shading one library's Java package (invasive but contained)
2. Separate processes with separate dex (heavier, and the shared-dex constraint still applies)
3. Shipping Generals as a second APK (abandons the single-app goal)

**Ruling: spike before commitment.** One day, one question — *does DXVK initialise on Mali-G715?*
Nobody currently knows. If it does not, no amount of integration work matters, and everything
downstream of that answer is wasted effort until it is known. Kill-or-continue.

**Sequencing consequence.** Because Michael requires all titles present before signing (no second
signing pass), the Generals answer gates the release. The spike therefore comes before the
performance pass and long before signing.

---

## D-26 · 2026-08-03 · Exit-to-picker is hooked at the engine's exit, not at a gesture

**Michael's request:** *"the games will each need a way to exit back to menu and I propose the
2finger esc at main menu screen."*

**Implemented differently, and this needs stating plainly rather than glossed.** The requirement —
every title needs a route back to the picker — is met in full. The proposed *mechanism* is not what
was built, for a reason that is technical rather than preferential.

**Why the gesture cannot do it.** Two-finger tap already emits `KN_ESC` unconditionally (D-19 era
work). To make it *also* mean "exit the game", the platform layer would have to know the engine is
sitting on its main menu. It cannot: the touch handler lives in `common/wwkeyboard_sdl2.cpp` and
has no access to game state, and giving it that access would mean reading game logic from the
platform layer — the one line this port has held since Phase 3. Detecting "main menu" would also
be fragile, because the engine reuses the same menu machinery for several screens.

**What was built instead — `CommandPostActivity.finish()` is overridden.** When `SDL_main` returns,
SDL's own `SDLMain.run()` calls `mSingleton.finish()` (`SDLActivity.java:1894`). Overriding
`finish()` therefore intercepts the engine's exit by **every** route at once:

| Exit route | Caught? |
|---|---|
| Main menu's own **Exit** item (`SEL_EXIT`, `init.cpp:1291`) | yes |
| `Prog_End()` (`startup.cpp:540`) | yes |
| `SDL_QUIT` | yes |

Zero C++ changes. Zero game logic. One override rather than a hook duplicated into both
`tiberiandawn/` and `redalert/`.

**This is arguably better than the gesture, not merely easier.** The main menu already displays a
visible **Exit** item. Using it is discoverable by anyone who opens the game; a hidden two-finger
gesture is not. The engine's existing, correct shutdown path also runs — fades, `Sound_End()`,
mouse and palette teardown — where a gesture-triggered exit would have to either duplicate that or
skip it.

**The process hazard this exposed, and why the picker now runs in `:launcher`.** An engine library
loaded with `System.loadLibrary()` **cannot be unloaded**. If the game process survived a return to
the picker and the user then chose the other title, `libvanillara.so` would load into a process
already holding `libvanillatd.so`: two libraries both exporting `SDL_main`, two sets of engine
globals, and `SDLActivity`'s static singleton state left over from the previous run. That is a
corruption waiting to happen, not a theoretical concern.

Resolved declaratively. `LauncherActivity` is given `android:process=":launcher"`, and
`finish()` kills the game process after starting the picker. The picker therefore always survives,
never shares a process with an engine, and every title starts in a clean process.

**Verified on device:**

```
u0_a461  9057  dev.pricharda.commandpost:launcher
u0_a461  9466  dev.pricharda.commandpost
```

Two processes, running simultaneously and independently, exactly as designed.

**NOT verified.** The exit itself has not been exercised — that requires an awake device and a
human selecting **Exit** from a game's main menu. Process separation is proven; the round trip is
not.

**Open for Michael's ruling.** If he still wants the gesture specifically, the honest options are:
(1) accept the menu item, which is what is built; (2) bind the Android **back** gesture to exit,
which is idiomatic and needs no game state; or (3) add main-menu detection, which breaks the
platform-layer boundary and is not recommended.

---

## D-28 · 2026-08-03 · The three expansions are data, not titles — the picker stays at two entries

**Michael's question:** *"confirm that counterstrike and aftermath are separate titles from RA - if
so they do not belong with RED, they would be standalones ... COps is also standalone."*

**Confirmed from the engine source: they are not separate titles.** All three are expansion data
detected at runtime by their parent engine and surfaced through that engine's own menus. There is
no separate executable, no separate entry point, and nothing for the picker to launch.

**Counterstrike and The Aftermath — `redalert/conquer.cpp:4640` and `:4664`:**

```cpp
bool Is_Counterstrike_Installed(void) {
    CCFileClass file("EXPAND.MIX");
    bInstalled = file.Is_Available();
    return bInstalled && Options.CounterstrikeEnabled;
}

bool Is_Aftermath_Installed(void) {
    CCFileClass file("EXPAND2.MIX");
    ...
    return bInstalled && Options.AftermathEnabled;
}
```

These are file-presence checks *inside the Red Alert engine*, gated further by an in-game option
the player controls. Red Alert asks itself whether the expansion data exists.

**Covert Operations — `tiberiandawn/init.cpp:707` and `:883`:**

```
SEL_NEW_SCENARIO,   // Expansion scenario to play.
...
if (Expansion_Dialog()) {
```

Covert Operations is a **menu item inside Tiberian Dawn**. `Expansion_Dialog()`
(`tiberiandawn/expand.cpp:123`) scans for scenario `.INI` files, which live inside `SC-000.MIX` and
`SC-001.MIX`.

**Why making them picker entries would be actively wrong**, not merely redundant: each entry would
launch the identical engine binary against the identical data directory and produce an identical
game. It would also fragment the data — Red Alert without `EXPAND.MIX` in its own directory *loses*
the Counterstrike content, because the detection is a file check in that directory. Splitting them
out would remove content rather than add a title.

**The picker therefore stays at two entries: Dawn and Red.** The expansions appear where the
original games put them — inside each game's menus.

**Delivered this session.**

| Expansion | Parent | File(s) | Location |
|---|---|---|---|
| Counterstrike | Red Alert | `EXPAND.MIX` (458,242 bytes) | `files/vanillara/` |
| The Aftermath | Red Alert | `EXPAND2.MIX` (469,922 bytes) | `files/vanillara/` |
| Covert Operations | Tiberian Dawn | `SC-000.MIX` (185,937), `SC-001.MIX` (490,318) | `files/vanillatd/` |

Source: Michael's licensed Remastered Collection — `RED_ALERT/AFTERMATH/` and
`TIBERIAN_DAWN/CD3/`.

**Known limitation, stated rather than hidden.** Covert Operations shipped its own `MOVIES.MIX`
(194 MB) and `SCORES.MIX` (75 MB) on CD3, containing that expansion's cutscenes and music. Those
were **not** pushed: they share filenames with Tiberian Dawn's existing files, and overwriting
would discard the GDI and NOD movies. The Covert Operations *missions* are playable; its cutscenes
and its extra music tracks are not present. Resolving that properly needs per-campaign data
handling, which is scope nobody has asked for.

**All three titles are now content-complete** for what the engines can reach from a single data
directory each.
