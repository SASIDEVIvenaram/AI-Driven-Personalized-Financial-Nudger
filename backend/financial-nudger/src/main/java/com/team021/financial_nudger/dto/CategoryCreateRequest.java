package com.team021.financial_nudger.dto;

import com.team021.financial_nudger.domain.Category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryCreateRequest(
        @NotBlank(message = "Category name is required")
        String categoryName,

        @NotNull(message = "Category type (EXPENSE, INCOME, TRANSFER) is required")
        CategoryType categoryType,

        // The userId is included here for simplicity in this DTO,
        // but in a secure application, it should typically be retrieved
        // from the Spring Security context (the authenticated user).
        @NotNull(message = "User ID is required for custom category creation")
        Integer userId
) {}