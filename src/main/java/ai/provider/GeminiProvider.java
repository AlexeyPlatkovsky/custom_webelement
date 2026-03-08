package ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.logging.iLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeminiProvider implements AiProvider {

    private static final String BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final AuthConfig auth;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GeminiProvider(AuthConfig auth, String model) {
        this.auth = auth;
        this.model = model;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(AiRequest request) {
        String body = buildRequestBody(request);
        String endpoint = buildEndpoint();
        iLogger.info("Sending request to Gemini API, model: " + model);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));

        if (auth.getType() == AuthConfig.AuthType.AUTH_TOKEN) {
            requestBuilder.header("Authorization", "Bearer " + auth.getValue());
        }

        HttpResponse<String> response;
        try {
            response = http.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Gemini API request failed: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(String.format(
                "Gemini API returned %d: %s", response.statusCode(), response.body()
            ));
        }

        return parseResponse(response.body());
    }

    private String buildEndpoint() {
        String url = String.format(BASE_URL, model);
        if (auth.getType() == AuthConfig.AuthType.API_KEY) {
            url = url + "?key=" + auth.getValue();
        }
        return url;
    }

    private String buildRequestBody(AiRequest request) {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("systemInstruction", Map.of(
            "parts", List.of(Map.of("text", request.getSystemPrompt()))
        ));

        List<Map<String, Object>> parts = new ArrayList<>();
        if (request.getBase64Image() != null) {
            parts.add(Map.of("inlineData", Map.of(
                "mimeType", "image/png",
                "data", request.getBase64Image()
            )));
        }
        parts.add(Map.of("text", request.getUserMessage()));

        bodyMap.put("contents", List.of(Map.of("parts", parts)));

        try {
            return mapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Gemini request body", e);
        }
    }

    private AiResponse parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0).path("text").asText();
            String responseModel = root.path("modelVersion").asText(model);
            int inputTokens = root.path("usageMetadata").path("promptTokenCount").asInt();
            int outputTokens = root.path("usageMetadata").path("candidatesTokenCount").asInt();
            return new AiResponse(text, responseModel, inputTokens, outputTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + responseBody, e);
        }
    }
}
