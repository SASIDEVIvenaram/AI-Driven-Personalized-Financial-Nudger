package com.team021.financial_nudger.domain;

import java.time.Instant;

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
@Table(name = "ingested_rows")
public class IngestedRow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Integer rowId;
    
    @NotNull(message = "File ID is required")
    @Column(name = "file_id", nullable = false)
    private Integer fileId;
    
    @NotNull(message = "Row number is required")
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    
    @NotNull(message = "Raw data is required")
    @Column(name = "raw_data", nullable = false, columnDefinition = "TEXT")
    private String rawData;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false)
    private ParseStatus parseStatus = ParseStatus.PENDING;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;
    
    // Enum for parse status
    public enum ParseStatus {
        PENDING, PARSED, ERROR, SKIPPED
    }
    
    // Constructors
    public IngestedRow() {}
    
    public IngestedRow(Integer fileId, Integer rowNumber, String rawData) {
        this.fileId = fileId;
        this.rowNumber = rowNumber;
        this.rawData = rawData;
    }
    
    // Getters and Setters
    public Integer getRowId() {
        return rowId;
    }
    
    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }
    
    public Integer getFileId() {
        return fileId;
    }
    
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }
    
    public Integer getRowNumber() {
        return rowNumber;
    }
    
    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }
    
    public String getRawData() {
        return rawData;
    }
    
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    
    public ParseStatus getParseStatus() {
        return parseStatus;
    }
    
    public void setParseStatus(ParseStatus parseStatus) {
        this.parseStatus = parseStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "IngestedRow{" +
                "rowId=" + rowId +
                ", fileId=" + fileId +
                ", rowNumber=" + rowNumber +
                ", rawData='" + rawData + '\'' +
                ", parseStatus=" + parseStatus +
                ", errorMessage='" + errorMessage + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
