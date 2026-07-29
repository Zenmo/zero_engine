<!-- GENERATED FILE - synced from lux-vault/claude/. Do not edit here: edit lux-vault/claude/ and re-run sync. -->

# LUX shared context (all Zero/LUX repos)

LUX Energy Twin: an open-source, agent-based AnyLogic model of energy systems, focused on electricity and grid congestion. Power (kW) balance only — no current/voltage; congestion only at grid nodes, never cables; 15-min timesteps; ≤ 1 year simulated; AnyLogic 8.9.9 / Java 17. Public docs: https://docs.lux.energy/

## The four packages

| Package | Repo | Role |
|---|---|---|
| Engine | zero_engine | Calculation model. Main agent `EnergyModel` owns all populations and accumulated flows |
| Loader/Interface | zero_Interface-Loader | Library: loads data into engine populations; all interface visuals |
| ResultsUI | zero_results_UI | Chart/KPI presentation, configured by the interface |
| Project | private, from LUX_ProjectTemplate | Child loader + child interface + startUp (main); project data; overrides |

## The golden rule (layering)

The three core repos are **generic public libraries** — no project names, no project data, no single-project functions, ever. Project-specific work belongs in the project repo via the child loader/interface. Order of preference for any change: **project repo → loader override surface → engine/resultsUI**. Core changes require the impact check in `../lux-vault/lux-vault/10-architecture/layering-and-override-rules.md`. Loader function signatures are load-bearing: breaking one breaks every customer project.

## AnyLogic files — safety essentials

Model source is XML with embedded Java (split `.alpx` + `_alp/` in core repos; monolithic `.alp` in projects). Before editing any of it, use the `alp-editing` skill. Non-negotiables: never change `<Id>` values; never hand-edit `database/`, `cache/`, `*.bak`, jars; keep XML well-formed and CDATA intact; new files/jars must be declared in `_alp/ModelResources.xml`; structural additions (new agents/parameters/functions) happen in the AnyLogic IDE, not by hand-writing XML. Plain Java in `_alp/Classes/Class.*.java` and `Code/*.java` is safe to edit as normal code.

## Knowledge vault (../lux-vault)

Deep knowledge lives in the sibling checkout `../lux-vault/lux-vault/` (private Obsidian vault). Sections: `10-architecture` (packages, layering, data flow, file formats), `20-engine`/`21-loader-interface`/`22-results-ui`/`23-project-layer` (module notes), `30-conventions` (naming/semantics — prefixes like `GC*`, `J_*`, `I_*`, `OL_*`, `p_*`/`v_*` are load-bearing), `40-playbooks`, `50-decisions` (ADRs), `60-learnings`.

Protocol:
- Consult before architectural decisions, cross-package changes, or unfamiliar modules: Grep the vault for the module/topic, read the matching notes.
- Trust order: note `status: verified` > `unverified` > guessing — and **code beats notes**. If code contradicts a note, that's a learning: capture it.
- If the vault is missing (external contributor), proceed with this file + repo READMEs + https://docs.lux.energy/.

## Verification reality

No headless build exists for the models: AnyLogic compiles on project reload, so the final gate is opening/running in the IDE (or AnyLogic Cloud for rapid-runs). What you *can* do after edits: XML well-formedness checks, reviewing the diff for forbidden changes (`<Id>`, database/), and consistency greps. The loader has a JUnit-in-AnyLogic test model (`tests/Zero_Loader_Test.alpx`) — runs inside AnyLogic, not via Gradle. State honestly what was and wasn't verified.

## Session habits

- Before committing non-trivial changes: run the `lux-reviewer` agent on the diff.
- Consequential design decision made? Record it: `/adr`.
- End of a session that produced durable insight (surprise, contradiction, gotcha, failed approach): offer `/learn` — it PRs the insight into the vault. This loop is how the whole setup improves; don't skip it.

# This repo: zero_engine

The calculation model. Public generic library — the golden rule applies with full force.

## Structure

- `Zero_engine.alpx` — workspace root: model config, option lists (`OL_*`)
- `_alp/Agents/<Name>/` — one folder per agent: `AOC.<Name>.xml` (definition), `Code/AdditionalClassCode.java` (plain-Java members — preferred home for agent logic), `Variables.xml`, `Levels/`
- `_alp/Classes/Class.<Name>.java` — plain Java: `I_*` capability interfaces, `J_*` strategies/data structures, `*Mixin` classes, `ZeroTimeSeries`/`ZeroAccumulator`
- `zeroPackage/` + `ZeroPackage.jar` — plain-Java library compiled outside AnyLogic (BatteryScheduler, Market, ZeroMath). Editing `.java` here does nothing until the jar is rebuilt — check `../lux-vault` for the rebuild procedure before touching it
- `database/` — HSQLDB internals, never edit

## Key semantics

- `EnergyModel` is the main agent; populations are filled **by the loader**, not here. Changing an agent's construction/wiring expectations = loader impact.
- EMS behavior = strategy pattern: `I_<Capability>Management` interfaces + `J_<Capability>Management<Policy>` implementations. New behavior → new strategy class, not flags in existing ones.
- Results accumulate via `J_RapidRunData` / `J_LiveData` and friends; resultsUI reads them. Renaming/retyping result structures = resultsUI impact.
- Known load-bearing reflection hack: `forceSetOwner` in GridConnection's AdditionalClassCode. Don't "clean up" magic without understanding it.

## Before you commit here

1. Loader still fills populations correctly? 2. ResultsUI still finds its data? 3. Template project still runs unmodified? 4. Nothing project-specific leaked in? Run `lux-reviewer`.
