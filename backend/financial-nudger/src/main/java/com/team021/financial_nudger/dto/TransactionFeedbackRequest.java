package com.team021.financial_nudger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionFeedbackRequest(
        @NotNull Integer transactionId,
        @NotNull Integer userId,
        @NotBlank String correctedCategoryName
) { }

