# Keyboard command audit — what the engine offers vs. what touch can reach

**Authored:** 2026-08-03 · **Basis:** read directly from the source tree, not from memory or
any wiki. Bindings from `tiberiandawn/options.cpp:85-140` (the `OptionsClass` constructor).
Handlers confirmed live in `tiberiandawn/conquer.cpp:429-800` (`Keyboard_Process`).

**Why this exists.** Michael asked whether the port is missing anything important. It is. This
document is the complete list, with the honest verdict on each — not a summary of the ones that
happen to work.

---

## 1. The complete binding table, verbatim from the engine

Every one of these was verified to have a **live handler**, not merely a declaration.

| Key | Setting | Function | Handler |
|---|---|---|---|
| `N` | `KeyNext` | select next unit | `conquer.cpp:494` |
| `B` | `KeyPrevious` | select previous unit | `:508` |
| `S` | `KeyStop` | stop / halt | `:526` |
| `G` | `KeyGuard` | guard mode | `:543` |
| `X` | `KeyScatter` | scatter | `:559` |
| `Home` | `KeyHome1/2` | centre view on base | `:576` |
| `H` | `KeyBase` | go to base | `:590` |
| `F` | `KeyFormation` | formation move toggle | `:630` |
| `R` | `KeyResign` | resign mission | `:634` |
| `A` | `KeyAlliance` | ally (multiplayer only) | `:644` |
| `E` | `KeySelectView` | select view | `:659` |
| `T` | `KeyRepair` | repair mode | `:667` |
| `Y` | `KeySell` | sell mode | `:675` |
| `U` | `KeyMap` | toggle radar map | `:683` |
| `↑` | `KeySidebarUp` | scroll sidebar up | `:692` |
| `↓` | `KeySidebarDown` | scroll sidebar down | `:701` |
| `Esc` | `KeyOption1` | options menu | `:709` |
| `Space` | `KeyOption2` | options menu | `:709` |
| `1`–`0` | `KeyTeam1`–`KeyTeam10` | **team groups** | `:742-778` |
| `F9`–`F12` | `KeyBookmark1-4` | view bookmarks | `:786-798` |
| `Q` | `KeyQueueMove1/2` | queued movement | declared |

### Modifiers — these are not keys, they are bits on the input

`conquer.cpp:484-488` reads them off the input word:

| Modifier | Setting | Effect |
|---|---|---|
| `Shift` | `KeySelect1/2` | **add to selection**; with a team key, add to team |
| `Ctrl` | `KeyForceAttack1/2` | **force attack**; with a team key, create team |
| `Alt` | `KeyForceMove1/2` | **force move**; with a team key, create team |

**Michael asked specifically about "group, hunt, hide".**
- **Group** — yes, `1`–`0`, and it is the single biggest gap in this port. Confirmed below.
- **Hunt** and **Hide** — **not present in Tiberian Dawn.** No such binding exists in
  `OptionsClass`. Hunt is a mission/AI state in the engine internals, not a player command.
  Those belong to later titles in the series, not this one.

---

## 2. What touch can actually reach today

| Command | Reachable? | How |
|---|---|---|
| Left click, drag-select | **yes** | tap, drag |
| Right click / cancel chain | **yes** | long-press (D-27) |
| Options menu (`Esc`) | **yes** | two-finger tap |
| Repair (`T`) | **yes** | on-screen sidebar button |
| Sell (`Y`) | **yes** | on-screen sidebar button |
| Radar map (`U`) | **yes** | on-screen sidebar button |
| Sidebar scroll (`↑`/`↓`) | **yes** | on-screen sidebar arrows |
| Map scroll | **yes** | hold-and-slide |
| **Team groups `1`–`0`** | **NO** | — |
| **Shift — add to selection** | **NO** | — |
| **Ctrl — force attack** | **NO** | — |
| **Alt — force move** | **NO** | — |
| **`S` stop** | **NO** | — |
| **`G` guard** | **NO** | — |
| **`X` scatter** | **NO** | — |
| **`N`/`B` next/prev unit** | **NO** | — |
| **`F` formation** | **NO** | — |
| **`H` / `Home` go to base** | **NO** | — |
| **`E` select view** | **NO** | — |
| **`F9`–`F12` bookmarks** | **NO** | — |
| **`Q` queued move** | **NO** | — |
| **`R` resign** | **NO** | — |

**Sixteen live commands are unreachable.** The port is fully playable without them — Michael
completed a mission — but several are not conveniences.

---

## 3. The four that actually matter

Ranked by how much their absence changes play, not by how easy they are to add.

**1. Team groups (`Ctrl`+`1-0` to create, `1-0` to recall).** This is the core control idiom of
the entire RTS genre. Without it, every engagement means re-selecting units by hand. On a phone,
where selection is already slower than a mouse, this hurts more than it does on desktop, not less.

**2. Shift — add to selection.** The only way to build a mixed group without a marquee box that
happens to enclose exactly the right units. Currently any new selection discards the old one.

**3. Ctrl — force attack.** Required to attack the ground, to attack neutral or allied targets, and
to fire on structures the engine would otherwise route you around. There are missions where this is
the intended solution to an obstacle.

**4. Alt — force move.** Move into a cell the pathfinder would refuse — driving over infantry,
entering contested ground deliberately.

`S` (stop) and `G` (guard) are the next tier: frequently used, but not load-bearing the way the
four above are.

---

## 4. Options, best first

**A. On-screen command bar.** A compact, toggleable overlay along one edge carrying the high-value
commands, plus a modifier latch for Shift / Ctrl / Alt that applies to the next tap. Ten team-group
slots would need their own affordance — likely a second row or a long-press on a group button to
assign.

*For:* discoverable, no gesture memorisation, works for every command in the table.
*Against:* it is real UI on a small screen, and it costs play area. It is also the largest piece of
work proposed in this port so far.

**B. Summon the soft keyboard in-game.** The plumbing already exists —
`SDL_StartTextInput()` is wired for text fields (D-29) and for high-score entry (F-15). A gesture
could raise the keyboard mid-game and every single binding above would work exactly as on desktop.

*For:* very small change; complete coverage immediately; nothing to design.
*Against:* a full keyboard over a phone screen during play is clumsy, and the modifiers still need
chording, which is awkward on a soft keyboard.

**C. More gestures.** Three-finger tap, double-tap, edge swipes.

*For:* no screen space consumed.
*Against:* undiscoverable, and the gesture space is nearly exhausted — tap, drag, hold-and-slide,
long-press and two-finger tap are all taken. This does not scale to sixteen commands and cannot
address team groups at all.

**Recommendation: A, with B as an interim.** B could ship in under an hour and unblocks everything;
A is the version that is actually good on a touch device. They are not exclusive.

---

## 5. Honest scope note

None of this is required for Gate 7. The port is playable and a mission has been completed without
any of it. This is a **playability gap, not a defect** — nothing here is broken, it is absent.

Michael asked whether anything important was missing. Sixteen commands are, and four of them
materially change how the game plays. Recording that plainly is more useful than reporting that the
port works, which it does.
