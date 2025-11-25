package com.team021.financial_nudger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExtractedTransactionDto(
        LocalDate date,
        BigDecimal amount,
        String description,
        TransactionType type,
        String category,
        BigDecimal confidence
) {
    public enum TransactionType { DEBIT, CREDIT }

    // Overloaded constructor (for pre-classification)
    public ExtractedTransactionDto(LocalDate date, BigDecimal amount, String description, TransactionType type) {
        this(date, amount, description, type, null, null);
    }
}
