package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;

// Use 'record' for a concise, immutable data class
public record ClassificationResult(
        String classifiedCategoryName,
        BigDecimal confidenceScore
) {}