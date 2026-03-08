package ai;

import ai.provider.AiProvider;
import ai.provider.AnthropicProvider;
import ai.provider.AuthConfig;
import ai.provider.GeminiProvider;
import ai.provider.OllamaProvider;
import ai.provider.OpenAiProvider;
import utils.readers.PropertyReader;

import java.util.Properties;

public class AiProviderFactory {

    private static final String PROPERTIES_FILE = "ai-provider.properties";

    public static AiProvider create() {
        return create(PropertyReader.load(PROPERTIES_FILE));
    }

    public static AiProvider create(Properties props) {
        String providerName = required(props, "ai.provider");

        return switch (providerName.toLowerCase()) {
            case "anthropic" -> buildAnthropic(props);
            case "gemini" -> buildGemini(props);
            case "openai" -> buildOpenAi(props);
            case "ollama" -> buildOllama(props);
            default -> throw new IllegalStateException(
                "Unknown ai.provider value: '" + providerName + "'. "
                + "Valid values: anthropic, gemini, openai, ollama"
            );
        };
    }

    private static AiProvider buildAnthropic(Properties props) {
        AuthConfig auth = buildAuth(props, "ai.anthropic.auth.type", "ai.anthropic.auth.value");
        String model = required(props, "ai.anthropic.model");
        return new AnthropicProvider(auth, model);
    }

    private static AiProvider buildGemini(Properties props) {
        AuthConfig auth = buildAuth(props, "ai.gemini.auth.type", "ai.gemini.auth.value");
        String model = required(props, "ai.gemini.model");
        return new GeminiProvider(auth, model);
    }

    private static AiProvider buildOpenAi(Properties props) {
        AuthConfig auth = buildAuth(props, "ai.openai.auth.type", "ai.openai.auth.value");
        String model = required(props, "ai.openai.model");
        return new OpenAiProvider(auth, model);
    }

    private static AiProvider buildOllama(Properties props) {
        String baseUrl = required(props, "ai.ollama.base-url");
        String model = required(props, "ai.ollama.model");
        return new OllamaProvider(baseUrl, model);
    }

    private static AuthConfig buildAuth(Properties props, String typeKey, String valueKey) {
        String typeStr = required(props, typeKey);
        String value = required(props, valueKey);

        if (value.startsWith("${")) {
            throw new IllegalStateException(
                "Property '" + valueKey + "' is not set. "
                + "Set the corresponding environment variable before running."
            );
        }

        AuthConfig.AuthType type = switch (typeStr.toLowerCase()) {
            case "api_key" -> AuthConfig.AuthType.API_KEY;
            case "auth_token" -> AuthConfig.AuthType.AUTH_TOKEN;
            default -> throw new IllegalStateException(
                "Unknown auth type '" + typeStr + "' for key '" + typeKey + "'."
            );
        };

        return new AuthConfig(type, value);
    }

    private static String required(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Required property '" + key + "' is missing from " + PROPERTIES_FILE
            );
        }
        return value.strip();
    }
}
