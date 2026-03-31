---
name: review-automation-code
description: Review Java UI automation under `src/test/java/tests/` and `src/test/java/pages/` for page-object design, abstraction level, ownership of constants and test data, shared test-support reuse, locator placement, and assertion quality. Use after creating, editing, or refactoring Selenium page objects or UI tests, especially for AI-generated or multi-file changes before validation or handoff.
---

# Review Automation Code

Review changed automation code as a system, not file-by-file. Focus on design issues that will make future tests harder to write, review, or maintain.

## Inputs

- Read the changed UI test classes and the page objects they use.
- Read any nearby test-support helpers when the code touches credentials, URLs, fixtures, or shared setup.
- Use the relevant local skills as the source of truth for conventions:
  - `create-page-object` — locator priority, URL hierarchy, base-page pattern, field naming, navigation method rules, `isOpened()` contract.
  - `write-test` — test placement, credential handling, page object construction order, `singleThreaded` usage, assertion library.
  - `ai-write-test` — domain grouping rules, self-review checklist for multi-file PO sets, review-before-validation gating.
  - `task-ready` — fix-loop completion gate and handoff criteria.

## Review Workflow

1. Identify the scenario boundary.
2. Inspect the page objects as a set, then the test class, then any shared support classes.
3. Record only material findings. Ignore purely stylistic preferences unless they conflict with repo rules.
4. If no findings remain, state that explicitly.

## What To Flag

- **Missing `isOpened()` on a top-level navigable page.** Every class annotated with `@PageURL` that can be opened directly must implement `isOpened()`. Components that extend `iPage` are exempt.
- **Navigation method returns a destination page object.** Navigation methods must be `void`; returning the destination PO creates a coupling between pages that belong to separate classes.
- **`Thread.sleep()` used in a page object.** Use `@Waiter` or framework explicit waits instead; `Thread.sleep()` is prohibited in POs.
- **`driver.findElement()` called directly inside a page object.** All element access must go through `iWebElement` fields initialized by the framework.
- **`System.out.println` used instead of `iLogger`.** All logging in page objects must use `iLogger`.
- **Raw TestNG/JUnit assertions instead of `iAssert`.** All assertions in test classes must use `iAssert` for consistent log output.
- Page-object APIs that expose low-level mechanics where a business action should exist.
- Page-specific constants, labels, URLs, or messages stored in tests instead of the owning page object.
- The same env-reading or test-support logic duplicated across multiple test classes when a single shared helper in `src/test/java/**` would cover it. Do not flag the first/only occurrence of `Objects.requireNonNull(System.getenv(...))` in a single test — that is the prescribed pattern.
- Credentials, locators, or Selenium mechanics leaking into test classes.
- Duplicate or overly chatty page methods when one clearer method would cover the scenario.
- Assertions that are weaker than the scenario contract.
- Locator choices that are less stable than available `id`, `name`, `aria-*`, or scoped CSS alternatives.
- Page hierarchy mistakes such as repeated absolute URLs on sibling pages from the same domain. Also flag a base-class domain used when `base_url` (Environment.getRootUrl()) is non-empty and equals the same domain — the framework prepends `base_url` to the assembled path and will produce a double-domain URL.

## What Not To Flag

- Personal style preferences that do not affect correctness, maintainability, or repo conventions.
- Missing abstractions when the scenario is genuinely one-off and the simpler local code is clearer.
- Requests to move test-only concerns into `src/main/java/` just to centralize them.
- Fluent chaining as a universal rule. Allow it only when it improves readability and does not conflict with navigation semantics.

## Output Format

Report findings first, ordered by severity:

- `HIGH` - likely to spread poor patterns or conflict with repo rules.
- `MEDIUM` - maintainability or reuse problem worth fixing now.
- `LOW` - optional improvement; mention only if the list is otherwise empty.

For each finding, include:
- file path
- concrete issue
- recommended direction

If there are no findings, say so in one line and mention any residual risk briefly.

## Fix Loop

When this skill is used as part of implementation:

1. Review after the first implementation pass.
2. Fix `HIGH` and `MEDIUM` findings before calling the task complete.
3. Re-run the review if a `HIGH` fix adds, removes, or renames a public PO method, or introduces a new base class.
