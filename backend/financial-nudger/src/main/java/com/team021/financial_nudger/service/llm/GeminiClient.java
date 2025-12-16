package com.team021.financial_nudger.service.llm;

import java.time.Duration;
import java.util.Objects;

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
        @Value("${gemini.model:gemini-2.0-flash}") String model,
        @Value("${gemini.timeout-seconds:90}") long timeoutSeconds // ⏳ Increased default timeout to 90s
    ) {
        this.webClient = builder
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();

        this.apiKey = apiKey;
        this.model = model;
        long safeTimeout = Math.max(30, timeoutSeconds);
        this.timeout = Duration.ofSeconds(safeTimeout);

        // ✅ Debug info
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("⚠️ Gemini API key is NOT loaded from environment or properties!");
        } else {
            System.out.println("✅ Gemini API key loaded successfully (length=" + apiKey.length() + ")");
        }

        System.out.println("🧠 Gemini model in use: " + model);
        System.out.println("⏱️ Timeout configured: " + safeTimeout + " seconds");
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
            String url = "/models/" + model + ":generateContent";
            System.out.println("🔗 Calling Gemini API: " + url + " (model: " + model + ")");

                final MediaType jsonType = Objects.requireNonNull(MediaType.APPLICATION_JSON);

                return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(jsonType)
                    .bodyValue(Objects.requireNonNull(request))
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block(timeout);
        } catch (WebClientResponseException ex) {
            String reason = ex.getResponseBodyAsString();
            int statusCode = ex.getStatusCode().value();
            String errorMsg = String.format("Gemini API error (HTTP %d): %s", statusCode, reason);
            System.err.println("❌ " + errorMsg);
            throw new GeminiClientException(errorMsg, ex);
        } catch (Exception ex) {
            String errorMsg = String.format(
                "Unexpected error while calling Gemini API: %s: %s",
                ex.getClass().getSimpleName(),
                ex.getMessage() != null ? ex.getMessage() : ex.toString()
            );
            System.err.println("❌ " + errorMsg);
            throw new GeminiClientException(errorMsg, ex);
        }
    }

    private void ensureApiKeyConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiClientException("Gemini API key is not configured. Set GEMINI_API_KEY or gemini.api.key.");
        }
    }
}
