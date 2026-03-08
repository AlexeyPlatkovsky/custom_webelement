# Page Crawler — Implementation Plan

## Overview

The **Page Crawler** feature takes one or more URLs and automatically generates Page Object
classes (`.java`) that represent the UI structure of the provided pages. It combines a
Playwright-based DOM/accessibility-tree extractor with an AI provider to produce
framework-compliant `iWebElement`-based Page Object code.

This document covers architecture, integration points, the step-by-step implementation
sequence, the full data flow, and known constraints.

---

## 1. Architecture and Component Breakdown

```
 ┌──────────────────────────────────────────────────────────────────────┐
 │                         Caller / Entry Point                          │
 │                                                                       │
 │   PageCrawlerFacade.generate(List<String> urls)                       │
 │   PageCrawlerFacade.generate(String url)                              │
 └──────────────────────────────┬───────────────────────────────────────┘
                                │  for each URL
           ┌────────────────────▼────────────────────┐
           │             PageCrawler                  │
           │  (ai.crawler.PageCrawler)                │
           │                                          │
           │  • Playwright Chromium (headless)        │
           │  • networkidle wait (30s default)        │
           │  • DomCleaner.clean(rawHtml)             │
           │  • page.accessibility().snapshot()       │
           └────────────────────┬────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │     PageSnapshot       │
                    │  url, title,           │
                    │  cleanedHtml,          │
                    │  accessibilityTree,    │
                    │  screenshotBase64?     │
                    └───────────┬───────────┘
                                │
           ┌────────────────────▼────────────────────┐
           │         PageObjectGenerator              │
           │  (ai.generator.PageObjectGenerator)      │
           │                                          │
           │  • Builds system + user prompts          │
           │  • Calls AiProvider.complete(request)    │
           │  • Extracts Java source from response    │
           │  • Validates class name + syntax         │
           └────────────────────┬────────────────────┘
                                │
           ┌────────────────────▼────────────────────┐
           │        GeneratedPageObject               │
           │  className, javaSource, sourceUrl        │
           └────────────────────┬────────────────────┘
                                │
           ┌────────────────────▼────────────────────┐
           │          PageObjectWriter                │
           │  (ai.generator.PageObjectWriter)         │
           │                                          │
           │  • Writes to src/test/java/generated/   │
           │  • One file per Page Object              │
           └──────────────────────────────────────────┘

Supporting infrastructure (shared with FailureAnalyzer / AiTestGenerator):

  AiProviderFactory  →  AiProvider interface
                            ├── AnthropicProvider
                            ├── GeminiProvider
                            ├── OpenAiProvider
                            └── OllamaProvider

  PropertyReader  →  reads ai-provider.properties + expands ${ENV_VAR}
  AuthConfig      →  API_KEY | AUTH_TOKEN
```

### Component Responsibilities

| Component | Package | Responsibility |
|-----------|---------|----------------|
| `PageCrawlerFacade` | `ai.crawler` | Public API; orchestrates single and batch crawls |
| `PageCrawler` | `ai.crawler` | Playwright lifecycle; raw HTML + accessibility tree extraction |
| `DomCleaner` | `ai.crawler` | Strips scripts/styles/comments, trims empty wrappers, enforces 50 KB cap |
| `PageSnapshot` | `ai.crawler` | Immutable value object holding crawl output |
| `PageObjectGenerator` | `ai.generator` | Prompt construction + AI call + response extraction |
| `GeneratedPageObject` | `ai.generator` | Immutable value object holding generated source |
| `PageObjectWriter` | `ai.generator` | Writes `.java` files to the output directory |
| `AiProvider` | `ai.provider` | Interface: `complete(AiRequest) → AiResponse` |
| `AiProviderFactory` | `ai` | Reads config, wires the correct provider |
| `AuthConfig` | `ai.provider` | Auth type + value; validated at startup |
| `AiRequest` | `ai.provider` | systemPrompt, userMessage, base64Image (nullable) |
| `AiResponse` | `ai.provider` | content, model, inputTokens, outputTokens |
| `AnthropicProvider` | `ai.provider` | Claude via Anthropic Messages API |
| `GeminiProvider` | `ai.provider` | Gemini via Google AI REST API |
| `OpenAiProvider` | `ai.provider` | GPT-4o / Codex via OpenAI Chat Completions |
| `OllamaProvider` | `ai.provider` | Local model via Ollama (OpenAI-compatible REST) |

