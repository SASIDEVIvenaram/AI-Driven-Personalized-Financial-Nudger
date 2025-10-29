package com.team021.financial_nudger.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.service.FileUploadService;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<FileUploadResponse> uploadCsv(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("userId") @NotNull Integer userId) {
        
        try {
            FileUploadResponse response = fileUploadService.processCsvFile(file, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing CSV: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/upload-receipt")
    public ResponseEntity<FileUploadResponse> uploadReceipt(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("userId") @NotNull Integer userId) {
        
        try {
            FileUploadResponse response = fileUploadService.processReceiptFile(file, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing receipt: " + e.getMessage())
                    .build());
        }
    }
}
