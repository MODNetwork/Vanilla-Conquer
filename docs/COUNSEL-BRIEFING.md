# COUNSEL BRIEFING — Android port of Command & Conquer: Tiberian Dawn

**Prepared for:** outside counsel, IP / software licensing
**Prepared by:** Michael Pricharda (via engineering agent), 2026-08-03
**Purpose:** obtain a reasoned opinion on whether this software may be published to the
Google Play Store, and under what conditions.

**This document is not legal advice and contains none.** It is a factual and documentary
briefing assembled so that counsel does not have to reconstruct the record. Every quotation below
was read directly from the cited source. Where something could not be verified it is labelled
**UNVERIFIED** and the probe that would settle it is named.

**The single question, stated once:** *May this application be listed on the Google Play Store,
free of charge, and if so what must change?* Everything else in this document exists to let you
answer that.

---

## 1. What the software is, precisely

| | |
|---|---|
| **Name** | Command Post |
| **Package id** | `dev.pricharda.commandpost` |
| **Base** | Vanilla-Conquer, a community fork of the engine source Electronic Arts released under GPL v3 in 2020 |
| **What was modified** | Platform layer only — Android windowing, touch input, audio backend, storage paths. No game logic altered. |
| **Language / build** | C++ / CMake / SDL2 / openal-soft, built as an Android APK |
| **Distribution status** | **Nothing has been distributed to anyone.** Built and run on one personal device only. |
| **Price if published** | Free. No ads, no in-app purchases, no monetisation of any kind currently contemplated. |

**The architectural fact that matters most to this analysis:** the application **contains no
Electronic Arts game assets and cannot be made to contain them.** It ships executable code only.
The user must supply their own `.MIX` data files — the artwork, audio, video and mission data —
from a copy they obtain themselves. The application reads them from external storage at runtime.

This is the same architecture as ScummVM and Dolphin Emulator, both discussed in §6.

---

## 2. What is distributed and what is not

This distinction is load-bearing and is the reason the analysis is not simply "you cannot ship
someone else's game."

| Component | Copyright holder | Licence | Do we distribute it? |
|---|---|---|---|
| Engine source and compiled binary | Electronic Arts Inc. | **GPL v3 + EA §7 additional terms** | **YES** |
| Vanilla-Conquer community modifications | Various contributors | GPL v3 | **YES** |
| Our platform-layer modifications | Michael Pricharda | GPL v3 | **YES** |
| SDL2 | SDL contributors | zlib *(needs confirmation)* | **YES** — bundled in APK |
| openal-soft | — | LGPL *(needs confirmation)* | **YES** — bundled as shared object |
| **Game assets (`.MIX` files)** | **Electronic Arts Inc.** | **Not covered by the GPL release** | **NO — never, by design** |

The asset exclusion is enforced as an engineering rule, not an aspiration: game data is excluded
from the repository, excluded from the APK, and excluded from every build artifact.

---

## 3. The four instruments in play

### 3.1 GPL v3 — the base licence

EA released the engine source under GNU General Public License version 3. Standard obligations
attach on **conveyance** (distribution), not on private use. Publishing to Play is unambiguously
conveyance.

### 3.2 EA's additional terms under GPL v3 §7 — VERIFIED VERBATIM

Source: `License.txt` in the source tree, 714 lines, read directly. Section header at **line 680**:
`ADDITIONAL TERMS per GNU GPL Section 7`.

**Trademark and publicity — lines 681–684, verbatim:**

> "No trademark or publicity rights are granted. This license does NOT give you any right, title
> or interest in "Command & Conquer" or any other Electronic Arts trademark. You may not
> distribute any modification of this program using any Electronic Arts trademark or claim any
> affiliation or association with Electronic Arts Inc. or its affiliates or their employees."

**Notice preservation — lines 686–687, verbatim:**

> "Any propagation or conveyance of this program must include this copyright notice and these
> terms."

**Indemnification — lines 689–692, verbatim:**

