package ai.provider;

public interface AiProvider {

    /**
     * Sends a request to the AI provider and returns the model's response.
     *
     * @param request the prompt and optional vision input
     * @return the model response including token usage
     * @throws RuntimeException if the provider returns a non-2xx status or the
     *                          response cannot be parsed
     */
    AiResponse complete(AiRequest request);
}
