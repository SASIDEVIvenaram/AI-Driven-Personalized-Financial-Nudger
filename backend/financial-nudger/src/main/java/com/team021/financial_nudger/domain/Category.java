package com.team021.financial_nudger.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
    
    @NotNull(message = "Category type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false)
    private CategoryType categoryType;
    
    @Column(name = "is_user_defined", nullable = false)
    private Boolean isUserDefined = false;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
    // Enum for category types
    public enum CategoryType {
        EXPENSE, INCOME, TRANSFER
    }
    
    // Constructors
    public Category() {}
    
    public Category(String categoryName, CategoryType categoryType, Boolean isUserDefined, Integer userId) {
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.isUserDefined = isUserDefined;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public CategoryType getCategoryType() {
        return categoryType;
    }
    
    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }
    
    public Boolean getIsUserDefined() {
        return isUserDefined;
    }
    
    public void setIsUserDefined(Boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", categoryType=" + categoryType +
                ", isUserDefined=" + isUserDefined +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                '}';
    }
}
