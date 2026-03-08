# Page Crawler — Implementation Tracker

The **Page Crawler** feature takes one or more URLs and automatically generates Page Object
classes (`.java`) that represent the UI structure of the provided pages. It combines a
Playwright-based DOM/accessibility-tree extractor with an AI provider to produce
framework-compliant `iWebElement`-based Page Object code.

---

## Progress Table

| Phase | Step | Deliverable | Depends on | Status |
|-------|------|-------------|------------|--------|
| 0 | [0.1](phase-0/step-0.1.md) | `PropertyReader` ENV expansion | — | Todo |
| 0 | [0.2](phase-0/step-0.2.md) | `build.gradle` + Playwright task | — | Todo |
| 0 | [0.3](phase-0/step-0.3.md) | `ai-provider.properties` template | — | Todo |
| 1 | [1.1](phase-1/step-1.1.md) | `AuthConfig`, `AiRequest`, `AiResponse`, `AiProvider` interface | Phase 0 | Todo |
| 1 | [1.2](phase-1/step-1.2.md) | `AnthropicProvider` | 1.1 | Todo |
| 1 | [1.3](phase-1/step-1.3.md) | `OllamaProvider` | 1.1 | Todo |
| 1 | [1.4](phase-1/step-1.4.md) | `GeminiProvider` | 1.1 | Todo |
| 1 | [1.5](phase-1/step-1.5.md) | `OpenAiProvider` | 1.1 | Todo |
| 1 | [1.6](phase-1/step-1.6.md) | `AiProviderFactory` | 1.2–1.5 | Todo |
| 2 | [2.1](phase-2/step-2.1.md) | `PageSnapshot` value object | — | Todo |
| 2 | [2.2](phase-2/step-2.2.md) | `DomCleaner` | — | Todo |
| 2 | [2.3](phase-2/step-2.3.md) | `PageCrawler` | 2.1, 2.2, 0.2 | Todo |
| 2 | [2.4](phase-2/step-2.4.md) | `PageCrawlerFacade` (crawl only) | 2.3 | Todo |
| 3 | [3.1](phase-3/step-3.1.md) | System prompt resource | Docs review | Todo |
| 3 | [3.2](phase-3/step-3.2.md) | `PageObjectGenerator` | Phase 1, 2.1 | Todo |
| 3 | [3.3](phase-3/step-3.3.md) | `GeneratedPageObject` value object | — | Todo |
| 3 | [3.4](phase-3/step-3.4.md) | `PageObjectWriter` | 3.3 | Todo |
| 3 | [3.5](phase-3/step-3.5.md) | `PageCrawlerFacade` full wiring | 2.4, 3.2, 3.4 | Todo |
| 4 | [4.1](phase-4/step-4.1.md) | Source validation | 3.2 | Todo |
| 4 | [4.2](phase-4/step-4.2.md) | Package declaration injection | 3.2 | Todo |
| 4 | [4.3](phase-4/step-4.3.md) | Batch summary logging | 3.5 | Todo |

**Status values:** `Todo` · `In Progress` · `Done`

---

## Architecture

