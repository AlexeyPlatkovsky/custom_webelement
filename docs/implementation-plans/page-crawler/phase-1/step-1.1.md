# Step 1.1 — Core AI Provider Data Model

**Phase:** 1 — AI provider layer
**Status:** Todo
**Depends on:** Phase 0 complete
**Blocks:** Steps 1.2, 1.3, 1.4, 1.5 (all providers depend on this interface)

---

## Goal

Define the shared data model and interface that all AI providers implement. This step
creates the structural contract for the entire AI layer — nothing else in Phase 1 can
start until these classes exist.

---

## Files to Create

All in `src/main/java/ai/provider/`:

```
ai/provider/
├── AuthConfig.java
├── AiRequest.java
├── AiResponse.java
├── AiProvider.java
└── UnsupportedAuthException.java
```

---

## `AuthConfig.java`

```java
package ai.provider;

import lombok.Value;

@Value
public class AuthConfig {

    public enum AuthType { API_KEY, AUTH_TOKEN }

    AuthType type;
    String value;
}
```

---

## `AiRequest.java`

```java
package ai.provider;

import lombok.Value;

@Value
public class AiRequest {
    String systemPrompt;
    String userMessage;
    String base64Image;   // nullable; pass null to omit vision input
}
```

---

## `AiResponse.java`

```java
package ai.provider;

import lombok.Value;

@Value
public class AiResponse {
    String content;
    String model;
    int inputTokens;
    int outputTokens;
}
```

---

## `AiProvider.java`

```java
package ai.provider;

public interface AiProvider {

    /**
     * Sends a request to the AI provider and returns the model's response.
     *
     * @param request the prompt and optional vision input
     * @return the model response including token usage
     * @throws RuntimeException if the provider returns a non-2xx status or the
     *                          response cannot be parsed
     */
    AiResponse complete(AiRequest request);
}
```

---

## `UnsupportedAuthException.java`

```java
package ai.provider;

public class UnsupportedAuthException extends RuntimeException {

    public UnsupportedAuthException(String providerName, AuthConfig.AuthType attempted) {
        super(String.format(
            "Provider '%s' does not support auth type %s. Check ai-provider.properties.",
            providerName, attempted
        ));
    }
}
```

---

## Design Notes

- `@Value` (Lombok) makes all data classes immutable with a constructor, getters, `equals`,
  `hashCode`, and `toString` — no manual boilerplate.
- `base64Image` in `AiRequest` is nullable to keep vision optional. Providers that do not
  support vision must ignore it silently (not throw).
- `UnsupportedAuthException` is thrown at **construction time** of the provider, not during
  `complete()`, so the failure is detected immediately at startup rather than mid-test.

---

## Definition of Done

- [ ] All five files created in `src/main/java/ai/provider/`
- [ ] `./gradlew compileJava` passes
- [ ] No Lombok compilation errors
