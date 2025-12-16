package com.team021.financial_nudger.service.llm;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.dto.ExtractedFinancialData;

/**
 * Handles multimodal Gemini API calls for extracting structured data
 * (date, merchant, items, category, confidence) from receipt images.
 * Supports both single-item and multi-item receipts.
 */
@Service
public class LLMExtractionService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public LLMExtractionService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends the receipt file to Gemini for multimodal parsing and returns
     * a list of structured transactions extracted from the receipt.
     */
    public List<ExtractedFinancialData> extractAndCategorizeReceipt(
            MultipartFile receiptFile,
            Integer userId,
            List<String> validCategories) {

        String instructions = """
                Extract all purchasable line items from this receipt image.
                For each item, extract:
                - date (YYYY-MM-DD)
                - amount (numeric)
                - merchant (name of seller)
                - description (item name)
                - category (choose one of: %s)
                - confidence (0 to 1)
                - type (DEBIT or CREDIT)

                Respond strictly in a pure JSON array (no markdown, no explanations):
                [
                  {
                    "date": "YYYY-MM-DD",
                    "amount": 219.04,
                    "merchant": "Aura & Co.",
                    "description": "HUL BF Veg Mayonnaise - 1kg",
                    "category": "Groceries",
                    "confidence": 0.95,
                    "type": "DEBIT"
                  },
                  {
                    "date": "YYYY-MM-DD",
                    "amount": 400.00,
                    "merchant": "Aura & Co.",
                    "description": "Milky.M Cheese Cubes - 600g",
                    "category": "Groceries",
                    "confidence": 0.95,
                    "type": "DEBIT"
                  }
                ]
                """.formatted(String.join(", ", validCategories));

        List<GeminiPart> parts = new ArrayList<>();
        parts.add(GeminiPart.text("User id: " + userId));
        parts.add(createInlinePart(receiptFile));

        String jsonResponse = geminiClient.generateJson(instructions, parts);
        return mapToFinancialDataList(jsonResponse);
    }

    /**
     * Encodes the receipt image as Base64 inline data for Gemini API.
     */
    private GeminiPart createInlinePart(MultipartFile file) {
        try {
            String mimeType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            return GeminiPart.inlineData(mimeType, base64);
        } catch (IOException ex) {
            throw new GeminiClientException("Failed to read receipt file for Gemini processing", ex);
        }
    }

    /**
     * Parses Gemini's JSON response (array or single object) into a list of ExtractedFinancialData.
     */
    private List<ExtractedFinancialData> mapToFinancialDataList(String jsonResponse) {
        List<ExtractedFinancialData> dataList = new ArrayList<>();
        try {
            String cleaned = jsonResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            JsonNode root = objectMapper.readTree(cleaned);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    dataList.add(parseItem(node));
                }
            } else {
                dataList.add(parseItem(root));
            }

            return dataList;
        } catch (java.io.IOException ex) {
            throw new GeminiClientException("Unable to parse Gemini receipt response: " + jsonResponse, ex);
        }
    }

    /**
     * Converts a single JSON node into a typed ExtractedFinancialData object.
     */
    private ExtractedFinancialData parseItem(JsonNode node) {
        LocalDate date = LocalDate.parse(node.path("date").asText());
        BigDecimal amount = new BigDecimal(node.path("amount").asText());
        String merchant = node.path("merchant").asText();
        String description = node.path("description").asText();
        String category = node.path("category").asText();
        BigDecimal confidence = new BigDecimal(node.path("confidence").asText("0.80"));
        Transaction.TransactionType type = Transaction.TransactionType.valueOf(
                node.path("type").asText("DEBIT").toUpperCase()
        );

        return new ExtractedFinancialData(
                date,
                amount,
                merchant,
                description,
                category,
                confidence,
                type
        );
    }
}
