package com.team021.financial_nudger.service.llm;

import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.dto.ExtractedTransactionDto.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatementParserService {

    // 🚨 IMPORTANT: This is a temporary MOCK implementation.
    // In Phase 5, this method will contain the actual Gemini API call
    // with instructions to return a List<ExtractedTransactionDto> in JSON format.

    /**
     * MOCK: Simulates calling Gemini to extract structured transactions from raw PDF text.
     * @param rawPdfText The raw text extracted by PDFBox.
     * @param userId The ID of the user (can be used for contextual prompting later).
     * @return A list of clean, structured transaction DTOs.
     */
    public List<ExtractedTransactionDto> extractTransactions(String rawPdfText, Integer userId) {

        // ⚠️ In a real implementation, this is where the Gemini API call goes.
        // For demonstration, we return a hardcoded list.
        List<ExtractedTransactionDto> transactions = new ArrayList<>();

        // Mocking the first two transactions from your example data:
        // 1. UPI OUT: 59.00
        transactions.add(new ExtractedTransactionDto(
                LocalDate.of(2025, 10, 4),
                new BigDecimal("59.00"),
                "UPI OUT VENDOLITE INDIA PAYMENT",
                TransactionType.DEBIT
        ));

        // 2. UPI IN: 60.00
        transactions.add(new ExtractedTransactionDto(
                LocalDate.of(2025, 10, 4),
                new BigDecimal("60.00"),
                "UPI IN sasi RECEIPT",
                TransactionType.CREDIT
        ));

        // Note: You must ensure your TransactionType enum exists in your domain.
        return transactions;
    }
}