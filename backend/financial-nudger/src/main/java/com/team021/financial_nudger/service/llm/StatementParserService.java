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

@Service
public class StatementParserService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final int maxStatementChars;

    public StatementParserService(GeminiClient geminiClient,
                                  ObjectMapper objectMapper,
                                  @Value("${gemini.max-statement-chars:6000}") int maxStatementChars) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.maxStatementChars = maxStatementChars;
    }

    /**
     * Calls Gemini to transform raw statement text into clean transaction DTOs.
     */
    public List<ExtractedTransactionDto> extractTransactions(String rawPdfText, Integer userId) {
        String truncatedText = truncate(rawPdfText);
        String instructions = """
                You are parsing a bank statement for user %d.
                Read the provided statement text and extract each transaction as JSON array items with keys:
                date (YYYY-MM-DD), amount (number), description (string), type (DEBIT or CREDIT).
                Only include rows that clearly represent monetary movements.
                """.formatted(userId);

        List<GeminiPart> parts = List.of(GeminiPart.text(truncatedText));
        String jsonResponse = geminiClient.generateJson(instructions, parts);
        return parseTransactions(jsonResponse);
    }

    private List<ExtractedTransactionDto> parseTransactions(String jsonResponse) {
        List<ExtractedTransactionDto> results = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(jsonResponse);
            if (!node.isArray()) {
                throw new GeminiClientException("Gemini statement response is not an array");
            }
            for (JsonNode txnNode : node) {
                LocalDate date = LocalDate.parse(txnNode.path("date").asText());
                BigDecimal amount = new BigDecimal(txnNode.path("amount").asText());
                String description = txnNode.path("description").asText();
                TransactionType type = TransactionType.valueOf(txnNode.path("type").asText("DEBIT").toUpperCase());
                results.add(new ExtractedTransactionDto(date, amount, description, type));
            }
            return results;
        } catch (Exception ex) {
            throw new GeminiClientException("Failed to parse Gemini statement response: " + jsonResponse, ex);
        }
    }

    private String truncate(String rawText) {
        if (rawText == null) {
            return "";
        }
        if (rawText.length() <= maxStatementChars) {
            return rawText;
        }
        return rawText.substring(0, maxStatementChars);
    }
}