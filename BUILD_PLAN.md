# BUILD_PLAN.md — cnc-td-android
<!-- v2.0 - re-based on Gate 0 findings under Michael's Option B ruling (D-7).
     v1.1 phases/gates preserved where still valid. Effort re-priced. -->

**Destination:** repo root of the Vanilla-Conquer fork (`MODNetwork/Vanilla-Conquer`,
cloned at `C:\DEV\cnc-td-android`, branch `vanilla`).

**Strategy (D-7, Option B):** clean fork of true upstream. The existing `sandstranger:android`
port is read as a **map**, never merged. Our implementation is authored on our tree, which keeps
`platform-layer-only diffs vs upstream` intact and keeps upstream merges one command away.

Seven phases, eight gates. No phase advances without its gate PASSING on the target device
(Gates 0–1 excepted: desktop). Hours are authoring estimates; verification time is Michael's
and additive.

**Reference doctrine:** `docs/reference/` — Reshi playbook + patterns, and
`android-port-map.diff` (the 17-file route). Read `docs/reference/README.md` first.

---

## PHASE 0 — Ecosystem recon · **CLOSED 2026-07-31 · GATE 0 PASSED**

Findings in `docs/RECON.md`. Summary of what recon bought:

- A working Android port of this exact engine exists (`sandstranger:android`, 31 commits ahead of
  upstream, 0 behind, shipping signed arm64-v8a APKs). The v1 premise *"no Android target exists;
  we are first"* was false.
- That port solves **compile and boot**. It contains **no touch layer** and no `android/`
  directory. Gate 5 is untouched by anyone.
- Upstream license VERIFIED as GPL v3 + EA additional terms (D-5). EA trademark clause constrains
  app name and package id (D-6, `docs/LICENSING.md`).
- Asset source located on host; legacy `.MIX` trees for both TD and RA present.

**Residual Phase 0 item, carried into Phase 1 step 0:** confirm Vanilla-Conquer consumes the
legacy `.MIX` set found on the host. Still ASSUMPTION. Probe: upstream README asset table.

---

## PHASE 1 — Desktop baseline (1–2 hrs authoring) — per D-1

0. Read `docs/reference/README.md` and skim `android-port-map.diff`. Know the route before driving it.
1. Confirm the asset format assumption above. Document the user-supply procedure in `docs/RECON.md`.
2. Build TD **on the Windows host**: SDL2 + OpenAL, `-DBUILD_VANILLARA=OFF -DNETWORKING=OFF`.
   This is the playtest loop.
3. Build TD **in WSL2** from the same tree: the porting canary (clang + POSIX + case-sensitive FS).
   Any bug reproducing in WSL2 but not Windows is an Android-class bug, fixed before Phase 3.
4. Place assets per the documented procedure; launch; play 5 minutes of GDI mission 1.
5. Tag `baseline-desktop`.

**GATE 1:** TD runs on the Windows host from this tree, and the WSL2 build compiles clean.
Reference implementation for every later bug.

*Prerequisite flagged by host audit (D-3): CMake, Ninja and a compiler toolchain are not yet on
this machine. Phase 1 begins with installs.*

---

## PHASE 2 — Android scaffold (2–4 hrs)

1. **Install the Android toolchain** (D-3: none present). JDK 17, SDK cmdline-tools, NDK r26+,
   build-tools, platform-tools. Plain PowerShell window, never a pane.
2. **Choose the neutral app name and package id NOW (D-6).** No EA marks: not "Command &
   Conquer", "C&C", "Tiberian Dawn", "Red Alert". No MOD OS / TCG identifiers. This binds the
   signing identity and `SDL_AndroidGetInternalStoragePath()`; changing it after Gate 6
   invalidates installs and save paths.
3. Import SDL2 `android-project` template into `android/`; SDL2 builds via Gradle
   externalNativeBuild + CMake.
4. Stub main: SDLActivity launches, clears screen to a solid colour.
5. Wire `versionCode`, package id, arm64-v8a-only ABI filter.
6. Fill `docs/TOOLCHAIN.md` completely, including min-API. Pin everything.

**GATE 2:** Colored-screen APK installs and launches via `adb install`. Lifecycle survives
home/resume. `docs/TOOLCHAIN.md` committed and complete.

---

## PHASE 3 — Engine cross-compile (2–4 hrs — **DERISKED, was the grind phase**)

The map says this is 17 files. Work them in the order the route suggests, one variable at a time.

1. Add the engine tree to the Android CMake build: `BUILD_VANILLATD=ON`, `SDL2=ON`,
   `NETWORKING=OFF`, `BUILD_VANILLARA=OFF`.
2. Work the known-hostile files behind `#ifdef __ANDROID__`. Route from
   `docs/reference/android-port-map.diff`:
   - `common/paths_posix.cpp` — writable paths
   - `common/rawfile.cpp`, `common/load.cpp`, `common/straw.cpp` — file I/O, save/load
   - `common/soundio_openal.cpp` — OpenAL on Android, link openal-soft
   - `common/video_sdl2.cpp`, `common/video.h` — surface, resolution reload on window resize
   - `common/debugstring.cpp` — logcat output, **tag `VCTD`**
   - `common/settings.cpp`, `common/connect.cpp`
   - `CMakeLists.txt`, `tiberiandawn/CMakeLists.txt`, `tiberiandawn/startup.cpp`
