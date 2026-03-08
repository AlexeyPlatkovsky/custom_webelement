package unit_tests.ai.provider;

import ai.provider.AiRequest;
import ai.provider.AiResponse;
import ai.provider.OllamaProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = "unit")
public class OllamaProviderTest {

    private static final String BASE_URL = "http://localhost:11434";
    private static final String MODEL = "qwen2.5-coder";

    private static final String SUCCESS_RESPONSE = """
        {
          "model": "qwen2.5-coder",
          "message": {"role": "assistant", "content": "Generated code here"},
          "done": true,
          "prompt_eval_count": 20,
          "eval_count": 15
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

    private OllamaProvider providerWithMockedHttp() throws Exception {
        OllamaProvider provider = new OllamaProvider(BASE_URL, MODEL);
        Field httpField = OllamaProvider.class.getDeclaredField("http");
        httpField.setAccessible(true);
        httpField.set(provider, mockHttp);
        return provider;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void successfulResponseParsedCorrectly() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(SUCCESS_RESPONSE);
        when(mockHttp.send(any(), any())).thenReturn(mockResponse);

        OllamaProvider provider = providerWithMockedHttp();
        AiResponse response = provider.complete(new AiRequest("system", "user", null));

        assertEquals(response.getContent(), "Generated code here");
        assertEquals(response.getModel(), MODEL);
        assertEquals(response.getInputTokens(), 20);
        assertEquals(response.getOutputTokens(), 15);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void nonOkStatusThrowsRuntimeException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\": \"internal error\"}");
        when(mockHttp.send(any(), any())).thenReturn(mockResponse);

        OllamaProvider provider = providerWithMockedHttp();
        try {
            provider.complete(new AiRequest("system", "user", null));
            fail("Expected RuntimeException on non-200 status");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("500"), "Exception message should contain status code");
        }
    }
}
