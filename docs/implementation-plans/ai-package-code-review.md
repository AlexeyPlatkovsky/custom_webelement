# AI Package Code Review — Findings & Fix Plan

**Reviewed:** `src/main/java/ai/`
**Date:** 2026-03-19
**Scope:** 20 files across `crawler`, `generator`, and `provider` packages

---

## Findings

### CRITICAL

#### C1 — API Key Exposed in URL Query String
**File:** `GeminiProvider.java:70`
**Description:** The Gemini API key is appended as a URL query parameter (`?key=<secret>`). Query
parameters appear in server access logs, proxy logs, browser history, and HTTP Referer headers
sent to third parties.
**Risk:** Full API key leakage via logs or intermediaries.
```java
// Current (vulnerable)
url = url + "?key=" + auth.getValue();
```

---

#### C2 — Unbounded Array Access in All Provider Response Parsers
**Files:**
- `AnthropicProvider.java:114` — `root.path("content").get(0)`
- `GeminiProvider.java:102` — `root.path("candidates").get(0).path("content").path("parts").get(0)`
- `OpenAiProvider.java:107` — `root.path("choices").get(0)`

**Description:** `.get(0)` on a Jackson `ArrayNode` throws `IndexOutOfBoundsException` if the
array is empty. An API error response or unexpected payload will crash the provider instead of
producing a meaningful error.
**Risk:** Unhandled crash; no actionable error message for the caller.

---

### HIGH

#### H1 — Resource Leak in `PageCrawler` Constructor
**File:** `PageCrawler.java:22-26`
**Description:** If `browser` creation throws an exception, the `playwright` instance is never
closed. The constructor has no try-finally guard to ensure cleanup.
**Risk:** Native Playwright process leaked on construction failure.

---

#### H2 — NPE After `page.screenshot()`
**File:** `PageCrawler.java:62-63`
**Description:** `page.screenshot()` can return `null` if the screenshot fails. The return value
is passed directly to `Base64.getEncoder().encodeToString(bytes)` without a null check.
**Risk:** NullPointerException silently crashing the crawl.

---

#### H3 — Incomplete Auth Handling in `GeminiProvider`
**File:** `GeminiProvider.java:46-48`
**Description:** `AUTH_TOKEN` adds a `Bearer` header, but `API_KEY` is only handled via the URL
(see C1). No validation or exception is raised for unsupported auth types — the provider silently
proceeds with a malformed request.
**Risk:** Silent auth failure; confusing runtime errors.

---

#### H4 — Silent Failure in `AccessibilitySerializer`
**File:** `AccessibilitySerializer.java:52-58`
**Description:** Any exception during JavaScript evaluation returns `{}` and logs only the message
string — no stack trace. There is no way to distinguish a JS evaluation error from a timing issue
or a Playwright bug.
**Risk:** Accessibility tree silently missing from snapshot; impossible to debug.

---

### MEDIUM

#### M1 — Regex Compiled on Every `DomCleaner.clean()` Call
**File:** `DomCleaner.java:16-22`
**Description:** `replaceAll()` calls compile regex patterns fresh on every invocation. The class
has no static `Pattern` fields.
**Risk:** Unnecessary CPU overhead on every page crawl.

---

#### M2 — `StringIndexOutOfBoundsException` Risk in Class Name Derivation
**File:** `PageObjectGenerator.java:145-146`
**Description:** After `split("/")` and `.filter(!segment.isBlank())`, `segment.charAt(0)` is
called unconditionally. An empty segment can survive the blank check and cause an exception.
**Risk:** Generator crash on certain URL patterns (e.g. trailing slashes, repeated slashes).

---

#### M3 — Null Title Not Handled in `PromptBuilder`
**File:** `PromptBuilder.java:73`
**Description:** `snapshot.getTitle()` can return `null` (Playwright does not guarantee a non-null
title). String concatenation produces `"Title: null"` in the AI prompt.
**Risk:** Misleading content passed to the AI; no crash but incorrect prompt.

---

#### M4 — Proxy Support Inconsistent Across Providers
**Files:** `AnthropicProvider.java:39-45` vs `GeminiProvider.java`, `OpenAiProvider.java`,
`OllamaProvider.java`
**Description:** `AnthropicProvider` configures JVM proxy settings and a custom authenticator on
its `HttpClient`. All other providers use `HttpClient.newHttpClient()` with no proxy support.
**Risk:** Providers other than Anthropic will fail in proxy-restricted environments.

---

