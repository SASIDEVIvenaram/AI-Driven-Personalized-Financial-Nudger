package com.team021.financial_nudger.config; // <-- NEW PACKAGE LOCATION

import com.team021.financial_nudger.domain.Category;
import com.team021.financial_nudger.domain.Category.CategoryType;
import com.team021.financial_nudger.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no categories exist (to prevent duplicates on restart)
        if (categoryRepository.count() == 0) {

            // Define your required categories (only EXPENSE as per your statement)
            List<String> expenseCategories = List.of(
                    "Groceries",
                    "Food",
                    "Shopping",
                    "Entertainment",
                    "Transportation",
                    "Bills & Utilities",
                    "Health & Fitness",
                    "Miscellaneous" // Essential fallback category for the LLM
            );

            // Save the categories to the database
            List<Category> categories = expenseCategories.stream()
                    .map(name -> {
                        Category c = new Category();
                        c.setCategoryName(name);
                        c.setCategoryType(CategoryType.EXPENSE);
                        c.setIsUserDefined(false); // Mark as system/standard category
                        c.setUserId(null); // System categories don't belong to a user
                        return c;
                    })
                    .collect(Collectors.toList());

            categoryRepository.saveAll(categories);
            System.out.println("✅ Initial system categories loaded: " + categories.size());
        }
    }

}