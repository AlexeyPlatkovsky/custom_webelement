package unit_tests.ai.provider;

import ai.AiProviderFactory;
import ai.provider.AiProvider;
import ai.provider.AnthropicProvider;
import ai.provider.OllamaProvider;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit")
public class AiProviderFactoryTest {

    private Properties baseProps() {
        Properties props = new Properties();
        props.setProperty("ai.anthropic.auth.type", "api_key");
        props.setProperty("ai.anthropic.auth.value", "sk-test-key");
        props.setProperty("ai.anthropic.model", "claude-sonnet-4-6");
        props.setProperty("ai.gemini.auth.type", "api_key");
        props.setProperty("ai.gemini.auth.value", "gemini-key");
        props.setProperty("ai.gemini.model", "gemini-2.0-flash");
        props.setProperty("ai.openai.auth.type", "api_key");
        props.setProperty("ai.openai.auth.value", "openai-key");
        props.setProperty("ai.openai.model", "gpt-4o");
        props.setProperty("ai.ollama.base-url", "http://localhost:11434");
        props.setProperty("ai.ollama.model", "qwen2.5-coder");
        return props;
    }

    @Test
    public void ollamaProviderSelectedWhenConfigured() {
        Properties props = baseProps();
        props.setProperty("ai.provider", "ollama");

        AiProvider provider = AiProviderFactory.create(props);

        assertTrue(provider instanceof OllamaProvider, "Expected OllamaProvider instance");
    }

    @Test
    public void anthropicProviderSelectedWhenConfigured() {
        Properties props = baseProps();
        props.setProperty("ai.provider", "anthropic");

        AiProvider provider = AiProviderFactory.create(props);

        assertTrue(provider instanceof AnthropicProvider, "Expected AnthropicProvider instance");
    }

    @Test
    public void unknownProviderThrowsIllegalStateException() {
        Properties props = baseProps();
        props.setProperty("ai.provider", "unknown-provider");

        try {
            AiProviderFactory.create(props);
            fail("Expected IllegalStateException for unknown provider");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("unknown-provider"),
                "Exception message should contain the unknown provider name");
        }
    }

    @Test
    public void unexpandedEnvVarThrowsIllegalStateException() {
        Properties props = baseProps();
        props.setProperty("ai.provider", "anthropic");
        props.setProperty("ai.anthropic.auth.value", "${UNSET_API_KEY}");

        try {
            AiProviderFactory.create(props);
            fail("Expected IllegalStateException for unexpanded env var");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("ai.anthropic.auth.value"),
                "Exception message should contain the property key");
        }
    }

    @Test
    public void missingProviderKeyThrowsIllegalStateException() {
        Properties props = baseProps();
        // do not set ai.provider

        try {
            AiProviderFactory.create(props);
            fail("Expected IllegalStateException for missing ai.provider key");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("ai.provider"),
                "Exception message should mention the missing property");
        }
    }
}
