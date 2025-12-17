package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class MlApiCategorizationService implements CategorizationService {

    private final WebClient webClient;
    private final Duration timeout;
    private final String fallbackCategory;

    public MlApiCategorizationService(
            WebClient.Builder builder,
            @Value("${ml.api.base-url:http://localhost:5000}") String baseUrl,
            @Value("${ml.api.timeout-seconds:8}") long timeoutSeconds,
            @Value("${ml.api.fallback-category:Miscellaneous}") String fallbackCategory
    ) {
        this.webClient = builder.baseUrl(Objects.requireNonNull(baseUrl)).build();
        this.timeout = Duration.ofSeconds(Math.max(3, timeoutSeconds));
        this.fallbackCategory = fallbackCategory;
    }

    @Override
    public ClassificationResult classifyExpense(Integer userId, String rawText, List<String> categories) {

        if (rawText == null || rawText.isBlank()) {
            return new ClassificationResult(fallbackCategory, BigDecimal.ZERO);
        }

        try {
                    MlPredictionResponse response = webClient.post()
                    .uri("/predict")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .body(BodyInserters.fromValue(Objects.requireNonNull(Map.of("text", Objects.requireNonNull(rawText)))))
                    .retrieve()
                    .bodyToMono(MlPredictionResponse.class)
                    .block(timeout);

            String category = response != null && response.category != null
                    ? response.category
                    : fallbackCategory;

                        double confVal = (response != null)
                            ? response.confidence
                            : 0.5d;
                    BigDecimal confidence = BigDecimal.valueOf(confVal);

            return new ClassificationResult(category, confidence);

        } catch (Exception ex) {
            return new ClassificationResult(fallbackCategory, BigDecimal.valueOf(0.3));
        }
    }

    private record MlPredictionResponse(String category, double confidence) {}
}