3. Redirect all writable paths (config, saves, cache) to `SDL_AndroidGetInternalStoragePath()`.
4. Verify artifact: `readelf -h` shows AArch64; `nm -u` clean. **Run the case-collision and
   include-case audit here** — Windows FS hides case bugs Android exposes.
5. Boot on device with assets absent → graceful "assets missing" path, not a crash.

**GATE 3:** Engine boots to its asset-check/main-menu code path on device, logcat-verified,
tag `VCTD`.

*Note: the map's author recorded "tiberian dawn is running on Android with tons of bugs and
crashes" at this stage. Expect the same. Gate 3 is a boot gate, not a quality gate.*

---

## PHASE 4 — Asset pipeline (1–2 hrs)

1. First-run flow: detect empty data dir, surface the path (toast + `VCTD` log), user pushes
   assets via `adb push` or file manager to the app-private external dir.
2. Case-sensitivity audit on asset filenames. Android FS is case-sensitive; DOS-era code is not.
3. Load main menu with real assets, then GDI mission 1.

**GATE 4:** Main menu renders with real assets; mission 1 loads. Screenshot evidence.

---

## PHASE 5 — Touch controls (4–7 hrs) — **THE PROJECT**

Nobody has built this. The existing port ships a generic virtual mouse/keyboard overlay shared
across fifteen unrelated games. An RTS needs a native scheme. This phase is the reason the
project still exists after Gate 0, and it carries the full remaining budget.

Scheme:

- Tap = left-click select / order
- Drag from empty ground = box select (marquee)
- Long-press = right-click equivalent (deselect / context)
- Two-finger drag = camera pan; edge-scroll disabled
- Pinch = zoom if the renderer supports scaling; TD is fixed-res, so implement integer scaling + pan
- Sidebar: enlarge hit targets via a transparent touch-region overlay, **not** sprite edits

Implementation: translation layer in `android/` synthesizing SDL mouse events into the existing
`wwmouse` / `wwkeyboard` path. **Game logic untouched.** Ships with a **raw-passthrough debug
toggle** that bypasses gesture recognition and sends raw single-touch as plain SDL mouse events.
Bisect rule: reproduces in raw mode = engine/SDL problem (W1); clean in raw mode = gesture layer
problem (W2).

**GATE 5:** Full GDI mission 1 playable start to finish by touch alone — build base, train units,
box-select, attack-move, win.

---

## PHASE 6 — Audio + lifecycle (1–3 hrs — partly mapped)

1. OpenAL device init on Android; music + SFX + EVA voice verified.
2. Pause/resume: audio suspends and restores; GL/SDL surface recreated on resume without crash.
   *(Map covers window-resize resolution reload.)*
3. Save/load across app kill. *(Map covers "fix games saving" and home-dir paths.)*

**GATE 6:** Sleep the phone mid-mission, resume, save, force-kill, relaunch, load — all clean.

---

## PHASE 7 — Packaging + licensing (2–3 hrs)

1. Release keystore (never in repo), signed APK, `adb install -r` clean on a second device profile.
2. DPI/aspect handling: letterbox or integer-scale decision locked and documented.
3. Performance pass: sustained 60fps or a documented floor; battery/thermal sanity check.
4. **Licensing compliance per `docs/LICENSING.md` — not optional:**
   - `License.txt` ships with the build; EA copyright notices preserved
   - Build marked as a **modified version** (GPL v3 §5a and EA §7)
   - Corresponding source available for the exact build conveyed (§6)
   - `THIRD-PARTY-LICENSES.txt` + in-app credits: EA/Vanilla-Conquer, SDL2, openal-soft,
     **and `sandstranger` where our implementation follows the map**
   - **VERIFY openal-soft's licence** before shipping; if LGPL, satisfy the relinking obligation
     (dynamic `.so`, not static)
   - No EA trademark in app name, package id, icon or listing
5. Fork README: lineage credits, GPL v3 notice, bring-your-own-assets instructions.

**GATE 7 (FINAL):** Signed APK + written install/asset procedure; cold install on a clean device
reaches a playable mission in under 10 minutes of user effort; licensing checklist fully green.

Publish decision is Michael's — GPL obligations trigger on conveyance, not on local commits.

---

## Effort ledger — v2.0, re-priced after Gate 0

| Phase | v1.1 estimate | v2.0 | Why changed |
|---|---|---|---|
| 0 | 1–2 hrs | **done** | Closed 2026-07-31 |
| 1 | 1–2 hrs | 1–2 hrs | Unchanged, plus toolchain installs |
| 2 | 2–4 hrs | 2–4 hrs | Unchanged; naming decision pulled forward |
| 3 | **3–6 hrs** | **2–4 hrs** | Derisked: 17-file route known |
| 4 | 1–2 hrs | 1–2 hrs | Unchanged |
| 5 | 3–5 hrs | **4–7 hrs** | Full weight. The differentiated work. |
| 6 | 2–3 hrs | 1–3 hrs | Partly mapped |
| 7 | 2–3 hrs | 2–3 hrs | Licensing work added |
| **Total authoring** | 15–27 hrs | **13–25 hrs** | |

**Risk profile is inverted from v1.1.** Gates 2 and 3 previously carried ~80% of schedule
variance; a shipping third-party APK proves that path exists and the map names the files. The
remaining risk sits in Gate 5, which is design risk, not toolchain risk — bounded, iterative, and
visible every time you pick up the phone.

Budget for heavy agent-compute burn regardless.

Wall clock: one weekend, or 4–6 evenings.
