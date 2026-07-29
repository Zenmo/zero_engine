---
name: alp-editing
description: Safe editing of AnyLogic model files in the LUX/Zero repos. Use whenever creating, modifying, or reviewing .alp, .alpx, or anything under _alp/ (AOC.*.xml, Variables.xml, Code/*.java, Class.*.java, ModelResources.xml) — including code changes embedded in XML CDATA and monolithic project .alp files.
---

# Editing AnyLogic model files (LUX)

AnyLogic model source is XML with embedded Java. Wrong edits corrupt models silently — follow the tiers and always run the checklist. Anatomy details: `../lux-vault/lux-vault/10-architecture/alpx-file-format.md`.

## Editability tiers

**Tier 1 — plain Java, edit freely (as normal code):**
- `_alp/Classes/Class.<Name>.java` — ordinary classes (`I_*`, `J_*`, mixins)
- `_alp/Agents/<Name>/Code/*.java` (e.g. `AdditionalClassCode.java`) — members injected into the generated agent class; they may reference `p_*` parameters / `v_*` variables defined in the agent's XML — grep the agent folder to confirm a referenced field exists before using it

**Tier 2 — code inside XML CDATA, edit the code only:**
- Function bodies, imports, parameter default expressions, generic parameters inside `AOC.<Name>.xml` / `.alpx` / monolithic `.alp`
- Edit strictly *between* `<![CDATA[` and `]]>`. If the code must contain the sequence `]]>`, stop — that needs CDATA splitting; flag it to the user instead of improvising

**Tier 3 — structural XML, do not author by hand:**
- New agents, parameters, variables, functions, events, presentation elements, option lists → create them **in the AnyLogic IDE**, then fill in bodies via Tier 1/2. Hand-built structural XML (with invented `<Id>` values) is the classic way to corrupt a model
- Exception: none by default. If the user explicitly wants a structural edit anyway, copy an existing sibling element exactly, generate a unique 13-digit Id, and say clearly this is unsupported territory

## Never

- Change any existing `<Id>` value (AnyLogic's internal cross-references)
- Edit `database/` (HSQLDB), `cache/`, `*.bak`, `*.class`, jars
- Touch `<Presentation>`/coordinates/`Levels/` except for deliberate UI work
- Add a file/jar resource without declaring it in `_alp/ModelResources.xml`
- Reformat/re-indent XML wholesale — diffs must stay minimal and reviewable

## Monolithic `.alp` (project repos)

One giant XML document. Locate the target element by searching for the agent/function name; make surgical CDATA/code edits only; anything structural → IDE. Keep a copy of the original section in the conversation before editing so you can restore precisely.

## Post-edit checklist (always, in order)

1. XML still well-formed — run a parser over every touched XML file, e.g.: `python -c "import xml.dom.minidom,sys; xml.dom.minidom.parse(sys.argv[1])" <file>`
2. `git diff` review: no `<Id>` lines changed, no unintended files (database/, .bak), diff minimal
3. CDATA sections intact (count `<![CDATA[` vs `]]>` per file)
4. References used in edited Java exist (grep for `p_*`/`v_*`/class names)
5. Tell the user the final gate: **reload the project in AnyLogic** — external edits are only proven when AnyLogic regenerates, compiles, and the model runs. Never claim a model-level change "works" before that
