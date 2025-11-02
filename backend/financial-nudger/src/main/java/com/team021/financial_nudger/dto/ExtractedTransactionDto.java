package com.team021.financial_nudger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO representing a single clean transaction extracted by the LLM from a PDF

// Raw data holder
public record ExtractedTransactionDto(
        LocalDate date,
        BigDecimal amount,
        String description,
        TransactionType type // Enum: DEBIT or CREDIT
) {
    public enum TransactionType {
        DEBIT,
        CREDIT
    }
}