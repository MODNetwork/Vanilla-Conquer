# RECON.md — cnc-td-android Phase 0 (Ecosystem recon)

**Run:** 2026-07-31 · **Method:** GitHub REST API via `gh` 2.96.0, authenticated.
**Discipline:** every claim below is verified against an artifact (submodule config, branch
comparison, release asset), not a README. Per BUILD_PLAN Phase 0 step 2.

---

## GATE 0 RESULT: **STOP CONDITION TRIGGERED**

BUILD_PLAN Phase 0 step 5 reads: *"If a working Android fork already exists, STOP and re-plan
from it."*

**A working Android port of Vanilla-Conquer exists, ships signed arm64-v8a APKs, is actively
maintained, and covers both Tiberian Dawn and Red Alert.**

Phase 1 is NOT started. The plan re-bases on Michael's ruling.

---

## The evidence chain

### 1. Upstream baseline (context)

| Field | Value |
|---|---|
| Repo | `TheAssemblyArmada/Vanilla-Conquer` |
| Default branch | `vanilla` (NOT master/main) |
| Forks | 81 |
| Stars | 456 |
| Open issues | 329 |
| Last push | 2026-07-16 |
| GitHub-detected license | `NOASSERTION` — see Open Question 1 |

### 2. No Android discussion upstream

- `gh search issues --repo TheAssemblyArmada/Vanilla-Conquer android --include-prs` → **0 results**
- Control query (`sdl`, same repo, same flags) returned 8 hits, so the search path is proven
  working and the zero is a real finding, not a broken command.

**Reading:** upstream has never discussed Android. The port happened entirely out-of-tree.

### 3. The existing port — `sandstranger/com.mobilerpgpack.phone`

Multi-game Android launcher ("Classic Core"), Kotlin + Gradle + CMake, 82 stars, last push
2026-07-29. Its own description names Vanilla-Conquer among its ports.

**Artifact proof it vendors our engine** — from its `.gitmodules`:

```
[submodule "app/src/main/jni/Vanilla-Conquer"]
	path = app/src/main/jni/Vanilla-Conquer
	url = https://github.com/sandstranger/Vanilla-Conquer.git
```

Supporting stack visible in the same file: SDL3 + `sdl2-compat`, `openal-soft`, Google `oboe`,
`NG-GL4ES` / `gl4es` (GL→GLES translation), `sse2neon`, FFmpeg, libpng/jpeg-turbo.

### 4. The engine fork — `sandstranger/Vanilla-Conquer`

| Field | Value |
|---|---|
| Parent | `TheAssemblyArmada/Vanilla-Conquer` (confirmed `fork: true`) |
| Branches | `android`, `master`, `vanilla`, `feature/steam-support`, `feature/readme-rooms` |
| Last push | 2026-07-27 |

**Branch comparison, `TheAssemblyArmada:vanilla` → `sandstranger:android`:**

```
status: ahead   ahead_by: 31   behind_by: 0
```

Fully current with upstream **and** carrying 31 Android commits. Not a stale experiment.

**Commit subjects (first 14 of 31), verbatim:**

```
engine compiles for Android
tiberian dawn is running on Android with tons of bugs and crashes
red alert Android support
Made sha size not hardcoded
If there is an error bytesread can be 0 and ferror will cause a loop
Adding required header file
Change connect code to be the same as tiberian
Read sha after writing for unknown reasons
logs writing to logcat
fix games saving
fix path to home dir
fix compiling
reload screen resolution after every window resizing on Android
allow to change game values from jna
```

### 5. Shipped artifacts — the proof it actually runs

`gh release list --repo sandstranger/com.mobilerpgpack.phone`:

| Tag | Published |
|---|---|
| **3.1.10 (latest)** | 2026-07-29 |
| 3.1.9 | 2026-07-28 |
| 3.1.8 | 2026-07-27 |
| 3.1.7 | 2026-07-26 |

Latest release asset:

```
Classic_Core_google_release_arm64-v8a_v3.1.10_b111_2026_07_29_14_44.apk
```

**arm64-v8a** — our exact target ABI. Build 111. Releases cadence roughly daily.

---

## What this does to BUILD_PLAN

Mapping their 31 commits onto our gates:

| Our gate | Their status |
|---|---|
| Gate 2 — APK boots | Solved (shipping APKs) |
| Gate 3 — engine cross-compiles | Solved (*"engine compiles for Android"*) |
| Gate 4 — assets + menu + mission loads | Solved (*"tiberian dawn is running on Android"*) |
| Gate 5 — touch controls | **Unknown — not verified. Their scheme may be a generic virtual-gamepad overlay, not RTS-native.** |
| Gate 6 — audio + lifecycle | Partly solved (*"fix games saving"*, *"reload screen resolution after window resizing"*, oboe + openal-soft present) |
| Gate 7 — signed APK | Solved for their launcher, not for a standalone C&C app |

The original 15-hour working target was priced against being first on Android with this
codebase. That premise is dead. The remaining differentiated work is Gate 5 (RTS touch controls)
and packaging a dedicated single-game app rather than a 15-game launcher.

---

## Open questions — VERIFY BEFORE ANY RE-PLAN COMMITS

1. ~~Upstream license reads `NOASSERTION`.~~ **CLOSED 2026-07-31.** Read `License.txt` (714
   lines) in the clone directly. It is **GPL v3 with EA ADDITIONAL TERMS per GNU GPL Section 7**.
   GitHub's classifier failed on the EA preamble, not on a missing license. The GPL v3 assumption
   is now VERIFIED. The additional terms are binding and are recorded in the new section
   "EA additional terms" below.
2. **Their touch control scheme is unexamined.** If it is a generic overlay rather than
   tap-select / drag-box / long-press, Gate 5 is still the real project and still worth doing.
   Probe: read their `app/src/main/jni/Vanilla-Conquer` android branch input code.
3. **Their repo commits `release-keystore.jks` and `release-keystore.properties` to source
   control.** Noted as an anti-pattern; our keystore wall stands. Do not copy their packaging
   practice.
4. **Fork-of-a-fork vs. platform-layer-only-diff law.** CLAUDE.md requires minimal
   platform-layer-only diffs *against upstream*. Basing on `sandstranger:android` puts us
   downstream of a downstream and complicates that constraint. Ruling needed.

---

## Asset source — Phase 0 step 4 · **ASSUMPTION KILLED 2026-07-31**

The `docs/RECON-SEED.md` pre-clearance was **WRONG** and is superseded. Upstream's own README
(read in-tree, lines 108–116) states:

