# AGENTS.md

## Purpose

Project-specific baseline for coding agents working in `custom_webelement`.

## Project Context

- Java 21 + Gradle
- Selenium 4 UI test framework with custom `iWebElement` / `iPageFactory`
- TestNG for tests, Allure and custom artifacts for reporting
- Main code: `src/main/java/`
- Tests and demo page objects: `src/test/java/`
- Docs and contributor rules: `docs/`

## Core Rules

- Read relevant code and docs before changing behavior.
- Prefer small, local changes over speculative refactors.
- Preserve existing behavior unless the task explicitly requires a change.
- Do not add dependencies unless the JDK or current dependencies are insufficient.
- Update docs when public usage, extension points, or workflow expectations change.
- For framework refactoring, use a TDD approach: add or adjust the protecting test first, confirm it fails for the expected reason, then change production code and re-run the smallest relevant checks.

## Trivial Vs Non-Trivial Tasks

### Trivial

Usually trivial if most are true:

- One file or a very small related set
- Docs/comments/naming/formatting only
- Narrow low-risk bug fix
- No API, config, page factory, driver, logging, or reporting change
- No migration path or cross-module coordination needed

Examples: typo fix, broken doc link, local rename, log wording cleanup.

### Non-Trivial

Treat as non-trivial if any are true:

- Adds a feature, abstraction, or extension point
- Changes shared framework behavior or defaults
- Changes public methods, annotations, config keys, or lifecycle behavior
- Touches multiple packages or both code and docs/tests
- Needs new tests, design tradeoffs, or migration guidance
- May affect parallel execution, stale elements, page factory behavior, or reporting

Examples: nested page objects, `iPageFactory` refactor, new AI provider, test execution changes.

Use @ORCHESTRATION.md for agent delegation and orchestration rules for non-trivial tasks

## Coding Guidelines

- Use Java 21 features when they improve clarity; do not force them.
- Keep imports explicit; no wildcard imports.
- Do not use raw types.
- Follow existing lowercase package naming.
- Use Lombok selectively; do not use `@Data` on mutable domain objects.
- Prefer readable, testable code over clever abstractions.
- Use `iLogger`; do not use `System.out.println`.
- Do not swallow exceptions silently.
- Keep lines reasonably compact; target the project's 120-char style.

## Validation

- Run the smallest meaningful verification first.
- For non-trivial work, run relevant checks before claiming completion.
- If you cannot run full validation, say what was skipped.
- For framework refactoring, do not treat the work as complete unless the new or updated protecting test is part of the validation story.

See the `validate` skill for commands and the decision table.

## Scope And Communication

- Do not mix unrelated cleanup into task work unless asked.
- If you see a broader issue, note it separately instead of expanding scope silently.
- For non-trivial work, summarize the plan before broad edits.
- State assumptions, risks, and unverified areas plainly.

## Skills

At session start, scan all `.claude/skills/**/SKILL.md` files and index their `name` and `description` frontmatter fields. Load the full body of a skill only when a task matches its description.

| Skill | Description |
|---|---|
| `create-page-object` | Scaffold a Page Object following project conventions |
| `explain-code` | Explain code with diagrams and analogies |
| `refactor` | Rules and practices for safely refactoring shared framework code |
| `task-ready` | Task completion checklist (compile, unit tests, checkstyle, doc review) |
| `validate` | Select and run the right verification commands based on what changed |
| `work-with-docs` | Rules for creating and maintaining docs, plans, guides, and ADRs |
| `work-with-git` | Branch strategy and git safety rules |
| `write-test` | Rules for writing and placing unit and UI tests |

