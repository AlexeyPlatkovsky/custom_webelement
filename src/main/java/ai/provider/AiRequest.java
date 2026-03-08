package ai.provider;

import lombok.Value;

@Value
public class AiRequest {

    String systemPrompt;
    String userMessage;
    /** Nullable — pass {@code null} to omit vision input. */
    String base64Image;
}