> For Tiberian Dawn the final freeware **Gold CD release** (GDI / NOD ISOs on ModDB) works fine.
> For Red Alert the freeware **CD release** works fine as well. The official Red Alert demo is
> also fully playable.
> **"While it is possible to use the game data from the Remastered Collection, The Ultimate
> Collection or The First Decade they are currently _not_ supported."**
> "Any repackaged version that you may already have from any unofficial source is _not_ supported."

The host's C&C Remastered Collection legacy `.MIX` trees are therefore an **unsupported** data
source, despite physically containing files with the right names.

### Why this matters more for a port than for normal play

"Not supported" does not mean "will not run." It means upstream will not accept bug reports
against it, and the README specifically warns the failure mode is **data-related bugs: invisible
objects, crashes on particular units.**

Gate 1's entire purpose is to be the reference implementation *"every later bug gets compared
against."* A baseline built on unsupported data is a **poisoned baseline**: when something breaks
on Android at Gate 4 or 5, we cannot distinguish "our port broke it" from "the data was never
supported." That destroys the diagnostic value of the desktop baseline and violates the
one-variable-per-attempt discipline before we have even started.

### Ruling for the plan

**Use the freeware Gold CD release for TD.** EA released it as freeware in 2007. It is free,
legal, supported upstream, and keeps the baseline diagnostic. The Remastered install stays on the
machine as a licensed fallback for comparison only, never as the Gate 1 data source.

Red Alert (post-Gate-7 per D-2) uses the freeware RA CD release or the official RA demo.

**ACTION REQUIRED (Michael):** obtain the freeware TD Gold GDI + NOD ISOs. Links are in the
upstream README. Downloading game data is Michael's action, not the agent's.

No asset has entered the repo. The asset wall holds.

## Reference doctrine — Phase 0 step 3

`PORTING_PLAYBOOK.md` + `PORTING_PATTERNS.md` from `ammaarreshi/Generals-Mac-iOS-iPad` are
**NOT yet pulled** into `docs/reference/`. Deliberately deferred: if the re-plan bases on
`sandstranger:android`, the more valuable reference doctrine is that branch's own 31-commit
diff, which is a direct map of the exact problems on the exact engine. Ruling on the re-plan
determines which doctrine gets imported.

---

## EA additional terms (GPL v3 §7) — binding on any distribution

Read verbatim from `License.txt` in the clone, 2026-07-31. These constrain Gate 7 and the publish
decision. Paraphrased, with the operative obligations named:

1. **No trademark or publicity rights are granted.** No right, title or interest in
   "Command & Conquer" or any other EA trademark. **You may not distribute any modification of
   this program using any Electronic Arts trademark**, or claim any affiliation with EA.
   *Direct consequence:* the Android app name, package id, store listing and icon **cannot use
   "Command & Conquer", "C&C", "Tiberian Dawn", "Red Alert", or EA marks.* This is a harder
   constraint than the existing MOD OS / TCG entity wall, and it lands on exactly the same
   fields. `docs/TOOLCHAIN.md` app-naming and the Phase 2 package-id step must respect it.
2. **Copyright notice and terms must travel with any propagation or conveyance.**
3. **Indemnification:** assuming contractual liability to recipients obliges you to indemnify EA
   for that liability.
4. **No misrepresentation of origin.** Modified versions **must be marked as modified** and not
   identified as the original program.
5. Warranty disclaimer supplements the GPL's own.

**Effect on this project:** EA released only `TiberianDawn.dll` and `RedAlert.dll` and their
corresponding source under this grant. Code is free to modify and convey under GPL v3 subject to
the above. **Assets remain EA copyright and are not covered** — the existing asset wall is
therefore a legal requirement, not merely a hygiene rule.

---

## ASSET PROCEDURE — VERIFIED 2026-08-01 (closes Phase 0 step 4 / Phase 1 step 1)

### Source, confirmed present on the host

The supported freeware C&C Gold Win95 discs are mounted:

| Drive | Volume label | Size | Identified by |
|---|---|---|---|
| `F:` | **GDI95** | 608,675,840 | `SETUP.INI` → `AppName=Command & Conquer Windows 95 Edition` |
| `J:` | **NOD95** | 592,164,864 | same release, NOD disc |

