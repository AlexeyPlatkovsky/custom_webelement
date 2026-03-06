# AI-QA Copilot — Implementation Plan

## How to Use This Document

This file is the single source of truth for implementation progress.
**After completing each step, mark it done by replacing `[ ]` with `[x]`.**

Phases must be completed in order — each phase lists its dependencies.
Deferred items at the bottom are explicitly out of scope for this MVP.

---

## Phase Summary

| Phase | Name | Depends On | Status |
|-------|------|------------|--------|
| 0 | Foundation | — | `[ ] Not started` |
| 1 | AI Provider Abstraction | Phase 0 | `[ ] Not started` |
| 2 | Provider Implementations | Phase 1 | `[ ] Not started` |
| 3 | PageCrawler | Phase 1 | `[ ] Not started` |
| 4 | FailureAnalyzer | Phase 1 | `[ ] Not started` |
| 5 | AiTestGenerator | Phase 3, Phase 4 | `[ ] Not started` |
| 6 | Integration & Cleanup | Phase 2–5 | `[ ] Not started` |

---

## Phase 0 — Foundation

**Goal:** Unblock all provider authentication. Nothing in Phases 1–6 works until `PropertyReader` expands `${ENV_VAR}` placeholders.

### Steps

- [ ] Extend `PropertyReader` to expand `${ENV_VAR}` syntax at load time
- [ ] Add `ai-provider.properties` template to `src/main/resources/`
- [ ] Add `ai-provider.properties` to `.gitignore` (actual credentials must not be committed)

### Class Skeleton

**`utils/readers/PropertyReader.java`** — extend existing class

```java
// Add to the existing load() or getProperty() method:

private String expandEnvVars(String value) {
    // Matches ${VAR_NAME} patterns and replaces with System.getenv("VAR_NAME")
    // Returns value unchanged if no match or env var is not set
    Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
    Matcher matcher = pattern.matcher(value);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
        String envKey = matcher.group(1);
        String envVal = System.getenv(envKey);
        matcher.appendReplacement(result, envVal != null ? envVal : matcher.group(0));
    }
    matcher.appendTail(result);
    return result.toString();
}
```

**`src/main/resources/ai-provider.properties`** — new file (template)

```properties
# Active provider: anthropic | gemini | openai | ollama
ai.provider=anthropic

# ── Anthropic ──────────────────────────────────────────
ai.anthropic.auth.type=api_key
ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
ai.anthropic.model=claude-sonnet-4-20250514

# ── Gemini ─────────────────────────────────────────────
# ai.provider=gemini
ai.gemini.auth.type=api_key
ai.gemini.auth.value=${GEMINI_API_KEY}
ai.gemini.model=gemini-2.0-flash

# ── OpenAI ─────────────────────────────────────────────
# ai.provider=openai
ai.openai.auth.type=api_key
ai.openai.auth.value=${OPENAI_API_KEY}
ai.openai.model=gpt-4o

# ── Ollama (local, no auth) ─────────────────────────────
# ai.provider=ollama
ai.ollama.base-url=http://localhost:11434
ai.ollama.model=qwen2.5-coder

# ── Feature flags ───────────────────────────────────────
ai.failure-analyzer.enabled=true
```

### Definition of Done

- [ ] `PropertyReader` unit test: property with `${TEST_VAR}` expands correctly when `TEST_VAR` is set in environment
- [ ] `PropertyReader` unit test: property with `${UNSET_VAR}` is returned as-is (not null, not blank)
- [ ] `ai-provider.properties` is listed in `.gitignore`

---

## Phase 1 — AI Provider Abstraction

**Goal:** Define the shared interfaces and data objects that all consumers (`FailureAnalyzer`, `AiTestGenerator`) and all providers depend on.

**Depends on:** Phase 0

### Steps

- [ ] Create package `ai.provider`
- [ ] Implement `AuthConfig` with `AuthType` enum
- [ ] Implement `UnsupportedAuthException`
- [ ] Implement `AiRequest`
- [ ] Implement `AiResponse`
- [ ] Implement `AiProvider` interface
- [ ] Implement `AiProviderFactory`

