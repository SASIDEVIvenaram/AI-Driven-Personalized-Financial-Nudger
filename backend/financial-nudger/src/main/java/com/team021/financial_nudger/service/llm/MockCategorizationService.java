package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MockCategorizationService implements CategorizationService {

    // Simple mock logic: checks keywords and returns a hardcoded confidence.
    @Override
    public ClassificationResult classifyExpense(Integer userId, String rawText, List<String> availableCategories) {

        String category = "Miscellaneous";
        BigDecimal confidence = new BigDecimal("0.5000");

        String lowerText = rawText.toLowerCase();

        if (lowerText.contains("starbucks") || lowerText.contains("cafe") || lowerText.contains("sandwich")) {
            category = "Food";
            confidence = new BigDecimal("0.9500");
        } else if (lowerText.contains("amazon") || lowerText.contains("store") || lowerText.contains("mall")) {
            category = "Shopping";
            confidence = new BigDecimal("0.9000");
        } else if (lowerText.contains("grocer") || lowerText.contains("supermarket") || lowerText.contains("milk")) {
            category = "Groceries";
            confidence = new BigDecimal("0.8500");
        }

        // Ensure the mock category is actually one of the available ones (if you care about strictness)
        if (!availableCategories.contains(category)) {
            category = availableCategories.stream().findFirst().orElse("Miscellaneous");
        }

        return new ClassificationResult(category, confidence);
    }
}