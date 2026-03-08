# Step 0.1 — `PropertyReader` ENV Variable Expansion

**Phase:** 0 — Infrastructure prerequisites
**Status:** Todo
**Depends on:** —
**Blocks:** Step 1.1 (all providers that use `${ENV_VAR}` in `ai-provider.properties`)

---

## Goal

Extend the existing `PropertyReader` so that property values containing `${VAR_NAME}`
placeholders are automatically replaced with the value of the matching environment variable
at load time.

Without this fix, no AI provider that reads its API key from an environment variable (Anthropic,
Gemini, OpenAI) can authenticate. **This is the single most critical prerequisite.**

---

## Location

`src/main/java/utils/properties/PropertyReader.java`

---

## Change Description

After the `.properties` file is loaded into the `Properties` object, iterate all values and
apply the substitution before returning the map.

```java
// Pattern to match ${VAR_NAME} tokens
private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

private String expandEnvVars(String value) {
    if (value == null || !value.contains("${")) {
        return value;
    }
    return ENV_PATTERN.matcher(value).replaceAll(matchResult -> {
        String varName = matchResult.group(1);
        String envValue = System.getenv(varName);
        return envValue != null ? envValue : matchResult.group(0); // leave unexpanded if missing
    });
}
```

Call this inside the property loading loop, for example:

```java
properties.replaceAll((key, value) -> expandEnvVars((String) value));
```

### Missing variable behaviour

If `${VAR_NAME}` is used but the variable is not set in the environment, the placeholder is
left as-is (literal string `${VAR_NAME}`). `AiProviderFactory` must detect unexpanded
placeholders at startup and throw:

```
IllegalStateException: Property 'ai.anthropic.auth.value' is not set.
Set environment variable ANTHROPIC_API_KEY before running.
```

---

## Unit Test

**File:** `src/test/java/unit_tests/utils/properties/PropertyReaderEnvVarTest.java`

Test cases:
- Property with `${SET_VAR}` where `SET_VAR=hello` → expands to `hello`
- Property with `${UNSET_VAR}` where variable is not set → returns `${UNSET_VAR}` unchanged
- Property with no `${...}` → returned unchanged
- Property with two placeholders in one value → both expanded independently
- `null` value → returned as `null` without NPE

---

## Definition of Done

- [ ] `expandEnvVars` method added to `PropertyReader`
- [ ] All existing `PropertyReader`-based tests still pass
- [ ] New `PropertyReaderEnvVarTest` passes for all cases above
- [ ] `./gradlew test -Psuite=unit` green