---

## 2. Integration Points with the Existing Framework

### 2.1 `PropertyReader` — ENV variable expansion (blocking prerequisite)

`ai-provider.properties` uses `${ENV_VAR}` placeholders. The existing `PropertyReader`
does not expand them. This **must** be fixed before any provider can authenticate.

**Change:** in `utils/properties/PropertyReader.java`, after loading the `.properties` file,
iterate all values and replace `${VAR_NAME}` tokens with `System.getenv("VAR_NAME")`.

```java
// pseudocode — to be applied to PropertyReader.load()
properties.replaceAll((k, v) ->
    ENV_PATTERN.matcher((String) v)
        .replaceAll(mr -> Optional.ofNullable(System.getenv(mr.group(1))).orElse(mr.group(0)))
);
```

### 2.2 `iLogger` — unified logging

All AI layer classes use the existing `iLogger` (Log4j2 wrapper) for consistency.
No new logging dependency is introduced. Token counts from `AiResponse` are logged
at INFO level so engineers can track cost without attaching to Allure.

### 2.3 `AbstractPage` / `iPageFactory` — output compatibility

Generated Page Objects extend `AbstractPage` and are initialized via `iPageFactory`.
The generator's system prompt enforces this convention. No changes to either class
are needed; the AI is instructed to follow the existing patterns.

### 2.4 `build.gradle` — new dependencies

Two new dependencies are required:

```groovy
// Playwright Java for DOM + accessibility tree extraction
implementation 'com.microsoft.playwright:playwright:1.44.0'

// Java HTTP client is used for AI provider REST calls (built-in since Java 11 — no new dependency)
// Jackson for JSON serialization of AI request/response bodies
implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.0'
```

Playwright browser binaries are not bundled in the jar. A Gradle task downloads them:

```groovy
tasks.register('installPlaywrightBrowsers', JavaExec) {
    group = 'playwright'
    description = 'Downloads Playwright Chromium browser binaries'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.microsoft.playwright.CLI'
    args = ['install', 'chromium']
}
```

CI pipelines should call `./gradlew installPlaywrightBrowsers` before running tests that
use the Page Crawler.

### 2.5 `ai-provider.properties` — new configuration file

Create `src/main/resources/ai-provider.properties` (template, safe to commit):

```properties
# Active provider: anthropic | gemini | openai | ollama
ai.provider=anthropic

ai.anthropic.auth.type=api_key
ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
ai.anthropic.model=claude-sonnet-4-6

ai.gemini.auth.type=api_key
ai.gemini.auth.value=${GEMINI_API_KEY}
ai.gemini.model=gemini-2.0-flash

ai.openai.auth.type=api_key
ai.openai.auth.value=${OPENAI_API_KEY}
ai.openai.model=gpt-4o

ai.ollama.base-url=http://localhost:11434
ai.ollama.model=qwen2.5-coder
```

### 2.6 Output directory

Generated Page Objects land in `src/test/java/generated/`. This directory is already
on the test compile classpath (per `test-generator.md`). No `build.gradle` change needed.

---

## 3. Step-by-Step Implementation Sequence

### Phase 0 — Infrastructure prerequisites (unblocks everything else)

**Step 0.1 — Extend `PropertyReader` with ENV variable expansion**

- Location: `src/main/java/utils/properties/PropertyReader.java`
- Add a private `expandEnvVars(String value)` method using regex `\$\{([^}]+)\}`
- Call it during property loading after reading each value
- Unit test: `PropertyReaderEnvVarTest` in `unit_tests/utils/`

**Step 0.2 — Add Playwright and Jackson to `build.gradle`**

- Add the two `implementation` entries shown in §2.4
- Add the `installPlaywrightBrowsers` Gradle task
- Verify `./gradlew compileJava` succeeds

**Step 0.3 — Create `ai-provider.properties`**

- Create the template file at `src/main/resources/ai-provider.properties`
- Add it to `.gitignore` comment guidance (file is safe to commit; secrets stay in ENV)

---

### Phase 1 — AI provider layer

