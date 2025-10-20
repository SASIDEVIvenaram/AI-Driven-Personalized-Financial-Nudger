package com.team021.financial_nudger.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "transaction_category_feedback")
public class TransactionCategoryFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;
    
    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false)
    private Integer transactionId;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "old_category_id")
    private Integer oldCategoryId;
    
    @NotNull(message = "New category ID is required")
    @Column(name = "new_category_id", nullable = false)
    private Integer newCategoryId;
    
    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
    // Constructors
    public TransactionCategoryFeedback() {}
    
    public TransactionCategoryFeedback(Integer transactionId, Integer userId, 
                                    Integer oldCategoryId, Integer newCategoryId) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.oldCategoryId = oldCategoryId;
        this.newCategoryId = newCategoryId;
    }
    
    // Getters and Setters
    public Integer getFeedbackId() {
        return feedbackId;
    }
    
    public void setFeedbackId(Integer feedbackId) {
        this.feedbackId = feedbackId;
    }
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getOldCategoryId() {
        return oldCategoryId;
    }
    
    public void setOldCategoryId(Integer oldCategoryId) {
        this.oldCategoryId = oldCategoryId;
    }
    
    public Integer getNewCategoryId() {
        return newCategoryId;
    }
    
    public void setNewCategoryId(Integer newCategoryId) {
        this.newCategoryId = newCategoryId;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "TransactionCategoryFeedback{" +
                "feedbackId=" + feedbackId +
                ", transactionId=" + transactionId +
                ", userId=" + userId +
                ", oldCategoryId=" + oldCategoryId +
                ", newCategoryId=" + newCategoryId +
                ", createdAt=" + createdAt +
                '}';
    }
}
