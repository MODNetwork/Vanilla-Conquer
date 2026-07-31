# BUILD_PLAN.md — cnc-td-android
<!-- v1.1 - Phase 1 / Gate 1 amended per D-1 (Windows native + WSL2 canary).
     Effort ledger SUPERSEDED per the 2026-07-07 reset. Phases and gates otherwise as authored. -->

**Destination:** repo root of the Vanilla-Conquer fork.
Seven phases, eight gates. No phase advances without its gate PASSING on the target device
(Gates 0–1 excepted: desktop). Hours are authoring estimates; verification time is Michael's
and additive.

**Upstream:** `TheAssemblyArmada/Vanilla-Conquer`
**Reference doctrine source:** `ammaarreshi/Generals-Mac-iOS-iPad`

---

## PHASE 0 — Ecosystem recon (1–2 hrs) — the step that saved Reshi months

1. Search GitHub for existing Android efforts: forks of Vanilla-Conquer with `android`
   branches, SDL2 C&C ports, OpenRA Android issues. Check Vanilla-Conquer issues/PRs for
   prior Android discussion.
2. Verify claims against artifacts (CI configs, releases), not READMEs.
3. Download `PORTING_PLAYBOOK.md` + `PORTING_PATTERNS.md` from
   `ammaarreshi/Generals-Mac-iOS-iPad` into `docs/reference/`.
4. Confirm current legal source for TD assets (C&C Gold freeware files vs. Steam Remastered
   `.MIX` extraction) and document the user-supply procedure. Do not assume; verify.
   **PARTIAL PRE-CLEARANCE — see docs/RECON-SEED.md.** The host machine already carries a
   licensed C&C Remastered Collection whose `DATA\CNCDATA\TIBERIAN_DAWN` tree holds the
   original legacy `.MIX` set. Directory listing is VERIFIED; that Vanilla-Conquer consumes
   exactly this set is ASSUMPTION until checked against the upstream README/asset table.
5. Record findings in `docs/RECON.md`.

**GATE 0:** `RECON.md` exists; base strategy confirmed or revised; asset procedure documented.
If a working Android fork already exists, **STOP and re-plan from it.**

---

## PHASE 1 — Desktop baseline (1–2 hrs authoring) — AMENDED PER D-1

1. Fork + clone.
2. Build TD **on the Windows host**: SDL2 + OpenAL preset, `-DBUILD_VANILLARA=OFF`,
   `-DNETWORKING=OFF`. This is the playtest loop.
3. Build TD **in WSL2** from the same tree: the porting canary (clang + POSIX +
   case-sensitive FS). Any bug reproducing in WSL2 but not Windows is an Android-class bug
   and is fixed before Phase 3.
4. Place assets per RECON.md procedure; launch; play 5 minutes of GDI mission 1.
5. Tag this commit `baseline-desktop`.

**GATE 1:** TD runs on the Windows host from this tree, and the WSL2 build compiles clean.
This is the reference implementation every later bug gets compared against.

---

## PHASE 2 — Android scaffold (2–4 hrs)

1. Install Android Studio CLI tooling / SDK / NDK (r26+); confirm `adb devices` sees the phone.
2. Import SDL2 `android-project` template into `android/`; SDL2 source builds via Gradle
   externalNativeBuild + CMake.
3. Stub main: SDLActivity launches, clears screen to a solid color.
4. Wire `versionCode`, package id (personal namespace, no MOD OS/TCG identifiers),
   arm64-v8a only ABI filter.
5. Record every pinned version into `docs/TOOLCHAIN.md`, including the min-API decision.

**GATE 2:** Colored-screen APK installs and launches on device via `adb install`.
Lifecycle survives home/resume. `docs/TOOLCHAIN.md` is filled and committed.

---

## PHASE 3 — Engine cross-compile (3–6 hrs — the grind phase)

1. Add Vanilla-Conquer tree to the Android CMake build; `BUILD_VANILLATD=ON`, `SDL2=ON`,
   `NETWORKING=OFF`, `BUILD_VANILLARA=OFF`.
2. Resolve per-platform breakage behind `#ifdef __ANDROID__`: filesystem paths, endianness
   assumptions (unlikely — ARM64 LE), missing headers, OpenAL → link openal-soft for Android.
