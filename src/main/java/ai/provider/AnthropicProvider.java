package ai.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.logging.iLogger;

import java.io.IOException;
import java.net.Authenticator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnthropicProvider implements AiProvider {

    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private static final int MAX_TOKENS = 4096;

    private final String apiKey;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public AnthropicProvider(AuthConfig auth, String model) {
        if (auth.getType() == AuthConfig.AuthType.AUTH_TOKEN) {
            throw new UnsupportedAuthException("anthropic", auth.getType());
        }
        this.apiKey = auth.getValue();
        this.model = model;
        // Build HttpClient with system proxy support.
        // Authenticator.getDefault() may be null in unit test environments, so guard before calling.
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
            .proxy(ProxySelector.getDefault());
        Authenticator defaultAuth = Authenticator.getDefault();
        if (defaultAuth != null) {
            clientBuilder.authenticator(defaultAuth);
        }
        this.http = clientBuilder.build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public AiResponse complete(AiRequest request) {
        String body = buildRequestBody(request);
        iLogger.info("Sending request to Anthropic API, model: " + model);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(ENDPOINT))
            .header("x-api-key", apiKey)
            .header("anthropic-version", API_VERSION)
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response;
        try {
            response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Anthropic API request failed: " + e.getMessage(), e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(String.format(
                "Anthropic API returned %d: %s", response.statusCode(), response.body()
            ));
        }

        return parseResponse(response.body());
    }

    private String buildRequestBody(AiRequest request) {
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("max_tokens", MAX_TOKENS);
        bodyMap.put("system", request.getSystemPrompt());

        Object content;
        if (request.getBase64Image() != null) {
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> imagePart = new LinkedHashMap<>();
            imagePart.put("type", "image");
            imagePart.put("source", Map.of(
                "type", "base64",
                "media_type", "image/png",
                "data", request.getBase64Image()
            ));
            parts.add(imagePart);
            parts.add(Map.of("type", "text", "text", request.getUserMessage()));
            content = parts;
        } else {
            content = request.getUserMessage();
        }

        bodyMap.put("messages", List.of(Map.of("role", "user", "content", content)));

        try {
            return mapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Anthropic request body", e);
        }
    }

    private AiResponse parseResponse(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);
            String text = root.path("content").get(0).path("text").asText();
            String responseModel = root.path("model").asText();
            int inputTokens = root.path("usage").path("input_tokens").asInt();
            int outputTokens = root.path("usage").path("output_tokens").asInt();
            return new AiResponse(text, responseModel, inputTokens, outputTokens);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Anthropic response: " + responseBody, e);
        }
    }
}