### Class Skeletons

**`ai/provider/AuthConfig.java`**

```java
package ai.provider;

public class AuthConfig {
    private final AuthType type;
    private final String value;

    public enum AuthType {
        API_KEY,
        AUTH_TOKEN
    }

    public AuthConfig(AuthType type, String value) { ... }
    public AuthType getType() { ... }
    public String getValue() { ... }
}
```

**`ai/provider/UnsupportedAuthException.java`**

```java
package ai.provider;

public class UnsupportedAuthException extends RuntimeException {
    public UnsupportedAuthException(String message) { super(message); }
}
```

**`ai/provider/AiRequest.java`**

```java
package ai.provider;

import java.util.Optional;

public class AiRequest {
    private final String systemPrompt;
    private final String userMessage;
    private final Optional<String> base64Image;   // empty if vision not needed

    public static AiRequest of(String systemPrompt, String userMessage) { ... }
    public static AiRequest withImage(String systemPrompt, String userMessage, String base64Image) { ... }

    public String getSystemPrompt() { ... }
    public String getUserMessage() { ... }
    public Optional<String> getBase64Image() { ... }
}
```

**`ai/provider/AiResponse.java`**

```java
package ai.provider;

public class AiResponse {
    private final String content;
    private final String model;
    private final int inputTokens;
    private final int outputTokens;

    public AiResponse(String content, String model, int inputTokens, int outputTokens) { ... }
    public String getContent() { ... }
    public String getModel() { ... }
    public int getInputTokens() { ... }
    public int getOutputTokens() { ... }

    @Override
    public String toString() {
        return String.format("model=%s | in=%d | out=%d", model, inputTokens, outputTokens);
    }
}
```

**`ai/provider/AiProvider.java`**

```java
package ai.provider;

public interface AiProvider {
    AiResponse complete(AiRequest request);
}
```

**`ai/AiProviderFactory.java`**

```java
package ai;

public class AiProviderFactory {
    private static final String CONFIG_FILE = "ai-provider.properties";

    public static AiProvider create() {
        // 1. Load ai-provider.properties via PropertyReader
        // 2. Read "ai.provider" key
        // 3. Build AuthConfig for selected provider
        // 4. Return matching implementation:
        return switch (provider) {
            case "anthropic" -> new AnthropicProvider(loadAuth("anthropic"), loadModel("anthropic"));
            case "gemini"    -> new GeminiProvider(loadAuth("gemini"),    loadModel("gemini"));
            case "openai"    -> new OpenAiProvider(loadAuth("openai"),    loadModel("openai"));
            case "ollama"    -> new OllamaProvider(loadBaseUrl(),         loadModel("ollama"));
            default -> throw new IllegalArgumentException("Unknown ai.provider: " + provider);
        };
    }

    private static AuthConfig loadAuth(String prefix) { ... }
    private static String loadModel(String prefix) { ... }
    private static String loadBaseUrl() { ... }
}
```

### Definition of Done

- [ ] `AiProviderFactory` unit test: `ai.provider=anthropic` + valid `api_key` returns `AnthropicProvider` instance
- [ ] `AiProviderFactory` unit test: `ai.provider=unknown` throws `IllegalArgumentException`
- [ ] `AiRequest.base64Image` returns `Optional.empty()` when constructed with `AiRequest.of()`
- [ ] All classes compile with no Lombok or external dependencies (pure Java)

---

## Phase 2 — Provider Implementations

**Goal:** Working HTTP clients for all four providers.

**Depends on:** Phase 1

### Steps

- [ ] Implement `AnthropicProvider`
- [ ] Implement `OllamaProvider`
- [ ] Implement `OpenAiProvider`
- [ ] Implement `GeminiProvider`

### Class Skeletons

**`ai/provider/AnthropicProvider.java`**

