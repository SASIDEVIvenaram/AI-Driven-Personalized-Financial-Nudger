package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Calls the local Flask ML API for fast, offline transaction categorization.
 * This replaces direct Gemini usage for textual inputs (manual entry, statements).
 */
@Service
public class MlApiCategorizationService implements CategorizationService {

    private final WebClient webClient;
    private final Duration timeout;
    private final String fallbackCategory;
    private final Map<String, String> synonymMap;

    public MlApiCategorizationService(WebClient.Builder builder,
                                      @Value("${ml.api.base-url:http://localhost:5000}") String baseUrl,
                                      @Value("${ml.api.timeout-seconds:8}") long timeoutSeconds,
                                      @Value("${ml.api.fallback-category:Uncategorized}") String fallbackCategory) {
        final String resolvedBaseUrl = baseUrl != null ? baseUrl : "http://localhost:5000";
        this.webClient = Objects.requireNonNull(builder)
            .baseUrl(resolvedBaseUrl)
            .build();
        this.timeout = Duration.ofSeconds(Math.max(3, timeoutSeconds));
        this.fallbackCategory = fallbackCategory;
        this.synonymMap = buildSynonyms();
    }

    @Override
    public ClassificationResult classifyExpense(Integer userId, String rawText, List<String> availableCategories) {
        if (rawText == null || rawText.isBlank()) {
            return new ClassificationResult(resolveCategory(null, availableCategories), BigDecimal.ZERO);
        }

        try {
                    final MediaType jsonType = Objects.requireNonNull(MediaType.APPLICATION_JSON);

                    MlPredictionResponse response = webClient.post()
                    .uri("/predict")
                        .contentType(jsonType)
                        .bodyValue(Objects.requireNonNull(Map.of("text", rawText)))
                    .retrieve()
                    .bodyToMono(MlPredictionResponse.class)
                    .block(timeout);

            String category = resolveCategory(Optional.ofNullable(response)
                    .map(MlPredictionResponse::category)
                    .orElse(null), availableCategories);

                        double confidenceValue = Optional.ofNullable(response)
                            .map(MlPredictionResponse::confidence)
                            .orElse(0.5d);

                        BigDecimal confidence = BigDecimal.valueOf(confidenceValue);

            return new ClassificationResult(category, confidence);
        } catch (WebClientResponseException ex) {
            String errorMsg = String.format("ML API error (HTTP %d): %s", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new RuntimeException(errorMsg, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to call ML API: " + ex.getMessage(), ex);
        }
    }

    private String resolveCategory(String predictedCategory, List<String> availableCategories) {
        if (availableCategories == null || availableCategories.isEmpty()) {
            return fallbackCategory;
        }

        String normalizedPred = normalize(predictedCategory);
        if (normalizedPred != null) {
            // Direct match ignoring case/punctuation
            for (String candidate : availableCategories) {
                if (normalizedPred.equals(normalize(candidate))) {
                    return candidate;
                }
            }

            // Synonym-based mapping (e.g., Food_Dining -> Food, Bills_Utilities -> Bills & Utilities)
            String mapped = synonymMap.get(normalizedPred);
            if (mapped != null) {
                for (String candidate : availableCategories) {
                    if (normalize(candidate).equals(normalize(mapped))) {
                        return candidate;
                    }
                }
            }
        }

        // Fallback to configured category if it exists in the list
        for (String candidate : availableCategories) {
            if (normalize(candidate).equals(normalize(fallbackCategory))) {
                return candidate;
            }
        }

        // If fallback is missing, keep the first available to avoid null category, but logically this should be avoided
        return availableCategories.get(0);
    }

    private Map<String, String> buildSynonyms() {
        Map<String, String> base = Map.ofEntries(
            Map.entry("fooddining", "Food_Dining"),
            Map.entry("food_dining", "Food_Dining"),
            Map.entry("food", "Food_Dining"),

            Map.entry("groceries", "Groceries"),

            Map.entry("billsutilities", "Utilities"),
            Map.entry("bills_utilities", "Utilities"),
            Map.entry("utilities", "Utilities"),

            Map.entry("transport", "Transport"),
            Map.entry("transportation", "Transport"),

            Map.entry("transfer", "Transfers"),
            Map.entry("transfers", "Transfers"),
            Map.entry("income", "Transfers"),
            Map.entry("credit", "Transfers"),

            Map.entry("misc", "Miscellaneous"),
            Map.entry("miscellaneous", "Miscellaneous"),
            Map.entry("others", "Miscellaneous")
        );

        java.util.Map<String, String> normalized = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> entry : base.entrySet()) {
            String key = normalize(entry.getKey());
            if (key == null) continue;
            normalized.putIfAbsent(key, entry.getValue());
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String compact = value
                .replace("&", "and")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();
        return compact.isBlank() ? null : compact;
    }

    /** Lightweight DTO for Flask response mapping */
    private record MlPredictionResponse(String category, Double confidence) { }
}
