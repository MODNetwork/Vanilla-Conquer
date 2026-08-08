# Google Play listing package — Command Post

**Status: PREPARED, NOT SUBMITTED.** Nothing here has been uploaded anywhere. This document
exists so that publishing is a copy-paste exercise if and when Michael decides to do it.

**Legal position, stated plainly and once.** The question of whether an engine port falls
under EA's Franchise Modding Guidelines was never resolved (see `COUNSEL-BRIEFING.md`, Q1).
Michael has declined outside counsel. Everything below is therefore written to the *most
conservative* reading — free, non-commercial, no ads, no in-app purchases, no EA artwork, no
claim of affiliation, and the required EA disclaimer front and centre. That is the posture
that satisfies both branches of the unresolved question. It is not legal advice and no one
here is a lawyer.

---

## 1. Store listing text

### App name (30 character limit)

```
Command Post
```
*12 characters. Contains no EA trademark.*

### Short description (80 character limit)

```
You must own the game. Engine only - contains nothing playable on its own.
```
*73 characters.*

**Revised on Michael's instruction to be blunter, and it is the right call.** The previous
wording ("bring your own game files") could be misread as being about save files. It is not:
the `.MIX` archives are the entire game — art, audio, video, missions and rules — and without
them the app cannot start a mission at all. Somebody who does not own the game gets nothing
from installing this, and the highest-visibility field in the listing should say so before
they install rather than after.

**Deliberately does not name the games.** The short description is the field most likely to be
read as promotional. EA's guidelines permit plain text identifying which game a project is for,
so naming them is allowed — but keeping the titles in the body rather than the headline is the
more conservative placement and costs nothing.

### Full description (4000 character limit)

```
EA has not endorsed and does not support this product.

THIS APP CONTAINS NO GAME DATA. You must already own the game and supply your
own data files. Command Post is an engine only — it ships executable code and
nothing else. Without your own files it will not start a mission.

WHAT IT IS

Command Post is a native Android build of the classic real-time strategy engine
that Electronic Arts released as open source. It runs data files from Command &
Conquer: Tiberian Dawn and Command & Conquer: Red Alert, including the
Covert Operations, Counterstrike and Aftermath expansions.

It is built on Vanilla-Conquer, the community port of that source. No gameplay,
balance, AI or mission logic has been altered. What was built here is the
Android layer: touch controls, gamepad support, audio, display handling and
storage.

BUILT FOR TOUCH

The original game was designed for a mouse and keyboard. Roughly sixteen of its
commands have no route on a touchscreen at all, so this build adds them:

• Tap to select and move, long-press to deselect or cancel
• Drag to box-select a group
• Hold and move to scroll the map
• Two-finger tap for Escape — skips briefings and movies
• An on-screen command bar carrying every keyboard-only command, including
  latching modifier keys for creating and recalling unit groups

FULL CONTROLLER SUPPORT

Connect any SDL-recognised gamepad and it works immediately, without turning
touch off. Both input methods stay live and share one cursor — tap the screen
to place it, nudge it with the stick.

• Left stick moves the cursor, right stick scrolls the map
• Face buttons for select, cancel, scatter and guard
• Triggers for force-fire and force-move
• Shoulder buttons to cycle through your units
• D-pad drives menus outside a mission and the sidebars inside one

DISPLAY AND BATTERY

• Adapts to foldables, including resizing when the device is opened or closed
• Frame rate capped to keep battery use sane on a phone
• Runs entirely offline — no network code, no accounts, no ads, no tracking

OPEN SOURCE

Licensed under the GNU General Public License v3. Complete corresponding source
for this exact build is published at the repository linked on this page. This
is a MODIFIED version of the original program and is marked as such.

Bundled open-source components: SDL2 and OpenAL Soft. Full licence texts ship
inside the app and are readable from the in-app credits screen.

INSTALLING YOUR GAME FILES

Copy the data files from your own copy of the game into the app's folder:

  Android/data/dev.pricharda.commandpost/files/vanillatd/   (Tiberian Dawn)
  Android/data/dev.pricharda.commandpost/files/vanillara/   (Red Alert)

Expansion files go in the same folder as the title they belong to.

This app does not help you find, download or obtain game files, and does not
link to anywhere that does. Where you get them is your responsibility.

—

Command & Conquer, Tiberian Dawn, Red Alert and Westwood Studios are trademarks
of Electronic Arts Inc. This project is not affiliated with, endorsed by, or
sponsored by Electronic Arts. EA has not endorsed and does not support this
product.
```

*Approximately 3,050 characters — inside the 4,000 limit with room to spare.*

**Four things in that text are load-bearing and must not be edited out:**

1. The EA disclaimer, verbatim, in the first line and repeated at the end.
2. "THIS APP CONTAINS NO GAME DATA" in the first screenful, before the fold.
3. The explicit statement that the app does not help users obtain game files. This is the
   RetroArch lesson from `COUNSEL-BRIEFING.md` §6 — the realistic Play policy risk is a
   content-acquisition objection, not a licensing one.
4. The GPL source offer. GPL v3 §6 requires it on any conveyance, and a Play listing is
   conveyance.

---

## 2. Store settings

