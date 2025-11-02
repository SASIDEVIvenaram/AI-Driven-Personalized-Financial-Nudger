package com.team021.financial_nudger.dto;

import com.team021.financial_nudger.domain.Category.CategoryType;

public record CategoryResponse(
        Integer categoryId,
        String categoryName,
        CategoryType categoryType,
        Boolean isUserDefined
) {}