---
description: Merge the learnings inbox into canonical vault notes (PR-gated)
argument-hint: [optional: max number of learnings to process]
---

Consolidate the vault's learning inbox into canonical notes. Limit if given: $ARGUMENTS

Use the `vault-librarian` agent for the analysis/merge work. Overall flow:

1. **Inventory** `../lux-vault/lux-vault/60-learnings/inbox/` (skip `.gitkeep`). Empty → report and stop.
2. **Branch first**: in `../lux-vault`, create `consolidate/<YYYY-MM-DD>` from up-to-date `main`. All edits below happen on this branch.
3. **Per learning** (oldest first):
   - Read it; identify the merge target (stated in the note, else infer from content; create a new module/architecture note from the template if none fits).
   - Merge the durable insight into the target note's proper section (Gotchas, Constraints, Extension points...). Rewrite for canonical tone: timeless, self-contained, no session narrative. Deduplicate against what's already there.
   - **Any canonical note edited this way gets `status: unverified`** (even if it was verified) and today's `last-reviewed` — a human re-verifies via the review queue.
   - **Contradictions**: if a learning conflicts with a `verified` claim, do NOT silently overwrite. Keep both, mark the disputed claim with `⚠️ disputed:`, and list it in the PR body under "Needs human ruling".
   - Move the processed learning to `60-learnings/archive/<YYYY>/` (create if needed).
4. **PR**: commit, push, `gh pr create` titled `consolidate: <date>` with a body listing every merge (learning → target note), contradictions needing ruling, and new notes created. Report the URL; on failure leave the branch local with instructions.

Never edit `00-meta/` templates or the vault guide here. Never delete a learning without archiving it.
