package com.team021.financial_nudger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team021.financial_nudger.dto.CategoryCreateRequest;
import com.team021.financial_nudger.dto.CategoryResponse;
import com.team021.financial_nudger.service.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody @Valid CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryResponse>> listForUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(categoryService.getCategoriesForUser(userId));
    }
}