3. Redirect all writable paths (config, saves, cache) to `SDL_AndroidGetInternalStoragePath()`.
4. Verify artifact: `readelf -h` shows AArch64; `nm -u` clean.
   **Run the case-collision and include-case audit here** (Windows FS hides case bugs Android
   exposes).
5. Boot on device with assets absent → expect graceful "assets missing" path, not crash.

**GATE 3:** Engine boots to its asset-check/main-menu code path on device (logcat-verified,
tag `VCTD`).

---

## PHASE 4 — Asset pipeline (1–2 hrs)

1. First-run flow: detect empty data dir, surface the path (toast/log), user pushes assets via
   `adb push` or file manager to app-private external dir.
2. Case-sensitivity audit on asset filenames (Android FS is case-sensitive; DOS-era code may
   assume otherwise).
3. Load main menu with real assets; then load GDI mission 1.

**GATE 4:** Main menu renders with real assets; mission 1 loads. Screenshot evidence.

---

## PHASE 5 — Touch controls (3–5 hrs)

Design (mirrors the proven Generals scheme, adapted to TD's simpler needs):

- Tap = left-click select / order
- Drag from empty ground = box select
- Long-press = right-click equivalent (deselect/context)
- Two-finger drag = camera pan; edge-scroll disabled
- Pinch = zoom if supported by renderer scaling, else omitted (TD is fixed-res; implement
  integer scaling + pan instead)
- Sidebar buttons: enlarge hit targets by transparent touch-region overlay, not sprite edits

Implementation: translation layer in `android/` feeding synthesized SDL mouse events into the
existing wwmouse/wwkeyboard path. Game logic untouched. Ships with a **raw-passthrough debug
toggle** that bypasses gesture recognition and sends raw single-touch as plain SDL mouse
events — reproduces in raw mode = engine/SDL problem (W1); clean in raw mode = gesture layer
problem (W2).

**GATE 5:** Full GDI mission 1 playable start-to-finish by touch alone: build base, train
units, box-select, attack-move, win.

---

## PHASE 6 — Audio + lifecycle (2–3 hrs)

1. OpenAL device init on Android; music + SFX + EVA voice verified.
2. Pause/resume: audio suspends and restores; GL/SDL surface recreated on resume without crash.
3. Save/load works across app kill.

**GATE 6:** Sleep the phone mid-mission, resume, save, force-kill, relaunch, load — all clean.

---

## PHASE 7 — Packaging + polish (2–3 hrs)

1. Release keystore, signed APK, `adb install -r` clean on a second device profile if available.
2. DPI/aspect handling: letterbox or integer-scale decision locked and documented.
3. Performance pass: sustained 60fps or documented floor; battery/thermal sanity check.
4. README for the fork: lineage credits (EA GPL release, Vanilla-Conquer, SDL), GPL v3 notice,
   bring-your-own-assets instructions. Publish decision (public fork vs. private) is Michael's
   call — GPL obligations trigger on distribution.

**GATE 7 (FINAL):** Signed APK + written install/asset procedure; a cold install on a clean
device reaches a playable mission in under 10 minutes of user effort.

---

## Effort ledger — SUPERSEDED 2026-07-07

The original per-phase ledger (15–27 hrs authoring + 8–12 hrs verification) padded every phase
independently and counted verification as serial when much of it overlaps authoring. Replaced by:

| Scenario | Total attended time | Assumes |
|---|---|---|
| Base case | 12–18 hrs | Toolchain cooperates; SDL2 Android template works as documented; OpenAL links clean |
| Working target | ~15 hrs | Realistic midpoint (Reshi parity) |
| Tail risk | 25–30 hrs | NDK/Gradle toolchain hell, OpenAL-on-Android audio fight, or an unknown from being first on Android with this codebase |

**Gates 2 and 3 carry ~80% of the schedule variance.** Clear Gate 3 inside the first 4–5 hours
and you are in the base case; the rest is touch controls and polish.

Budget for heavy agent-compute burn — this class of task exhausted a full Max quota in two days
in the reference precedent.

Wall clock: one hard weekend, or 5–7 evenings.
