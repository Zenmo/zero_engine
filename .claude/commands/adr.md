---
description: Record an architecture decision in the lux-vault (PR-gated)
argument-hint: <short decision title>
---

Record an architecture decision record for: $ARGUMENTS

1. Reconstruct from this session (ask the user if unclear): the context forcing the decision, the decision itself stated as a followable rule, consequences, and alternatives considered with why they lost.
2. Create `../lux-vault/lux-vault/50-decisions/adr-<YYYYMMDD>-<slug>.md` following `00-meta/templates/adr.md` (frontmatter `type: adr`, `status: unverified`, ADR body **Status**: proposed).
3. If the decision changes what `10-architecture/layering-and-override-rules.md` or a CLAUDE.md block says, note that explicitly in the ADR under Consequences — do not edit those files here; `/consolidate` or a human does that after acceptance.
4. In `../lux-vault`: branch `adr/<date>-<slug>`, commit only the ADR, push, `gh pr create` (title `adr: <title>`). Report the PR URL; on failure leave the branch local with instructions.

An ADR is warranted for: core-repo interface changes, new override patterns, verification-strategy choices, anything two future developers could otherwise re-litigate. Not for routine implementation choices.
