package com.team021.financial_nudger.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.team021.financial_nudger.domain.Category;
import com.team021.financial_nudger.dto.CategoryCreateRequest;
import com.team021.financial_nudger.dto.CategoryResponse;
import com.team021.financial_nudger.exception.ResourceAlreadyExistsException;
import com.team021.financial_nudger.exception.ResourceNotFoundException;
import com.team021.financial_nudger.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ------------------------------------------
    // 1. LLM & TRANSACTION UTILITY METHODS
    // ------------------------------------------

    /**
     * Retrieves all standard system categories and the user's custom categories.
     * This method is crucial for providing the LLM with a list of valid category names
     * to choose from when classifying a transaction.
     * * @param userId The ID of the user requesting the category list.
     * @return A List of String containing all available category names.
     */
    public List<String> getAvailableCategoryNames(Integer userId) {
        return categoryRepository.findAvailableCategoriesForUser(userId)
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }

    /**
     * Finds the Category entity (and thus the ID) based on the category name
     * returned as a String by the LLM.
     * * @param categoryName The category name (e.g., "Groceries") returned by the LLM.
     * @param userId The user ID, needed to check for custom categories.
     * @return The Category entity.
     * @throws ResourceNotFoundException if the name doesn't match any system or user-defined category.
     */
    public Category getCategoryByName(String categoryName, Integer userId) {
        // Since the LLM is expected to return a valid name, we first try to find it
        // among all available categories.
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: '" + categoryName +
                        "'. LLM returned an invalid category name."));

        // Note: The findByCategoryName is sufficient if the category names are unique
        // across system and user categories. If you allow a user to create a category
        // with the SAME name as a system one, you would need more complex logic.
        // Assuming unique names for simplicity and better data integrity.
    }

    // ------------------------------------------
    // 2. CRUD OPERATIONS
    // ------------------------------------------

    /**
     * Creates a new user-defined category.
     * * @param request The DTO containing the category details.
     * @return The response DTO for the created category.
     * @throws ResourceAlreadyExistsException if the category name is already in use by the user.
     */
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        // Validation: Check for name collision against the user's existing custom categories
        if (categoryRepository.existsByCategoryNameAndUserId(request.categoryName(), request.userId())) {
            throw new ResourceAlreadyExistsException("Category with name '" + request.categoryName() +
                    "' already exists for this user.");
        }

        Category category = new Category();
        category.setCategoryName(request.categoryName());
        category.setCategoryType(request.categoryType());
        category.setIsUserDefined(true); // Explicitly mark as user-defined
        category.setUserId(request.userId());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    /**
     * Fetches all categories (system and user's custom) available to a user.
     */
    public List<CategoryResponse> getCategoriesForUser(Integer userId) {
        return categoryRepository.findAvailableCategoriesForUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------
    // 3. MAPPER
    // ------------------------------------------

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getCategoryType(),
                category.getIsUserDefined()
        );
    }

    public String getCategoryNameById(Integer categoryId) {
        if (categoryId == null) {
            return "Uncategorized";
        }
        return categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("Unknown");
    }
}