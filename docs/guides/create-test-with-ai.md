# Skill `ai-write-test` usage

Authors new tests from a test case template (BDD or plain steps), or edits and fixes existing tests given a file path and a description of the problem. The skill reads existing Page Objects, uses Playwright CLI when new locators are needed, compiles, and runs the targeted test — all within `src/test`.

## Before start

Ensure that Playwright CLI is installed and properly configured — see [Playwright CLI Setup](playwright-cli.md).

## How to Invoke

**New test** — fill in the [test case template](test-case-template.md) (BDD or plain steps format), save it into `docs/cases`, then run SKILL:

```
/ai-write-test docs/cases/<my-test-case>.md
```

**Edit or fix an existing test** — pass the test file path and describe the problem:

```
/ai-write-test src/test/java/tests/MyTest.java "assertion on line 42 uses wrong expected value"
```

## What the Skill Does

| Stage | Action |
|---|---|
| 1. Parse & clarify | Infers starting page and auth from steps; asks only when genuinely ambiguous |
| 2. Plan | Walks the scenario, compares pages against `src/test/java/pages/`, prints plan |
| 3. DOM capture | Uses Playwright CLI to discover locators, timing risks, and URLs |
| 4. Implement | PO first (via `create-page-object`), then test (via `write-test`) |
| 5. Run & repair | Compiles and runs the test; up to 3 repair cycles |
| 6. Report | Lists files changed; surfaces any `src/main` gaps |

## Prerequisites

- Playwright CLI installed and skills registered — see [Playwright CLI Setup](playwright-cli.md).
- `.env` populated with `TEST_USERNAME` and `TEST_PASSWORD` (only needed for authenticated scenarios).

## Scope

The skill writes only under `src/test/*`. It never edits `src/main/java/`. If a framework gap is found, it prints a summary at the end.

## Known Limits

- **Shadow DOM** — locator capture may be incomplete; review generated locators.
- **Nested iframes** — CLI may not cross frame boundaries; use the HTML-paste fallback.
- **Complex auth flows** — log in manually first, then resume the skill.
- **One scenario per run** — split multi-scenario files before invoking.
