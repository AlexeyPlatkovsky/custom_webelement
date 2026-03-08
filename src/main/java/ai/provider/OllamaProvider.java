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

public class OllamaProvider implements AiProvider {

    private final String baseUrl;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public OllamaProvider(String baseUrl, String model) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(AiRequest request) {
        String body = buildRequestBody(request);
        String endpoint = baseUrl + "/api/chat";
        iLogger.info("Sending request to Ollama, model: " + model);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response;
        try {
            response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ollama API request failed: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(String.format(
                "Ollama API returned %d: %s", response.statusCode(), response.body()
            ));
        }

        return parseResponse(response.body());
    }

    private String buildRequestBody(AiRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));

        if (request.getBase64Image() != null) {
            Map<String, Object> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getUserMessage());
            userMessage.put("images", List.of(request.getBase64Image()));
            messages.add(userMessage);
        } else {
            messages.add(Map.of("role", "user", "content", request.getUserMessage()));
        }

        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("messages", messages);
        bodyMap.put("stream", false);

        try {
            return mapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Ollama request body", e);
        }
    }

    private AiResponse parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("message").path("content").asText();
            String responseModel = root.path("model").asText();
            int inputTokens = root.path("prompt_eval_count").asInt();
            int outputTokens = root.path("eval_count").asInt();
            return new AiResponse(text, responseModel, inputTokens, outputTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Ollama response: " + responseBody, e);
        }
    }
}