**Step 1.1 — Core data model**

Create the following in `src/main/java/ai/provider/`:

```
ai/provider/
├── AuthConfig.java          // AuthType enum + value field
├── AiRequest.java           // systemPrompt, userMessage, base64Image
├── AiResponse.java          // content, model, inputTokens, outputTokens
├── AiProvider.java          // interface: complete(AiRequest)
└── UnsupportedAuthException.java  // extends RuntimeException
```

Use Lombok `@Value` for immutable data classes where it reduces boilerplate.

**Step 1.2 — `AnthropicProvider`**

- Package: `ai.provider`
- Auth: API_KEY only; AUTH_TOKEN throws `UnsupportedAuthException` at construction time
- HTTP: `java.net.http.HttpClient` POST to `https://api.anthropic.com/v1/messages`
- Headers: `x-api-key`, `anthropic-version: 2023-06-01`, `content-type: application/json`
- Request body: `{"model": "...", "max_tokens": 4096, "system": "...", "messages": [{"role": "user", "content": "..."}]}`
- Response parsing: extract `content[0].text`, `usage.input_tokens`, `usage.output_tokens`

**Step 1.3 — `OllamaProvider`** (enables keyless local testing)

- Base URL read from `ai.ollama.base-url` property
- POST to `{baseUrl}/api/chat` with OpenAI-compatible payload
- Auth: ignored entirely

**Step 1.4 — `GeminiProvider`**

- API_KEY: query param `?key={value}` on `https://generativelanguage.googleapis.com/`
- AUTH_TOKEN: `Authorization: Bearer {value}`
- Maps `AiRequest` to Gemini `generateContent` body format

**Step 1.5 — `OpenAiProvider`**

- API_KEY: `Authorization: Bearer {value}`, base URL `api.openai.com`
- AUTH_TOKEN: `Authorization: Bearer {value}`, base URL `chatgpt.com/backend-api/codex`
- On 401 with AUTH_TOKEN: surfaces "Codex OAuth token expired — run: `codex login`"

**Step 1.6 — `AiProviderFactory`**

- Location: `src/main/java/ai/AiProviderFactory.java`
- Reads `ai-provider.properties` via `PropertyReader`
- Switch on `ai.provider` value to instantiate the correct provider
- Fail fast on unknown provider value

**Unit tests for Phase 1:**

- `AnthropicProviderTest` — mock `HttpClient`, verify request shape, header presence
- `OllamaProviderTest` — mock HTTP, verify endpoint and body
- `AiProviderFactoryTest` — property-driven provider selection

---

### Phase 2 — PageCrawler

**Step 2.1 — `PageSnapshot` value object**

```java
// src/main/java/ai/crawler/PageSnapshot.java
@Value
public class PageSnapshot {
    String url;
    String title;
    String cleanedHtml;
    String accessibilityTree;   // JSON string
    String screenshotBase64;    // null unless crawlWithScreenshot() was called
}
```

**Step 2.2 — `DomCleaner`**

- Location: `src/main/java/ai/crawler/DomCleaner.java`
- Input: raw `page.content()` HTML string
- Operations (in order):
  1. Remove `<script>` and `<style>` blocks
  2. Remove HTML comments `<!-- ... -->`
  3. Strip inline event handler attributes (`onclick`, `onmouseover`, etc.)
  4. Remove empty structural elements: `<div>` / `<span>` with no text, no `id`, no `role`,
     no `class` that carries semantic meaning
  5. Collapse multiple whitespace sequences to a single space
  6. Truncate to 50,000 characters at a clean tag boundary (not mid-token)
- Returns cleaned `String`
- Implementation note: use Java's built-in `javax.xml` or simple regex passes; avoid adding
  an external HTML parser dependency. Jsoup would be ideal but adds a dependency — evaluate
  whether the benefit justifies the addition. If Jsoup is added, declare it `implementation`
  scope only.

**Step 2.3 — `PageCrawler`**

```java
// src/main/java/ai/crawler/PageCrawler.java
public class PageCrawler implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;

    public PageCrawler() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    public PageSnapshot crawl(String url) { ... }

    public PageSnapshot crawlWithScreenshot(String url) { ... }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }
}
```

Internal `crawl` logic:

