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
