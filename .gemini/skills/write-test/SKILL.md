---
name: write-test
description: Rules for writing and placing tests in this project. Use when creating unit tests, UI tests, or reviewing generated tests.
---

# write-test Skill (Gemini)

## Test Locations

| Type | Location |
|---|---|
| Unit tests | `src/test/java/unit_tests/` |
| UI tests | `src/test/java/tests/` |
| Page Objects | `src/test/java/pages/` |

## TestNG Groups

Prefer class-level grouping so all test methods inherit the same suite unless a method needs a different group.

```java
@Test(groups = {"unit"})
public class MyUnitTest {
    @Test
    public void scenarioNameTest() {
    }
}
```

Use method-level groups only when a specific method intentionally differs from the class default.

## Rules

- **Use `iAssert` for assertions.** Always use the project's custom assertion utility (`utils.assertions.iAssert`) instead of standard TestNG `Assert` to ensure consistent logging and reporting.
- Use plain-English test names that describe the scenario, not the implementation.
- **Never put credentials or secrets in test code** — not as constants, not as default values, not in comments. Strictly use `java.util.Objects.requireNonNull(System.getenv("VAR_NAME"))` with no hardcoded fallback.
- **Declare and construct all page objects at the top of the test method**, before any actions or assertions. Avoid instantiating or initializing POs mid-test after interactions.
- Prefer targeted unit validation before broad UI runs.
- Do not add `@Test` methods or assertions inside Page Objects. They belong in test classes.
- If a UI test changes only one page or workflow, prefer a single targeted smoke test over running the whole UI suite.
- Use `singleThreaded = true` on class-level `@Test` only when shared mutable state or filesystem artifacts make parallel execution unsafe.
- Keep test data local to the test unless multiple tests truly need the same fixture.
- For framework refactoring, follow the `refactor` skill - the protecting test must exist and pass before the refactor is considered complete.

## Review Checklist For Generated UI Tests

When reviewing generated or AI-edited UI automation, inspect Page Objects as a set, not one file at a time.

- Check page-object hierarchy and URL modeling against the `create-page-object` skill, especially when multiple pages share one domain.
- Flag repeated absolute URLs on sibling pages when a shared abstract site base page should be used instead.
- Verify assertions match the scenario contract precisely; prefer asserting a specific visible element over broad container-text checks when the test case names a label, message, or landmark.