> "If you convey this program (or any modifications of it) and assume contractual liability for
> the program to recipients of it, you agree to indemnify Electronic Arts for any liability that
> those contractual assumptions impose on Electronic Arts."

**Marking modified versions — lines 694–695, verbatim:**

> "You may not misrepresent the origins of this program; modified versions of the program must be
> marked as such and not identified as the original program."

Lines 697–714 add a supplemental all-caps warranty disclaimer.

**Structural observation (VERIFIED against the same file):** each of EA's four terms maps onto a
category GPL v3 §7 expressly permits — §7(e) trademark (lines 383–384), §7(d) publicity
(380–381), §7(c) misrepresentation of origin (376–378), §7(b) notice preservation (372–374),
§7(f) indemnification (386–390), §7(a) supplemental disclaimer (369–370). EA stayed inside the
enumerated permitted set. These are conventional §7 terms, not exotic ones.

Also potentially relevant, **lines 392–396**: any non-permissive additional term outside the §7
enumeration is treated as a "further restriction" under §10 and may be removed by a recipient.

### 3.3 EA Command & Conquer Franchise Modding Guidelines, Revision 2, 27 February 2025

**This is the newest instrument and the one we believe presents the real question.**
Source: https://www.ea.com/games/command-and-conquer/modding-faq — fetched and read.

Verbatim:

> "**Intellectual property ownership; license from EA.** C&C assets, including game code, art,
> music, and all other content, are and shall remain the sole and exclusive property of EA (the
> "C&C Assets"). To the extent any C&C Assets are used in Mods, EA grants the Mod creator a
> **revocable, limited, non-exclusive, non-commercial license** to use the C&C Assets solely in
> connection with, and as a part of, the Mod and subject to the terms of this Policy. **Your Mod
> must not include any music files from any C&C games.** EA reserves the right, in its sole
> discretion, to revoke this license at any time."

The policy further requires that mods be non-commercial and distributed free of charge, and it
states that it supersedes conflicting EA rules **including the GPL licence terms**.

### 3.4 Google Play Developer Distribution Agreement

Source: https://play.google/developer-distribution-agreement.html — fetched and read in full
(52,915 bytes).

**§5.3, verbatim:**

> "You grant to the user a nonexclusive, worldwide, and perpetual license to perform, modify color
> of, or add themes to, your Product icons, display (including with the color and theme
> modifications), and use the Product."

**§3.9, verbatim:** "Users are allowed unlimited reinstalls of each Product distributed via Google
Play without any additional fee..."

**§14.1:** developer indemnifies Google against "infringement or violation by Your Product(s) of
any Intellectual Property Right."

---

## 4. Questions for counsel, in priority order

### Q1 — Does an engine port constitute a "Mod" under EA's February 2025 Modding Guidelines?

**This is the question that decides the matter.** The two governing documents point in opposite
directions and nothing available resolves which applies.

| If it IS a "Mod" | If it is NOT a "Mod" |
|---|---|
| Non-commercial only | GPL v3 governs |
| Free of charge, no monetary transactions of any type | GPL v3 expressly permits commercial distribution |
| No ads for goods or services | No non-commercial restriction |
| Licence revocable by EA at will | Licence irrevocable |
| **No C&C music files** | — |

Arguments that it is **not** a Mod: it contains no EA assets whatsoever; it is a port of
GPL-licensed source code that EA itself released under a licence expressly permitting commercial
redistribution; a "Mod" in ordinary industry usage modifies a game's content, whereas this
recompiles released source for a different operating system.

Arguments that it **is** a Mod: it is derivative of C&C code; it exists solely to play C&C; EA's
policy language is broad and defines C&C Assets to include "game code."

**Sub-question:** can EA's 2025 policy validly supersede GPL v3 terms attached to a 2020 source
release, given GPL v3 §10's prohibition on imposing further restrictions on downstream recipients?

### Q2 — Does a free Play Store listing constitute "commercial" use?

