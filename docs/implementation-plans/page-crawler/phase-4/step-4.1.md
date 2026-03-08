# Step 4.1 — Basic Syntax Validation on Generated Source

**Phase:** 4 — Validation and polish
**Status:** Todo
**Depends on:** Step 3.2 (`PageObjectGenerator` calls validation before returning)
**Blocks:** —

---

## Goal

Catch obvious AI generation failures before the file is written to disk. Two failure modes
are detected: missing class declaration (model returned an error message or empty response)
and leftover markdown fences (source extraction failed silently).

---

## Location

This logic already appears in `PageObjectGenerator` as `validateSource()`. This step
formally documents it as a completed, tested contract rather than an afterthought.

See Step 3.2 for the implementation:

```java
private void validateSource(String javaSource, String url) {
    if (!javaSource.contains("class ")) {
        throw new IllegalStateException(
            "AI response for URL '" + url + "' does not contain a class declaration. "
            + "The model may have returned an error or the response was truncated."
        );
    }
    if (javaSource.contains("```")) {
        throw new IllegalStateException(
            "AI response for URL '" + url + "' still contains markdown fences after extraction."
        );
    }
}
```

---

## What is NOT validated in MVP

| Check | Reason deferred |
|-------|----------------|
| Balanced braces `{}` | Requires parsing; false positives on string literals |
| All imports resolvable | Requires classpath access — use `./gradlew compileTestJava` instead |
| No syntax errors | Requires compiler invocation (slow, complex) |
| Class name matches file name | Done implicitly by `extractClassName` + `PageObjectWriter` |

Instruct developers to always run `./gradlew compileTestJava` after generation to catch
syntax errors before the generated file enters test runs.

---

## Failure Handling in Batch Mode

When `validateSource` throws in `PageObjectGenerator.generate()`, the exception propagates
to `PageCrawlerFacade.generatePageObjects()` which catches it, logs an ERROR, and continues
with remaining URLs. The failed URL appears in the batch summary as `[FAILED: ...]`.

---

## Definition of Done

- [ ] `validateSource()` implemented in `PageObjectGenerator` (covered by Step 3.2)
- [ ] Unit tests in `PageObjectGeneratorTest` cover both failure modes
- [ ] `PageObjectGeneratorTest`: response with no `class` → exception with URL in message
- [ ] `PageObjectGeneratorTest`: response with leftover fences → exception
- [ ] Developer documentation notes that `compileTestJava` is the safety net for syntax errors
