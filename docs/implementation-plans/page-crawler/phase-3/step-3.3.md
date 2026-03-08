# Step 3.3 — `GeneratedPageObject` Value Object

**Phase:** 3 — Page Object Generator
**Status:** Todo
**Depends on:** —
**Blocks:** Steps 3.2, 3.4

---

## Goal

Define the immutable data structure that `PageObjectGenerator` produces and
`PageObjectWriter` consumes. Analogous to `PageSnapshot` on the crawl side.

---

## File

`src/main/java/ai/generator/GeneratedPageObject.java`

---

## Implementation

```java
package ai.generator;

import lombok.Value;

@Value
public class GeneratedPageObject {

    /** The Java class name extracted from the generated source (e.g. "LoginPage"). */
    String className;

    /** Complete, valid Java source ready to be written to a .java file. */
    String javaSource;

    /** The URL that was crawled to produce this Page Object. */
    String sourceUrl;
}
```

---

## Notes

- Lombok `@Value` → immutable, all-args constructor, getters, `equals`, `hashCode`, `toString`.
- `className` is the bare class name (no package prefix). `PageObjectWriter` appends `.java`.
- `javaSource` is guaranteed to contain `package generated;` before it reaches `PageObjectWriter`
  (injected by `PageObjectGenerator.injectPackageIfMissing()`).

---

## Definition of Done

- [ ] File created in `src/main/java/ai/generator/`
- [ ] `./gradlew compileJava` passes
