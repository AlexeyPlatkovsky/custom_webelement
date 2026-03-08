package ai.provider;

import lombok.Value;

@Value
public class AiResponse {

    String content;
    String model;
    int inputTokens;
    int outputTokens;
}
