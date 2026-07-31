# RECON-SEED.md — pre-cleared findings for Phase 0

Handed to the Phase 0 agent so it does not re-run work already done. This is a SEED, not
docs/RECON.md. The Phase 0 agent still produces RECON.md and still owns Gate 0.

## Asset source — Phase 0 step 4, partially pre-cleared

VERIFIED by directory listing on the host, 2026-07-31:

The machine carries a licensed **C&C Remastered Collection** install at
`C:\Program Files\EA Games\CnCRemastered\`. Its `DATA\CNCDATA\` tree ships the **original
legacy game data**, not the Remastered `.MEG` archives:

**TIBERIAN_DAWN** — trees: `CD1`, `CD2`, `CD3`, `CONSOLE_1`, `CONSOLE_2`, `COMMUNITY`, `CUSTOMMAPS`
MIX payload includes: `CONQUER.MIX`, `DESERT.MIX`, `TEMPERAT.MIX`, `WINTER.MIX`, `SOUNDS.MIX`,
`SPEECH.MIX`, `SCORES.MIX`, `MOVIES.MIX`, `LOCAL.MIX`, `TRANSIT.MIX`, `GENERAL.MIX`,
`UPDATE.MIX` / `UPDATEC.MIX` / `UPDATA.MIX`, plus `TEMPERAT.PAL`.

**RED_ALERT** — trees: `CD1`, `AFTERMATH`, `COUNTERSTRIKE`, `COMMUNITY`, `CUSTOMMAPS`
MIX payload includes: `MAIN.MIX`, `REDALERT.MIX`, `EXPAND.MIX`, `EXPAND2.MIX`, `HIRES1.MIX`,
`LORES1.MIX`, plus `REDALERT.INI`.

**ASSUMPTION requiring the Phase 0 agent's verification:** that Vanilla-Conquer consumes exactly
this legacy MIX set. Probe: the upstream README asset table and `vanillatd` runtime asset
requirements. Do not proceed on my word.

**Still open for Phase 0 step 4:** whether the user-supply procedure should point at this
Remastered-bundled legacy data or at the 2007 C&C Gold freeware release. Both are plausible
sources; the procedure documented in RECON.md should name one as primary and the other as
fallback. Neither path puts an asset in the repo.

## Toolchain — feeds Phase 2, see docs/TOOLCHAIN.md

Host audit is complete and recorded in `docs/TOOLCHAIN.md`. Short version: git is present,
everything Android is absent. Gate 2 is install-then-pin. Logged as D-3.

## Not yet done — the Phase 0 agent owns all of this

- Step 1: GitHub search for existing Android forks / branches / SDL2 C&C ports / OpenRA Android issues
- Step 2: verify every claim against CI configs and release artifacts, not READMEs
- Step 3: pull `PORTING_PLAYBOOK.md` + `PORTING_PATTERNS.md` from `ammaarreshi/Generals-Mac-iOS-iPad`
  into `docs/reference/`
- Step 5: author `docs/RECON.md`

**If a working Android fork of this engine already exists: STOP and re-plan from it.**
