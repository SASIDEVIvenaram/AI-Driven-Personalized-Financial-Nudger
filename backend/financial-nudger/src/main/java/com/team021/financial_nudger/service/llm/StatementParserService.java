package com.team021.financial_nudger.service.llm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.dto.ExtractedTransactionDto.TransactionType;

/**
 * Uses Gemini LLM to extract transactions from a bank statement PDF
 * and then calls GeminiCategorizationService to categorize each transaction.
 */
@Service
public class StatementParserService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final GeminiCategorizationService geminiCategorizationService;
    private final int maxStatementChars;

    public StatementParserService(GeminiClient geminiClient,
                                  ObjectMapper objectMapper,
                                  GeminiCategorizationService geminiCategorizationService,
                                  @Value("${gemini.max-statement-chars:6000}") int maxStatementChars) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.geminiCategorizationService = geminiCategorizationService;
        this.maxStatementChars = maxStatementChars;
    }

    /**
     * Extracts transactions using Gemini and then classifies each one.
     */
    public List<ExtractedTransactionDto> extractTransactions(String rawPdfText, Integer userId) {
        String truncatedText = truncate(rawPdfText);

        String instructions = """
                You are analyzing a bank statement text. Extract each clear transaction line.
                For every transaction, output the following fields:
                - date (YYYY-MM-DD)
                - amount (number)
                - description (string)
                - type (DEBIT or CREDIT)
                Return a valid JSON array only.
                """;

        List<GeminiPart> parts = List.of(GeminiPart.text(truncatedText));
        String jsonResponse = geminiClient.generateJson(instructions, parts);

        // Parse Gemini's structured output
        List<ExtractedTransactionDto> baseTransactions = parseTransactions(jsonResponse);

        // Fetch valid categories once
        List<String> validCategories = geminiCategorizationService.getAvailableCategoriesForUser(userId);

        // Build a new enriched list
        List<ExtractedTransactionDto> enriched = new ArrayList<>();

        for (ExtractedTransactionDto txn : baseTransactions) {
            String context = txn.description() + " " + txn.amount() + " " + txn.type();

            ClassificationResult result =
                    geminiCategorizationService.classifyExpense(userId, context, validCategories);

            // ✅ Create a new DTO with category & confidence
            ExtractedTransactionDto enrichedTxn = new ExtractedTransactionDto(
                    txn.date(),
                    txn.amount(),
                    txn.description(),
                    txn.type(),
                    result.classifiedCategoryName(),
                    result.confidenceScore()
            );

            enriched.add(enrichedTxn);
        }

        return enriched;
    }

    /**
     * Parses Gemini’s raw JSON response into transaction DTOs.
     */
    private List<ExtractedTransactionDto> parseTransactions(String jsonResponse) {
        List<ExtractedTransactionDto> results = new ArrayList<>();
        try {
            String cleaned = jsonResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);
            if (!node.isArray()) {
                throw new GeminiClientException("Gemini statement response is not an array");
            }

            for (JsonNode txnNode : node) {
                LocalDate date = LocalDate.parse(txnNode.path("date").asText());
                BigDecimal amount = new BigDecimal(txnNode.path("amount").asText());
                String description = txnNode.path("description").asText();
                TransactionType type = TransactionType.valueOf(txnNode.path("type").asText("DEBIT").toUpperCase());

                // Base DTO (no category/confidence yet)
                ExtractedTransactionDto dto = new ExtractedTransactionDto(date, amount, description, type);
                results.add(dto);
            }

            return results;
        } catch (Exception ex) {
            throw new GeminiClientException("Failed to parse Gemini statement response: " + jsonResponse, ex);
        }
    }

    private String truncate(String rawText) {
        if (rawText == null) return "";
        return rawText.length() <= maxStatementChars ? rawText : rawText.substring(0, maxStatementChars);
    }
}
