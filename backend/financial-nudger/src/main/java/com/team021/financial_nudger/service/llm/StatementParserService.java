package com.team021.financial_nudger.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.dto.ExtractedTransactionDto.TransactionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class StatementParserService {

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final GeminiCategorizationService geminiCategorizationService;
    private final int maxStatementChars;

    private static final int MAX_PARALLELISM = 3; // adjust based on your CPU/network

    public StatementParserService(
            GeminiClient geminiClient,
            ObjectMapper objectMapper,
            GeminiCategorizationService geminiCategorizationService,
            @Value("${gemini.max-statement-chars:3000}") int maxStatementChars) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.geminiCategorizationService = geminiCategorizationService;
        this.maxStatementChars = maxStatementChars;
    }

    public List<ExtractedTransactionDto> extractTransactions(String rawPdfText, Integer userId) {
        if (rawPdfText == null || rawPdfText.isBlank()) {
            throw new IllegalArgumentException("Bank statement text is empty");
        }

        System.out.println("📄 Extracted PDF text length: " + rawPdfText.length());

        // Split OCR text into smaller safe chunks
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < rawPdfText.length(); i += maxStatementChars) {
            int end = Math.min(i + maxStatementChars, rawPdfText.length());
            chunks.add(rawPdfText.substring(i, end));
        }

        System.out.println("📚 Created " + chunks.size() + " text chunks for Gemini parsing.");

        String instructions = """
            You are parsing an Indian bank statement for user %d.
            Each line usually looks like:
            Date | Narration | Chq/Ref No | Withdrawal (Dr) | Deposit (Cr) | Balance
            Example:
            02-08-2025  UPI/NELLAI SWEETS A/109122519947/UPI  65.00(Dr)
            02-08-2025  UPI/R K Fuels/109127449897/UPI  100.00(Dr)
            Rules:
            - Ignore headers, totals, and balance rows.
            - (Dr) means DEBIT, (Cr) means CREDIT.
            - Output ONLY a valid JSON array. No markdown, no text.
            Example:
            [{"date":"2025-08-02","amount":65.00,"description":"UPI/NELLAI SWEETS A","type":"DEBIT"}]
            """.formatted(userId);

        // Process chunks in parallel
        ExecutorService executor = Executors.newFixedThreadPool(MAX_PARALLELISM);
        List<CompletableFuture<List<ExtractedTransactionDto>>> futures = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            final int chunkIndex = i;
            final String chunk = chunks.get(i);

            CompletableFuture<List<ExtractedTransactionDto>> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("🧩 Sending chunk " + (chunkIndex + 1) + "/" + chunks.size() +
                        " to Gemini (length=" + chunk.length() + ")");
                List<GeminiPart> parts = List.of(GeminiPart.text(chunk));

                try {
                    String jsonResponse = retry(() -> geminiClient.generateJson(instructions, parts), 3);
                    List<ExtractedTransactionDto> parsed = parseTransactions(jsonResponse);
                    System.out.println("✅ Parsed " + parsed.size() + " transactions from chunk " + (chunkIndex + 1));
                    return parsed;
                } catch (Exception e) {
                    System.err.println("❌ Chunk " + (chunkIndex + 1) + " failed: " + e.getMessage());
                    return List.of();
                }
            }, executor);

            futures.add(future);
        }

        // Collect all results
        List<ExtractedTransactionDto> allTransactions = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        executor.shutdown();

        // Categorization step
        List<String> validCategories = geminiCategorizationService.getAvailableCategoriesForUser(userId);
        List<ExtractedTransactionDto> enriched = new ArrayList<>();

        for (ExtractedTransactionDto txn : allTransactions) {
            String context = txn.description() + " " + txn.amount() + " " + txn.type();
            try {
                ClassificationResult result =
                        geminiCategorizationService.classifyExpense(userId, context, validCategories);
                enriched.add(new ExtractedTransactionDto(
                        txn.date(),
                        txn.amount(),
                        txn.description(),
                        txn.type(),
                        result.classifiedCategoryName(),
                        result.confidenceScore()
                ));
            } catch (Exception ex) {
                System.err.println("⚠️ Failed to classify: " + txn.description() + " → " + ex.getMessage());
                enriched.add(txn);
            }
        }

        System.out.println("🏁 Completed extraction. Total parsed transactions: " + allTransactions.size());
        return enriched;
    }

    // ✅ retry helper (simple and blocking-safe)
    private <T> T retry(Callable<T> action, int retries) {
        int attempt = 0;
        while (true) {
            try {
                return action.call();
            } catch (Exception e) {
                attempt++;
                if (attempt >= retries) {
                    throw new RuntimeException("Retries exhausted: " + attempt, e);
                }
                System.err.println("🔁 Retrying attempt " + attempt + " after error: " + e.getMessage());
                try {
                    Thread.sleep(2000L * attempt);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private List<ExtractedTransactionDto> parseTransactions(String jsonResponse) {
        List<ExtractedTransactionDto> results = new ArrayList<>();
        try {
            String cleaned = jsonResponse
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);
            if (!node.isArray()) {
                throw new GeminiClientException("Gemini response is not a valid JSON array");
            }

            for (JsonNode txnNode : node) {
                LocalDate date = LocalDate.parse(txnNode.path("date").asText());
                BigDecimal amount = new BigDecimal(txnNode.path("amount").asText());
                String description = txnNode.path("description").asText();
                TransactionType type = TransactionType.valueOf(txnNode.path("type").asText("DEBIT").toUpperCase());
                results.add(new ExtractedTransactionDto(date, amount, description, type));
            }

        } catch (Exception ex) {
            System.err.println("❌ Failed to parse Gemini response. Raw response:\n" + jsonResponse);
            throw new GeminiClientException("Failed to parse Gemini response: " + ex.getMessage(), ex);
        }
        return results;
    }
}
