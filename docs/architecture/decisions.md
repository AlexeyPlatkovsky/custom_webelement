# Architecture Decision Records

## ADR-001: Use Playwright Java for DOM/accessibility tree extraction

**Status:** Accepted

**Context:**
`AiTestGenerator` needs to crawl a page's DOM and accessibility tree to build a prompt for the AI. Options considered: Selenium (already a dependency), Playwright Java, Playwright MCP, Jsoup.

**Decision:**
Use `com.microsoft.playwright` (Playwright Java) directly.

**Rationale:**
- Playwright MCP is designed for AI agents that act as orchestrators (Claude Desktop, Cursor). In this framework, Java is the orchestrator — using Playwright MCP would add an unnecessary layer.
- Playwright Java provides first-class accessibility tree access (`page.accessibility().snapshot()`), which Selenium does not expose natively.
- Jsoup is static HTML only; it cannot render JavaScript-heavy SPAs.
- Playwright Java gives equivalent DOM access to MCP with a simpler, direct integration path.

**Consequences:**
Adds `com.microsoft.playwright:playwright` to `build.gradle`. Playwright manages its own browser binaries (Chromium); these are downloaded on first run via `playwright install`.

---

## ADR-002: No OAuth / auth_token support for Anthropic

**Status:** Accepted

**Context:**
Anthropic is actively removing OAuth support from third-party integrations (confirmed via legal notices to OpenCode/OpenClaw, March 2026).

**Decision:**
`AnthropicProvider` accepts only `API_KEY`. Passing `AUTH_TOKEN` throws `UnsupportedAuthException` immediately.

**Rationale:**
- Supporting a capability that is being actively deprecated would create dead code and user confusion.
- Failing fast with a clear message ("Anthropic does not support auth_token. Use api_key.") is better than silently failing later.

**Consequences:**
Users who want to use Claude must obtain an API key from console.anthropic.com. No workarounds.

---

## ADR-003: Include Ollama in the MVP

**Status:** Accepted

**Context:**
The framework needs at least one provider that works without any external API key for contributors, CI environments, and evaluation purposes.

**Decision:**
Implement `OllamaProvider` as part of the initial provider set.

**Rationale:**
- Ollama exposes an OpenAI-compatible REST API — implementation is trivial (reuse HTTP client logic from `OpenAiProvider`).
- Zero cost, zero key, runs on a laptop. Removes the "I need a credit card to try this" barrier for GitHub evaluators.
- Default model (`qwen2.5-coder`) is well-suited for code generation tasks.

**Consequences:**
Output quality from local models will be lower than hosted providers. Documentation should set expectations accordingly. No auth validation needed; `AuthConfig` is ignored by `OllamaProvider`.

---

## ADR-004: FailureAnalyzer fires in @AfterEach, not @AfterMethod

**Status:** Accepted

**Context:**
TestNG's `@AfterMethod` receives an `ITestResult` object and can distinguish pass/fail/skip. JUnit 5's `@AfterEach` with `TestInfo` requires a different approach.

**Decision:**
Use TestNG's `@AfterMethod` (referred to as `@AfterEach` colloquially in the design docs) with `ITestResult.getStatus()` check.

**Rationale:**
- The framework already uses TestNG (see `build.gradle` `useTestNG` block).
- `FailureAnalyzer` must **only** fire on `ITestResult.FAILURE`, not on `ITestResult.SKIP` (which the framework uses to handle missing elements gracefully via `SkipException`).
- Firing on skips would waste tokens and generate misleading analysis.

**Consequences:**
`FailureAnalyzer` integration is TestNG-specific. JUnit 5 support would require a separate extension mechanism.

---

## ADR-005: Single properties file (ai-provider.properties) for all AI config

**Status:** Accepted

**Context:**
Options: environment variables only, per-provider files, single unified file, or Spring-style profiles.

**Decision:**
Single `ai-provider.properties` file at project root with `${ENV_VAR}` placeholders for sensitive values.

**Rationale:**
- Consistent with the existing `webelement.properties` pattern in the framework.
- One place to look for all AI configuration reduces cognitive overhead.
- Sensitive values (API keys, tokens) are kept out of the file via `${ENV_VAR}` expansion — the file itself is safe to commit.
- No Spring dependency introduced.

**Consequences:**
`PropertyReader` must be extended to expand `${ENV_VAR}` syntax at load time (currently not implemented). See [Known Limitations](ai-provider-design.md#known-limitations-and-open-items).
