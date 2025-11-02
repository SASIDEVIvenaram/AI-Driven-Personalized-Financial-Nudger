package com.team021.financial_nudger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    // Core Required Fields
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    // Nullable/Optional Fields
    @Column(name = "category_id")
    private Integer categoryId; // Nullable for uncategorized or manual entries

    @Column(name = "file_id")
    private Integer fileId; // Nullable, only used for PDF/Imported transactions

    @Column(name = "description")
    private String description;

    @Column(name = "channel", length = 32)
    private String channel; // Nullable, usually derived from bank text

    @Column(name = "currency", length = 8)
    private String currency = "INR";

    @Column(name = "merchant_name", length = 255)
    private String merchantName;

    // AI/Categorization Flags
    @Column(name = "category_confidence", precision = 5, scale = 4)
    private BigDecimal categoryConfidence;

    @Column(name = "is_ai_categorized", nullable = false)
    private Boolean isAiCategorized = false;

    @Column(name = "is_user_categorized", nullable = false)
    private Boolean isUserCategorized = false;

    // Metadata
    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    // Enum for transaction types
    public enum TransactionType {
        DEBIT, CREDIT
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public BigDecimal getCategoryConfidence() {
        return categoryConfidence;
    }

    public void setCategoryConfidence(BigDecimal categoryConfidence) {
        this.categoryConfidence = categoryConfidence;
    }

    public Boolean getAiCategorized() {
        return isAiCategorized;
    }

    public void setAiCategorized(Boolean aiCategorized) {
        isAiCategorized = aiCategorized;
    }

    public Boolean getUserCategorized() {
        return isUserCategorized;
    }

    public void setUserCategorized(Boolean userCategorized) {
        isUserCategorized = userCategorized;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}