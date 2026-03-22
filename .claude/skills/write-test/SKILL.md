---
name: write-test
description: Rules for writing and placing tests in this project. Use when creating unit tests, UI tests, or reviewing generated tests.
---

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

- Use `iAssert` for assertions. It keeps logs readable and consistent with the framework.
- Use plain-English test names that describe the scenario, not the implementation.
- Do not hardcode real credentials or secrets in tests.
- Prefer targeted unit validation before broad UI runs.
- Do not add `@Test` methods or assertions inside Page Objects. They belong in test classes.
- If a UI test changes only one page or workflow, prefer a single targeted smoke test over running the whole UI suite.
- Use `singleThreaded = true` on class-level `@Test` only when shared mutable state or filesystem artifacts make parallel execution unsafe.
- Keep test data local to the test unless multiple tests truly need the same fixture.
- For framework refactoring, follow the `refactor` skill - the protecting test must exist and pass before the refactor is considered complete.
