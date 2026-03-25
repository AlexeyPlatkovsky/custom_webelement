---
name: validate
description: Select and run the right verification steps based on what changed. Use after making code, docs, config, or skill changes to confirm the result is coherent.
---

# validate Skill (Gemini)

Inspect what changed first, then run the smallest meaningful verification and broaden only when the change surface requires it.

## Decision Table

| What changed | Commands to run |
|---|---|
| `src/main/java/**` only | `compileJava`, `checkstyleMain`; add `compileTestJava` if public APIs changed |
| `src/test/java/unit_tests/**` only | `compileTestJava`, `test -Dsuite=unit`; add `checkstyleTest` if test structure changed materially |
| `src/test/java/pages/**` only | `compileTestJava`, then the narrowest relevant UI smoke test if one exists |
| `src/test/java/tests/**` only | `compileTestJava`, targeted `test -Dsuite=ui --tests "<relevant test class>"` if the environment supports it |
| Main + test code together | `compileJava`, `compileTestJava`, `test -Dsuite=unit`, `checkstyleMain`, `checkstyleTest` |
| Build files (`build.gradle`, `settings.gradle`) | `compileJava`, `compileTestJava`, `test -Dsuite=unit` |
| Logging, reporting, page factory, driver, or lifecycle behavior | First add or update a protecting test, then run `compileJava`, `compileTestJava`, `test -Dsuite=unit`, plus the narrowest relevant UI smoke test if available |
| `.gemini/skills/**` only | No Gradle commands. Review trigger text and instructions for clarity, verify referenced paths and commands exist, and run a skill validator if one is available |
| Docs only (`docs/**`, `README.md`, `AGENTS.md`) | No compilation needed. Review for accuracy, current paths, and consistency with the active repo structure |
| Config or resource files only | Review carefully, then run the narrowest affected compile or test command when the change can influence runtime behavior; do not treat config changes as automatically review-only |

## Commands

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew test -Dsuite=unit
./gradlew test -Dsuite=ui
./gradlew test -Dsuite=ui --tests "tests.DuckDuckGoPageTest"
./gradlew checkstyleMain
./gradlew checkstyleTest
```

## Rules

- Always compile before running tests. A compile error is faster feedback than a test failure.
- Run unit tests before UI tests; they are faster and have no browser dependency.
- Prefer targeted test execution over broad UI runs when one or two affected test classes can cover the change.
- Page Object changes are usually non-trivial because they can affect stale-element behavior, page initialization, or reporting.
- For `.gemini/skills/**` changes, verify that referenced paths, commands, and doc names still exist in the repo before calling the task complete.
- If the full suite cannot run, say exactly what was skipped and why.
- For non-trivial changes, run at minimum:
  ```bash
  ./gradlew compileJava
  ./gradlew compileTestJava
  ./gradlew test -Dsuite=unit
  ```