1. Create a new `BrowserContext` (isolated session per crawl)
2. `page.navigate(url)` with `WaitUntilState.NETWORKIDLE` and 30s timeout
3. Capture `page.title()`
4. Capture `page.content()` → pass through `DomCleaner.clean()`
5. Capture `page.accessibility().snapshot()` → serialize to JSON string
6. If screenshot requested: `page.screenshot()` → Base64 encode
7. Close context (releases memory, cookies, local storage)
8. Return `PageSnapshot`

**Step 2.4 — `PageCrawlerFacade`**

Provides the public API and manages `PageCrawler` lifecycle:

```java
public class PageCrawlerFacade {

    // Single URL
    public static PageSnapshot crawl(String url) { ... }

    // Batch — returns snapshots in the same order as input URLs
    public static List<PageSnapshot> crawl(List<String> urls) { ... }
}
```

Internally creates one `PageCrawler` instance (one Playwright + Browser), crawls all URLs
sequentially, then closes. This avoids spawning multiple browser processes for batch runs.

**Unit / integration tests for Phase 2:**

- `DomCleanerTest` — verify script removal, comment stripping, 50KB cap, whitespace collapse
- `PageCrawlerTest` — integration test using a local HTML file served via a simple HTTP server
  (`com.sun.net.httpserver` from JDK, no extra dep). Verifies title, cleanedHtml non-null,
  accessibilityTree parseable as JSON.

---

### Phase 3 — Page Object Generator

**Step 3.1 — Prompt design**

System prompt (stored as a string constant or classpath resource):

```
You are a Java test automation engineer working with the custom_webelement framework.
Generate a single Page Object class for the provided page snapshot.

Framework conventions:
- Extend AbstractPage (package: pages)
- Annotate the class with @PageURL("...") using the page URL
- Declare all interactive elements as `iWebElement` (not WebElement)
- Declare all repeated element lists as `iWebElementsList`
- Initialize in the constructor via: iPageFactory.initElements(this.driver, this)
- Use @FindBy(css = "...") or @FindBy(xpath = "...") for locators
  - Prefer CSS selectors; use XPath only when CSS is insufficient
  - Prefer id > data-testid > aria-label > CSS class > XPath text()
- Apply @CacheElement to static elements (headers, nav, stable form fields)
- Apply @Waiter(waitFor = N) to slow-loading elements (default 5s; increase if needed)
- Use @FindBy(xpath = "//tag[text()='%s']") + .template(value) for repeated items
- Name fields descriptively (e.g., loginButton, emailInput — NOT element1, div3)
- Add public action methods that group related interactions
- Do NOT include test methods (@Test annotations)
- Do NOT include main() method
- Output ONLY the Java source code — no explanation, no markdown code blocks

Required imports (always include these):
  import core.web.iWebElement;
  import core.web.iWebElementsList;
  import core.web.iPageFactory;
  import core.web.annotations.CacheElement;
  import core.web.annotations.Waiter;
  import core.web.annotations.PageURL;
  import org.openqa.selenium.support.FindBy;
  import pages.AbstractPage;
```

User message template:

```
Page URL: {url}
Page title: {title}

--- CLEANED HTML ---
{cleanedHtml}

--- ACCESSIBILITY TREE ---
{accessibilityTree}

Generate a complete Page Object class for this page following the framework conventions
in the system prompt.
```

**Step 3.2 — `PageObjectGenerator`**

```java
// src/main/java/ai/generator/PageObjectGenerator.java
public class PageObjectGenerator {

    private final AiProvider provider;

    public PageObjectGenerator(AiProvider provider) {
        this.provider = provider;
    }

    public GeneratedPageObject generate(PageSnapshot snapshot) {
        String systemPrompt = loadSystemPrompt();   // from classpath resource
        String userMessage  = buildUserMessage(snapshot);

        AiRequest request = new AiRequest(systemPrompt, userMessage, null);
        AiResponse response = provider.complete(request);

        iLogger.info(String.format(
            "PageObjectGenerator: url=%s model=%s inputTokens=%d outputTokens=%d",
            snapshot.getUrl(), response.getModel(),
            response.getInputTokens(), response.getOutputTokens()
        ));

        String javaSource = extractJavaSource(response.getContent());
        String className  = extractClassName(javaSource);

        return new GeneratedPageObject(className, javaSource, snapshot.getUrl());
    }
}
```

