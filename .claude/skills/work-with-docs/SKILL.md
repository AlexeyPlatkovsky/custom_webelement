---
name: work-with-docs
description: Rules for creating and maintaining project documentation. Use when writing or editing plans, guides, ADRs, or any doc file.
---

## Folder Conventions

| Folder | What goes there |
|---|---|
| `docs/plans/` | All plans: refactoring, implementation, design spikes |
| `docs/guides/` | User-facing how-to docs (setup, configuration, writing pages) |
| `docs/architecture/` | ADRs and structural overviews |
| `docs/ai-features/` | Feature docs for AI capabilities |

Never create plan files at the `docs/` root or in arbitrary subfolders.

## Plan Naming

```
docs/plans/yyyy-MM-dd-<ai-agent-name>-<plan-name>.md
```

- `yyyy-MM-dd` - today's date (absolute, not relative)
- `ai-agent-name` - the agent or author creating the plan (e.g. `claude`, `codex`, `gemini`)
- `plan-name` - short kebab-case description (e.g. `core-refactor`, `page-crawler-phase2`)

Examples:
- `2026-03-22-claude-core-refactor.md`
- `2026-03-22-claude-page-crawler-phase2.md`

## Plan Template

```markdown
# <Plan Title>

**Status:** draft | in-progress | done
**Branch:** `<prefix>/<name>`
**Scope:** <one sentence: what is changing and where>

---

## Overview

<1–2 sentences max. What problem this solves and the approach.>

## Steps

### Step N - <Title> (<risk: Low/Medium/High>)

**Files:** `path/to/File.java`
**Problem:** <what is wrong>
**Change:** <what to do - code snippet if helpful>
**Validation:** `./gradlew ...`

---

## Execution Order

| # | Step | Risk | Effort |
|---|---|---|---|
| 1 | ... | Low | Small |
```

## Lifecycle

- **draft** - plan written, not yet started
- **in-progress** - implementation underway; update the status field as you start
- **done** - all steps implemented and validated; do not delete, just mark done

Do not append new work to an existing plan file. Create a new dated file for each distinct effort.

## Format Rules

- Keep every doc under 150 lines. If it exceeds that, suggest user to split into focused sections or extract a sub-doc and link to it.
- Prefer tables and bullets over narrative prose.
- Use code blocks for commands and diffs.
- Use absolute dates - never "last week", "yesterday", or "Thursday".
- Do not restate information already in another doc - link to it instead.

## When to Update Existing Docs

- **`docs/guides/`** - update when user-facing setup, config, or usage instructions change.
- **`README.md`** - update when public-facing capabilities or getting-started steps change.

## What NOT to Put in Docs

- Credentials, API keys, tokens, or real URLs containing auth info.
- Step-by-step implementation details that belong in code comments or Javadoc.
- Duplicate content - if it already exists elsewhere, link to it.
