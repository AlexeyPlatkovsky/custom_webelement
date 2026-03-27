---
name: ai-write-test
description: Author, edit, or fix Java tests under src/test — driven by a test case template (BDD or plain steps) for new tests, or a file path + description for edits and fixes.
---

# ai-write-test Skill (Gemini)

Author new tests from a test case template (BDD or plain steps), or edit and fix existing tests given a file path and a description of the problem. Browser automation is a discovery tool; final code is grounded in existing Page Objects, local docs, and the observed DOM.

## Scope Constraints

- Write only under `src/test/*`.
- Never edit `src/main/java/`.
- Prefer updating an existing PO over creating a duplicate class.
- Keep locators out of test classes; they belong in Page Objects.
- Prefer CSS locators; use XPath only when CSS is insufficient.

---

## Stage 1 — Parse & Clarify

Determine the mode from the input:

**New test** (test case template provided) — infer first, ask only when genuinely ambiguous:

1. **Feature name** — must be present; ask if missing.
2. **Starting page** — derive from any of: explicit `Starting page` / `Background` field, first step ("User opens X", "Navigate to X"), or an implied entry point ("User logged in as Admin" → login page or an authenticated landing page). Ask only if the entry point cannot be determined from the steps at all.
3. **Auth required** — infer from step content ("logged in as", "user opens login", credentials in test data). Identify sensitive data placeholders (like "login", "password", "API key") and ensure they are NOT hardcoded. Ask only if auth state is ambiguous after reading all steps.
4. **Steps and expected outcome** — must be present in some form (BDD `Given/When/Then`, numbered list + `Expected result`, or plain prose steps). Ask only if the intended action or outcome is unclear.

**Edit / fix** (existing file path + problem description provided) — read the target files first:
- Read the test class and any POs it references.
- Confirm the described problem is reproducible from the code (compile error, assertion mismatch, broken locator, flaky step, etc.).
- Identify whether the fix is confined to the test class, requires a PO change, or both.

In both modes: collect all questions in a single batch. Do not generate code or open a browser until every question is resolved.

---

## Stage 2 — Plan

1. Walk the scenario path mentally from the starting page through each step.
2. List every page or component the scenario touches, noting the URL of each.
3. **Domain grouping** — if two or more new pages share the same domain and no existing base page covers it, plan a new abstract base page that carries that domain in its `@PageURL`. Child pages then use relative path-only `@PageURL` values and extend the base. Do not repeat the full absolute URL on each sibling page. See the `create-page-object` skill URL Hierarchy section for the pattern and its constraints.
4. Compare that list against `src/test/java/pages/` to identify:
   - Pages that already have a matching PO (reuse or extend).
   - Pages with no matching PO (must create).
5. Print a short plan — files to create, files to update — and wait for acknowledgement before proceeding.

---

## Stage 3 — DOM Capture

Skip this stage for edit/fix mode when all affected pages already have complete POs and the problem is a logic or assertion issue — no new locators are needed.

For new tests, or when a locator is missing or broken, use the `playwright-cli` skill to walk the scenario path live:

- Open the browser and navigate to the starting page.
- If the scenario requires authentication: fill the login form using credentials from `.env` (read via `System.getenv()`), then save session state with `playwright-cli state-save auth.json`. On subsequent runs, restore it with `playwright-cli state-load auth.json`. Never embed credential values in generated test code — not even as fallback defaults.
- Follow each scenario step using the appropriate CLI commands (`click`, `fill`, `type`, `press`, etc.).
- After each navigation or interaction, read the snapshot to capture element refs and current DOM state.
- For each page encountered, record:
  - Stable element refs and their corresponding CSS selectors for every element the test must interact with or assert on.
  - Timing risks (spinners, loaders, delayed renders).
  - iframes or shadow DOM that may require special handling.
- If the CLI cannot reach a page (auth wall, local-only env), ask the user to paste the page HTML as a fallback.
- Record each discovered URL; cross-reference with `@PageURL` values in existing POs.

---

## Stage 4 — Implement

Implement in this order:

1. **Page Objects first.** Delegate all PO conventions to the `create-page-object` skill. Use discovered locators. Extend existing POs when a small addition covers the need; create a new class only when the page has no existing PO.
2. **Test class second.** Delegate all placement and naming rules to the `write-test` skill. No locators in the test class; every interaction goes through PO methods.

Write only under `src/test`.

Before moving to validation, run a self-review on the full test and PO set created or edited for the scenario:

- **Security Check:** Confirm that ALL sensitive data (login, password, keys) identified in Stage 1 or discovered in Stage 3 is being read via `System.getenv()` and is NOT hardcoded in the test or PO.
- If two or more pages share a domain, confirm they reuse an existing abstract site base page or create a new one.
- Treat repeated absolute URLs on sibling pages as a defect unless the `create-page-object` exception for absolute leaf pages clearly applies.
- Confirm each top-level page models only its own path segment when a shared site base page exists.

---

## Stage 5 — Run & Repair

Run verification in order:

```bash
./gradlew compileTestJava
./gradlew test -Dsuite=ui --tests "tests.<TestClassName>"
```

If compilation or the test fails, diagnose and fix. Repeat up to **3 cycles**. After 3 failed cycles, stop and report the blocker to the user with the last error output and a recommended next step.

---

## Stage 6 — Report

Print a summary:

| Item | Detail |
|---|---|
| Files created | List with paths |
| Files updated | List with paths |
| Test result | Pass / Fail / Not run |
| Repair cycles used | n of 3 |
| src/main gaps | List any missing framework support with affected files and reason |

`src/main` gaps are informational only; never edit `src/main/java/` from this skill.