`extractJavaSource`: strips any leading/trailing markdown fences (` ```java `) that the model
may emit despite instructions. Trims whitespace.

`extractClassName`: regex `public\s+class\s+(\w+)` on the source. Throws
`IllegalStateException` if no class declaration found (indicates a bad generation).

**Step 3.3 — `GeneratedPageObject` value object**

```java
@Value
public class GeneratedPageObject {
    String className;
    String javaSource;
    String sourceUrl;
}
```

**Step 3.4 — `PageObjectWriter`**

```java
// src/main/java/ai/generator/PageObjectWriter.java
public class PageObjectWriter {

    private static final String OUTPUT_DIR = "src/test/java/generated";

    public void write(GeneratedPageObject pageObject) {
        Path outputPath = Paths.get(OUTPUT_DIR, pageObject.getClassName() + ".java");
        Files.writeString(outputPath, pageObject.getJavaSource(), StandardOpenOption.CREATE_NEW);
        iLogger.info("PageObjectWriter: written → " + outputPath.toAbsolutePath());
    }
}
```

Uses `CREATE_NEW` to avoid silently overwriting existing files. If the file already exists,
an `IOException` is thrown — the caller must handle or delete first.

**Step 3.5 — Wire everything in `PageCrawlerFacade`**

```java
public class PageCrawlerFacade {

    public static void generatePageObjects(List<String> urls) {
        AiProvider provider = AiProviderFactory.create();
        PageObjectGenerator generator = new PageObjectGenerator(provider);
        PageObjectWriter writer = new PageObjectWriter();

        try (PageCrawler crawler = new PageCrawler()) {
            for (String url : urls) {
                iLogger.info("PageCrawlerFacade: crawling " + url);
                PageSnapshot snapshot = crawler.crawl(url);
                GeneratedPageObject pageObject = generator.generate(snapshot);
                writer.write(pageObject);
            }
        }
    }

    public static void generatePageObject(String url) {
        generatePageObjects(List.of(url));
    }
}
```

**Unit tests for Phase 3:**

- `PageObjectGeneratorTest` — mock `AiProvider`; verify prompt structure contains URL/title/
  cleanedHtml/accessibilityTree; verify class name extraction; verify markdown fence stripping
- `PageObjectWriterTest` — verify file created with correct name; verify duplicate detection
- `PageCrawlerFacadeTest` — integration: mock `AiProvider`, use local test HTML, verify
  `.java` file written with `class` keyword present

---

### Phase 4 — Validation and polish

**Step 4.1 — Basic syntax check on generated source**

Before writing, attempt to detect obvious generation failures:

```java
private void validateSource(String javaSource) {
    if (!javaSource.contains("class ")) {
        throw new IllegalStateException("Generated output does not contain a class declaration. "
            + "Response may be truncated or the model returned an error message.");
    }
    if (javaSource.contains("```")) {
        throw new IllegalStateException("Generated output still contains markdown fences. "
            + "Source extraction may have failed.");
    }
}
```

**Step 4.2 — `@SuppressWarnings` and package declaration injection**

The AI is instructed to output only source, but it may omit `package generated;`. If the
class declaration is found but no `package` declaration is present, prepend
`package generated;\n\n` automatically.

**Step 4.3 — Batch summary logging**

After all URLs are processed, log a summary:

```
PageCrawlerFacade: batch complete — 3 page objects generated
  ✓ LoginPage    ← https://example.com/login
  ✓ DashboardPage ← https://example.com/dashboard
  ✗ CheckoutPage ← https://example.com/checkout [FAILED: timeout after 30s]
```

Failed URLs are logged but do not abort the batch. Results for successful URLs are still
written.

---

## 4. Data Flow: URL Input to Page Object Output

```
Developer invocation
    │
    │   PageCrawlerFacade.generatePageObject("https://example.com/login")
    │
    ▼
[1] URL validation
    │   • Check URL is well-formed (java.net.URI.toURL())
    │   • Reject non-http(s) schemes
    │
    ▼
[2] PageCrawler.crawl(url)
    │
    ├── Playwright creates isolated BrowserContext
    ├── page.navigate(url, NETWORKIDLE, 30s timeout)
    ├── page.title()                         → "Login | My App"
    ├── page.content()                       → raw HTML (~200KB)
    │       └── DomCleaner.clean(rawHtml)    → trimmed HTML (≤50KB)
    ├── page.accessibility().snapshot()      → JSON tree (~10–40KB)
    └── BrowserContext.close()
    │
    ▼
[3] PageSnapshot assembled
    │   url              = "https://example.com/login"
    │   title            = "Login | My App"
    │   cleanedHtml      = "<body><form id=\"login-form\">...</form></body>"
    │   accessibilityTree = "{\"role\":\"RootWebArea\",\"name\":\"Login | My App\",...}"
    │
    ▼
[4] PageObjectGenerator.generate(snapshot)
    │
    ├── systemPrompt loaded from classpath resource
    ├── userMessage = template(url, title, cleanedHtml, accessibilityTree)
    ├── AiRequest(systemPrompt, userMessage, null)
    │
    ├── AiProviderFactory.create()
    │       └── reads ai-provider.properties
    │               ai.provider = anthropic
    │               ai.anthropic.auth.value = ${ANTHROPIC_API_KEY}  → expands via PropertyReader
    │
    ├── AnthropicProvider.complete(request)
    │       └── POST https://api.anthropic.com/v1/messages
    │           Response: { "content": [{ "text": "package generated;\n\npublic class LoginPage..." }],
    │                       "usage": { "input_tokens": 2847, "output_tokens": 634 } }
    │
    ├── extractJavaSource(response.content)  → strips any markdown fences
    ├── extractClassName(javaSource)          → "LoginPage"
    └── returns GeneratedPageObject("LoginPage", "package generated;\n...", url)
    │
    ▼
[5] PageObjectWriter.write(pageObject)
    │
    ├── Output path: src/test/java/generated/LoginPage.java
    ├── Inject "package generated;\n\n" if missing
    ├── Files.writeString(..., CREATE_NEW)
    └── iLogger.info("PageObjectWriter: written → /.../generated/LoginPage.java")
    │
    ▼
[6] Result
    src/test/java/generated/LoginPage.java
    ─────────────────────────────────────────
    package generated;

    import core.web.iWebElement;
    import core.web.iPageFactory;
    import core.web.annotations.CacheElement;
    import core.web.annotations.PageURL;
    import org.openqa.selenium.support.FindBy;
    import pages.AbstractPage;

    @PageURL("https://example.com/login")
    public class LoginPage extends AbstractPage {

        @FindBy(id = "email")
        @CacheElement
        private iWebElement emailInput;

        @FindBy(id = "password")
        @CacheElement
        private iWebElement passwordInput;

        @FindBy(css = "button[type='submit']")
        private iWebElement loginButton;

        public LoginPage() {
            iPageFactory.initElements(this.driver, this);
        }

        public LoginPage enterEmail(String email) {
            emailInput.sendText(email);
            return this;
        }

        public LoginPage enterPassword(String password) {
            passwordInput.sendText(password);
            return this;
        }

        public void clickLogin() {
            loginButton.click();
        }
    }
```

---

## 5. Edge Cases and Constraints

### 5.1 Authentication-protected pages

**Problem:** `PageCrawler` loads the page in a fresh, unauthenticated Playwright context.
Pages behind a login wall return the login page, not the target content.

**Constraint (MVP):** Static crawl only. No session injection in Phase 1.

**Workaround documented for users:**
- Crawl the login page → generate `LoginPage`
- Manually navigate after login → use Selenium DevTools to export cookies → inject via
  `BrowserContext.addCookies()` in a future `crawlWithSession(url, cookies)` overload

**Planned extension (post-MVP):** `PageCrawler.crawlWithCookies(String url, List<Cookie> cookies)`
passes cookies into the `BrowserContext` before `navigate()`.

### 5.2 JavaScript-heavy SPAs (lazy loading)

**Problem:** `NETWORKIDLE` wait does not guarantee all lazy-loaded content (infinite scroll,
deferred components) is visible at snapshot time.

**Constraint:** Some components may not appear in the snapshot.

**Mitigations:**
- Wait for `NETWORKIDLE` which handles most XHR/fetch-driven rendering
- Document that generated Page Objects may be incomplete for heavy SPAs
- Planned: `crawlWithDelay(url, extraWaitMs)` adds a `page.waitForTimeout(N)` after
  `networkidle` before taking the snapshot

### 5.3 Bot-detection and Captcha

**Problem:** Some sites detect headless Chromium and serve a challenge page.

**Constraint:** No built-in bypass.

**Response:** Document the limitation. If the generated Page Object only contains the
Captcha elements, it is a signal the site blocked the crawler.

### 5.4 DOM size exceeding 50 KB cap

**Problem:** After cleaning, `cleanedHtml` may still exceed 50,000 characters, producing
a prompt that hits model context limits or generates a truncated/broken class.

**DomCleaner strategy:**
1. Apply all cleaning steps first
2. If still > 50,000 characters, truncate at the last complete `>` before the 50,000th
   character (never truncate mid-tag)
3. Append a comment: `<!-- [DOM TRUNCATED AT 50KB LIMIT] -->`

**Impact on generation quality:** truncation means some page elements will not appear in
the Page Object. Logged as a WARN in `PageCrawler`.

### 5.5 AI response quality failures

| Failure mode | Detection | Recovery |
|---|---|---|
| Response contains no `class` declaration | `validateSource()` throws | Log ERROR, skip file write, continue batch |
| Response still contains markdown fences | `extractJavaSource()` strips them | Transparent to caller |
| Response has wrong class name | `extractClassName()` regex fails | Derive name from URL path segment as fallback |
| Response has syntax errors (unmatched braces, missing semicolons) | Not detected in MVP (requires compiler invocation) | Documented: review generated files before running |
| AI returns an error message instead of code | No `class` keyword found | `validateSource()` throws; message logged |

### 5.6 Duplicate Page Object class names

**Problem:** Two different URLs may map to the same derived class name (e.g., two login
pages at `/en/login` and `/fr/login` both produce `LoginPage`).

**Detection:** `PageObjectWriter` uses `CREATE_NEW` — `FileAlreadyExistsException` is thrown.

**Recovery:** Append a numeric suffix: `LoginPage2.java`. Log a WARN.
Alternatively, prefix with a hash of the URL (less readable). Suffix approach chosen for MVP.

### 5.7 Network timeout during crawl

**Problem:** `page.navigate()` times out after 30s.

**Behavior:** Playwright throws a `PlaywrightException`. `PageCrawler.crawl()` catches it,
wraps in a `PageCrawlerException` (extends `RuntimeException`), closes the context, and
re-throws.

**Batch mode:** `PageCrawlerFacade` catches `PageCrawlerException` per URL, logs it, and
continues with remaining URLs. The failed URL is included in the batch summary with
`[FAILED: ...]`.

### 5.8 Playwright binary download in CI

**Problem:** CI agents without internet access cannot download Playwright Chromium binaries
at runtime.

**Solution:**
- Pre-download via `./gradlew installPlaywrightBrowsers` in a build step with internet access
- Cache the directory pointed to by `PLAYWRIGHT_BROWSERS_PATH` between CI runs
- Document this in CI setup guide

### 5.9 Thread safety in batch mode

`PageCrawler` holds a single Playwright `Browser` instance. Playwright Java is not
thread-safe — multiple `Page` instances on the same `Browser` must be used from the same
thread.

**MVP constraint:** batch crawl is sequential (single thread). Each URL gets its own
`BrowserContext` (isolated), but all run on the calling thread.

**Post-MVP parallel path:** use `PlaywrightFactory` thread-local pattern with one
`Playwright` + `Browser` per thread, coordinated by a `ForkJoinPool`.

### 5.10 PropertyReader ENV expansion — missing variable

If `${ENV_VAR}` is used in `ai-provider.properties` but the variable is not set in the
environment, the `PropertyReader` expansion leaves the literal string `${ENV_VAR}` as the
value. `AiProviderFactory` must detect this and throw at startup with a clear message:

```
IllegalStateException: ai.anthropic.auth.value is not set.
Set environment variable ANTHROPIC_API_KEY before running.
```

---

## 6. File Layout After Implementation

```
src/main/java/
└── ai/
    ├── AiProviderFactory.java
    ├── crawler/
    │   ├── DomCleaner.java
    │   ├── PageCrawler.java
    │   ├── PageCrawlerFacade.java
    │   └── PageSnapshot.java
    ├── generator/
    │   ├── GeneratedPageObject.java
    │   ├── PageObjectGenerator.java
    │   └── PageObjectWriter.java
    └── provider/
        ├── AiProvider.java
        ├── AiRequest.java
        ├── AiResponse.java
        ├── AuthConfig.java
        ├── UnsupportedAuthException.java
        ├── AnthropicProvider.java
        ├── GeminiProvider.java
        ├── OpenAiProvider.java
        └── OllamaProvider.java

src/main/resources/
├── ai-provider.properties          (new)
├── ai/generator/system-prompt.txt  (new — loaded from classpath)
└── ... (existing)

src/test/java/
├── generated/                      (output directory — already exists per docs)
└── unit_tests/
    ├── ai/
    │   ├── crawler/
    │   │   ├── DomCleanerTest.java
    │   │   ├── PageCrawlerTest.java    (integration, uses local HTTP server)
    │   │   └── PageCrawlerFacadeTest.java
    │   ├── generator/
    │   │   ├── PageObjectGeneratorTest.java
    │   │   └── PageObjectWriterTest.java
    │   └── provider/
    │       ├── AnthropicProviderTest.java
    │       ├── OllamaProviderTest.java
    │       └── AiProviderFactoryTest.java
    └── utils/
        └── properties/
            └── PropertyReaderEnvVarTest.java (new)
```

---

## 7. Implementation Sequence Summary

| Phase | Step | Deliverable | Depends on |
|-------|------|-------------|------------|
| 0 | 0.1 | `PropertyReader` ENV expansion | — |
| 0 | 0.2 | `build.gradle` + Playwright task | — |
| 0 | 0.3 | `ai-provider.properties` template | — |
| 1 | 1.1 | `AuthConfig`, `AiRequest`, `AiResponse`, `AiProvider` interface | Phase 0 |
| 1 | 1.2 | `AnthropicProvider` | 1.1 |
| 1 | 1.3 | `OllamaProvider` | 1.1 |
| 1 | 1.4 | `GeminiProvider` | 1.1 |
| 1 | 1.5 | `OpenAiProvider` | 1.1 |
| 1 | 1.6 | `AiProviderFactory` | 1.2–1.5 |
| 2 | 2.1 | `PageSnapshot` | — |
| 2 | 2.2 | `DomCleaner` | — |
| 2 | 2.3 | `PageCrawler` | 2.1, 2.2, Phase 0.2 |
| 2 | 2.4 | `PageCrawlerFacade` (crawl only) | 2.3 |
| 3 | 3.1 | System prompt resource | Docs review |
| 3 | 3.2 | `PageObjectGenerator` | Phase 1, 2.1 |
| 3 | 3.3 | `GeneratedPageObject` | — |
| 3 | 3.4 | `PageObjectWriter` | 3.3 |
| 3 | 3.5 | `PageCrawlerFacade` (full wiring) | 2.4, 3.2, 3.4 |
| 4 | 4.1 | Source validation | 3.2 |
| 4 | 4.2 | Package declaration injection | 3.2 |
| 4 | 4.3 | Batch summary logging | 3.5 |

---

## 8. Acceptance Criteria

The implementation is complete when:

1. `PageCrawlerFacade.generatePageObject("https://example.com")` produces a `.java` file in
   `src/test/java/generated/` that compiles without errors.
2. The generated class extends `AbstractPage`, uses `iWebElement` fields with `@FindBy`,
   and initializes via `iPageFactory.initElements()`.
3. `PageCrawlerFacade.generatePageObject(List.of(url1, url2, url3))` produces three separate
   `.java` files; a failure on one URL does not abort the others.
4. Switching `ai.provider=ollama` in `ai-provider.properties` (with Ollama running locally)
   generates a Page Object without any external API key.
5. All unit tests in `unit_tests/ai/` pass under `./gradlew test -Psuite=unit`.
6. `./gradlew compileTestJava` succeeds after generating a Page Object from a real URL.