```java
package ai.provider;

public class AnthropicProvider implements AiProvider {
    private static final String BASE_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";

    private final String apiKey;
    private final String model;

    public AnthropicProvider(AuthConfig auth, String model) {
        if (auth.getType() != AuthConfig.AuthType.API_KEY) {
            throw new UnsupportedAuthException(
                "Anthropic does not support auth_token. Use api_key.");
        }
        this.apiKey = auth.getValue();
        this.model = model;
    }

    @Override
    public AiResponse complete(AiRequest request) {
        // Build JSON body: { model, max_tokens, system, messages: [{role:user, content}] }
        // Set headers: x-api-key, anthropic-version, content-type
        // POST to BASE_URL
        // Parse response: content[0].text, usage.input_tokens, usage.output_tokens
        // Return AiResponse
    }
}
```

**`ai/provider/OllamaProvider.java`**

```java
package ai.provider;

public class OllamaProvider implements AiProvider {
    private final String baseUrl;   // e.g. http://localhost:11434
    private final String model;

    public OllamaProvider(String baseUrl, String model) { ... }

    @Override
    public AiResponse complete(AiRequest request) {
        // POST to {baseUrl}/api/chat (OpenAI-compatible format)
        // No auth headers
        // Parse response: message.content, prompt_eval_count, eval_count
    }
}
```

**`ai/provider/OpenAiProvider.java`**

```java
package ai.provider;

public class OpenAiProvider implements AiProvider {
    private static final String API_BASE_URL   = "https://api.openai.com/v1/chat/completions";
    private static final String CODEX_BASE_URL = "https://chatgpt.com/backend-api/codex/completions";

    private final String token;
    private final String model;
    private final String baseUrl;

    public OpenAiProvider(AuthConfig auth, String model) {
        this.token = auth.getValue();
        this.model = model;
        // AUTH_TOKEN → Codex endpoint; API_KEY → standard endpoint
        this.baseUrl = auth.getType() == AuthConfig.AuthType.AUTH_TOKEN
            ? CODEX_BASE_URL : API_BASE_URL;
    }

    @Override
    public AiResponse complete(AiRequest request) {
        // POST to baseUrl with Authorization: Bearer {token}
        // On 401: surface "Codex OAuth token expired — run: codex login"
        // Parse response: choices[0].message.content, usage.*
    }
}
```

**`ai/provider/GeminiProvider.java`**

```java
package ai.provider;

public class GeminiProvider implements AiProvider {
    private static final String BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final AuthConfig auth;
    private final String model;

    public GeminiProvider(AuthConfig auth, String model) { ... }

    @Override
    public AiResponse complete(AiRequest request) {
        // API_KEY    → append ?key={value} to URL
        // AUTH_TOKEN → set Authorization: Bearer {value} header
        // Build body: { contents: [{ role: user, parts: [{ text }] }] }
        // Parse response: candidates[0].content.parts[0].text, usageMetadata.*
    }
}
```

### Definition of Done

- [ ] Each provider compiles and connects (smoke test with real or mocked HTTP)
- [ ] `AnthropicProvider` throws `UnsupportedAuthException` when constructed with `AUTH_TOKEN`
- [ ] `OpenAiProvider` uses `CODEX_BASE_URL` when `AUTH_TOKEN` is supplied
- [ ] `OllamaProvider` sends no auth headers (verified by request inspection in tests)
- [ ] All providers log `AiResponse.toString()` (model + token counts) at DEBUG level via `iLogger`

---

## Phase 3 — PageCrawler

**Goal:** Extract DOM and accessibility tree from any URL for use as AI prompt context.

**Depends on:** Phase 1 (uses `PageSnapshot` returned to `AiTestGenerator`)

### Steps

- [ ] Add Playwright dependency to `build.gradle`
- [ ] Create package `ai.crawler`
- [ ] Implement `PageSnapshot`
- [ ] Implement `PageCrawler`

### Class Skeletons

**`build.gradle`** — add dependency

```groovy
implementation 'com.microsoft.playwright:playwright:1.44.0'
```

**`ai/crawler/PageSnapshot.java`**

