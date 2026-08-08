# Command Post

A native Android port of **Command & Conquer: Tiberian Dawn** and **Command & Conquer: Red Alert**,
built on the [Vanilla-Conquer](https://github.com/Vanilla-Conquer/Vanilla-Conquer) engine.

Two titles, one app:

| Title | Engine | Launcher name |
|---|---|---|
| Tiberian Dawn (+ Covert Operations) | `vanillatd` | **Dawn** |
| Red Alert (+ Counterstrike, Aftermath) | `vanillara` | **Red** |

Package id `dev.pricharda.commandpost`. Built and tested on a Pixel 9 Pro Fold.

---

## ⚠️ This repository contains no game data

**You must already own Command & Conquer.** No `.MIX` file, movie, audio track or
other game asset is in this repository or in any build produced from it. The port
is engine code only. You supply the data files from your own legally obtained copy.

This is a hard rule in this project, not a preference.

---

## What this is, and what it is not

**Not custom — inherited from Vanilla-Conquer and Electronic Arts:**

- The entire game engine. All gameplay, AI, pathfinding, balance, missions, rules.
- The rendering, audio and file-format layers.
- Everything both games do. No gameplay behaviour has been altered.

**Custom — written for this project:**

Everything below lives in the platform layer (`common/`, `android/`) except where noted.
The rule followed throughout was to change how the game is *driven*, never what it *does*.

| Area | What was built |
|---|---|
| Android app | Launcher with title picker, per-title save directories, process isolation so each engine gets a clean address space |
| Touch input | Complete gesture layer — tap, long-press, drag-select, two-finger tap, hold-to-pan |
| Command bar | On-screen overlay exposing keyboard-only commands that a touch device otherwise cannot reach, including latching modifier keys |
| Controller | Full gamepad mapping with context-sensitive behaviour, built on the engine's existing SDL controller support |
| Audio | OpenAL Soft wired into the Android build |
| Display | Fold-aware rescaling, aspect handling, 60 fps cap on battery |
| Soft keyboard | Text entry for save names and high-score entry |
| Licensing | Licence texts shipped inside the APK with an in-app credits screen |

**Two deliberate exceptions to platform-layer-only**, both documented in `docs/DECISIONS.md`:

1. `tiberiandawn/score.cpp`, `redalert/score.cpp` — the high-score name entry reads raw keys
   and bypasses the normal widget path, so the soft keyboard had to be opened from there.
   Without it the score screen is a dead end on a touch device.
2. `tiberiandawn/conquer.cpp`, `redalert/conquer.cpp` — the sidebar's stock `TAB` binding sits
   on a key path that the gadget layer intercepts during a mission. The same binding, calling
   the same function, was moved onto the key path that works. No new behaviour.

---

## Playing

### Touch

| Gesture | Action |
|---|---|
| Tap | Left click — select, move, attack, press buttons |
| Long press | Right click — deselect, cancel placement, exit repair/sell mode |
| Drag | Drag-select a group of units |
| Hold, then move | Scroll the map |
| Two-finger tap | Escape — skips movies, opens the menu |

Text entry (save names, high scores) opens the Android keyboard automatically.

### The command bar

A glass panel on the left edge, visible only during a mission. Tap the tab to open it.
It exists because roughly sixteen engine commands are keyboard-only and unreachable by touch.

| Button | Sends | Notes |
|---|---|---|
| `ADD` | Shift | **Latching** — stays held. Add units to the current selection |
| `TEAM` | Ctrl | **Latching.** Hold, then press a number to *create* that team |
| `GOTO` | Alt | **Latching.** Hold, then a number to recall and centre on that team |
| `1`–`0` | Team slots | Press alone to select a team |
| `STOP` | S | Stop |
| `GUARD` | G | Guard |
| `SCAT` | X | Scatter |
| `FORM` | F | Formation — **Red Alert only**, dimmed in Tiberian Dawn |
| `QUEUE` | Q | **Latching.** Queue waypoints — **Red Alert only** |
| `NEXT` / `PREV` | N / B | Cycle through your units |
| `BASE` | H | Centre on your base |
| `ALL` | E | Select everything on screen |
| `MAP` | U | Radar zoom — needs an active radar to do anything |

Labels describe the *effect*, not the key, because "Shift" and "Ctrl" do not tell you
that Ctrl is the one that creates a team.

### Controller

Tested with a **Nacon MG-X Pro (NC7273)**. Any SDL-recognised gamepad should work —
the app logs on startup whether SDL accepted your pad, which is the usual failure point.

**The controller never replaces touch.** Both are live at once and share one cursor:
tap the screen to place it, nudge it with the stick. Plug in or unplug at any time.

| Control | In a mission | In menus |
|---|---|---|
| **Left stick** | Move the cursor | Move the cursor |
| **Right stick** | Scroll the map | — |
| **RT** | Cursor speed boost, proportional | — |
| **LT** | Ctrl — force fire, and **create a team** | — |
| **A** | Left click | Click, **or** confirm the highlighted item if you last used the D-pad |
| **B** | Right click — deselect, cancel, exit repair/sell | Left click |
| **X** | Scatter | — |
| **Y** | Guard | — |
| **LB** / **RB** | Previous / next unit | — |
| **L3** (left stick click) | Centre on base | — |
| **R3** (right stick click) | Alt — force move, recall team | — |
| **BACK / Select** | Select everything on screen | — |
| **START** | Escape | Escape |
| **D-pad ↑ / ↓** | Scroll the game's build sidebar | Move the menu selection |
| **D-pad ←** | Open / close **the command bar** (this project's overlay) | Left — difficulty, sliders |
| **D-pad →** | Open / close **the game's build sidebar** | Right — difficulty, sliders |

**Two different sidebars, two different buttons.** The command bar is this project's
shortcut overlay. The build sidebar is the game's own. They are unrelated.

**Why A does two things.** The D-pad moves a *keyboard highlight*; a click acts on whatever
is under the *cursor*. These are separate systems in this engine, so A follows whichever you
touched last — stick or screen means click, D-pad means confirm. B is always a plain click
outside a mission if you want to force it.

**Known limitation:** the difficulty control is a `SliderClass`, a mouse-only widget. No
widget in this engine responds to arrow keys, so the D-pad cannot drive it. Move the cursor
onto the slider and click.

---

## Building

Requires the Android SDK and NDK. The native build is driven by CMake through Gradle.

```
cd android
./gradlew assembleDebug
```

Release builds are signed from a keystore kept **outside** this repository, located via the
`CNC_KEYSTORE_PROPS` environment variable. No signing material is committed.

Desktop builds are unaffected — see [README-VANILLA-CONQUER.md](README-VANILLA-CONQUER.md)
for upstream's platform matrix and build instructions.

### Installing game data

Copy the data files from your own copy of the game to:

```
Android/data/dev.pricharda.commandpost/files/vanillatd/     Tiberian Dawn
Android/data/dev.pricharda.commandpost/files/vanillara/     Red Alert
```

Expansions go in the same directory as their parent title.

**Copy the files with the phone plugged in as a media device, or from a file manager on the
phone itself.** Do not place them with `adb shell cp` from another location on the device:
files created that way are owned by the shell user, the app cannot read them through scoped
storage, and the engine crashes on the first missing file rather than reporting it. If that
happens, the fix is `chmod 777` on the two title directories and `chmod 666` on their
contents - or simply recopy them properly.

**Reinstalling deletes your game data.** Android removes
`Android/data/dev.pricharda.commandpost` on uninstall, and switching between a debug and a
release build requires an uninstall because they are signed with different keys. Back the
directory up first if you do not want to copy 950 MB again. Saves live in the app's private
storage and cannot be recovered once uninstalled.

---

## Attribution and licensing

**This is a modified version of Vanilla-Conquer.** It is not the original software.

- **Original engine source** released by **Electronic Arts** under the **GNU General Public
  License version 3** with additional terms under GPL §7. Those terms include: no use of EA
  trademarks, modified versions must be marked as such, and the copyright notice must be
  retained. This project is marked as modified here and in `License.txt`.
- **Vanilla-Conquer** — the portable engine fork this is built on, by
  [The Assembly Armada](https://github.com/Vanilla-Conquer/Vanilla-Conquer). All engine
  portability work is theirs.
- **SDL2** — zlib licence.
- **OpenAL Soft** — LGPL.

Full texts are in `License.txt` and `THIRD-PARTY-LICENSES.txt`, both shipped inside the APK
and readable from the in-app credits screen.

**Command & Conquer, Tiberian Dawn, Red Alert and Westwood Studios are trademarks of
Electronic Arts Inc.** This project is not affiliated with, endorsed by, or sponsored by
Electronic Arts. No EA trademark is used in the app name or branding.

---

## Project documentation

- `docs/DECISIONS.md` — every design decision, why it was taken, and what was verified
- `docs/RECON.md` — the failure ledger: what broke, the root cause, and the rule that prevents it
- `docs/KEYBOARD-AUDIT.md` — every engine keyboard command and how it is reached on Android
