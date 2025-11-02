package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.util.List;

public interface CategorizationService {

    ClassificationResult classifyExpense(String rawText, List<String> availableCategories);
}