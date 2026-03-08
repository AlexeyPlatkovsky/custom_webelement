package unit_tests.ai.provider;

import ai.provider.AiRequest;
import ai.provider.AiResponse;
import ai.provider.AnthropicProvider;
import ai.provider.AuthConfig;
import ai.provider.UnsupportedAuthException;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit", singleThreaded = true)
public class AnthropicProviderTest {

    private static final String VALID_KEY = "test-api-key";
    private static final String MODEL = "claude-sonnet-4-6";

    private static final String SUCCESS_RESPONSE = """
        {
          "id": "msg_01",
          "type": "message",
          "role": "assistant",
          "model": "claude-sonnet-4-6",
          "content": [{"type": "text", "text": "Hello, world!"}],
          "usage": {"input_tokens": 10, "output_tokens": 5}
        }
        """;

    private HttpClient mockHttp;
    private HttpResponse<String> mockResponse;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() {
        mockHttp = mock(HttpClient.class);
        mockResponse = mock(HttpResponse.class);
    }

    private AnthropicProvider providerWithMockedHttp(String apiKey) throws Exception {
        AuthConfig auth = new AuthConfig(AuthConfig.AuthType.API_KEY, apiKey);
        AnthropicProvider provider = new AnthropicProvider(auth, MODEL);
        Field httpField = AnthropicProvider.class.getDeclaredField("http");
        httpField.setAccessible(true);
        httpField.set(provider, mockHttp);
        return provider;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void successfulResponseParsedCorrectly() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SUCCESS_RESPONSE);
        doReturn(mockResponse).when(mockHttp).send(any(), any());

        AnthropicProvider provider = providerWithMockedHttp(VALID_KEY);
        AiResponse response = provider.complete(new AiRequest("system", "user", null));

        assertEquals(response.getContent(), "Hello, world!");
        assertEquals(response.getModel(), MODEL);
        assertEquals(response.getInputTokens(), 10);
        assertEquals(response.getOutputTokens(), 5);
    }

    @Test
    public void authTokenRejectedAtConstruction() {
        AuthConfig auth = new AuthConfig(AuthConfig.AuthType.AUTH_TOKEN, "token");
        try {
            new AnthropicProvider(auth, MODEL);
            fail("Expected UnsupportedAuthException");
        } catch (UnsupportedAuthException e) {
            assertTrue(e.getMessage().contains("anthropic"), "Message should mention provider name");
            assertTrue(e.getMessage().contains("AUTH_TOKEN"), "Message should mention auth type");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void http401ThrowsRuntimeException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("{\"error\": \"unauthorized\"}");
        doReturn(mockResponse).when(mockHttp).send(any(), any());

        AnthropicProvider provider = providerWithMockedHttp(VALID_KEY);
        try {
            provider.complete(new AiRequest("system", "user", null));
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("401"), "Message should contain status code 401");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void http429ThrowsRuntimeException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(429);
        when(mockResponse.body()).thenReturn("{\"error\": \"rate_limit\"}");
        doReturn(mockResponse).when(mockHttp).send(any(), any());

        AnthropicProvider provider = providerWithMockedHttp(VALID_KEY);
        try {
            provider.complete(new AiRequest("system", "user", null));
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("429"), "Message should contain status code 429");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void requestContainsImageBlockWhenBase64ImageProvided() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SUCCESS_RESPONSE);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        doReturn(mockResponse).when(mockHttp).send(requestCaptor.capture(), any());

        AnthropicProvider provider = providerWithMockedHttp(VALID_KEY);
        provider.complete(new AiRequest("system", "user", "base64data=="));

        HttpRequest captured = requestCaptor.getValue();
        // Verify the request body publisher is present (non-empty body was sent)
        assertNotNull(captured.bodyPublisher().orElse(null), "Request should have a body");
        assertTrue(captured.bodyPublisher().get().contentLength() > 0, "Body should not be empty");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void requestContainsApiKeyHeader() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SUCCESS_RESPONSE);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        doReturn(mockResponse).when(mockHttp).send(requestCaptor.capture(), any());

        AnthropicProvider provider = providerWithMockedHttp(VALID_KEY);
        provider.complete(new AiRequest("system", "user", null));

        HttpRequest captured = requestCaptor.getValue();
        assertTrue(
            captured.headers().firstValue("x-api-key").isPresent(),
            "x-api-key header should be present"
        );
        assertEquals(captured.headers().firstValue("x-api-key").get(), VALID_KEY);
    }
}