Relevant even if Q1 resolves as "Mod." The app would be free with no ads and no IAP, but Google
charges a developer registration fee and the DDA uses merchant framing throughout. Fact-specific.

### Q3 — Does EA's trademark term restrict our Play Store listing text?

The app name "Command Post" and package `dev.pricharda.commandpost` contain no EA mark. But a Play
listing also has a description, screenshots, and search keywords. To function at all, the listing
must communicate what game the engine runs.

EA's separate modding policy appears to permit plain-text naming of the games while barring logos
and artwork in promotion. A listing's title, icon and screenshots sit precisely on that line.
**Specifically: may the store description say "runs Command & Conquer: Tiberian Dawn data files"?**

### Q4 — Assets: is directing users to third-party mirrors a contributory risk?

The app ships no assets, but users must obtain them. Two findings bear on this:

- **The 2007 freeware release had no licence document.** EA published Tiberian Dawn as freeware on
  31 August 2007 (Red Alert 2008, Tiberian Sun 2010). We retrieved EA's own archived page
  (Wayback capture `20100214144634` of `commandandconquer.com/classic`) and it hosted direct
  download links on EA's CDN with **no EULA, no click-through, no stated terms, no grant
  language** — a keyword search of the page body for `eula|license|licence|terms of|redistribut|copyright`
  returned zero matches. It was a download link and nothing more.
- **EA withdrew it in February 2011.** The section is gone; Wayback returns 200 only for 2010-era
  captures. All current availability is third-party mirrors.

So: no document grants redistribution rights in the assets, and the "official free download" no
longer exists. **UNVERIFIED and probably unverifiable from public sources:** whether *directing*
users to third-party mirrors creates contributory exposure.

### Q5 — Stacked indemnities

EA's §7 term (License.txt 689–692) and Play DDA §14.1 both run against Michael personally. Does
publishing on Play create the "contractual assumptions of liability" that trigger EA's clause?
Should this be held in an entity rather than personally?

---

## 5. What we have already verified — please do not re-do this

| Item | Status |
|---|---|
| EA §7 terms | **VERIFIED VERBATIM** — quoted above with line numbers |
| App name and package contain no EA mark | **VERIFIED** |
| No EA assets in repository, APK, or any build artifact | **VERIFIED** — architectural, enforced |
| GPL v3 §7 permits each of EA's four term categories | **VERIFIED** against License.txt |
| Play DDA contains no user-facing copy/modify/redistribute restriction | **VERIFIED** — full text read |
| 2007 freeware release had no accompanying licence | **VERIFIED BY ABSENCE** — archived EA page |
| Freeware release withdrawn Feb 2011 | **VERIFIED** — Wayback CDX index |
| EA Modding Guidelines Rev 2 dated 27 Feb 2025 | **VERIFIED** — fetched from ea.com |

**One correction to a common assumption:** the well-known GPL/app-store incompatibility is
**Apple-specific**. It arose because Apple's terms imposed device-count and usage restrictions on
users that GPL §6/§10 forbid adding. The current Play DDA contains no analogous clause — §5.3 runs
*developer to user* as a grant, not a restriction. We could find **no documented instance of Google
Play removing an app over a GPL licensing conflict.**

---

## 6. Comparable projects

| Project | Package | Licence | Assets | Status |
|---|---|---|---|---|
| **Dolphin Emulator** | `org.dolphinemu.dolphinemu` | GPL v3+ | User-supplied | Live on Play, 10M+ installs |
| **ScummVM** | `org.scummvm.scummvm` | GPL v3+ | User-supplied | Live on Play |
| **RetroArch** | `com.retroarch` | GPL v3+ | User-supplied | Live on Play |

Dolphin's Play listing states verbatim: *"This app is licensed under the GNU GPL v3+, and the full
source code is available through the public Git repository..."* and *"THIS APP DOES NOT COME WITH
GAMES. You must buy and dump your own games to use them with Dolphin."*

