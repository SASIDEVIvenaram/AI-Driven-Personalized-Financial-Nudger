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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "ingested_files")
public class IngestedFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer fileId;
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;
    
    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "MIME type is required")
    @Size(max = 128, message = "MIME type must not exceed 128 characters")
    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false)
    private UploadStatus uploadStatus = UploadStatus.PENDING;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "uploaded_at", updatable = false, insertable = false)
    private Instant uploadedAt;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    // Enums
    public enum FileType {
        CSV, RECEIPT, MANUAL
    }
    
    public enum UploadStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
    
    // Constructors
    public IngestedFile() {}
    
    public IngestedFile(Integer userId, String fileName, FileType fileType, Long fileSize) {
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
    
    // Getters and Setters
    public Integer getFileId() {
        return fileId;
    }
    
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public FileType getFileType() {
        return fileType;
    }
    
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }
    
    public void setUploadStatus(UploadStatus uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Instant getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public Instant getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    @Override
    public String toString() {
        return "IngestedFile{" +
                "fileId=" + fileId +
                ", userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                ", fileSize=" + fileSize +
                ", uploadStatus=" + uploadStatus +
                ", errorMessage='" + errorMessage + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", processedAt=" + processedAt +
                '}';
    }
}