```
 ┌──────────────────────────────────────────────────────────────────────┐
 │                         Caller / Entry Point                          │
 │   PageCrawlerFacade.generatePageObject(String url)                    │
 │   PageCrawlerFacade.generatePageObjects(List<String> urls)            │
 └──────────────────────────────┬───────────────────────────────────────┘
                                │  for each URL
           ┌────────────────────▼────────────────────┐
           │             PageCrawler                  │
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
           │  • Builds system + user prompts          │
           │  • Calls AiProvider.complete(request)    │
           │  • Extracts Java source from response    │
           │  • Validates class name + syntax         │
           └────────────────────┬────────────────────┘
                                │
           ┌────────────────────▼────────────────────┐
           │          PageObjectWriter                │
           │  • Writes to src/test/java/generated/   │
           │  • One file per Page Object              │
           └──────────────────────────────────────────┘

Supporting infrastructure:
  AiProviderFactory → AiProvider
    ├── AnthropicProvider
    ├── GeminiProvider
    ├── OpenAiProvider
    └── OllamaProvider
  PropertyReader  →  ai-provider.properties + ${ENV_VAR} expansion
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

## Data Flow

```
URL input
  │
  ▼ [1] URL validation (well-formed, http/https only)
  │
  ▼ [2] PageCrawler.crawl(url)
  │       Playwright BrowserContext (isolated)
  │       navigate → networkidle → title + rawHtml + accessibilityTree
  │       DomCleaner: remove scripts/styles/comments → truncate at 50KB
  │       BrowserContext.close()
  │
  ▼ [3] PageSnapshot assembled
  │       url, title, cleanedHtml, accessibilityTree
  │
  ▼ [4] PageObjectGenerator.generate(snapshot)
  │       systemPrompt (classpath resource) + userMessage (template)
  │       AiProvider.complete(AiRequest) → AiResponse
  │       extractJavaSource → strip markdown fences
  │       extractClassName → regex public\s+class\s+(\w+)
  │
  ▼ [5] PageObjectWriter.write(GeneratedPageObject)
  │       inject "package generated;" if missing
  │       Files.writeString(path, source, CREATE_NEW)
  │
  ▼ [6] src/test/java/generated/<ClassName>.java written
```

---

## Edge Cases and Constraints

| # | Scenario | Constraint / Resolution |
|---|----------|------------------------|
| 5.1 | Auth-protected pages | MVP: crawls unauthenticated only. Post-MVP: `crawlWithCookies()` |
| 5.2 | JS-heavy SPAs / lazy load | `NETWORKIDLE` covers most cases; may miss deferred components |
| 5.3 | Bot detection / Captcha | No bypass; generated class will reflect the challenge page |
| 5.4 | DOM > 50 KB after cleaning | Truncate at clean tag boundary; append `<!-- [DOM TRUNCATED] -->` |
| 5.5 | AI response quality failures | Validate, strip fences, fallback class name from URL path |
| 5.6 | Duplicate class names | `CREATE_NEW` throws; recover by appending numeric suffix |
| 5.7 | Network timeout | 30s default; wrap in `PageCrawlerException`; batch continues |
| 5.8 | Playwright binaries in CI | Pre-download via `installPlaywrightBrowsers` task; cache directory |
| 5.9 | Thread safety | MVP: sequential batch only; post-MVP: thread-local Playwright |
| 5.10 | Missing ENV variable | Detected at startup; `IllegalStateException` with clear message |

---

## Acceptance Criteria

1. `PageCrawlerFacade.generatePageObject("https://example.com")` produces a `.java` file in
   `src/test/java/generated/` that compiles without errors.
2. The generated class extends `AbstractPage`, uses `iWebElement` fields with `@FindBy`,
   and initializes via `iPageFactory.initElements()`.
3. `PageCrawlerFacade.generatePageObjects(List.of(url1, url2, url3))` produces three separate
   `.java` files; a failure on one URL does not abort the others.
4. Switching `ai.provider=ollama` in `ai-provider.properties` (with Ollama running locally)
   generates a Page Object without any external API key.
5. All unit tests in `unit_tests/ai/` pass under `./gradlew test -Psuite=unit`.
6. `./gradlew compileTestJava` succeeds after generating a Page Object from a real URL.

---

## File Layout After Implementation

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
├── ai-provider.properties
└── ai/generator/system-prompt.txt

src/test/java/
├── generated/
└── unit_tests/ai/
    ├── crawler/
    │   ├── DomCleanerTest.java
    │   ├── PageCrawlerTest.java
    │   └── PageCrawlerFacadeTest.java
    ├── generator/
    │   ├── PageObjectGeneratorTest.java
    │   └── PageObjectWriterTest.java
    └── provider/
        ├── AnthropicProviderTest.java
        ├── OllamaProviderTest.java
        └── AiProviderFactoryTest.java
```
