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
