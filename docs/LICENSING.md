# LICENSING.md — cnc-td-android

**Status:** doctrine. Binds Phase 2 (package id), Phase 7 (packaging), and any publish decision.
**Authored:** 2026-07-31 · **Basis:** `License.txt` read verbatim in this tree (714 lines);
`CMakeLists.txt` dependency inspection.
**Not legal advice.** Michael is not represented by counsel on this project. Items marked
**[COUNSEL]** should be reviewed by a lawyer before any distribution.

---

## 1. The trigger

GPL v3 obligations attach on **conveyance** (distribution), not on private use. Building and
playing this on your own phone triggers essentially nothing. Handing the APK to one other person,
free or not, public or private, triggers all of Section 2 below.

There is no "friends only" exemption. There is no "it's just a sideload" exemption.

---

## 2. Absolute obligations on any distribution

These are not negotiable and not scrubable.

1. **Ship the license text and copyright notices.** EA's additional terms state it directly:
   *"Any propagation or conveyance of this program must include this copyright notice and these
   terms."* `License.txt` travels with the build.
2. **Provide corresponding source** for the exact version conveyed (GPL v3 §6). For an APK this
   means the complete modified engine source plus the scripts and config used to build it. A
   public repo link satisfies this cleanly; that is the simplest reason to publish the fork.
3. **Mark modified versions as modified.** Required twice over — GPL v3 §5(a) and EA's own term:
   *"modified versions of the program must be marked as such and not identified as the original
   program."*
4. **Preserve all upstream notices.** No stripping headers, no removing attribution.
5. **No additional restrictions** (GPL v3 §10). Cannot add EULA terms that narrow recipients'
   GPL rights.
6. **Pass EA's §7 additional terms downstream** intact.
7. **Indemnify EA** if you assume contractual liability to recipients for the program.

---

## 3. THE TRAP — branding scrub is not an attribution scrub

These two things point in **opposite directions** and must not be confused:

| | Required action |
|---|---|
| **EA trademarks** (product identity) | **REMOVE.** Cannot brand the app with them. |
| **EA copyright notices** (attribution) | **KEEP.** Removing them is a GPL violation. |

**Scrub these** — the marks used as our product's identity:
- App name / display name
- Package id
- Launcher icon and any EA logo art
- Store or distribution page listing, screenshots framing, marketing copy
- Any claim of affiliation or association with EA

**Never scrub these** — the notices:
- `License.txt`
- EA copyright headers in source files
- The statement that this derives from EA's GPL source release
- Vanilla-Conquer upstream authorship and contributor credit
- Third-party notices per Section 4

Removing an EA copyright notice to "steer clear of EA" would trade a trademark exposure for a
copyright violation. That is a strictly worse position. **The scrub touches product identity
only. It never touches provenance.**

**[COUNSEL] Known tension:** GPL compels you to state that Electronic Arts Inc. holds copyright.
EA's §7 term forbids distributing a modification "using any Electronic Arts trademark." Naming
EA as a rightsholder in a copyright notice is ordinarily distinct from using the mark as your
product's identity, and both obligations can normally be satisfied at once. Have counsel confirm
the exact wording before publishing.

---

## 4. Third parties beyond EA

Verified from `CMakeLists.txt`: SDL2 and OpenAL are resolved via `find_package`, i.e. **external,
not vendored in this tree.** On Android they will be bundled into our APK, so their licenses
attach to our distribution.

| Component | License | Status |
|---|---|---|
| Vanilla-Conquer engine | GPL v3 + EA §7 terms | **VERIFIED** (License.txt read) |
| SDL2 | zlib (believed) | **NEEDS VERIFICATION** — read the shipped COPYING before Gate 7 |
| openal-soft | LGPL (believed v2) | **NEEDS VERIFICATION.** If LGPL, it carries a *relinking* obligation distinct from GPL. Dynamic linking (shared `.so` in the APK) is the normal way to satisfy it. Do not statically link without checking. |
| Android/SDL template, oboe, any GLES shim added later | varies | **NEEDS VERIFICATION** as introduced |

**Action at Gate 7:** produce `THIRD-PARTY-LICENSES.txt` in the repo and an in-app credits screen.
Every bundled library gets its notice.

---

## 5. Assets

Game assets remain **EA copyright and are NOT covered by the GPL release.** EA released only
`TiberianDawn.dll`, `RedAlert.dll` and their corresponding source.

The asset wall in CLAUDE.md is therefore a **legal requirement, not a hygiene preference.**
No `.MIX`, no extracted data, no bundling in the APK, ever. Users supply their own from their own
licensed copy. This is the same pattern the iOS Generals precedent used.

---

## 6. TIMING CORRECTION — the scrub cannot wait for "fully up and running"

The package id and app name are set at **Phase 2 step 4**, long before the port runs.

Changing a package id late is expensive: it is bound to the signing identity, to
`SDL_AndroidGetInternalStoragePath()`, and therefore to where saves and config live on device.
A late rename invalidates existing installs and save paths and forces a Gate 6 re-test.

**Ruling for the plan:** choose the neutral, EA-mark-free name and package id **at Phase 2**, not
at Phase 7. Phase 7's branding work is then limited to icon, credits, README and the
third-party notices — cosmetic and additive, not structural.

Placeholder rule until a name is chosen: package id under a personal namespace, no EA marks, no
MOD OS or CoCreate identifiers (entity wall, CLAUDE.md).
