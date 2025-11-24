package com.team021.financial_nudger.service.llm;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final Duration timeout;

    public GeminiClient(
            WebClient.Builder builder,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model:gemini-1.5-flash}") String model,
            @Value("${gemini.timeout-seconds:60}") long timeoutSeconds
    ) {
        this.webClient = builder
            .baseUrl("https://generativelanguage.googleapis.com/v1") // <-- changed from v1beta
            .build();

        this.apiKey = apiKey;
        this.model = model;
        long safeTimeout = timeoutSeconds < 5 ? 5 : timeoutSeconds;
        this.timeout = Duration.ofSeconds(safeTimeout);
    }

    public String generateJson(String instructions, java.util.List<GeminiPart> parts) {
        GeminiRequest request = GeminiRequest.jsonRequest(instructions, parts);
        GeminiResponse response = execute(request);
        return response.firstText()
                .orElseThrow(() -> new GeminiClientException("Gemini response did not contain text output"));
    }

    private GeminiResponse execute(GeminiRequest request) {
        ensureApiKeyConfigured();
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block(timeout);
        } catch (WebClientResponseException ex) {
            String reason = ex.getResponseBodyAsString();
            throw new GeminiClientException("Gemini API error: " + reason, ex);
        } catch (Exception ex) {
            throw new GeminiClientException("Unexpected error while calling Gemini API", ex);
        }
    }

    private void ensureApiKeyConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiClientException("Gemini API key is not configured. Set GEMINI_API_KEY or gemini.api.key.");
        }
    }
}

