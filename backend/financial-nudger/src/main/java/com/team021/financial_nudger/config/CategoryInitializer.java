package com.team021.financial_nudger.config; // <-- NEW PACKAGE LOCATION

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.team021.financial_nudger.domain.Category;
import com.team021.financial_nudger.domain.Category.CategoryType;
import com.team021.financial_nudger.repository.CategoryRepository;

@Component
public class CategoryInitializer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Ensure required system categories exist. If already present, skip; if missing, insert.
        List<String> required = List.of(
            "Food_Dining",   // matches model label
            "Groceries",
            "Transfers",
            "Transport",
            "Utilities",
            "Miscellaneous"  // fallback bucket
        );

        List<Category> missing = required.stream()
                .filter(name -> !categoryRepository.existsByCategoryNameAndIsUserDefined(name, false))
                .map(name -> {
                    Category c = new Category();
                    c.setCategoryName(name);
                    c.setCategoryType(CategoryType.EXPENSE);
                    c.setIsUserDefined(false);
                    c.setUserId(null);
                    return c;
                })
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            categoryRepository.saveAll(Objects.requireNonNull(missing));
            System.out.println("✅ System categories ensured/added: " + missing.size());
        }
    }

}