```java
package ai.crawler;

public class PageSnapshot {
    private final String url;
    private final String title;
    private final String cleanedHtml;           // trimmed DOM, max 50k chars
    private final String accessibilityTree;     // JSON string
    private final Optional<String> screenshotBase64;  // empty unless crawlWithScreenshot()

    // All-args constructor + getters
}
```

**`ai/crawler/PageCrawler.java`**

```java
package ai.crawler;

public class PageCrawler implements AutoCloseable {
    private static final int DOM_SIZE_LIMIT = 50_000;
    private static final int PAGE_LOAD_TIMEOUT_MS = 30_000;

    private final Playwright playwright;
    private final Browser browser;

    public PageCrawler() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true));
    }

    public PageSnapshot crawl(String url) {
        // 1. browser.newPage() → page
        // 2. page.navigate(url, waitUntil=networkidle, timeout=PAGE_LOAD_TIMEOUT_MS)
        // 3. cleanedHtml = stripJunk(page.content())  truncated to DOM_SIZE_LIMIT
        // 4. accessibilityTree = page.accessibility().snapshot().toString()
        // 5. return new PageSnapshot(url, page.title(), cleanedHtml, accessibilityTree, Optional.empty())
    }

    public PageSnapshot crawlWithScreenshot(String url) {
        // Same as crawl() but also:
        // screenshot = page.screenshot() → Base64.getEncoder().encodeToString(bytes)
        // return PageSnapshot with Optional.of(screenshot)
    }

    private String stripJunk(String html) {
        // Remove: <script>, <style>, HTML comments, inline event handlers (on*)
        // Truncate to DOM_SIZE_LIMIT
    }

    @Override
    public void close() {
        browser.close();
        playwright.close();
    }
}
```

### Definition of Done

- [ ] `PageCrawler.crawl("https://example.com")` returns non-null `cleanedHtml` and `accessibilityTree`
- [ ] `cleanedHtml` contains no `<script>` or `<style>` tags
- [ ] `cleanedHtml` is truncated at 50,000 characters when source exceeds limit
- [ ] `crawlWithScreenshot()` returns a non-empty `Optional<String>` for `screenshotBase64`
- [ ] `PageCrawler` implements `AutoCloseable` and does not leak Playwright processes

---

## Phase 4 — FailureAnalyzer

**Goal:** Attach an AI root-cause analysis to every failed test in the Allure report.

**Depends on:** Phase 1

### Steps

- [ ] Create package `ai.analyzer`
- [ ] Implement `FailureAnalyzer` as a TestNG `ITestListener`
- [ ] Implement log buffer in `iLogger` (per-test buffer that `FailureAnalyzer` drains on failure)
- [ ] Wire `ai.failure-analyzer.enabled` flag from `ai-provider.properties`

### Class Skeletons

**`utils/logging/iLogger.java`** — extend existing class

```java
// Add per-test log buffer support:

private static final ThreadLocal<List<String>> testLogBuffer =
    ThreadLocal.withInitial(ArrayList::new);

public static void startCapture() {
    testLogBuffer.get().clear();
}

public static List<String> drainBuffer() {
    List<String> copy = new ArrayList<>(testLogBuffer.get());
    testLogBuffer.get().clear();
    return copy;
}

// All existing log methods append to testLogBuffer when capture is active
```

**`ai/analyzer/FailureAnalyzer.java`**