| Field | Value | Why |
|---|---|---|
| Category | Games → Strategy | |
| Contains ads | **No** | Must stay No. Ads would be commercial use. |
| In-app purchases | **No** | Same. |
| Price | **Free** | Non-negotiable under the conservative reading. |
| Privacy policy URL | `https://github.com/MODNetwork/Vanilla-Conquer/blob/vanilla/PRIVACY.md` | Required for every app |
| Target audience | 13+ | Avoids the Families policy programme entirely |
| Contains user-generated content | No | |
| Government app | No | |
| Financial features | None | |

---

## 3. Data safety form

Play requires this and audits it. Every answer is **no**, which is unusually simple and is
true — verify against `PRIVACY.md` before submitting.

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | N/A — no data is transmitted |
| Do you provide a way for users to request data deletion? | N/A — no data is collected |
| Does your app contain ads? | No |

If Play's form forces a selection where N/A is unavailable, the honest answer is that the app
collects no data of any kind and has no network capability.

---

## 4. Content rating questionnaire (IARC)

Answer honestly. The game is a 1990s military RTS with small sprite-based units.

| Question area | Answer |
|---|---|
| Violence | Yes — mild. Military combat between small sprite units. No blood or gore depicted at this resolution. |
| Realistic depictions of violence toward humans | Yes, stylised. Infantry units can be destroyed. |
| Sexual content, nudity | No |
| Profanity, crude humour | No |
| Controlled substances, gambling | No |
| Horror or fear elements | No |
| User interaction / shares location / digital purchases | No, no, no |

**One item needs a judgement call and must not be guessed at:** the game's live-action
briefing videos are part of the user's own data files, not shipped by the app, but they are
displayed by it. They contain live-action war footage and depictions of armed conflict. Answer
the questionnaire on the basis that the app *can* display that material. Expected outcome is
roughly **Teen / PEGI 12**, but IARC assigns the rating, not us.

---

## 5. Graphic assets — specification and rules

**The binding constraint:** EA's modding guidelines permit plain text naming the game, but
prohibit using EA artwork, logos or trademarks to promote a project. That rules out the C&C
logo, the GDI and Nod emblems, box art, and any rendered EA asset in the icon or feature
graphic.

**Screenshots are the exception worth understanding.** They show the app running, which is
what a store screenshot is for, and every comparable project (Dolphin, ScummVM, RetroArch)
does exactly this. They are not promotional artwork. Keep them to plain gameplay and the
project's own UI.

| Asset | Spec | Status |
|---|---|---|
| App icon | 512×512 PNG, 32-bit | **TO DO** — must be original artwork, no EA emblem |
| Feature graphic | 1024×500 PNG or JPEG | **TO DO** — typographic treatment of "Command Post" is the safe route |
| Phone screenshots | 2–8, min 320px, max 3840px, 16:9 or 9:16 | **TO DO** — capture from device |
| Tablet screenshots | Optional but recommended for the fold | **TO DO** |

### Suggested screenshot shot list

1. Title picker — shows the two-title launcher, which is this project's own UI
2. Mission in progress with the on-screen command bar open
3. Mission in progress with the build sidebar open
4. Unfolded / large-screen view, showing the foldable handling
5. The in-app credits screen — reinforces the open-source and no-affiliation position

Capture unfolded where possible. The folded outer screen is a narrow portrait panel and does
not represent the game well.

---

## 6. Practical blocker before any of this matters

**A new personal developer account cannot publish to production until it has run a closed test
with at least 12 testers, opted in continuously for 14 days.** Organisation accounts
registered to a legal business entity are exempt.

Source: https://support.google.com/googleplay/android-developer/answer/14151465

This is a real gate, not a formality — testers must actually opt in and stay opted in, and
dropping below twelve resets the clock. Budget three weeks minimum from account creation to a
public listing.

**The organisation-account exemption is tempting and should not be taken casually.** Michael
holds business entities, but publishing a Command & Conquer port under a commercial entity
invites exactly the "commercial use" reading that EA's guidelines restrict. Publishing free,
personally, and accepting the tester gate is the more conservative path. Flagged rather than
decided.

---

## 7. Pre-submission checklist

Build and compliance:

- [x] Release APK signed with Michael's keystore, verified `CN=Michael Pricharda`
- [x] `versionCode` 2 / `versionName` 1.0
- [x] No EA assets in repository, APK, or any build artifact
- [x] `License.txt` shipped in the APK with EA's §7 terms intact
- [x] `THIRD-PARTY-LICENSES.txt` shipped, in-app credits screen present
- [x] Build marked as a modified version, not the original
- [x] EA disclaimer in README and root-level licence file
- [x] Complete corresponding source published (GPL v3 §6)
- [x] `PRIVACY.md` written and publicly reachable

Still to do before submitting:

- [ ] Capture screenshots on the unfolded device
- [ ] Produce 512×512 icon — original artwork only
- [ ] Produce 1024×500 feature graphic — typographic, no EA imagery
- [ ] Build an **App Bundle (.aab)**, not an APK — Play requires AAB for new apps
- [ ] Decide personal vs organisation account (see §6)
- [ ] Recruit 12 testers if publishing personally
- [ ] Re-read the full description one final time against EA's guidelines as they stand on
      the submission date, not as they stood today

**On the last item:** EA revised these guidelines once already, in February 2025, and tied the
revision explicitly to the source release. Assume they can change again and check rather than
remember.
