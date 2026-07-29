---
description: Capture durable learnings from this session into the lux-vault (PR-gated)
argument-hint: [optional focus, e.g. "the loader signature issue"]
---

Capture what this session learned into the knowledge vault. Focus if given: $ARGUMENTS

## 1. Extract

Reflect over the whole session and list candidate learnings. A learning qualifies only if it is **durable and reusable**: a surprise, a contradiction with a vault note or CLAUDE.md, a gotcha, a failed approach worth not repeating, a discovered constraint/convention, or a verified fact that was previously an open question. Task minutiae, one-off details, and things already correctly documented do NOT qualify. If nothing qualifies, say so and stop — an empty /learn is a fine outcome.

## 2. Locate the vault

The vault repo is the sibling checkout `../lux-vault` (notes under `lux-vault/`). If it's missing, tell the user and stop.

## 3. Draft

For each learning (usually 1, rarely >2): create `../lux-vault/lux-vault/60-learnings/inbox/<YYYY-MM-DD>-<short-slug>.md` following `00-meta/templates/learning-note.md`. Requirements:

- Frontmatter: `type: learning`, `repo:` where the work happened, `status: unverified`, real dates, `source-files:` with exact paths.
- **The learning** section must be self-contained: a future session should be able to apply it without this conversation.
- **Merge target**: name the canonical note(s) it should be folded into by `/consolidate` (e.g. `[[layering-and-override-rules]]`, a module note). If the right canonical note doesn't exist yet, say which section it belongs in.
- If the learning **contradicts** a `verified` note, quote the conflicting claim and set the note's `status: outdated` is NOT your call — instead flag the contradiction prominently in the learning note.

## 4. PR (never commit to main)

In `../lux-vault`: create branch `learn/<date>-<slug>` from `main`, commit **only** the new inbox note(s), push, then `gh pr create` with a title like `learn: <slug>` and a body summarizing the insight + context. Report the PR URL. If push/`gh` fails, leave the branch local and give the user the exact commands to finish.

Rules: never modify canonical notes or anything outside `60-learnings/inbox/` in this command; never touch the user's working branch in the code repo.
