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

## Branch Strategy

Based on [docs/contributing.md](D:/coding/custom_webelement/docs/contributing.md):

- `master` - stable, releasable code
- `feature/<name>` - new features
- `fix/<name>` - bug fixes
- `docs/<name>` - documentation-only changes
- `claude/<name>` - AI-assisted development branches

Rules:

- Base branches on `master`.
- Never push directly to `master`.
- Suggest branch creation for non-trivial tasks before substantial implementation.
- For trivial changes, branch creation is optional unless the user wants strict git hygiene.

## Git Safety

- Do not commit without explicit user permission.
- Do not push without explicit user permission.
- Do not create or switch branches without user approval.
- Do not rewrite history, force-push, reset, or revert user changes without permission.
- Avoid unrelated files in a dirty worktree.
- If unexpected user edits overlap your target files, stop and clarify.

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

## Page Object Rules

- Use `iWebElement` / `iWebElementsList`, not raw `WebElement`.
- Use `iPageFactory`, not Selenium `PageFactory`.
- Page Objects belong in `src/test/java/pages/`.
- Keep assertions and `@Test` methods out of Page Objects.
- Do not call `driver.findElement()` directly in tests when it belongs in a Page Object.
- Do not use `Thread.sleep()`; use `@Waiter` or explicit waits.
- Use `@CacheElement` only for stable non-dynamic elements.
- Prefer stable locators: `id`, `data-testid`, `aria-*`, stable CSS, then XPath as fallback.
- Name fields by intent: `searchInput`, `submitButton`, `errorMessage`, `menuItems`.

## Test Rules

- Unit tests go in `src/test/java/unit_tests/`.
- UI tests go in `src/test/java/tests/`.
- Generated tests go in `src/test/java/generated/` and must be reviewed.
- Use TestNG groups: `unit`, `ui`, `disabled`.
- Prefer `iAssert` for assertions so logs stay readable.
- Use plain-English test names that describe the scenario.
- Do not hardcode real credentials or secrets in tests.
- Prefer targeted unit validation before broad UI runs when possible.

## Validation

- For behavior changes, run the smallest meaningful verification first.
- For non-trivial work, run relevant tests or compile checks before claiming completion.
- If you cannot run full validation, say what was not verified.

Useful commands:

- `./gradlew compileJava`
- `./gradlew compileTestJava`
- `./gradlew test -Psuite=unit`
- `./gradlew test -Psuite=ui`
- `./gradlew checkstyleMain`
- `./gradlew checkstyleTest`

## Documentation Rules

- Update docs when behavior, setup, extension points, or workflow changes.
- Keep docs compact and high-signal.
- If a doc exceeds 150 lines, suggest refactoring, compacting, or splitting it.
- Likely targets: `README.md`, `docs/guides/*.md`, `docs/architecture/*.md`, `docs/contributing.md`.

## Scope And Communication

- Do not mix unrelated cleanup into task work unless asked.
- If you see a broader issue, note it separately instead of expanding scope silently.
- For non-trivial work, summarize the plan before broad edits.
- State assumptions, risks, and unverified areas plainly.

## Default Workflow

1. Classify the task as trivial or non-trivial.
2. Inspect relevant code and docs.
3. Suggest a branch for non-trivial work if appropriate.
4. Implement the smallest coherent change set.
5. Add or update tests for behavior changes.
6. Update docs when needed.
7. Report what changed, what was verified, and what remains unverified.
