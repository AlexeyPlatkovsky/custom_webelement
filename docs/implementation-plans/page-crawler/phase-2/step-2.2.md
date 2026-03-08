# Step 2.2 — `DomCleaner`

**Phase:** 2 — PageCrawler
**Status:** Todo
**Depends on:** —
**Blocks:** Step 2.3 (`PageCrawler` calls `DomCleaner.clean()`)

---

## Goal

Implement the HTML cleaning pipeline that transforms raw page HTML into a compact,
prompt-friendly representation. The goal is maximum signal, minimum noise, within a
hard 50 KB character cap.

---

## File

`src/main/java/ai/crawler/DomCleaner.java`

---

## Cleaning Pipeline (in order)

### 1. Remove `<script>` blocks

```java
html = html.replaceAll("(?si)<script[^>]*>.*?</script>", "");
```

### 2. Remove `<style>` blocks

```java
html = html.replaceAll("(?si)<style[^>]*>.*?</style>", "");
```

### 3. Remove HTML comments

```java
html = html.replaceAll("(?s)<!--.*?-->", "");
```

### 4. Strip inline event handler attributes

Attributes like `onclick`, `onmouseover`, `onkeyup`, `onsubmit`, etc. carry no structural
information and consume tokens.

```java
html = html.replaceAll("\\s+on\\w+=\"[^\"]*\"", "");
html = html.replaceAll("\\s+on\\w+='[^']*'", "");
```

### 5. Collapse whitespace

```java
html = html.replaceAll("\\s{2,}", " ").strip();
```

### 6. Truncate at 50,000 characters (at a clean tag boundary)

If the cleaned HTML exceeds 50,000 characters, find the last `>` at or before position
50,000 and truncate there. Append a truncation marker so the AI knows the DOM is partial.

```java
private static final int DOM_LIMIT = 50_000;
private static final String TRUNCATION_MARKER = " <!-- [DOM TRUNCATED AT 50KB LIMIT] -->";

if (cleaned.length() > DOM_LIMIT) {
    int cutPoint = cleaned.lastIndexOf('>', DOM_LIMIT);
    if (cutPoint < 0) cutPoint = DOM_LIMIT; // fallback: hard cut
    cleaned = cleaned.substring(0, cutPoint + 1) + TRUNCATION_MARKER;
    iLogger.warn("DomCleaner: DOM truncated to " + DOM_LIMIT + " chars for prompt safety");
}
```

---

## Public API

```java
public class DomCleaner {

    private DomCleaner() {}   // utility class — no instances

    public static String clean(String rawHtml) {
        // apply steps 1–6 in sequence
    }
}
```

---

## Implementation Note on HTML Parser

Steps 1–5 are implemented with regex passes rather than a full HTML parser (e.g. Jsoup).
This avoids adding a new dependency. The trade-off:

- Regex is sufficient for removing well-formed blocks (`<script>...</script>`).
- Malformed HTML (unclosed tags, attribute quoting issues) may not be cleaned perfectly —
  acceptable for the MVP goal of reducing token count, as leftover noise does not break
  generation, just wastes tokens.

If cleaning quality becomes a problem in practice, add Jsoup (`org.jsoup:jsoup:1.17.x`)
and rewrite `clean()` using the DOM API. Declare it `implementation` scope only.

---

## Unit Tests

**File:** `src/test/java/unit_tests/ai/crawler/DomCleanerTest.java`

Test cases:
- Input with `<script>` block → output contains no `<script>` tags
- Input with `<style>` block → output contains no `<style>` tags
- Input with `<!-- comment -->` → output contains no `<!--`
- Input with `onclick="foo()"` attribute → attribute removed from output
- Input with excessive whitespace → collapsed to single spaces
- Input of exactly 50,000 chars → not truncated, no marker appended
- Input of 60,000 chars → truncated at or before 50,000, marker appended, output ends with `>`
- Input where `>` does not exist before 50,000 → hard cut applied, no exception
- `null` input → throws `NullPointerException` or `IllegalArgumentException` (document choice)

---

## Definition of Done

- [ ] All six cleaning steps implemented in order
- [ ] `DOM_LIMIT = 50_000` constant, truncation at clean tag boundary
- [ ] Truncation logged as WARN via `iLogger`
- [ ] Truncation marker appended when cutting
- [ ] All unit tests pass
