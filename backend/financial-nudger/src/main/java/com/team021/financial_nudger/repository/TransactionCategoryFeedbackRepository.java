package com.team021.financial_nudger.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.team021.financial_nudger.domain.TransactionCategoryFeedback;

@Repository
public interface TransactionCategoryFeedbackRepository extends JpaRepository<TransactionCategoryFeedback, Integer> {
    
    List<TransactionCategoryFeedback> findByUserId(Integer userId);
    
    List<TransactionCategoryFeedback> findByTransactionId(Integer transactionId);
    
    List<TransactionCategoryFeedback> findByUserIdAndCreatedAtAfter(Integer userId, Instant since);
    
    @Query("SELECT f FROM TransactionCategoryFeedback f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<TransactionCategoryFeedback> findRecentFeedbackByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT f.oldCategoryId, f.newCategoryId, COUNT(f) FROM TransactionCategoryFeedback f WHERE f.userId = :userId GROUP BY f.oldCategoryId, f.newCategoryId")
    List<Object[]> getFeedbackPatternsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT f FROM TransactionCategoryFeedback f WHERE f.createdAt >= :since")
    List<TransactionCategoryFeedback> findFeedbackSince(@Param("since") Instant since);
}