This is the source upstream names as supported (README: *"the final freeware Gold CD release
(GDI, NOD) works fine"*). It is **not** the Remastered Collection data, which D-8 rules out.

### Files taken

From the disc root: `AUD.MIX`, `CONQUER.MIX`, `DESERT.MIX`, `GENERAL.MIX`, `MOVIES.MIX`,
`SCORES.MIX`, `SOUNDS.MIX`, `TEMPERAT.MIX`, `WINTER.MIX`.
From `\INSTALL\`: `CCLOCAL.MIX`.

Total 10 files, 498,059,494 bytes.

### Staging location — OUTSIDE THE REPO

```
C:\DEV\_cnc-run\        <- run directory, NOT under C:\DEV\cnc-td-android
    vanillatd.exe       (copied from build-win\RelWithDebInfo)
    SDL2.dll
    *.MIX               (the 10 files above)
```

A separate run directory is used deliberately rather than dropping assets into `build-win\`.
`build-win/` sits inside the repo working tree, and although it is gitignored, the asset wall is a
**licence requirement** (EA retains copyright on game data; only the engine source is GPL v3).
Keeping game data on a path that has no relationship to the repo makes accidental commit
structurally impossible rather than merely unlikely.

### Result

`vanillatd.exe` launched from `C:\DEV\_cnc-run` and **stayed resident at ~92 MB RSS**, i.e. it
located and loaded the MIX data rather than exiting on missing assets. Two instances confirmed via
`tasklist`, then terminated.

**NOT YET VERIFIED:** that it renders correctly and is playable. Agents cannot see the screen.
Gate 1 requires Michael's playtest of 5 minutes of GDI mission 1 — HUMAN-GATE, per CLAUDE.md.

### Files NOT present on the discs, deferred until proven necessary

`LOCAL.MIX`, `SPEECH.MIX`, `TRANSIT.MIX`, `UPDATE*.MIX`, `SC-*.MIX` do not exist on the disc roots;
they are packed inside the InstallShield payload (`SETUP.Z`). `CCLOCAL.MIX` was taken from
`\INSTALL\` in their place. The engine started without them. If Michael's playtest surfaces missing
speech, missing mission text, or absent campaign scenarios, extract `SETUP.Z` and add them —
do not pre-emptively run the Win95 installer.

---

## FAILURE LEDGER F-1 · 2026-08-01 · Gate 1 FAIL — black screen after intro

**Symptom (Michael, verbatim):** *"fail game does not start, black screen after intro"*

**What the symptom ruled OUT immediately.** The intro played. That proves the SDL2 window, the
video/VQA decoder, the palette path and rendering all work. The failure is specifically at the
transition from intro to main menu, not at startup.

**Process state:** `tasklist` showed no `vanillatd.exe` after the report — it exited rather than
hanging. A crash, not a deadlock.

**Root cause — found by reading the engine, not by guessing.** `tiberiandawn/init.cpp` registers
several MIX files with **no `Is_Available()` guard**:

| Line | Call |
|---|---|
| 199 | `new MFCD("UPDATE.MIX")` |
| 200 | `new MFCD("UPDATA.MIX")` |
| 202 | `new MFCD("UPDATEC.MIX")` + `MFCD::Cache("UPDATEC.MIX")` |
| 206 | `new MFCD("LANGUAGE.MIX")` |
| 419 | `new MFCD("TRANSIT.MIX")` |

Contrast line 449, where `SPEECH.MIX` **is** guarded by `CCFileClass(...).Is_Available()` — which
is why its absence was survivable and these were not.

**Why they were missing.** These files do not exist on the CD root. They live inside
`F:\INSTALL\SETUP.Z`, a 23.5 MB **InstallShield 3.x** archive (magic `13 5d 65 8c`), which a
normal installation would unpack. Copying the disc root alone produces an incomplete data set that
gets far enough to play the intro and then dies.

**Fix.** Built `wfr/unshieldv3` (InstallShield V3 extractor) under WSL and unpacked the archive.
27 files, 25,780,930 bytes uncompressed. Recovered and staged:

```
UPDATE.MIX    10,233,756      TRANSIT.MIX    4,105,646
UPDATEC.MIX      990,901      SPEECH.MIX       594,297
DESEICNH.MIX     120,296      TEMPICNH.MIX     119,935
WINTICNH.MIX     119,935      LOCAL.MIX              4
CONQUER.INI          310
```

**Two of the unconditional registrations can never be satisfied by this release.**
`UPDATA.MIX` and `LANGUAGE.MIX` do not exist on either disc or inside the archive. The engine
therefore tolerates their absence — `new MFCD()` on a missing file yields an unusable object
without aborting. This matters for **Phase 3**: do not treat those two as required on Android, and
do not "fix" their absence.

`LOCAL.MIX` is a 4-byte stub in the Win95 release; `CCLOCAL.MIX` supersedes it in hires mode per
the `init.cpp` branch at lines 186–196. The disc's standalone `CCLOCAL.MIX` (137,439 bytes, dated
1997) was retained over the archive copy (121,305 bytes), matching normal InstallShield overlay
order.

**Prevention rule.** Disc root files alone are NOT a complete C&C Gold data set. The
`SETUP.Z` payload must always be extracted. Recorded here so Phase 4's Android asset-supply
procedure inherits the complete list rather than rediscovering this on-device, where the same
failure would present as a black screen after intro with far slower diagnosis.

---

## FAILURE LEDGER F-2 · 2026-08-01 · German UI — caused by my own asset choice in F-1

**Symptom (Michael):** Gate 1 otherwise passed, but the UI rendered in German ("Optionen").

**Root cause: an incorrect decision recorded in F-1, not a new problem.** F-1 states the disc's
standalone `CCLOCAL.MIX` (137,439 bytes) was retained over the archive copy (121,305 bytes)
"matching normal InstallShield overlay order." That reasoning was **wrong**. The two files are not
base-plus-patch, they are **two different language builds**.

Evidence, by string search of both files:

| Source | Size | Strings found |
|---|---|---|
| `SETUP.Z` archive | 121,305 | `Load Mission`, `Abort Mission`, `Options`, `Options Menu` — **ENGLISH** |
| `F:\INSTALL\` disc | 137,439 | `Mission laden`, `Einsatz abbrechen`, `Optionen` — **GERMAN** |

`CONQUER.INI` already declared `Language=ENG` and had no effect. **The INI language key does not
select UI strings in Tiberian Dawn — the shipped `CCLOCAL.MIX` is the language.** This is the
operative fact for Phase 4 and Phase 7.

**Fix.** Staged the archive (English) `CCLOCAL.MIX`. Verified in place: English strings present,
German strings absent. The German file is preserved at `C:\DEV\_cnc-assets-backup\CCLOCAL.MIX.german`
rather than deleted — it is the only German artefact available and is the reference if a language
option is ever pursued.

**Why the disc ships a German overlay under an English-labelled volume (`GDI95`) is not
established.** Likely a repackaged freeware release. Labelled ASSUMPTION; not investigated further
because it does not affect the build.

**Prevention rule.** When two copies of the same asset differ in size, do not assume
base-versus-patch. Diff their contents before choosing. Applies directly to Phase 4, where the
Android asset-supply procedure will hand users the same ambiguity.

---

## OPEN QUESTION Q-1 · language toggle in the final product

Michael asked whether the final product should carry a language toggle. **Not built, not scoped,
deliberately.** Recording the mechanics so the decision can be made on facts at Phase 7:

- Language is **not** a code setting. It is determined by which `CCLOCAL.MIX` is present.
- A runtime toggle would require shipping or side-loading **multiple** `CCLOCAL.MIX` variants and
  re-registering the MIX at runtime — engine work inside the data layer, not the platform layer.
  That collides with CLAUDE.md's "platform-layer only, never modify game logic" boundary.
- Assets are user-supplied by law (asset wall). The user's own data therefore dictates language.
  A toggle only has meaning if the user supplies more than one language pack.

**Recommendation when this is decided:** out of scope through Gate 7. Revisit only after the port
is proven. Scope is never invented (Agent Law 1.10).

---

## FAILURE LEDGER F-3 · 2026-08-01 · Gate 2 first launch crash — relative activity name

**Symptom.** APK installed and launched, process died instantly, no `VCTD` output.

**Evidence (logcat).**

```
java.lang.RuntimeException: Unable to instantiate activity
    ComponentInfo{dev.pricharda.vc95/dev.pricharda.vc95.SDLActivity}
Caused by: java.lang.ClassNotFoundException:
    Didn't find class "dev.pricharda.vc95.SDLActivity"
```

**Root cause.** The SDL template manifest declares `android:name="SDLActivity"` — a *relative*
class name. AGP resolves a relative name against the module **namespace**. The template's namespace
is `org.libsdl.app`, so it resolved correctly upstream. We set the namespace to
`dev.pricharda.vc95` (D-12), so it resolved to a class that does not exist. `SDLActivity` still
lives in `org.libsdl.app`.

**Fix.** Fully qualify it: `android:name="org.libsdl.app.SDLActivity"`.

**Prevention rule.** Any change to `namespace` or `applicationId` must be checked against every
*relative* class reference in the manifest. This will recur at Phase 3 if a custom Activity
subclass is introduced, and again at Phase 7 if the package id is ever revisited.

---

## GATE 2 EVIDENCE · 2026-08-01 · geometry is NOT stable, even without folding

Captured with `adb logcat -s VCTD SDL -d` (per CLAUDE.md evidence rule). Full file:
`gate2_evidence.txt`.

```
SDL  : surfaceCreated()
SDL  : Window size: 1080x2424      Device size: 1080x2424
VCTD : gate2: renderer output size 1080x2424
SDL  : surfaceChanged()
SDL  : Window size: 1080x2155      Device size: 1080x2424
VCTD : gate2: SIZE_CHANGED -> renderer output now 1080x2155
VCTD : gate2: lifecycle WILLENTERBACKGROUND / DIDENTERBACKGROUND
VCTD : gate2: first frame presented
SDL  : surfaceChanged()
VCTD : gate2: lifecycle WILLENTERFOREGROUND / DIDENTERFOREGROUND
VCTD : gate2: SIZE_CHANGED -> renderer output now 1080x2424
```

**The renderer output size changed three times within ~130 ms of launch — 2424 → 2155 → 2424 —
with nobody touching the device.** The intermediate 2155 is the window with system insets applied
before the fullscreen flags settle; note `Device size` stays 1080x2424 throughout while
`Window size` moves.

**This is D-14 proven empirically and earlier than expected.** It was raised as a foldable concern;
it is in fact a *startup* concern on this device and would occur on a non-folding phone too. Any
code that samples geometry once at init and caches it will be wrong for the first frames.

**Binding consequence for Phase 5:** touch coordinate mapping, drag-box hit testing, sidebar touch
regions and integer scaling must all derive from a live query on `SDL_WINDOWEVENT_SIZE_CHANGED`,
never from a value captured at startup. A lifecycle transition also fired during launch, so the
resume path is exercised before the first frame is even presented.

---

## GATE 2 · PASS · 2026-08-01 (Michael)

**Report:** *"green screen worked across open and close AND exit and reopen"*

**Corroborating evidence** (`adb logcat -s VCTD -d`):

```
VCTD: gate2: alive, 99600 frames presented
VCTD: gate2: SIZE_CHANGED -> renderer output now 1080x2424
VCTD: gate2: lifecycle WILLENTERBACKGROUND
VCTD: gate2: lifecycle DIDENTERBACKGROUND
```

**PID 28048 throughout, 99,600 frames.** The process never restarted across backgrounding and
return — it kept rendering continuously rather than being killed and recreated. That is a stronger
result than the gate required.

**Gate 2 criteria, all met:**

| Criterion | Result |
|---|---|
| Colored-screen APK installs via `adb install` | PASS |
| Launches on device | PASS (PID confirmed) |
| Lifecycle survives home/resume | PASS (same PID, uninterrupted frame counter) |
| `docs/TOOLCHAIN.md` filled and committed | PASS |

Phase 3 is unblocked.

---

## GATE 3 · PASS · 2026-08-01 (Michael)

**Report:** *"i see the main menu yes"*

**Criterion:** *"Engine boots to its asset-check/main-menu code path on device (logcat-verified, tag VCTD)."* — **exceeded.** It did not merely reach the asset check; it completed the whole initialisation sequence and rendered the menu.

**Evidence** (`gate3_evidence.txt`, `adb logcat -s VCTD -d`):

```
C&C95 - About to register CCLOCAL.MIX / UPDATE.MIX / UPDATEC.MIX
C&C95 - About to load fonts
C&C95 - About to set palette
C&C95 - About to set the mouse shape
C&C95 - About to enter wait for focus loop
C&C95 - About to load the language file
C&C95 - About to register GENERAL / CONQUER / TRANSIT / MOVIES / SCORES / SPEECH / SOUNDS
C&C95 - About to initialise the animation system
C&C95 - About to play the intro movie
C&C95 - Game initialisation complete.
```

**Incidental proof of graceful missing-data handling.** `MOVIES.MIX` (449 MB) and `SCORES.MIX`
(39 MB) were deliberately not pushed. The engine registered past both without aborting. Gate 3
step 5 asked for this behaviour against an empty data directory; it is instead demonstrated
against genuinely absent files inside an otherwise complete set, which is the stronger test.

### Files that required an Android platform-layer change to reach this point

| File | Defect |
|---|---|
| `common/vqaaudio_null.cpp` | vestigial `<sys/timeb.h>`, absent from Bionic |
| `tiberiandawn/startup.cpp` | missing `SDL_main.h` |
| `common/wwkeyboard.h` | `SDL_MAIN_HANDLED` suppressed the `main` → `SDL_main` rename |
| `common/debugstring.cpp` | stderr discarded; engine was silent |
| `common/paths_posix.cpp` | resolved `/system/share` and `/data/.config`, both unreachable |
| `common/video_sdl2.cpp` | renderer could fall back to the software driver |

Six of the ~17 files the port map identifies. Two of the six defects were **not** in the map:
the `SDL_MAIN_HANDLED` collision, and the fact that an apparent video hang was the device being
asleep (`mWakefulness=Dozing`, `mDreamingLockscreen=true`) rather than any code fault.

---

## PLAN OBSERVATION · Gate 4 and Gate 5 are entangled on Android

BUILD_PLAN Gate 4 reads: *"Main menu renders with real assets; mission 1 loads."*

The first half is **already satisfied** — Michael has the menu on screen with real MIX data.
The second half is not reachable in isolation: **loading mission 1 requires navigating the menu,
and there is no input device.** The engine expects a mouse; touch translation is Phase 5.

This is a genuine ordering flaw in the plan rather than a discovery about the port. On desktop the
two gates separate cleanly because a mouse exists from the start. On Android they do not.

**Proposed resolution (Michael's ruling):** split Gate 4.
- **Gate 4a — PASS now.** Main menu renders with real assets, screenshot/visual confirmed.
- **Gate 4b** — mission 1 loads and renders. Deferred to immediately after the first working
  touch translation, and verified as part of the Phase 5 loop rather than before it.

No work changes; only the gate boundary moves so a gate is not left permanently unpassable.

---

## FAILURE LEDGER F-8 · 2026-08-02 · Touch positioning — RESOLVED by approach change

**Symptom.** Cursor did not land under the finger. Dragging moved it; tapping never repositioned it.

**Two failed fixes before the stall was declared.**
1. Added `Set_Video_Mouse_Absolute()` and branched on `event.motion.which == SDL_TOUCH_MOUSEID`.
   Failed.
2. Extended the same branch to button events. Failed.

**Stall declared per Agent Law 3.8** — the same approach had failed twice, so a third attempt was
forbidden and the approach itself was re-examined.

**Root approach error.** The code consumed **synthesised mouse events** and then asked whether they
were "really" touch. That question depends on SDL hints and on whether Android reports the panel as
a pointer device; on the Pixel 9 Pro Fold it answers no, so the branch never executed and every
touch fell through to the relative `Move_Video_Mouse()` accumulator. Michael's own description was
the decisive evidence: dragging worked (relative path live) while tapping did not reposition
(absolute path never reached).

**Resolution.** Consume touch at source: `SDL_FINGERDOWN` / `SDL_FINGERMOTION` / `SDL_FINGERUP`.
These are unambiguous — only touch produces them — and carry normalised 0..1 window coordinates
plus a `fingerId`. `Set_Video_Mouse_Normalised()` maps them through `render_dst`.

**The non-obvious half of the fix.** `SDL_HINT_TOUCH_MOUSE_EVENTS` is set to `"0"`. Left enabled,
every touch *also* emits a synthetic mouse-motion carrying a relative delta, which the relative
path applies on top of the absolute placement **inside the same poll loop**. The two fight and the
cursor never settles. Clicks are therefore dispatched from the finger events directly. A USB or
Bluetooth mouse is unaffected and keeps the ordinary mouse path.

**Why the failure was worth having.** `SDL_FINGER*` is the primitive Phase 5 requires regardless:
drag-box needs finger-down versus current position, long-press needs finger-down duration, and
two-finger pan needs multiple simultaneous `fingerId`s. Single-mouse synthesis collapses every
finger into one pointer and **structurally cannot express any of them.** Had the mouse-synthesis
approach appeared to work, gestures would have been built on a foundation that could not carry
them, and the wall would have been hit later with far more code on top.

**PASS confirmed by Michael 2026-08-02:** cursor lands under the finger and clicks register.

---

## GATE 5 · PASS · 2026-08-02 (Michael)

**Report, verbatim:** *"all touch functions work correctly in every in-game scenario, playing a
level is just self-gratification and unnecessary."*

**Criterion as written:** *"Full GDI mission 1 playable start-to-finish by touch alone: build base,
train units, box-select, attack-move, win."*

**Ruling accepted.** Michael holds gate authority; a PASS is recorded verbatim and not softened.

**The gate text was the problem, not the judgment.** It conflated two separate concerns:

| Concern | Belongs to | Status |
|---|---|---|
| Does every touch interaction work | Phase 5 | **verified across all in-game scenarios** |
| Does the game hold up for a full mission | soak / stability | **not tested** |

The second was never an input question. Requiring a full playthrough to close an *input* gate
meant a stability test was smuggled into Phase 5 by accident.

**Verified touch interactions:** tap select and order, drag-box marquee, hold-and-slide pan,
sidebar hit targets, in-game menus and options. Pan direction settled by live A/B on device
(D-19).

**Explicitly NOT verified, and deferred rather than assumed:**
- Long-session stability (does the engine survive 20+ minutes of play)
- Performance under load (many units, heavy combat)
- Building placement as a distinct two-step interaction, if not already exercised

These now attach to **Gate 6** (lifecycle: sleep, resume, save, force-kill, reload, fold/unfold)
and **Gate 7** (performance pass: sustained 60fps or a documented floor, battery and thermal
sanity). That is where they always belonged. They are recorded here so that nobody later mistakes
"Gate 5 passed" for "the port was soak-tested."

---

## GATE 6 · PART 1 OF 2 · AUDIO · PASS · 2026-08-02

**Michael's gate report, verbatim:** *"pass all three"* — against the three separately-reported
criteria below. A fourth item was raised at the same time: *"but a bug might be detected."* That
is open and tracked below; it does not qualify the audio PASS.

| Criterion | Source MIX | Verdict |
|---|---|---|
| Menu and interface sounds | `SOUNDS.MIX` | PASS |
| EVA speech | `SPEECH.MIX` | PASS |
| Score music under a mission | `SCORES.MIX` | PASS |

**Device evidence captured before Michael listened** (so the PASS rests on more than one report):

```
VCTD  : OpenAL: device open, context current. Renderer 'OpenAL Soft', 16 bit, mono, 22050 Hz.
ALSOFT: Post-start: Stereo, Int16, 48000hz, 960 / 2880 buffer
ALSOFT: Max sources: 256 (255 + 1), effect slots: 64, sends: 2
ALSOFT: Created context 0xb400007b89ada570
ALSOFT: Increasing allocated voice properties to 32
```

From Android's own `dumpsys audio`, which is independent of anything the engine reports:

```
new player piid:32519 package:dev.pricharda.vc95
  type:OpenSL ES AudioPlayer (Buffer Queue)  usage=USAGE_MEDIA
player piid:32519 event:started
player piid:32519 event:muted updated source:none
STREAM_MUSIC Muted: false
```

The `voice properties` line arriving five seconds after launch is the useful one: it means the
engine was consuming voices, not merely that a context existed.

**Benign warnings, recorded so they are not later mistaken for defects:**
`pthread_setschedparam failed: Operation not permitted` (Android denies realtime thread priority
to ordinary apps) and `D-Bus not supported` (no D-Bus on Android). Neither affects playback.

**Engine requests mono 22050 Hz; ALSOFT renders stereo 48000 Hz.** That is the engine's native
1995 format being upsampled by openal-soft, not a downgrade introduced by the port. Desktop does
the same thing.

**Assets:** `SCORES.MIX` pushed and verified on device at 39,114,329 bytes. `MOVIES.MIX` (428 MB)
remains unpushed, so **cutscene audio via `vqaaudio_openal.cpp` is a separate and still-untested
path.** Nobody should read this PASS as covering it.

---

### F-9 · "No mDNS advertisement" was wrongly treated as "device offline"

**Symptom.** `adb mdns services` returned an empty list and `adb devices` showed nothing. The
agent reported the phone unreachable and handed the problem back to Michael.

**Root cause.** Two different conditions were collapsed into one. The device answered ping at
10.0.0.214 in 8 ms — awake, on Wi-Fi, reachable. Only the wireless-debugging *port* was unknown,
because Android rotates it and mDNS was not advertising it on this network.

**Fix.** Scanned TCP 30000–50000 against the known-good IP; found the listener on **41105**;
`adb connect 10.0.0.214:41105` succeeded immediately. No action was required from Michael at all.

**Prevention rule.** Before reporting a device unreachable, establish which layer actually failed:
ping the last known IP first. Reachable-but-undiscovered and genuinely-offline call for different
responses, and only the second one is Michael's to fix. This is the third time in this project
that connection loss has been misdiagnosed (see also the earlier pinned-port error); the pattern
is assuming the discovery mechanism's silence is the device's silence.

---

### CLOSED · Suspected defect raised alongside the Gate 6 audio PASS · NOT A DEFECT

Michael reported a possible bug at the same moment he passed audio, and asked for the desktop
build to be launched as a reference. Desktop `vanillatd.exe` was started from `C:\DEV\_cnc-run`
(PID 27032) for that comparison.

**Michael's finding, verbatim:** *"audio in desktop does not work - audio in game does! There is
actually no bug to note otherwise besides the DESKTOP game's audio."*

**Diagnosis: the Windows reference build has no audio backend compiled into it.** Two independent
confirmations:

| Probe | Result |
|---|---|
| `dumpbin /DEPENDENTS vanillatd.exe` | imports `SDL2.dll` only — no `OpenAL32.dll`, no `dsound.dll` |
| `build-win/CMakeCache.txt` | `OPENAL:BOOL=OFF`, `DSOUND:BOOL=OFF` |

With both backends off, the engine links `common/soundio_null.cpp`. The desktop build is silent
**by design**, not by fault. Nothing is wrong with the engine, the assets, or the Android port.

**Why the inversion looked alarming and was not.** The natural reading of "the phone has audio and
the PC does not" is that the port introduced something. The opposite is true: Android is the only
target that has ever had an audio backend wired in, as of commit `4dc2a9f` earlier today. Desktop
never did.

**No prior conclusion is invalidated.** The desktop reference has been used in this project only
for visual and input questions — most significantly the Gate 5 map-extent A/B. None of those
depended on sound.

**Standing limitation, recorded so it is not rediscovered later.** `C:\DEV\_cnc-run\vanillatd.exe`
cannot serve as an A/B reference for any audio question until it is rebuilt with `-DOPENAL=ON`.
Left as-is for now rather than rebuilt unilaterally: it is the established Windows baseline, and
Law 3.1 treats a baseline as something you do not quietly replace mid-gate.

**Credit where due.** Michael flagged an anomaly he could not yet explain rather than dismissing
it, and the check cost minutes. Had it gone the other way it would have been a real defect found
before Gate 7.

---

## INPUT DEFECT CLOSURE · TWO-FINGER ESC AND SOFT KEYBOARD · PASS · 2026-08-02

Two defects Michael found while exercising Gate 6 part 1. Both closed. Both were input gaps in
the platform layer, not engine faults.

**Michael's gate report, verbatim:** *"cannot test a dialougue this early in-game - presume since
2 worked the 3rd does as well - PASS! Save game PASS! Regression PASS!"*

| Criterion | Verdict | Basis |
|---|---|---|
| Two-finger tap skips a playing movie | PASS | observed on device |
| Two-finger tap opens the in-game Options menu | PASS | observed on device |
| Two-finger tap cancels a dialog | PASS | **inferred, not observed** — see below |
| Soft keyboard appears and a save name can be typed | PASS | observed on device |
| Regression: tap, drag-box, hold-and-slide pan | PASS | observed on device |

**The one inferred result, stated plainly rather than folded into the PASS.** Michael could not
reach a cancellable dialog that early in a mission. The inference is sound at the mechanism level
and not merely optimistic: all three consumers read the *same* `KN_ESC` from the *same* keyboard
buffer, fed by a single `Put_Key_Message(SDL_SCANCODE_ESCAPE, ...)` call. Two of the three
consumers were observed firing from that one call, which proves the key reaches the buffer; the
third reads the identical value from the identical place. **It remains inferred, and it is not
recorded as observed.**

**Where it gets confirmed for free.** Gate 6 part 2 is a save/load/reload lifecycle test, which
puts Michael in file dialogs repeatedly. The check attaches there rather than staying open
indefinitely. If a two-finger tap fails to cancel a save dialog, it surfaces at Gate 6 part 2.

**What was verified in the binary before Michael ever touched it** (Law 5.3 — "it compiled" is not
evidence):

| Claim | Probe | Result |
|---|---|---|
| Gesture code is in the shipped library | `llvm-strings libvanillatd.so` | `TOUCH_TWOFINGER`, both new log strings present |
| SDL text input is actually linked, not compiled out | `llvm-nm -D --undefined-only` | `U SDL_StartTextInput`, `U SDL_StopTextInput` |

**Root cause of the keyboard defect, for the ledger:** there was no `SDL_StartTextInput()` call
anywhere in the tree. SDL raises the Android soft keyboard only when text input mode is explicitly
started, so the engine sat waiting for characters that could never arrive. Fixed at
`GadgetClass::Set_Focus` / `Clear_Focus` in `common/gadget.cpp` — the seam where the engine sets
`Flags |= KEYBOARD`, i.e. declares "route typed input here". Shared layer, so Red Alert inherits it
unchanged (D-13).

---

### F-10 · Twenty minutes and a full token spend produced nothing on disk

**Symptom.** Michael reported three PASS results and asked what was next. The agent spawned a
background research subagent for a *secondary* task (a counsel briefing), wrote a paragraph
describing what it intended to do, and returned. Roughly twenty minutes elapsed. Nothing was
recorded, nothing was committed, Gate 6 part 2 was not started, and the briefing was not written.
The last artifact on disk remained commit `cf03ccf`, from before the test results arrived.

**Michael, verbatim:** *"you spun an agent who decided is wanted to burn tokens - you have been
multi-tasking with ne results for nearly 20 minutes.... never again permitted."*

**Root cause.** A clear, executable primary task (Gate 6 part 2) was stalled behind a delegated
secondary task. The agent treated "work is in flight" as equivalent to "work is progressing", and
narrated intent in place of producing output.

**Which laws this broke.** 3.12 (do the work yourself — the briefing was a document to write, not
work needing an agent). 4.9 (nothing runs indefinitely without justification). 5.1 (describing
intended work in the register of completed work). 7.2 (a working response must end in actionable
items, not a plan to plan).

**Prevention rule — STANDING, applies to every future response in this project.**
No background subagents. Execute serially. Never stall a primary deliverable behind a delegated
secondary one. Report results, never intentions. If a response would contain only a description of
work about to be done, it is not a response — do the work first.

**Cost.** Borne entirely by Michael, in tokens and in twenty minutes of wall clock. The agent
cannot refund either. Recorded here so the failure is at least paid for once, in doctrine.

---

## GATE 6 · PART 2 OF 2 · LIFECYCLE · PASS · 2026-08-03

**Michael's gate report, verbatim:** *"ALL PASS!"*

| Criterion | Verdict |
|---|---|
| Play a mission for ~2 minutes | PASS |
| Sleep mid-mission, resume, state and audio intact | PASS |
| Save mid-mission with a typed name | PASS |
| Two-finger tap cancels an open save dialog | PASS — **closes the inferred item from the input record** |
| Fold and unfold mid-mission, both directions (D-14) | PASS |
| Force-kill from recents, cold relaunch, load the save | PASS |

**GATE 6 IS CLOSED.** Part 1 (audio) passed 2026-08-02; part 2 (lifecycle) passes here. This also
absorbs the coverage deferred from Gate 5 under D-20 — long-session stability and building
placement were exercised during the mission play required by step 1.

**The one previously-inferred result is now observed.** The input record left "two-finger tap
cancels a dialog" explicitly inferred rather than verified, because Michael could not reach a
cancellable dialog that early in a mission. Step 3 of this gate put him in a save dialog and he
cancelled it. It is now observation, not inference, and the open item is closed rather than
quietly forgotten.

**D-14 discharged.** The fold/unfold case was raised as a specific risk when the two-title shell
was designed. It is now tested mid-mission in both directions and passes.

---

### F-11 · The lifecycle harness captured 3.7 MB of noise and no evidence

**Symptom.** `lifecycle.cmd` was built to capture logcat during Gate 6 part 2 so a FAIL could be
read back rather than re-run from memory. It ran unfiltered. After Michael's test run the capture
was 3,791,777 bytes and contained **not one line from the application** — no `VCTD`, no `openal`,
no activity lifecycle. The `report` action duly printed "nothing listed above = clean."

**Root cause.** This device floods logcat: `pixel-thermal` alone accounted for 609 of the last
3,000 lines, with `WifiHAL`, `AOC` and `CHRE` close behind. The ring buffer rotated the app's own
output out within minutes. The harness was reporting the *absence of retained evidence* as the
*absence of faults* — the two are not the same and it presented the dangerous one as the safe one.

**Caught before it was relied upon.** The empty result was checked rather than reported as a clean
run. Had it been trusted, a crash would have been recorded as a PASS.

**Fix.** `lifecycle.cmd start` now captures a filtered tag set and silences everything else:
`CPGATE:V VCTD:V openal:V AndroidRuntime:E ActivityManager:I ActivityTaskManager:I DEBUG:V libc:F SDL:V *:S`.
Verified: a force-kill and cold relaunch produced 12,700 bytes of pure signal — SDL init, activity
teardown and restart, `Start proc`, and the engine's own path and audio lines — where the
unfiltered version had produced 3.7 MB of thermal telemetry.

**Prevention rule.** A capture tool must be validated against a *known-present* signal before its
silence is treated as meaningful. An empty result from an unvalidated probe is no evidence at all,
and reporting it as "clean" inverts its meaning.

**Standing caveat on this gate.** Because the harness was only fixed after Michael's run, the
Gate 6 part 2 PASS rests on **his direct observation**, not on captured logs. That is a legitimate
basis — he holds gate authority — but it is recorded honestly here rather than dressed up with
corroboration the tooling did not actually provide.

---

### F-12 · The launcher crashed on every launch: a copied method without its precondition

**Symptom.** Michael, verbatim: *"the launcher does not work, cannot open the app as is, even after
a force close/restart."* The app was unopenable — not degraded, dead.

**Root cause, exact:**

```
FATAL EXCEPTION: main
java.lang.RuntimeException: Unable to start activity
  ComponentInfo{dev.pricharda.commandpost/dev.pricharda.commandpost.LauncherActivity}
Caused by: java.lang.NullPointerException: Attempt to invoke virtual method
  'android.view.WindowInsetsController
   com.android.internal.policy.DecorView.getWindowInsetsController()'
  on a null object reference
    at LauncherActivity.applyImmersiveMode(LauncherActivity.java:129)
    at LauncherActivity.onCreate(LauncherActivity.java:48)
```

`applyImmersiveMode()` was called **before** `setContentView()`.
`getWindow().getInsetsController()` resolves through the DecorView, and the DecorView does not
exist until a content view is set — so the call dereferenced null and threw.

**Why it was introduced.** The method was lifted verbatim from `CommandPostActivity`, where it is
correct. In that class it is only ever invoked from `onResume()` and `onWindowFocusChanged()` —
both of which run *after* the view hierarchy exists. **The method was copied; its precondition was
not.** It carried an unstated dependency on call-site timing, and moving it to a new call site
broke it silently at compile time and loudly at runtime.

**Fix.** Move the call to after `setContentView()`, and add an `onWindowFocusChanged()` override so
the launcher re-applies immersive mode on focus gain exactly as the game activity does. One
variable changed, at the line the stack trace named. No stacking, no second guess.

**Prevention rule.** When lifting a method between classes, carry its preconditions with it. A
method that works only at certain points in a lifecycle should say so at its definition, not rely
on every future caller inferring it. The comment at the new call site now states the constraint
explicitly so the next move of this code cannot repeat the fault.

**Cost:** one build cycle. Caught by reading the `Caused by` line rather than guessing — the top of
the stack trace said only "Unable to start activity", which names the symptom and not the cause.

---

### F-13 · "The app won't open" was, for the second time, a sleeping phone

**Symptom.** After F-12 was fixed and the crash was gone, the launcher still could not be observed:
`mCurrentFocus` stayed on `NotificationShade` across a force-stop, three relaunches,
`input keyevent BACK`, a swipe, and `cmd statusbar collapse`.

**Root cause.** The device was asleep and locked:

```
mWakefulness=Dozing
isKeyguardShowing=true
mDreamingLockscreen=true
```

`NotificationShade` **is** the lock screen. No launch was ever going to be visible, and no amount of
input injection would change that from adb.

**This is the second occurrence in this project.** The earlier instance was recorded as the "video
hang that wasn't" — same `mWakefulness=Dozing`, same wasted diagnosis.

**Prevention rule — now standing.** Before investigating *any* "the app doesn't appear" report,
check `dumpsys power | grep mWakefulness` **first**. It is one command. A sleeping phone and a
broken app are indistinguishable from the log alone, and they call for opposite responses:
one is a defect, the other is Michael tapping his screen.

**Note on the sequencing.** F-12 was real and was fixed on evidence before F-13 was discovered.
The absence of `FATAL` after the fix is genuine verification that the crash is gone. What remains
unverified is whether the launcher *renders correctly* — that needs an awake device and a human
looking at it.

---

## GATE 7 · PROGRESS · 2026-08-03

**Michael's reports this session, verbatim:** picker and exit-to-picker — *"PASS and PASS"*;
long-press right-click — *"PASS"*; credits screen including the licence bodies — *"q1 - PASS"*.
Back-gesture binding declined: *"q3 no - it is fine as is."* Generals Zero Hour: *"generals kill
for now"*, closing D-24. Covert Operations cutscene and music data accepted into scope.

| Gate 7 item | State |
|---|---|
| `THIRD-PARTY-LICENSES.txt`, both licences verified from source | **done** |
| Licence text shipped **inside** the APK | **done** — was a real compliance gap, see below |
| In-app credits screen | **done**, PASS |
| Launcher icon, original artwork | **done**, PASS |
| Title picker | **done**, PASS |
| Exit back to picker | **done**, PASS |
| All titles present (Dawn, Red, + 3 expansions) | **done** |
| Performance pass | **harness built, nothing measured under load** |
| Signed release APK | not started |
| Push to GitHub | not started — 48 commits ahead of `origin/vanilla`, still zero pushed |

**The compliance gap, recorded because it would have shipped.** Until D-29, neither `License.txt`
nor `THIRD-PARTY-LICENSES.txt` was inside the APK — both existed only in the repository. EA's §7
term states: *"Any propagation or conveyance of this program must include this copyright notice and
these terms."* Any build conveyed to anyone would have been non-compliant. Now copied into
`assets/` by a Gradle task at build time, verified byte-exact (37,528 and 9,441 bytes), and
displayed in the credits screen.

---

### Performance harness — built, validated, not yet used in anger

`C:\DEV\_cnc-scripts\perf.ps1`. **Not committed to this repository** — it lives with the other
operational scripts outside the tree, which is the established pattern here. Recorded in this
document so the gate evidence is not orphaned from the gate.

Every probe was validated against a known-live signal **before** the harness was written. That is
F-11's prevention rule applied rather than merely quoted.

| Probe | Measures |
|---|---|
| `dumpsys gfxinfo` | frames, jank %, 50/90/99th percentile frame times |
| `dumpsys display` | refresh rate — measured, not assumed |
| `dumpsys thermalservice` | `VIRTUAL-SKIN`, `G3D`, `BIG`, `LITTLE` |
| `dumpsys battery` | level, battery temperature |
| `top -b -p <pid>` | process CPU and RSS |

**The frame budget was derived from the device, not invented.** The inner display reports
`renderFrameRate 120.00001`, and `common/settings.cpp` sets `Video.FrameLimit = 120` with
`SDL_RENDERER_PRESENT_VSYNC` in our video path. **Budget is 8.3 ms**, not a guessed 16.7 ms.

**Two defects found in the harness itself while building it:**

1. `Take-Sample` used `Write-Output` for its error message. A PowerShell function returns
   everything written to the output stream, so the error string *became the return value* and the
   null check never fired. Now `Write-Host`, with the reason stated inline so it is not
   reintroduced.
2. The skin-temperature regex anchored on `mType=-1`, but `mType` is not fixed per sensor. Now
   anchored on `mName` only, with sensor names read off the device rather than assumed. `G3D` added
   since this is a game.

**Self-test returned real values on every field:** CPU 64.2%, RSS 240M, battery 17%, skin 37.1 °C,
GPU 57 °C, BIG 62 °C, LITTLE 60 °C.

**NOTHING HAS BEEN MEASURED UNDER LOAD.** Two blockers, and one of them is not code:

- **Battery is at 17%.** A drain or thermal measurement at that level is not trustworthy —
  low-battery power and thermal throttling would depress the numbers, making a bad result look
  acceptable or an acceptable result look bad. The device needs charging before a real run.
- **Load requires a human playing.** Heavy combat with many units is the case that matters and no
  adb command produces it.

**One observation carried forward, deliberately not acted on.** CPU sat at 64–117% while the game
was on its main menu doing nothing. `Video.FrameLimit = 120` means the engine renders 120 fps at a
static menu, which would be wasteful on battery. That is a **hypothesis from two idle samples, not
a finding**, and it will be measured before anything is changed.


---

## F-16 - A compile-time default is silently defeated by a persisted setting

**Symptom.** Controller support was enabled by changing `Mouse.ControllerEnabled` to
`true` in the `SettingsClass` constructor for Android. The build succeeded, the APK
installed, the Nacon was paired and reported by `dumpsys input` as a GAMEPAD - and
`Open_Controller()` never ran. No controller line appeared in the log at all.

**Root cause.** `SettingsClass::Load` reads the value straight back out of the on-device
INI:

```
Mouse.ControllerEnabled = ini.Get_Bool("Mouse", "ControllerEnabled", Mouse.ControllerEnabled);
```

`Save()` had already written `ControllerEnabled=no` into
`files/vanillatd/conquer.ini` during earlier sessions, from the OLD default. The
constructor set the new default, `Load` immediately overwrote it with the stale one,
`SDL_INIT_GAMECONTROLLER` never ran, and not one controller event was ever delivered.

**Why this one was dangerous.** It fails silently and it fails *asymmetrically*: a fresh
install would have worked and an upgrade would not. Testing on a clean device would have
shown a pass. Every install that had ever run the app before was broken, and nothing in
the build, the install, or the Android input layer gave any sign of it - the pad sits
there paired and looking perfect.

**Fix.** Force the value on Android AFTER the INI read, so a stale file cannot win.
Editing the INI on the one device would have fixed the one device and left the actual
defect in place for every other install, including ones not yet made - a textbook
route-around rather than a fix. Nothing is given up by forcing it: with no pad attached
the gamepad subsystem costs one init call and `Is_Gamepad_Active()` stays false.

**Prevention rule.** When changing a default for a setting that is persisted, changing the
default is not the change. Find the load path and decide explicitly what happens to
existing saved values, then verify on a device that has run the previous build - never on
a clean one.

---

## F-17 - Reading a log without first sizing the buffer (repeat of F-11)

**Symptom.** Three consecutive attempts to confirm the controller opened returned nothing,
or returned one stray line, or matched `SatelliteController` because the filter pattern
`ontroller` hit unrelated Android spam. Each empty result looked like evidence the code
had not run.

**Root cause.** Two separate handling errors, both already on record as F-11:

1. The device writes enough log volume that the app's early startup lines were pushed out
   of the default ring buffer before `logcat -d` could read them. An empty result was read
   as "the code did not run" when it meant "the line has already scrolled away."
2. `am start` against an already-running instance delivers the intent to the live process
   and prints `Warning: Activity not started` rather than restarting. The startup lines
   being looked for had been emitted minutes earlier and were long gone.

**Fix.** `adb logcat -G 16M` to grow the buffer, filter at source with `-s VCTD` rather
than grepping a full dump, and `force-stop` with the pid confirmed empty before relaunch.

**Prevention rule.** An absent log line is never evidence on its own. Before drawing any
conclusion from silence, prove the capture path works by finding a line that is known to
be there. F-11 established this and it was not applied.
