---
name: lux-architecture
description: LUX layering decisions and knowledge-vault consultation. Use when starting feature work, bugfixes, or refactors in any LUX/Zero repo; when deciding which repo/layer a change belongs in; for cross-package questions (engine ↔ loader ↔ resultsUI ↔ project); or when unsure whether project-specific work is leaking into core repos.
---

# LUX architecture decisions

Full rules: `../lux-vault/lux-vault/10-architecture/layering-and-override-rules.md` (read it for anything consequential). Operating summary:

## Which layer does this change belong in?

1. Project-specific (one customer/model needs it)? → **project repo**: child loader/interface override or project data. Core repos stay untouched.
2. Override can't express it? → smallest viable core change, preferring **loader override surface** (additive!) over engine/resultsUI internals. This is now Workflow A: generic implementation only + ADR (`/adr`).
3. Two+ projects carry the same override? → promote it into core as a generic feature (Workflow A + ADR).
4. Bug in core? → fix in the owning repo, generic, with the impact check below.

## Impact check before any core commit

- Engine changed → loader still fills populations? resultsUI still reads its result structures? template runs unmodified?
- Loader/interface changed → existing child loaders still compile (signatures/collections are the override surface)? `*_data` ↔ Excel expectations intact?
- ResultsUI changed → interface still configures/connects it?
- Any core repo → zero project-specific content leaked (names, data, functions, images)?

Run the `lux-reviewer` agent on the diff as the final step.

## Consulting the vault (token-lean)

Sibling checkout `../lux-vault/lux-vault/`:

- Module semantics → grep the module name in `20-engine/`, `21-loader-interface/`, `22-results-ui/`, `23-project-layer/`
- Wiring/contract questions → `10-architecture/` (data-flow, contracts, override map)
- Naming/prefix meaning → `30-conventions/naming-semantics.md`
- How-to workflows → `40-playbooks/`
- Past decisions → grep `50-decisions/`

Read only the notes you need. Trust: `verified` > `unverified` > guessing; **code beats notes** — on conflict, follow code and capture the correction with `/learn`. Notes cite `source-files`: for load-bearing claims in unverified notes, spot-check the cited source before relying on it.
