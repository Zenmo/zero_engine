---
description: Generate or refresh a vault module note from source analysis
argument-hint: <repo> <module-name> [pr]
---

Document a module into the knowledge vault: $ARGUMENTS
(first argument = repo, second = module/agent/class name, optional third `pr` = open a PR instead of leaving the file for manual commit)

## 1. Resolve sources

Find the module in the given repo:
- Agent → `_alp/Agents/<Name>/` (read `AOC.<Name>.xml`, `Code/*.java`, `Variables.xml`)
- Plain class → `_alp/Classes/Class.<Name>.java`
- zeroPackage class → `zeroPackage/<Name>.java`
- Project-layer element → inside the monolithic `.alp` (search the XML)

If ambiguous or not found, list candidates and ask.

## 2. Analyze

- Read the sources fully. Extract: purpose, key parameters (`p_*`)/variables (`v_*`)/fields with units, behavior per timestep/event, implemented `I_*` interfaces and injected `J_*` strategies.
- **Wiring (mandatory):** Grep all four sibling repos for the module name to find who instantiates, calls, overrides, or reads it. Cross-package hits are the most valuable content in the note.
- Check the vault's `30-conventions/naming-semantics.md` — flag naming that deviates.

## 3. Check existing coverage

- Existing vault note? Refresh it in place (keep verified human edits unless provably outdated; note conflicts in Open questions).
- Public docs page? Engine modules often have one under `https://docs.lux.energy/dev/engine/...` (grid_nodes, grid_connections, energy_assets, energy_management_systems, energy_model, energy_carriers, energy_data, energy_coops, consistency_checks, option_lists, data_structures/<name>), loader under `/dev/loader/`, interface `/dev/interface/`, resultsUI `/dev/resultsUI/`. **Link it; don't duplicate its content** — the vault note covers what docs don't: constraints, wiring, gotchas, override points.

## 4. Write

Create/update the note in the right section (`20-engine`, `21-loader-interface`, `22-results-ui`, `23-project-layer`) using `00-meta/templates/module-note.md`. Frontmatter: `status: unverified`, exact `source-files`, today's dates. Every claim must trace to a cited source; uncertainties go under Open questions, not stated as fact. Add wiki-links to related notes both ways where obvious.

## 5. Coverage index + delivery

- If `../lux-vault/lux-vault/00-meta/coverage-index.md` exists, mark this module generated (▣) with today's date.
- Default: leave files uncommitted and tell the user what was written. With `pr`: branch `docs/<repo>-<module>`, commit, push, `gh pr create`.