**The distinguishing feature of our case, and why the comparables are not dispositive:** none of
those projects is derived from source released by the rightsholder of the games it runs, and none
faces a rightsholder-published modding policy asserting supersession over the GPL. Dolphin is not
built from Nintendo's code. **We are built from EA's.**

**One instructive near-miss:** RetroArch's Play build ships without its Core Downloader "in
compliance with the Google Play Store's policies." That is a *content-acquisition policy*
objection, not a licensing one — and it is the shape of risk most likely to apply to us: not "GPL
is a problem" but "a feature that helps users obtain content may be a problem."

**Most directly comparable positioning — OpenRA**, a 19-year-old project reimplementing these same
games. Its legal page (https://www.openra.net/legal/) states verbatim:

> "The original game assets are not covered by this license and remain property of Electronic Arts
> Inc. We provide asset packages based on the freeware releases of these games as a convenience to
> our players, **which we believe falls under the spirit of** the Command & Conquer™ Franchise
> Modding Guidelines."

"Which we believe falls under the spirit of" is an assertion of belief, not a claim of right. A
long-running project chose that hedge deliberately. **Note that OpenRA does something we do not:
it distributes asset packages. We do not and will not.**

---

## 7. Obligations we will meet regardless of the answer

Committed to independent of counsel's opinion, because GPL v3 requires them on any conveyance:

1. Ship `License.txt` including EA's §7 terms with the build
2. Publish complete corresponding source for the exact version conveyed (GPL v3 §6)
3. Mark the build as a modified version, not the original program
4. Preserve all upstream copyright notices, EA's included
5. Add no further restrictions (GPL v3 §10)
6. Pass EA's §7 terms downstream intact
7. Ship `THIRD-PARTY-LICENSES.txt` and an in-app credits screen covering SDL2 and openal-soft

**A trap worth flagging explicitly:** removing EA's copyright notices to "steer clear of EA" would
trade a trademark exposure for a copyright violation — strictly worse. The branding scrub touches
**product identity only** (app name, package id, icon, listing). It never touches **provenance**
(copyright notices, licence text, attribution).

---

## 8. The decision that turns on your answer

| Counsel's conclusion | Consequence |
|---|---|
| Not a "Mod"; GPL governs | Publish free on Play. Complete §7 checklist. Commercial option preserved. |
| Is a "Mod"; policy binds | Publish free only. No ads, no IAP, ever. Accept revocable licence. |
| Too uncertain to publish | Remain a private personal build. GPL obligations do not attach. Zero exposure. |

**Michael's stated position:** he would like to publish if it is lawful, and is entirely willing to
stay private if it is not. There is no commercial pressure on this decision and no deadline.

---

## 9. Open verification items

| Item | Why it matters |
|---|---|
| SDL2 licence — believed zlib | Bundled in APK; notice obligations attach |
| openal-soft licence — believed LGPL | **If LGPL, carries a relinking obligation distinct from GPL.** We link it dynamically as a shared object in the APK, which is the normal way to satisfy that — but this should be confirmed rather than assumed. |
| Whether disc-image EULAs inside the freeware ISOs impose terms | Not opened or examined |

---

## Appendix — how to reproduce every citation

| Claim | Source |
|---|---|
| EA §7 terms | `License.txt` lines 680–714, in the source tree |
| GPL §7 enumerated categories | `License.txt` lines 347–396 |
| EA Modding Guidelines Rev 2 | https://www.ea.com/games/command-and-conquer/modding-faq |
| Play DDA | https://play.google/developer-distribution-agreement.html |
| Dolphin listing | https://play.google.com/store/apps/details?id=org.dolphinemu.dolphinemu |
| OpenRA legal position | https://www.openra.net/legal/ |
| Freeware release dates | https://cncnz.com/features/freeware-classic-command-conquer-games/ |
| EA's archived freeware page | Wayback capture `20100214144634` of `www.commandandconquer.com/classic` |
