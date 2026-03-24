# AI-Assisted Test Authoring - Merged Plan

**Status:** done
**Branch:** `main`
**Date:** `2026-03-24`
**Scope:** New `ai-write-test` skill and supporting files that convert a structured test case into working, verified code under `src/test` only.

---

## Phase Status

| Phase | Status | Outcome |
|---|---|---|
| 1. Test case template | 🟢 Done | Stable BDD input format users fill in before invoking the skill |
| 2. `ai-write-test` skill | 🟢 Done | Orchestration skill covering all 6 stages |
| 3. Playwright MCP setup | 🟢 Done | Config + `.env.example` for auth credentials |
| 4. User-facing guide | 🟢 Done | `docs/ai-features/` doc explaining the full flow |

**Status legend:** `🟡 Planned` | `🔵 In Progress` | `🟢 Done`

---

## Overview

Browser automation is a discovery tool, not the primary code author. Final code is grounded in existing POs, local docs, and the observed DOM. Existing `write-test` skill keeps its placement-rules role; this skill orchestrates the full end-to-end flow.

---

## Design Decisions

| Decision | Choice |
|---|---|
| Input format | BDD (Given/When/Then) with a clear starting page or entry action in the steps |
| PO discovery | Walk the described path with MCP first, then compare discovered pages/components with `src/test/java/pages/` |
| URL source | MCP walk-through of the scenario path, plus `@PageURL` from existing POs when available |
| Auth for Playwright MCP | Credentials from `.env` |
| Max repair cycles | 3, then escalate to user |
| `src/main` boundary | Explain gaps at end of run; never edit |
| `src/main` gap surfacing | Printed summary at end of skill run with affected files and rationale |

---

## Guardrails

- Write only under `src/test/java/pages`, `src/test/java/tests`, and `src/test/java/tests/support`.
- Never edit `src/main/java`.
- Prefer updating an existing PO over creating a duplicate class.
- Keep locators out of test classes; they belong in POs.
- Prefer CSS locators; use XPath only when CSS is insufficient.
- Ask all clarification questions in one batch before any code generation or browser inspection starts.

---

### Step 1 - Test case template (`Low`)

**File:** `docs/ai-features/test-case-template.md`

```markdown
Feature: <feature name>
Auth required: yes | no

Background:
  Given I start on <page name>

Scenario: <scenario name>
  Given <precondition>
  When  <action>
  Then  <expected outcome>

Test data:
  - key: value
```

- Do not require `Pages` or URL fields in the input template.
- The scenario must make the starting page or entry action explicit.
- The skill walks the scenario path with MCP, identifies encountered pages and URLs, then compares them with existing POs before deciding what to create or update.
- One scenario per invocation; split multi-scenario files before running.

**Validation:** human review - every field the skill reads is present and unambiguous

---

### Step 2 - `ai-write-test` skill (`Medium`)

**File:** `.claude/skills/ai-write-test/SKILL.md`

| Stage | What it does | Key detail |
|---|---|---|
| 1. Parse & clarify | Validate template fields | Batch all questions; block code gen until resolved |
| 2. Plan | Walk the described path and compare against `src/test/java/pages/` | Output short plan before writing any code |
| 3. DOM capture | Use Playwright MCP to explore missing or unclear pages/components | Load `.env`; capture timing risks, iframes, dynamic DOM |
| 4. Implement | PO first, then test | Touch only `src/test`; no locators in test classes |
| 5. Run & repair | `./gradlew compileTestJava` then targeted test | Max 3 repair cycles before stopping |
| 6. Report | List files changed | Print `src/main` gaps with affected files and reason |

Delegates PO conventions to `create-page-object` skill and placement rules to `write-test` skill; does not duplicate them.

**Validation:** invoke against existing `ComponentPlaygroundPage`; skill reaches Stage 5 without errors

---

### Step 3 - Playwright MCP setup (`Low`)

**Files:** `.env.example`, `docs/ai-features/playwright-mcp-setup.md`

```
TEST_USERNAME=
TEST_PASSWORD=
```

Setup doc covers: adding Playwright MCP to `.claude/settings.json`, populating `.env`, `.gitignore` entry, and the HTML-paste fallback for pages the MCP cannot reach (auth walls, local-only environments).

**Validation:** `.env` present in `.gitignore`

---

### Step 4 - User-facing guide (`Low`)

**File:** `docs/ai-features/ai-write-test.md`

What the skill does, how to invoke (`/ai-write-test`), link to template, link to MCP setup doc, known limits (shadow DOM, iframes, complex auth flows).

**Validation:** human review; all links resolve

---

## Execution Order

| # | Step | Risk | Effort |
|---|---|---|---|
| 1 | Test case template | Low | Small |
| 2 | `ai-write-test` skill | Medium | Medium |
| 3 | Playwright MCP setup | Low | Small |
| 4 | User-facing guide | Low | Small |
