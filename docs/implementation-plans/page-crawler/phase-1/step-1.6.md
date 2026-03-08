# Step 1.6 — `AiProviderFactory`

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Steps 1.2, 1.3, 1.4, 1.5
**Blocks:** Step 3.2 (`PageObjectGenerator` calls `AiProviderFactory.create()`)

---

## Goal

Implement the factory that reads `ai-provider.properties`, constructs an `AuthConfig`, and
instantiates the correct `AiProvider` implementation. This is the single point of
configuration for provider selection.

---

## File

`src/main/java/ai/AiProviderFactory.java`

---

## Implementation Spec

```java
public class AiProviderFactory {

    private static final String PROPERTIES_FILE = "ai-provider.properties";

    public static AiProvider create() {
        Properties props = PropertyReader.load(PROPERTIES_FILE);  // uses expanded ENV vars

        String providerName = required(props, "ai.provider");

        return switch (providerName.toLowerCase()) {
            case "anthropic" -> buildAnthropic(props);
            case "gemini"    -> buildGemini(props);
            case "openai"    -> buildOpenAi(props);
            case "ollama"    -> buildOllama(props);
            default -> throw new IllegalStateException(
                "Unknown ai.provider value: '" + providerName + "'. "
                + "Valid values: anthropic, gemini, openai, ollama"
            );
        };
    }
}
```

### Per-provider build methods

```java
private static AiProvider buildAnthropic(Properties props) {
    AuthConfig auth = buildAuth(props, "ai.anthropic.auth.type", "ai.anthropic.auth.value");
    String model    = required(props, "ai.anthropic.model");
    return new AnthropicProvider(auth, model);
}

private static AiProvider buildGemini(Properties props) {
    AuthConfig auth = buildAuth(props, "ai.gemini.auth.type", "ai.gemini.auth.value");
    String model    = required(props, "ai.gemini.model");
    return new GeminiProvider(auth, model);
}

private static AiProvider buildOpenAi(Properties props) {
    AuthConfig auth = buildAuth(props, "ai.openai.auth.type", "ai.openai.auth.value");
    String model    = required(props, "ai.openai.model");
    return new OpenAiProvider(auth, model);
}

private static AiProvider buildOllama(Properties props) {
    String baseUrl = required(props, "ai.ollama.base-url");
    String model   = required(props, "ai.ollama.model");
    return new OllamaProvider(baseUrl, model);
}
```

### `buildAuth` helper

```java
private static AuthConfig buildAuth(Properties props, String typeKey, String valueKey) {
    String typeStr = required(props, typeKey);
    String value   = required(props, valueKey);

    // Detect unexpanded ENV placeholder (PropertyReader expansion failed)
    if (value.startsWith("${")) {
        throw new IllegalStateException(
            "Property '" + valueKey + "' is not set. "
            + "Set the corresponding environment variable before running."
        );
    }

    AuthConfig.AuthType type = switch (typeStr.toLowerCase()) {
        case "api_key"    -> AuthConfig.AuthType.API_KEY;
        case "auth_token" -> AuthConfig.AuthType.AUTH_TOKEN;
        default -> throw new IllegalStateException(
            "Unknown auth type '" + typeStr + "' for key '" + typeKey + "'."
        );
    };

    return new AuthConfig(type, value);
}
```

### `required` helper

```java
private static String required(Properties props, String key) {
    String value = props.getProperty(key);
    if (value == null || value.isBlank()) {
        throw new IllegalStateException(
            "Required property '" + key + "' is missing from " + PROPERTIES_FILE
        );
    }
    return value.strip();
}
```

---

## Unit Test

**File:** `src/test/java/unit_tests/ai/provider/AiProviderFactoryTest.java`

Test cases (use a temp properties file or mock `PropertyReader`):
- `ai.provider=ollama` → returns `OllamaProvider` instance
- `ai.provider=anthropic` with valid API key → returns `AnthropicProvider` instance
- `ai.provider=unknown` → `IllegalStateException` with provider name in message
- `ai.anthropic.auth.value=${UNSET}` (unexpanded) → `IllegalStateException` with clear message
- Missing `ai.provider` key → `IllegalStateException`

---

## Definition of Done

- [ ] `AiProviderFactory.create()` reads `ai-provider.properties` and returns the correct provider
- [ ] Unexpanded `${ENV_VAR}` detected with clear error message
- [ ] Unknown provider name throws with all valid values listed
- [ ] Missing required properties throw with the property key in the message
- [ ] All unit tests pass
- [ ] `./gradlew test -Psuite=unit` green
