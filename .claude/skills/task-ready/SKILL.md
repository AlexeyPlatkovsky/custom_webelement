---
name: task-ready
description: Task completion checklist. Run before declaring work done or handing off for human review.
---

Work through each item in order. Stop and fix before moving on if any step fails.

## Checklist

- [ ] **Validation scope selected** - use the `validate` skill to choose the smallest meaningful verification for the changed files before declaring the task done.
- [ ] **Selected verification passes** - run the chosen commands or manual review steps and fix failures before handoff.
- [ ] **Automation design review** - if material UI automation code changed under `src/test/java/tests/` or `src/test/java/pages/`, run `review-automation-code` and address non-trivial findings.
- [ ] **Public API docs** - new or changed public methods have Javadoc when they add user-facing behavior or extension points.
- [ ] **No secrets** - no API keys, tokens, or real credentials are introduced in committed files.
- [ ] **Docs updated** - if behavior, setup, extension points, configuration, or workflow changed, the relevant docs are updated.
- [ ] **Scope clean** - no unrelated cleanup or opportunistic refactors are bundled into the task result.
- [ ] **Framework refactor** - if framework behavior was changed, the protecting test passes (see `refactor` skill).

## Notes

- If you cannot run a step, state explicitly what was skipped and why.
- Treat failures from the selected verification set as hard blocks.
- Prefer the smallest relevant verification first, then broaden only when the change surface requires it.
- Docs-only and skill-only work may not need Gradle commands if the `validate` skill selects review-only checks.
- If UI behavior changed, run a targeted UI test when the environment supports it.
- For non-trivial code changes, the minimum expected validation is usually `./gradlew compileJava`, `./gradlew compileTestJava`, and `./gradlew test -Dsuite=unit`.
