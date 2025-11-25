package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team021.financial_nudger.repository.TransactionCategoryFeedbackRepository;
import com.team021.financial_nudger.service.CategoryService;

@Service
@Primary
public class GeminiCategorizationService implements CategorizationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiCategorizationService.class);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final TransactionCategoryFeedbackRepository feedbackRepository;
    private final CategoryService categoryService;

    public GeminiCategorizationService(GeminiClient geminiClient,
                                       ObjectMapper objectMapper,
                                       TransactionCategoryFeedbackRepository feedbackRepository,
                                       CategoryService categoryService) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.feedbackRepository = feedbackRepository;
        this.categoryService = categoryService;
    }

    @Override
    public ClassificationResult classifyExpense(Integer userId, String rawText, List<String> availableCategories) {
        String instructions = buildPrompt(availableCategories, userId);
        List<GeminiPart> parts = List.of(GeminiPart.text("Expense details:\n" + rawText));

        String jsonResponse = geminiClient.generateJson(instructions, parts);
        try {
            JsonNode node = objectMapper.readTree(jsonResponse);
            String categoryName = node.path("category").asText();
            BigDecimal confidence = new BigDecimal(node.path("confidence").asText("0.80"));
            return new ClassificationResult(categoryName, confidence);
        } catch (Exception ex) {
            log.error("Failed to parse Gemini classification response: {}", jsonResponse, ex);
            throw new GeminiClientException("Unable to parse Gemini classification response", ex);
        }
    }

    private String buildPrompt(List<String> availableCategories, Integer userId) {
        String categories = String.join(", ", availableCategories);
        StringBuilder builder = new StringBuilder("""
                You classify personal finance transactions into categories.
                Choose strictly from this comma separated list: %s.
                Reply with JSON: {"category":"<one of the categories>","confidence":0-1}.
                """.formatted(categories));

        String feedbackSummary = buildFeedbackSummary(userId);
        if (!feedbackSummary.isBlank()) {
            builder.append("\nUser corrections to respect: ").append(feedbackSummary);
        }
        builder.append("\nPrefer higher confidence when the description explicitly mentions a merchant that matches a prior correction.");
        return builder.toString();
    }

    private String buildFeedbackSummary(Integer userId) {
        return feedbackRepository.findRecentFeedbackByUserId(userId).stream()
                .limit(5)
                .map(feedback -> {
                    String oldName = categoryService.getCategoryNameById(feedback.getOldCategoryId());
                    String newName = categoryService.getCategoryNameById(feedback.getNewCategoryId());
                    return oldName + " ➜ " + newName;
                })
                .collect(Collectors.joining("; "));
    }
    public List<String> getAvailableCategoriesForUser(Integer userId) {
        return categoryService.getAvailableCategoryNames(userId);
    }
    
}

