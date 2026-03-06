# Getting Started

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | 17+ | `JAVA_HOME` must be set |
| Gradle | 7+ | Use the included `./gradlew` wrapper |
| Chrome or Firefox | Latest | For local test runs |
| Git | Any | — |

For the AI-QA Copilot features, additionally:

| Requirement | Notes |
|-------------|-------|
| API key for at least one AI provider | See [Auth Setup](auth-setup.md) |
| Node.js 18+ | Only if using Codex OAuth (`auth_token` for OpenAI) |

---

## Installation

```bash
# Clone the repository
git clone https://github.com/AlexeyPlatkovsky/custom_webelement.git
cd custom_webelement

# Verify the build
./gradlew compileJava
```

---

## Running the Example Tests

```bash
# Run all tests (Chrome, default settings)
./gradlew test

# Run a specific suite
./gradlew test -Psuite=ui

# Run with a different browser
./gradlew test -Pdriver=FIREFOX

# Run unit tests only
./gradlew test -Psuite=unit
```

Test reports are generated at `build/reports/tests/testng/`. An Allure report is generated at `build/allure-report/` (requires `allureReport` task or `finalizedBy` which runs automatically after tests).

---

## Configuring Drivers

The framework uses [WebDriverManager](https://github.com/bonigarcia/webdrivermanager) to download and manage browser drivers automatically. No manual driver setup is needed.

Supported driver names (passed via `-Pdriver=`):

| Value | Browser |
|-------|---------|
| `CHROME` | Google Chrome (default) |
| `FIREFOX` | Mozilla Firefox |
| `EDGE` | Microsoft Edge |

For remote execution (LambdaTest), set:

```bash
./gradlew test \
  -Pdriver=CHROME \
  -Plambda_key=YOUR_KEY \
  -Plambda_username=YOUR_USERNAME
```

---

## Setting Up AI Features

1. Copy the sample config:

   ```bash
   cp ai-provider.properties.sample ai-provider.properties
   ```

2. Set your API key as an environment variable:

   ```bash
   export ANTHROPIC_API_KEY=sk-ant-...
   ```

3. Verify the active provider in `ai-provider.properties`:

   ```properties
   ai.provider=anthropic
   ai.anthropic.auth.type=api_key
   ai.anthropic.auth.value=${ANTHROPIC_API_KEY}
   ai.anthropic.model=claude-sonnet-4-20250514
   ```

4. See [Configuration Reference](configuration.md) for all options, or [Auth Setup](auth-setup.md) for provider-specific token instructions.

---

## Project Layout

```
custom_webelement/
├── src/main/java/
│   ├── ai/                  ← AI-QA Copilot layer (new)
│   └── core/                ← Selenium framework (existing)
│       ├── web/             ← iWebElement, iPageFactory, annotations
│       └── driver/          ← DriverFactory, DriverCaps
├── src/main/resources/
│   ├── webelement.properties
│   └── log4j2.xml
├── src/test/java/
│   ├── pages/               ← Page Object classes
│   ├── tests/               ← Test classes
│   └── generated/           ← AI-generated test output
├── ai-provider.properties   ← AI provider config (do not commit keys)
├── build.gradle
└── docs/                    ← You are here
```
