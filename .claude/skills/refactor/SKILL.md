---
name: refactor
description: Rules and practices for safely refactoring framework code. Use when restructuring iPage, iPageFactory, iWebElement, or any shared framework behavior.
---

## When to Use This Skill

Use for any change that:
- Restructures shared framework code (`iPage`, `iPageFactory`, `iWebElement`, `iWebElementsList`)
- Changes public methods, annotations, lifecycle behavior, or defaults
- Moves or renames classes used across multiple packages
- Modifies logging, reporting, or driver initialization behavior

Do NOT use for: feature additions, isolated bug fixes in a single page/test, or doc-only changes.

## TDD-First Rule

Before changing any production code:

1. Write or update a unit test that describes the behavior being preserved (or the bug being fixed).
2. Run it - confirm it passes (behavior preservation) or fails for the expected reason (bug fix).
3. Change production code.
4. Rerun the protecting test - it must pass before the refactor is considered complete.

Name protecting tests by behavior, not by implementation:
- Good: `elementIsReusedAfterPageNavigation`
- Avoid: `testIWebElementRefactor`

## Plan First

For non-trivial refactors, write a plan before starting (see `work-with-docs` skill). The plan should:
- Name the protecting test
- List affected files and public APIs
- Specify execution order with risk levels
- Note call sites and any migration steps

## High-Risk Areas

Extra care required - these have hidden coupling across the framework:

| Area | Risk |
|------|------|
| `iPageFactory` field injection | Changes affect all `@FindBy` fields across all pages |
| `iWebElement` / `iWebElementsList` | Stale element handling, `@CacheElement` invalidation |
| `@Waiter` | Timing-sensitive; failures may appear as flakiness, not errors |
| `iLogger` / reporting | Affects Allure artifacts and custom reporting output |
| TestNG lifecycle (`@BeforeMethod`, `@AfterMethod`) | Shared state across all test classes |

## Step Size

- Prefer many small steps over one large refactor.
- Each step should leave the build and all tests passing.
- If a step cannot be completed atomically, stop and clarify scope before continuing.

## Scope Discipline

- Do not mix refactoring with feature work in the same branch.
- Do not opportunistically clean up unrelated code while refactoring.
- If you discover a separate issue, note it and create a separate task.

## Public API Changes

If the refactor changes a public method signature, annotation name, or config key:
- Check all call sites before renaming.
- Prefer adding the new form and deprecating the old one if call sites are numerous.
- Document the migration path in the plan file.

## Validation After Refactoring

Run in this order:

1. `./gradlew compileJava` - must be clean
2. `./gradlew compileTestJava` - must be clean
3. `./gradlew test -Dsuite=unit` - protecting test and all unit tests must pass
4. `./gradlew checkstyleMain` - style must be clean
5. If lifecycle, page factory, or driver behavior changed: run the narrowest relevant UI smoke test

See the `validate` skill for the full decision table.
