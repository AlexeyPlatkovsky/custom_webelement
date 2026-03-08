# Step 3.4 — `PageObjectWriter`

**Phase:** 3 — Page Object Generator
**Status:** Todo
**Depends on:** Step 3.3
**Blocks:** Step 3.5 (full wiring)

---

## Goal

Write a `GeneratedPageObject`'s Java source to `src/test/java/generated/<ClassName>.java`,
handling duplicate file names gracefully.

---

## File

`src/main/java/ai/generator/PageObjectWriter.java`

---

## Implementation

```java
package ai.generator;

import utils.logging.iLogger;

import java.io.IOException;
import java.nio.file.*;

public class PageObjectWriter {

    private static final Path OUTPUT_DIR = Paths.get("src", "test", "java", "generated");
    private static final int MAX_SUFFIX  = 99;

    public void write(GeneratedPageObject pageObject) {
        Path outputPath = resolveOutputPath(pageObject.getClassName());
        try {
            Files.createDirectories(OUTPUT_DIR);
            Files.writeString(outputPath, pageObject.getJavaSource(), StandardOpenOption.CREATE_NEW);
            iLogger.info("PageObjectWriter: written → " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write page object to " + outputPath, e);
        }
    }

    private Path resolveOutputPath(String className) {
        Path candidate = OUTPUT_DIR.resolve(className + ".java");
        if (!Files.exists(candidate)) {
            return candidate;
        }

        // Duplicate detected — append numeric suffix
        for (int i = 2; i <= MAX_SUFFIX; i++) {
            Path suffixed = OUTPUT_DIR.resolve(className + i + ".java");
            if (!Files.exists(suffixed)) {
                iLogger.warn(String.format(
                    "PageObjectWriter: '%s.java' already exists — writing as '%s%d.java'",
                    className, className, i
                ));
                return suffixed;
            }
        }
        throw new IllegalStateException(
            "Cannot write " + className + ".java: all suffixes up to " + MAX_SUFFIX + " already exist."
        );
    }
}
```

---

## Design Notes

- `Files.createDirectories(OUTPUT_DIR)` ensures the `generated/` folder exists. This is a
  no-op if it already exists.
- `StandardOpenOption.CREATE_NEW` is used on the primary write attempt. If the file somehow
  appeared between `resolveOutputPath()` and `writeString()` (race condition), an
  `IOException` is thrown — acceptable since batch mode is single-threaded.
- The output path is relative to the **working directory**, which is the project root when
  running via Gradle. If tests are run from a different CWD, the path may need to be
  absolute. This is documented as a known limitation.

---

## Unit Tests

**File:** `src/test/java/unit_tests/ai/generator/PageObjectWriterTest.java`

Use a `@TempDir` JUnit/TestNG temporary directory or redirect `OUTPUT_DIR` via a subclass.

Test cases:
- Write a new page object → `.java` file created with correct name
- File content matches `GeneratedPageObject.javaSource`
- Write two objects with the same class name → second written as `ClassName2.java`, WARN logged
- Write 99 duplicates → 101st throws `IllegalStateException`
- `OUTPUT_DIR` does not exist → created automatically, no exception
- `javaSource` content written verbatim (no transformation applied by the writer)

---

## Definition of Done

- [ ] `PageObjectWriter.write()` creates the file in `src/test/java/generated/`
- [ ] `OUTPUT_DIR` created if it does not exist
- [ ] Duplicate names resolved by numeric suffix up to 99
- [ ] Duplicate suffix logged as WARN via `iLogger`
- [ ] Exhausted suffixes throw `IllegalStateException`
- [ ] All unit tests pass
