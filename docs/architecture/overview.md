# Architecture Overview

## System at a Glance

**custom_webelement** is a Selenium-based Java testing framework with built-in action logging and an AI-QA Copilot layer for test generation and failure analysis.

```
┌─────────────────────────────────────────────────────────┐
│                    Test Code (user)                      │
│   AbstractPage → iPageFactory → iWebElement              │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                  AI-QA Copilot Layer                     │
│                                                          │
│  AiTestGenerator     FailureAnalyzer     PageCrawler     │
│        │                   │                 │           │
│        └───────────────────▼─────────────────┘           │
│                      AiProvider (interface)               │
│                           │                              │
│        ┌──────────────────┼──────────────────┐           │
│        ▼                  ▼                  ▼           │
│  AnthropicProvider  GeminiProvider    OpenAiProvider     │
│                                       OllamaProvider     │
└─────────────────────────────────────────────────────────┘
```

## Module Responsibilities

| Module | Package | Responsibility |
|--------|---------|----------------|
| `iWebElement` | `core.web` | Selenium `WebElement` wrapper with logging, caching, templates |
| `iPageFactory` | `core.web` | Initializes page fields via reflection, reads `@FindBy`, `@CacheElement`, `@Waiter` |
| `iWebElementsList` | `core.web` | List variant of `iWebElement` for repeated elements |
| `DriverFactory` | `core.driver` | Creates local/remote WebDriver instances |
| `AiProvider` | `ai.provider` | Interface for all LLM providers |
| `AiProviderFactory` | `ai` | Reads config, constructs the correct provider |
| `PageCrawler` | `ai.crawler` | Playwright Java: DOM + accessibility tree extraction |
| `AiTestGenerator` | `ai.generator` | URL → Selenium test class (`.java`) |
| `FailureAnalyzer` | `ai.analyzer` | `@AfterEach` hook: AI root-cause analysis on test failure |

## Key Design Principles

- **Provider-agnostic AI client** — `TestGenerator` and `FailureAnalyzer` depend only on `AiProvider`. Swapping providers requires zero code changes outside configuration.
- **Fail-fast auth** — invalid `AuthType` for a provider throws `UnsupportedAuthException` at startup, not mid-test.
- **Logging by default** — every `iWebElement` action is logged via `iLogger` without any user configuration.
- **Page Factory compatible** — drop-in replacement for Selenium's `PageFactory`; only the init call changes.

## Related Documents

- [AI Provider Design](ai-provider-design.md) — `AuthConfig`, `AiProvider` interface, factory, per-provider auth handling
- [Architecture Decision Records](decisions.md) — why Playwright Java, why no MCP, why Ollama in MVP
- [Getting Started](../guides/getting-started.md) — setup and first test run
