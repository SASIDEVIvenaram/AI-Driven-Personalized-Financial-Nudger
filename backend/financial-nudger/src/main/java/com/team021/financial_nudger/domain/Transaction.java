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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Column(name = "file_id")
    private Integer fileId;
    
    @Column(name = "row_id")
    private Integer rowId;
    
    @NotNull(message = "Date is required")
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @NotNull(message = "Amount is required")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "channel", length = 32)
    private String channel;
    
    @Column(name = "currency", length = 8)
    private String currency = "INR";
    
    @Column(name = "merchant_name", length = 255)
    private String merchantName;
    
    @Column(name = "category_confidence", precision = 5, scale = 4)
    private BigDecimal categoryConfidence;
    
    @Column(name = "is_ai_categorized", nullable = false)
    private Boolean isAiCategorized = false;
    
    @Column(name = "is_user_categorized", nullable = false)
    private Boolean isUserCategorized = false;
    
    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
    // Enum for transaction types
    public enum TransactionType {
        DEBIT, CREDIT
    }
    
    // Constructors
    public Transaction() {}
    
    public Transaction(Integer userId, LocalDate date, BigDecimal amount, TransactionType type, 
                      String description, String channel, String currency) {
        this.userId = userId;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.channel = channel;
        this.currency = currency;
    }
    
    // Getters and Setters
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
    
    public Integer getRowId() {
        return rowId;
    }
    
    public void setRowId(Integer rowId) {
        this.rowId = rowId;
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
    
    public Boolean getIsAiCategorized() {
        return isAiCategorized;
    }
    
    public void setIsAiCategorized(Boolean isAiCategorized) {
        this.isAiCategorized = isAiCategorized;
    }
    
    public Boolean getIsUserCategorized() {
        return isUserCategorized;
    }
    
    public void setIsUserCategorized(Boolean isUserCategorized) {
        this.isUserCategorized = isUserCategorized;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", date=" + date +
                ", amount=" + amount +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", channel='" + channel + '\'' +
                ", currency='" + currency + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", categoryConfidence=" + categoryConfidence +
                ", isAiCategorized=" + isAiCategorized +
                ", isUserCategorized=" + isUserCategorized +
                ", createdAt=" + createdAt +
                '}';
    }
}
