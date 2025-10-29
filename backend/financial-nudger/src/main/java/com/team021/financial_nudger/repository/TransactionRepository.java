package com.team021.financial_nudger.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team021.financial_nudger.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    List<Transaction> findByUserId(Integer userId);
    
    List<Transaction> findByUserIdAndDateBetween(Integer userId, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserIdAndCategoryId(Integer userId, Integer categoryId);
    
    List<Transaction> findByUserIdAndIsAiCategorized(Integer userId, Boolean isAiCategorized);
    
    List<Transaction> findByUserIdAndIsUserCategorized(Integer userId, Boolean isUserCategorized);
    
    List<Transaction> findByCategoryIdIsNullAndUserId(Integer userId);

    // Fetch by category without scoping to user (use carefully; prefer user-scoped in controllers)
    List<Transaction> findByCategoryId(Integer categoryId);
    
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.categoryId IS NULL")
    List<Transaction> findUncategorizedTransactionsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.categoryConfidence < :threshold")
    List<Transaction> findLowConfidenceTransactions(@Param("userId") Integer userId, @Param("threshold") BigDecimal threshold);
    
    @Query("SELECT t.categoryId, COUNT(t) FROM Transaction t WHERE t.userId = :userId GROUP BY t.categoryId")
    List<Object[]> getCategoryCountsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT t.categoryId, SUM(t.amount) FROM Transaction t WHERE t.userId = :userId AND t.type = 'DEBIT' GROUP BY t.categoryId")
    List<Object[]> getCategorySpendingByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    List<Transaction> findTransactionsByUserAndDateRange(@Param("userId") Integer userId, 
                                                       @Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);
}