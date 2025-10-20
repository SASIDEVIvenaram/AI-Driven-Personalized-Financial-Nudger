package com.team021.financial_nudger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team021.financial_nudger.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    List<Category> findByUserId(Integer userId);
    
    List<Category> findByIsUserDefined(Boolean isUserDefined);
    
    List<Category> findByUserIdAndIsUserDefined(Integer userId, Boolean isUserDefined);
    
    @Query("SELECT c FROM Category c WHERE c.userId = :userId OR c.isUserDefined = false")
    List<Category> findAvailableCategoriesForUser(@Param("userId") Integer userId);
    
    boolean existsByCategoryNameAndUserId(String categoryName, Integer userId);
    
    boolean existsByCategoryNameAndIsUserDefined(String categoryName, Boolean isUserDefined);
}