#### M5 — Full API Response Body Logged on Error (All Providers)
**Files:** `AnthropicProvider.java:72`, `GeminiProvider.java:60`, `OpenAiProvider.java:67`,
`OllamaProvider.java:54`
**Description:** When a non-200 status is received, the full response body is included in the
exception message without truncation or sanitization.
**Risk:** Sensitive data written to logs; large response bodies may cause issues.

---

#### M6 — Repeated Boilerplate Across All 4 Providers
**Files:** All provider implementations
**Description:** The following patterns are duplicated verbatim in every provider:
- HTTP error checking (`statusCode != 200` → throw RuntimeException)
- `IOException | InterruptedException` catch with `Thread.currentThread().interrupt()`
- `ObjectMapper` instantiated as instance field

**Risk:** Any fix or improvement to error handling must be applied in four places.

---

#### M7 — Batch Crawl Results Not Mappable to Input URLs
**File:** `PageCrawlerFacade.java:63-70`
**Description:** When crawling a list of URLs, failed entries are silently omitted from the result
list. Callers receive a shorter list with no indication of which URLs succeeded or failed.
**Risk:** Data loss in batch operations; caller cannot implement retry logic.

---

#### M8 — Missing Constructor Argument Validation
**Files:** `PageObjectGenerator.java:30`, `OllamaProvider.java:25-26`
**Description:** Constructor arguments (`provider`, `baseUrl`) are not validated for null or empty
values. Failures surface only later at usage time with cryptic errors.
**Risk:** Confusing NullPointerExceptions far from the actual misconfiguration.

---

## Step-by-Step Fix Plan

Steps are ordered critical → medium. Each step is independent unless noted.

---

### Step 1 — Fix Gemini API Key in URL (C1)

**File:** `GeminiProvider.java`

1. Remove the `?key=...` query parameter from the URL.
2. Add a case for `API_KEY` that sets the `x-goog-api-key` request header (Google's documented
   header for API key auth).
3. Ensure `AUTH_TOKEN` continues to use the `Authorization: Bearer` header.
4. Add a default/else branch that throws `UnsupportedAuthException` for any unrecognised type.

---

### Step 2 — Guard All Array Accesses in Response Parsers (C2)

**Files:** `AnthropicProvider.java`, `GeminiProvider.java`, `OpenAiProvider.java`

Replace all `.get(N)` calls with `.path(N)` (returns `MissingNode` instead of throwing).
After each access, check `.isMissingNode()` and throw a descriptive `RuntimeException` with
the full response body included for diagnosis.

```java
JsonNode content = root.path("content").path(0);
if (content.isMissingNode()) {
    throw new RuntimeException(
        "Unexpected response structure — missing 'content[0]'. Body: " + responseBody);
}
```

Apply the same pattern to all nested array accesses in Gemini (`candidates[0]`, `parts[0]`).

---

### Step 3 — Fix Resource Leak in `PageCrawler` Constructor (H1)

**File:** `PageCrawler.java`

1. Wrap `browser = playwright.chromium().launch(...)` in a try-catch.
2. In the catch block, call `playwright.close()` before re-throwing.

```java
try {
    this.browser = playwright.chromium().launch(...);
} catch (Exception e) {
    playwright.close();
    throw e;
}
```

---

### Step 4 — Add Null Check After `page.screenshot()` (H2)

**File:** `PageCrawler.java:62-63`

1. Store the result of `page.screenshot()` in a local variable.
2. If null, log a warning and set `screenshotB64 = null` (the `PageSnapshot` already accepts a
   nullable screenshot field).

```java
byte[] bytes = page.screenshot();
String screenshotB64 = null;
if (bytes != null) {
    screenshotB64 = Base64.getEncoder().encodeToString(bytes);
} else {
    iLogger.warn("Screenshot returned null for URL: " + url);
}
```

---

### Step 5 — Complete Auth Handling in `GeminiProvider` (H3)

**File:** `GeminiProvider.java`

Covered by Step 1. After that fix, verify:
- `API_KEY` → `x-goog-api-key` header (not URL)
- `AUTH_TOKEN` → `Authorization: Bearer` header
- Other types → `throw new UnsupportedAuthException(...)`

---

### Step 6 — Improve `AccessibilitySerializer` Error Logging (H4)

**File:** `AccessibilitySerializer.java:52-58`

1. Change the catch block to log the full exception (not just `e.getMessage()`).
2. Include the page URL in the log message for context.
3. Downgrade to `WARN` if returning `{}` is an acceptable fallback.

