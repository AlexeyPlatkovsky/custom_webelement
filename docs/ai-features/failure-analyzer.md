# FailureAnalyzer

`FailureAnalyzer` hooks into the TestNG lifecycle and uses AI to produce a root-cause analysis when a test fails.

## How It Works

```
@Test method fails
 │
 ▼
@AfterMethod fires (ITestResult.FAILURE only)
 │
 ▼
FailureAnalyzer.analyze(testResult)
 │  → Collect: exception message + stack trace
 │  → Collect: iWebElement action log for this test
 │  → Collect: page screenshot (base64)
 │  → Collect: test method name + parameters
 │
 ▼
AiProvider.complete(request)
 │  → System prompt: "You are a QA engineer analyzing a Selenium test failure"
 │  → User message: structured failure context
 │  → base64Image: screenshot (if provider supports vision)
 │
 ▼
AiResponse.content → formatted root-cause analysis
 │
 ▼
Allure attachment: "AI Failure Analysis"
```

## Integration

Add `FailureAnalyzer` as a TestNG listener. Either via `testng.xml`:

```xml
<listeners>
    <listener class-name="ai.analyzer.FailureAnalyzer"/>
</listeners>
```

Or programmatically in `BaseTest`:

```java
@Listeners(FailureAnalyzer.class)
public class BaseTest {
    // ...
}
```

## What Gets Included in the Analysis Prompt

| Data | Source | Always included |
|------|--------|----------------|
| Exception class + message | `ITestResult.getThrowable()` | ✅ |
| Trimmed stack trace (framework frames stripped) | `ITestResult.getThrowable()` | ✅ |
| Test method name + parameters | `ITestResult.getMethod()` | ✅ |
| `iWebElement` action log | `iLogger` buffer for current test | ✅ |
| Page screenshot | WebDriver `TakesScreenshot` | ✅ (if driver is alive) |
| Page URL at failure | `WebDriver.getCurrentUrl()` | ✅ (if driver is alive) |
| Page title at failure | `WebDriver.getTitle()` | ✅ (if driver is alive) |

## Skip vs. Failure Handling

`FailureAnalyzer` only fires when `ITestResult.getStatus() == ITestResult.FAILURE`.

It does **not** fire for `ITestResult.SKIP`. The framework uses `SkipException` (thrown by `iWebElement.getWebElement()` on timeout) as a soft signal for missing elements — these are expected skips, not failures, and do not warrant AI analysis.

## Allure Attachment Format

The analysis is attached to the Allure report as a text attachment named **"AI Failure Analysis"**. Format:

```
## Root Cause
<1–3 sentence summary of what went wrong>

## Evidence
- Last action: <element name> <action> at <timestamp>
- Exception: <exception class>: <message>
- URL: <url at failure>

## Likely Fix
<concrete suggestion: locator update / timing fix / data dependency / etc.>

## Token Usage
Input: <n> | Output: <n> | Model: <model>
```

## Cost Control

AI analysis runs only on failures. In a stable test suite, this should be rare. To disable in specific environments (e.g., cost-sensitive CI):

```properties
# In ai-provider.properties (planned)
ai.failure-analyzer.enabled=false
```

Or set `ai.provider=ollama` to use a free local model for analysis.

## Limitations

- If the WebDriver session has crashed before `@AfterMethod` runs, the screenshot and URL will be unavailable. The analyzer runs with whatever context it can collect.
- Analysis quality depends on the provider and model. Claude Sonnet and GPT-4o produce better root-cause reasoning than smaller models.
- The element action log is only as detailed as `iLogger` output. Ensure log level is `DEBUG` in `log4j2.xml` for maximum context.
