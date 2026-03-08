# Step 3.2 — `PageObjectGenerator`

**Phase:** 3 — Page Object Generator
**Status:** Todo
**Depends on:** Phase 1 (AI provider layer), Step 2.1 (`PageSnapshot`)
**Blocks:** Step 3.5 (full wiring)

---

## Goal

Implement the component that constructs the AI prompt from a `PageSnapshot`, calls the
configured `AiProvider`, and extracts a validated `GeneratedPageObject` from the response.

---

## File

`src/main/java/ai/generator/PageObjectGenerator.java`

---

## Implementation

```java
package ai.generator;

import ai.provider.AiProvider;
import ai.provider.AiRequest;
import ai.provider.AiResponse;
import ai.crawler.PageSnapshot;
import utils.logging.iLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageObjectGenerator {

    private static final Pattern CLASS_NAME_PATTERN =
        Pattern.compile("public\\s+class\\s+(\\w+)");
    private static final Pattern URL_PATH_LAST_SEGMENT =
        Pattern.compile("/([^/?#]+)(?:[?#].*)?$");

    private final AiProvider provider;

    public PageObjectGenerator(AiProvider provider) {
        this.provider = provider;
    }

    public GeneratedPageObject generate(PageSnapshot snapshot) {
        String systemPrompt = loadSystemPrompt();
        String userMessage  = buildUserMessage(snapshot);

        AiRequest  request  = new AiRequest(systemPrompt, userMessage, null);
        AiResponse response = provider.complete(request);

        iLogger.info(String.format(
            "PageObjectGenerator: url=%s model=%s inputTokens=%d outputTokens=%d",
            snapshot.getUrl(), response.getModel(),
            response.getInputTokens(), response.getOutputTokens()
        ));

        String javaSource = extractJavaSource(response.getContent());
        validateSource(javaSource, snapshot.getUrl());
        javaSource = injectPackageIfMissing(javaSource);
        String className = extractClassName(javaSource, snapshot.getUrl());

        return new GeneratedPageObject(className, javaSource, snapshot.getUrl());
    }
}
```

### `buildUserMessage`

```java
private String buildUserMessage(PageSnapshot snapshot) {
    return "Page URL: " + snapshot.getUrl() + "\n"
        + "Page title: " + snapshot.getTitle() + "\n\n"
        + "--- CLEANED HTML ---\n"
        + snapshot.getCleanedHtml() + "\n\n"
        + "--- ACCESSIBILITY TREE ---\n"
        + snapshot.getAccessibilityTree() + "\n\n"
        + "Generate a complete Page Object class for this page following the framework "
        + "conventions in the system prompt.";
}
```

### `extractJavaSource`

Strip any markdown code fences the model may emit despite instructions:

```java
private String extractJavaSource(String raw) {
    String cleaned = raw.strip();
    // Remove leading ```java or ``` fence
    if (cleaned.startsWith("```")) {
        int firstNewline = cleaned.indexOf('\n');
        cleaned = firstNewline >= 0 ? cleaned.substring(firstNewline + 1) : cleaned;
    }
    // Remove trailing ``` fence
    if (cleaned.endsWith("```")) {
        cleaned = cleaned.substring(0, cleaned.lastIndexOf("```")).stripTrailing();
    }
    return cleaned;
}
```

### `validateSource`

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

### `injectPackageIfMissing`

```java
private String injectPackageIfMissing(String javaSource) {
    if (!javaSource.startsWith("package ")) {
        return "package generated;\n\n" + javaSource;
    }
    return javaSource;
}
```

### `extractClassName`

```java
private String extractClassName(String javaSource, String url) {
    Matcher m = CLASS_NAME_PATTERN.matcher(javaSource);
    if (m.find()) {
        return m.group(1);
    }
    // Fallback: derive from URL last path segment
    Matcher urlMatcher = URL_PATH_LAST_SEGMENT.matcher(url);
    String segment = urlMatcher.find() ? urlMatcher.group(1) : "GeneratedPage";
    String name = Character.toUpperCase(segment.charAt(0)) + segment.substring(1) + "Page";
    iLogger.warn("PageObjectGenerator: could not extract class name from source, using fallback: " + name);
    return name;
}
```

### `loadSystemPrompt`

```java
private String loadSystemPrompt() {
    try (InputStream is = getClass().getResourceAsStream("/ai/generator/system-prompt.txt")) {
        if (is == null) {
            throw new IllegalStateException("system-prompt.txt not found on classpath at /ai/generator/");
        }
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new UncheckedIOException("Failed to load system prompt", e);
    }
}
```

---

## Unit Tests

**File:** `src/test/java/unit_tests/ai/generator/PageObjectGeneratorTest.java`

Use `Mockito.mock(AiProvider.class)` for all tests.

Test cases:
- Valid response → `GeneratedPageObject` has correct `className` and `sourceUrl`
- Response wrapped in ` ```java ... ``` ` → fences stripped, source valid
- Response has no `class` keyword → `IllegalStateException` with URL in message
- Response still has ` ``` ` after extraction → `IllegalStateException`
- Response missing `package` declaration → injected automatically
- `extractClassName` falls back to URL segment when regex does not match
- `loadSystemPrompt` throws `IllegalStateException` when file not on classpath
- Token counts logged at INFO level (verify via log capture or spy)

---

## Definition of Done

- [ ] `PageObjectGenerator.generate()` returns a `GeneratedPageObject` for a valid response
- [ ] Markdown fence stripping handles leading and trailing fences
- [ ] `validateSource` catches both missing class and leftover fences
- [ ] Package declaration injected when absent
- [ ] Class name fallback uses URL path segment + `"Page"` suffix
- [ ] System prompt loaded from classpath resource
- [ ] All unit tests pass
