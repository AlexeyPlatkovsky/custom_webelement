# AiTestGenerator

`AiTestGenerator` takes a URL, crawls the page, and generates a Selenium test class (`.java`) using an AI provider.

## How It Works

```
URL
 │
 ▼
PageCrawler.crawl(url)
 │  → DOM structure (cleaned HTML)
 │  → Accessibility tree snapshot
 │
 ▼
Prompt construction
 │  → System prompt: framework conventions, iWebElement API
 │  → User message: page structure + "generate a test class"
 │
 ▼
AiProvider.complete(request)
 │  → AI returns Java source code
 │
 ▼
Java code extraction + validation
 │  → Parse class name from response
 │  → Write to src/test/java/generated/<ClassName>.java
 │
 ▼
Generated test class
```

## Usage

```java
AiProvider provider = AiProviderFactory.create();
AiTestGenerator generator = new AiTestGenerator(provider);

// Generate a test for a URL
generator.generate("https://example.com/login");
// → writes src/test/java/generated/LoginPageTest.java
```

## What the Generated Test Contains

The AI generates a TestNG test class that:

- Extends `BaseTest` (or the equivalent in the target project)
- Declares page elements as `iWebElement` fields with `@FindBy` annotations
- Contains at least one `@Test` method that exercises primary user interactions on the page
- Uses `iPageFactory.initElements()` for initialization
- Follows the naming conventions in the existing test codebase

## Prompt Engineering Notes

The system prompt instructs the AI to:

1. Use `iWebElement` (not `WebElement`) for all element declarations
2. Use `iPageFactory` (not `PageFactory`) for initialization
3. Use `@CacheElement` for stable, static elements
4. Use `@Waiter` for elements known to be slow
5. Use `.template()` for repeated elements that differ only by text
6. Name fields descriptively (not `element1`, `div3`)
7. Include `Assert` statements — not just interactions

## Prompt Token Budget

The page DOM and accessibility tree can be large. The crawler trims:
- Script and style tags
- Comments
- Deeply nested structural wrappers with no semantic content
- Duplicate or near-duplicate subtrees

Target prompt size: under 4,000 tokens for the page context. Adjust in `PageCrawler` config if needed.

## Output Location

Generated files land in `src/test/java/generated/`. This directory is on the compile/test classpath and tests are picked up automatically on the next run.

> **Warning:** A bad generation (syntax error, wrong imports) will break the build. Review generated files before running. A staging directory with manual promotion is a safer pattern — planned for a future iteration.

## Limitations

- The AI cannot interact with the live page. It works from a static snapshot. Dynamic states (post-login, after form submission) require separate crawl calls.
- Vision (`base64Image`) is not used by default. Enable it by passing a page screenshot to improve element identification for complex layouts.
- Generated tests are a starting point, not production-ready. Review locators, add edge cases, and adjust assertions to reflect actual business logic.

## Token Cost Reference

| Provider | Input (est. per page) | Output (est. per test) |
|----------|-----------------------|------------------------|
| Claude Sonnet | ~2,000–4,000 tokens | ~500–1,500 tokens |
| GPT-4o | ~2,000–4,000 tokens | ~500–1,500 tokens |
| Gemini Flash | ~2,000–4,000 tokens | ~500–1,500 tokens |
| Ollama (local) | — | — (no cost) |

Token counts are logged in `AiResponse.inputTokens` and `AiResponse.outputTokens`. Attach them to Allure reports for cost tracking at scale.
