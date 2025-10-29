package com.team021.financial_nudger.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
    
    private boolean success;
    private String message;
    private Integer fileId;
    private String fileName;
    private Long fileSize;
    private Integer processedRows;
    private Integer successfulTransactions;
    private Integer failedTransactions;
    private List<String> errors;
    private Instant processedAt;
    
    public static FileUploadResponse success(Integer fileId, String fileName, Long fileSize, 
                                          Integer processedRows, Integer successfulTransactions) {
        return FileUploadResponse.builder()
            .success(true)
            .message("File processed successfully")
            .fileId(fileId)
            .fileName(fileName)
            .fileSize(fileSize)
            .processedRows(processedRows)
            .successfulTransactions(successfulTransactions)
            .failedTransactions(processedRows - successfulTransactions)
            .processedAt(Instant.now())
            .build();
    }
    
    public static FileUploadResponse failure(String message, List<String> errors) {
        return FileUploadResponse.builder()
            .success(false)
            .message(message)
            .errors(errors)
            .processedAt(Instant.now())
            .build();
    }
}
