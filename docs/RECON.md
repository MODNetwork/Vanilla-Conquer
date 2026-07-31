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

## Asset source — Phase 0 step 4

Carried from `docs/RECON-SEED.md`. Host holds a licensed C&C Remastered Collection at
`C:\Program Files\EA Games\CnCRemastered\DATA\CNCDATA\` containing the original legacy `.MIX`
trees for **both** TIBERIAN_DAWN and RED_ALERT (verified by directory listing 2026-07-31).

**Still ASSUMPTION:** that Vanilla-Conquer consumes exactly this legacy MIX set. Not yet checked
against the upstream asset table. Probe remains open.

No asset has entered the repo. The asset wall holds.

---

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
