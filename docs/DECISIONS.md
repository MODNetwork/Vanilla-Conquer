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