```java
} catch (Exception e) {
    iLogger.warn("Failed to extract accessibility tree — returning empty. Cause: " + e.getMessage());
    return "{}";
}
```

---

### Step 7 — Pre-compile Regex Patterns in `DomCleaner` (M1)

**File:** `DomCleaner.java`

1. Extract all inline regex strings to `private static final Pattern` fields.
2. Replace `str.replaceAll("pattern", replacement)` with
   `PATTERN.matcher(str).replaceAll(replacement)`.

---

### Step 8 — Guard `charAt(0)` in Class Name Derivation (M2)

**File:** `PageObjectGenerator.java:145-146`

Add an `!segment.isEmpty()` guard in addition to `!segment.isBlank()`, or replace the manual
char manipulation with logic that handles empty strings safely:

```java
.filter(s -> !s.isBlank())
.filter(s -> !s.isEmpty())
.map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
```

---

### Step 9 — Handle Null Title in `PromptBuilder` (M3)

**File:** `PromptBuilder.java:73`

```java
// Before
"Title: " + snapshot.getTitle()

// After
"Title: " + (snapshot.getTitle() != null ? snapshot.getTitle() : "(no title)")
```

---

### Step 10 — Centralise `HttpClient` Construction with Proxy Support (M4)

**Files:** All providers

1. Extract the proxy-aware `HttpClient` build logic from `AnthropicProvider` into a
   package-private `HttpClientFactory` helper (or a protected method on a shared base class).
2. Replace all `HttpClient.newHttpClient()` calls in `GeminiProvider`, `OpenAiProvider`, and
   `OllamaProvider` with the shared factory.

---

### Step 11 — Truncate Response Body in Error Messages (M5)

**Files:** All 4 providers

Before including `response.body()` in an exception message, truncate to a safe max length:

```java
private static String truncate(String body) {
    return body.length() > 500 ? body.substring(0, 500) + "..." : body;
}
```

Pairs naturally with Step 12 (shared base class can host this helper).

---

### Step 12 — Extract Common Provider Boilerplate to Base Class (M6)

**Files:** All 4 providers + new `AbstractAiProvider.java`

1. Create `AbstractAiProvider` (package-private) implementing `AiProvider`.
2. Move into the base class:
   - `protected final ObjectMapper mapper = new ObjectMapper()`
   - `protected void checkStatus(HttpResponse<String> response)` — non-200 check with truncated body
   - `protected HttpRequest.Builder baseRequest(String url)` — common header setup
   - `protected <T> HttpResponse<T> send(HttpClient client, HttpRequest request, BodyHandler<T> handler)` — wraps `IOException | InterruptedException`
3. Each provider extends `AbstractAiProvider` and implements only request building and response
   parsing.

---

### Step 13 — Return Structured Batch Results in `PageCrawlerFacade` (M7)

**File:** `PageCrawlerFacade.java`

Change the batch crawl return type to expose both successes and failures:

```java
public record CrawlBatchResult(
    Map<String, PageSnapshot> succeeded,
    Map<String, Exception> failed
) {}
```

If a breaking API change is undesirable, add the new method under a different name and
deprecate the old one.

---

### Step 14 — Add Constructor Argument Validation (M8)

**Files:** `PageObjectGenerator.java`, `OllamaProvider.java`

Add `Objects.requireNonNull` (and non-empty checks where applicable) at the top of each
constructor:

```java
this.provider = Objects.requireNonNull(provider, "provider must not be null");
```

---

## Fix Priority Summary

| Step | Issue ID | Description | Severity |
|------|----------|-------------|----------|
| 1 | C1 | Gemini API key in URL | Critical |
| 2 | C2 | Array bounds in response parsers | Critical |
| 3 | H1 | PageCrawler constructor resource leak | High |
| 4 | H2 | Null check after screenshot | High |
| 5 | H3 | Complete Gemini auth handling | High (part of Step 1) |
| 6 | H4 | AccessibilitySerializer error logging | High |
| 7 | M1 | Pre-compile DomCleaner regex | Medium |
| 8 | M2 | Guard charAt(0) in class name derivation | Medium |
| 9 | M3 | Null title in PromptBuilder | Medium |
| 10 | M4 | Centralise HttpClient with proxy support | Medium |
| 11 | M5 | Truncate response body in error messages | Medium |
| 12 | M6 | Extract provider boilerplate to base class | Medium |
| 13 | M7 | Structured batch crawl results | Medium |
| 14 | M8 | Constructor argument validation | Medium |
