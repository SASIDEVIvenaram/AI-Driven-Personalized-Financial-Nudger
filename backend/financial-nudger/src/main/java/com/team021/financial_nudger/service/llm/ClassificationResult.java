package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;

// Immutable classification result record
public record ClassificationResult(
        String classifiedCategoryName,
        BigDecimal confidenceScore
) {}
