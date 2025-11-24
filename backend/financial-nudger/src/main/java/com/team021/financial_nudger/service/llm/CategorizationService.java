package com.team021.financial_nudger.service.llm;

import java.util.List;

public interface CategorizationService {

    ClassificationResult classifyExpense(Integer userId, String rawText, List<String> availableCategories);
}