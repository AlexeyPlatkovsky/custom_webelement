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

public class OpenAiProvider implements AiProvider {

    private static final String OPENAI_BASE_URL = "https://api.openai.com/v1";
    private static final String CODEX_BASE_URL = "https://chatgpt.com/backend-api/codex";

    private final AuthConfig auth;
    private final String model;
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public OpenAiProvider(AuthConfig auth, String model) {
        this.auth = auth;
        this.model = model;
        this.baseUrl = auth.getType() == AuthConfig.AuthType.AUTH_TOKEN
            ? CODEX_BASE_URL
            : OPENAI_BASE_URL;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(AiRequest request) {
        String body = buildRequestBody(request);
        String endpoint = baseUrl + "/chat/completions";
        iLogger.info("Sending request to OpenAI API, model: " + model);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Authorization", "Bearer " + auth.getValue())
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response;
        try {
            response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenAI API request failed: " + e.getMessage(), e);
        }

        if (response.statusCode() == 401 && auth.getType() == AuthConfig.AuthType.AUTH_TOKEN) {
            throw new RuntimeException(
                "Codex OAuth token expired — run: codex login"
            );
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException(String.format(
                "OpenAI API returned %d: %s", response.statusCode(), response.body()
            ));
        }

        return parseResponse(response.body());
    }

    private String buildRequestBody(AiRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));

        if (request.getBase64Image() != null) {
            List<Map<String, Object>> contentParts = new ArrayList<>();
            contentParts.add(Map.of("type", "text", "text", request.getUserMessage()));
            contentParts.add(Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/png;base64," + request.getBase64Image())
            ));
            Map<String, Object> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", contentParts);
            messages.add(userMessage);
        } else {
            messages.add(Map.of("role", "user", "content", request.getUserMessage()));
        }

        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("messages", messages);

        try {
            return mapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OpenAI request body", e);
        }
    }

    private AiResponse parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("choices").get(0).path("message").path("content").asText();
            String responseModel = root.path("model").asText();
            int inputTokens = root.path("usage").path("prompt_tokens").asInt();
            int outputTokens = root.path("usage").path("completion_tokens").asInt();
            return new AiResponse(text, responseModel, inputTokens, outputTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAI response: " + responseBody, e);
        }
    }
}