```java
package ai.analyzer;

public class FailureAnalyzer implements ITestListener {
    private final AiProvider provider;
    private final boolean enabled;

    public FailureAnalyzer() {
        this.enabled = Boolean.parseBoolean(
            PropertyReader.get("ai.failure-analyzer.enabled", "true"));
        this.provider = enabled ? AiProviderFactory.create() : null;
    }

    @Override
    public void onTestStart(ITestResult result) {
        iLogger.startCapture();   // begin buffering element actions for this test
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!enabled) return;

        // 1. Collect context
        String exceptionInfo = formatException(result.getThrowable());
        List<String> actionLog = iLogger.drainBuffer();
        String screenshot = captureScreenshot();    // base64 or null
        String currentUrl = safeGetUrl();
        String pageTitle  = safeGetTitle();
        String testName   = result.getMethod().getMethodName();

        // 2. Build prompt
        AiRequest request = buildRequest(testName, exceptionInfo, actionLog,
                                         currentUrl, pageTitle, screenshot);

        // 3. Call AI
        AiResponse response = provider.complete(request);

        // 4. Attach to Allure
        Allure.addAttachment("AI Failure Analysis", response.getContent());
        iLogger.debug("FailureAnalyzer: " + response);  // log token usage
    }

    private AiRequest buildRequest(String testName, String exceptionInfo,
                                   List<String> actionLog, String url,
                                   String title, String screenshot) {
        String systemPrompt = "You are a QA engineer analyzing a Selenium test failure. "
            + "Provide: ## Root Cause, ## Evidence, ## Likely Fix. Be concise.";

        String userMessage = String.format("""
            Test: %s
            URL: %s | Title: %s
            Exception: %s
            Last actions:
            %s
            """, testName, url, title, exceptionInfo, String.join("\n", actionLog));

        return screenshot != null
            ? AiRequest.withImage(systemPrompt, userMessage, screenshot)
            : AiRequest.of(systemPrompt, userMessage);
    }

    private String formatException(Throwable t) { ... }
    private String captureScreenshot() { ... }   // returns null if driver is dead
    private String safeGetUrl() { ... }
    private String safeGetTitle() { ... }
}
```

### Definition of Done

- [ ] `FailureAnalyzer` is recognized by TestNG as an `ITestListener` (no ClassCastException on startup)
- [ ] On test failure: Allure report contains "AI Failure Analysis" attachment with Root Cause / Evidence / Likely Fix sections
- [ ] On test skip: no AI call is made
- [ ] When `ai.failure-analyzer.enabled=false`: no AI call, no Allure attachment
- [ ] When WebDriver session is dead at failure time: analyzer runs with available context (no NPE/exception)

---

## Phase 5 — AiTestGenerator

**Goal:** Convert any URL into a compilable TestNG test class using the framework conventions.

**Depends on:** Phase 3 (PageCrawler), Phase 4 (iLogger buffer pattern)

### Steps

- [ ] Create package `ai.generator`
- [ ] Implement `AiTestGenerator`
- [ ] Create `src/test/java/generated/` directory with `.gitkeep`
- [ ] Add `src/test/java/generated/` to `.gitignore` (generated files are not committed)

### Class Skeletons

**`ai/generator/AiTestGenerator.java`**

```java
package ai.generator;

public class AiTestGenerator {
    private static final String OUTPUT_DIR = "src/test/java/generated/";
    private static final String SYSTEM_PROMPT = """
        You are a Selenium test generator for a Java/TestNG framework.
        Rules:
        - Use iWebElement (NOT WebElement) for all element fields
        - Use iPageFactory.initElements(driver, this) (NOT PageFactory)
        - Use @FindBy(xpath/css) for locators
        - Apply @CacheElement to stable, static elements
        - Apply @Waiter(seconds=N) to slow-loading elements
        - Use .template(value) for repeated elements differing only by text
        - Name fields descriptively (loginButton not button1)
        - Extend BaseTest
        - Include at least one Assert statement
        - Return ONLY the Java source code. No explanations, no markdown fences.
        """;

    private final AiProvider provider;
    private final PageCrawler crawler;

    public AiTestGenerator(AiProvider provider) {
        this.provider = provider;
        this.crawler = new PageCrawler();
    }

    public Path generate(String url) throws IOException {
        // 1. Crawl the page
        PageSnapshot snapshot = crawler.crawl(url);

        // 2. Build prompt
        String userMessage = buildUserMessage(snapshot);
        AiRequest request = AiRequest.of(SYSTEM_PROMPT, userMessage);

        // 3. Call AI
        AiResponse response = provider.complete(request);
        iLogger.info("AiTestGenerator: " + response);

        // 4. Extract class name from generated source
        String source = response.getContent();
        String className = extractClassName(source);   // parse "class XxxTest"

        // 5. Write to output dir
        Path outputPath = Paths.get(OUTPUT_DIR + className + ".java");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, source);

        iLogger.info("Generated: " + outputPath);
        return outputPath;
    }

    private String buildUserMessage(PageSnapshot snapshot) {
        return String.format("""
            Page URL: %s
            Page Title: %s

            --- DOM (cleaned) ---
            %s

            --- Accessibility Tree ---
            %s

            Generate a complete TestNG test class for this page.
            """, snapshot.getUrl(), snapshot.getTitle(),
                snapshot.getCleanedHtml(), snapshot.getAccessibilityTree());
    }

    private String extractClassName(String source) {
        // Regex: "public class (\\w+)" → group(1)
        // Falls back to "GeneratedTest" if not found
    }
}
```

