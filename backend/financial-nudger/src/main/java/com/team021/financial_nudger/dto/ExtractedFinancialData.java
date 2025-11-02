package com.team021.financial_nudger.dto;// package com.team021.financial_nudger.dto;

import com.team021.financial_nudger.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExtractedFinancialData(
        LocalDate date,
        BigDecimal amount,
        String merchantName,
        String description,
        String classifiedCategoryName,
        BigDecimal confidenceScore,
        Transaction.TransactionType type // CREDIT or DEBIT
) {}