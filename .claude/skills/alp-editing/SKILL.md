---
name: alp-editing
description: Safe editing of AnyLogic model files in the LUX/Zero repos. Use whenever creating, modifying, or reviewing .alp, .alpx, or anything under _alp/ (AOC.*.xml, Variables.xml, Code/*.java, Class.*.java, ModelResources.xml) — including code changes embedded in XML CDATA and monolithic project .alp files.
---

# Editing AnyLogic model files (LUX)

AnyLogic model source is XML with embedded Java. Wrong edits corrupt models silently — follow the tiers and always run the checklist. Anatomy details: `../lux-vault/lux-vault/10-architecture/alpx-file-format.md`. Format by repo: core repos, the project template, and new projects use the split `.alpx` + `_alp/` format; only older projects, created before the template's conversion, are monolithic `.alp`.

## Editability tiers

**Tier 1 — plain Java, edit freely (as normal code):**
- `_alp/Classes/Class.<Name>.java` — ordinary classes (`I_*`, `J_*`, mixins)
- `_alp/Agents/<Name>/Code/AdditionalClassCode.java` — free-form members injected into the generated agent class
- `_alp/Agents/<Name>/Code/Functions.java` and `Events.java` — function/event **bodies**, each delimited by `/*ALCODESTART::<Id>*/` ... `/*ALCODEEND*/`. Edit ONLY between the markers; never touch the markers or their Ids (they pair with declarations in `Functions.xml`/`Events.xml`). Bodies may reference `p_*`/`v_*` fields defined in the agent's XML — grep the agent folder to confirm a referenced field exists before using it
- Adding a NEW function, or changing a signature/arguments, is NOT a body edit: declarations live in `Functions.xml` with Ids → Tier 3 (IDE)

**Tier 2 — code inside XML CDATA, edit the code only:**
- Function bodies, imports, parameter default expressions, generic parameters inside `AOC.<Name>.xml` / `.alpx` / monolithic `.alp`
- Edit strictly *between* `<![CDATA[` and `]]>`. If the code must contain the sequence `]]>`, stop — that needs CDATA splitting; flag it to the user instead of improvising

**Tier 3 — structural XML, do not author by hand:**
- New agents, parameters, variables, functions, events, presentation elements, option lists → create them **in the AnyLogic IDE**, then fill in bodies via Tier 1/2. Hand-built structural XML (with invented `<Id>` values) is the classic way to corrupt a model
- Exception: none by default. If the user explicitly wants a structural edit anyway, copy an existing sibling element exactly, generate a unique 13-digit Id, and say clearly this is unsupported territory

## Check the Ignore flag before reading or editing

An element whose XML declaration contains `<ExcludeFromBuild>true</ExcludeFromBuild>` is **ignored** in AnyLogic: greyed out, not compiled, not part of the model. This applies to functions, events, variables, parameters, embedded objects, and entire agent types (the flag then sits at the top of `AOC.<Name>.xml`).

The team uses Ignore as its retirement mechanism instead of deleting, so dead code stays in the tree and reads as live. Before you rely on a function or field you found in the source:

```bash
# agent members (functions, events, variables, parameters, whole agent types)
grep -A2 -B8 'ExcludeFromBuild' _alp/Agents/<Agent>/Code/Functions.xml

# Java classes and option lists - declared in the .alpx ROOT file, NOT under _alp/
grep -B8 'ExcludeFromBuild' *.alpx
```

⚠ **Check both locations.** `_alp/` holds agent members; the `.alpx` root holds `JavaClass` and `OptionList` declarations. A scan of `_alp/` alone will report retired `J_*` classes, mixins and option lists as live — including whole strategy classes. AnyLogic also groups retired code in folders named `Deprecated` / `Unused*`, which is a useful corroborating signal.

- Never infer behavior from an ignored element, and never cite one as evidence for how the model works.
- **But don't propose deleting one either.** Ignored can mean retired *or* planned groundwork — the source doesn't distinguish. Ask.
- Its `Code/*.java` body still exists — presence of a body proves nothing.
- Editing an ignored element's body is almost always pointless; say so rather than doing it silently.
- Un-ignoring something is a structural change (Tier 3, IDE) and usually needs its call sites uncommented too.

## Never

- Change any existing `<Id>` value (AnyLogic's internal cross-references)
- Edit, remove, or duplicate `/*ALCODESTART::<Id>*/` / `/*ALCODEEND*/` markers
- Edit `database/` (HSQLDB), `cache/`, `*.bak`, `*.class`, jars
- Touch `<Presentation>`/coordinates/`Levels/` except for deliberate UI work — see below
- Add a file/jar resource without declaring it in `_alp/ModelResources.xml`
- Reformat/re-indent XML wholesale — diffs must stay minimal and reviewable

## Editing presentation (`Levels/`)

Permitted, but only against a clear brief. The rules:

- **Element order is draw order.** Whether a text appears in front of or behind a rectangle depends on its position in the file, so reordering elements changes what the user sees. Never reorder for tidiness.
- Edit `Levels/` when the modeller has **clearly described the visual result they want**. If the intent is ambiguous — which element should be on top, what should move, how it should look — **ask before editing**. Guessing at someone's visual intent is not a diff they can review meaningfully.
- Correspondingly, describe presentation changes in terms of the visible outcome ("the label now draws over the panel"), not just the XML delta.

## Monolithic `.alp` (legacy project repos only)

One giant XML document — projects created before the template's conversion. Locate the target element by searching for the agent/function name; make surgical CDATA/code edits only; anything structural → IDE. Keep a copy of the original section in the conversation before editing so you can restore precisely. Suggest converting the project to `.alpx` before substantial new work.

## Post-edit checklist (always, in order)

1. XML still well-formed — run a parser over every touched XML file, e.g.: `python -c "import xml.dom.minidom,sys; xml.dom.minidom.parse(sys.argv[1])" <file>`
2. `git diff` review: no `<Id>` lines changed, no unintended files (database/, .bak), diff minimal
3. CDATA sections intact (count `<![CDATA[` vs `]]>` per file); ALCODE marker pairs intact in touched `Code/*.java` (count `ALCODESTART` vs `ALCODEEND`)
4. References used in edited Java exist (grep for `p_*`/`v_*`/class names)
5. Tell the user the final gate: **reload the project in AnyLogic** — external edits are only proven when AnyLogic regenerates, compiles, and the model runs. Never claim a model-level change "works" before that