### Definition of Done

- [ ] `generate("https://example.com")` produces a `.java` file under `src/test/java/generated/`
- [ ] Generated file compiles (`./gradlew compileTestJava` passes)
- [ ] Generated class extends `BaseTest` and uses `iWebElement` (verified by string search in output)
- [ ] `extractClassName` falls back to `GeneratedTest` when AI omits the class declaration
- [ ] `src/test/java/generated/` is in `.gitignore`

---

## Phase 6 — Integration & Cleanup

**Goal:** Wire everything together, validate end-to-end, and close known gaps.

**Depends on:** Phases 2–5

### Steps

- [ ] Add `@Listeners(FailureAnalyzer.class)` to `BaseTest`
- [ ] Add Playwright install step to CI / `Dockerfile`
- [ ] Add token usage summary to Allure report environment properties
- [ ] Write end-to-end smoke test: real test failure → Allure attachment present
- [ ] Review and close all Phase 0–5 Definition of Done items

### CI — Playwright browser install

**`Dockerfile`** — add after Gradle dependency download:

```dockerfile
RUN ./gradlew run -PmainClass=com.microsoft.playwright.CLI --args="install chromium --with-deps"
```

**`build.gradle`** — pass AI env vars to test JVM:

```groovy
test {
    environment 'ANTHROPIC_API_KEY', System.getenv('ANTHROPIC_API_KEY') ?: ''
    environment 'OLLAMA_BASE_URL',   System.getenv('OLLAMA_BASE_URL') ?: 'http://localhost:11434'
}
```

### Definition of Done

- [ ] `./gradlew test` passes with `ai.provider=ollama` (no external API key required)
- [ ] A deliberately failing test (`throw new AssertionError("forced")`) produces an Allure report with "AI Failure Analysis" attachment
- [ ] `AiTestGenerator.generate(url)` runs from a standalone `main()` and produces a compilable test
- [ ] No provider credentials appear in any committed file (`.gitignore` audit)
- [ ] All Phase 0–5 Definition of Done checkboxes are checked

---

## Deferred Items (Post-MVP)

These are known gaps that are explicitly out of scope for the initial implementation.
Track them as follow-up issues when MVP is complete.

| # | Item | Reason Deferred |
|---|------|-----------------|
| D-1 | `AuthConfig.value` → `Supplier<String>` for token rotation | Gemini ADC and Codex tokens expire; requires retry logic too |
| D-2 | Retry / backoff decorator on `AiProvider` (429, 503) | MVP uses stable API keys; retry adds complexity |
| D-3 | Split `CodexProvider` from `OpenAiProvider` | Coupling is acceptable for MVP; split when Codex auth diverges further |
| D-4 | Staging directory for generated tests (manual promotion before compile) | Bad generations break build; acceptable risk at MVP scale |
| D-5 | JUnit 5 support for `FailureAnalyzer` | Framework is TestNG-only; JUnit support is a separate effort |
| D-6 | Vision enabled by default in `AiTestGenerator` | Adds latency and cost; opt-in via `crawlWithScreenshot()` |
