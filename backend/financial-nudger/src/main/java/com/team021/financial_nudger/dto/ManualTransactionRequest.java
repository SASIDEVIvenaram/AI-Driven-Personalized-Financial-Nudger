package com.team021.financial_nudger.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

// Note: The 'note' field here is the text input for your LLM
public record ManualTransactionRequest(
        @NotNull Integer userId,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,
        @NotBlank(message = "Expense note/description is required")
        String note,
        @NotNull(message = "Date is required")
        LocalDate date
) {}