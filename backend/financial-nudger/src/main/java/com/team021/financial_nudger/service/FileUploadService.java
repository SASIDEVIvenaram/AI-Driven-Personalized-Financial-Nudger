package com.team021.financial_nudger.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.domain.IngestedFile;
import com.team021.financial_nudger.domain.IngestedRow;
import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.repository.IngestedFileRepository;
import com.team021.financial_nudger.repository.IngestedRowRepository;
import com.team021.financial_nudger.repository.TransactionRepository;

@Service
public class FileUploadService {

    private final IngestedFileRepository ingestedFileRepository;
    private final IngestedRowRepository ingestedRowRepository;
    private final TransactionRepository transactionRepository;
    private final CsvParsingService csvParsingService;

    public FileUploadService(IngestedFileRepository ingestedFileRepository,
                            IngestedRowRepository ingestedRowRepository,
                            TransactionRepository transactionRepository,
                            CsvParsingService csvParsingService) {
        this.ingestedFileRepository = ingestedFileRepository;
        this.ingestedRowRepository = ingestedRowRepository;
        this.transactionRepository = transactionRepository;
        this.csvParsingService = csvParsingService;
    }

    @Transactional
    public FileUploadResponse processCsvFile(MultipartFile file, Integer userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV");
        }

        // Create IngestedFile record
        IngestedFile ingestedFile = new IngestedFile(
            userId,
            fileName,
            IngestedFile.FileType.CSV,
            file.getSize()
        );
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.PROCESSING);
        ingestedFile = ingestedFileRepository.save(ingestedFile);

        try {
            // Parse CSV and create IngestedRows
            List<IngestedRow> rows = parseCsvToRows(file, ingestedFile.getFileId());
            ingestedRowRepository.saveAll(rows);

            // Process each row and create transactions
            int successfulTransactions = 0;
            List<String> errors = new ArrayList<>();

            for (IngestedRow row : rows) {
                try {
                    Transaction transaction = csvParsingService.parseRowToTransaction(row, userId);
                    if (transaction != null) {
                        transaction.setFileId(ingestedFile.getFileId());
                        transaction.setRowId(row.getRowId());
                        transactionRepository.save(transaction);
                        successfulTransactions++;
                        row.setParseStatus(IngestedRow.ParseStatus.PARSED);
                    } else {
                        row.setParseStatus(IngestedRow.ParseStatus.ERROR);
                        row.setErrorMessage("Failed to parse transaction data");
                        errors.add("Row " + row.getRowNumber() + ": Failed to parse");
                    }
                } catch (Exception e) {
                    row.setParseStatus(IngestedRow.ParseStatus.ERROR);
                    row.setErrorMessage(e.getMessage());
                    errors.add("Row " + row.getRowNumber() + ": " + e.getMessage());
                }
            }

            // Update file status
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFile.setProcessedAt(Instant.now());
            ingestedFileRepository.save(ingestedFile);

            // Update row statuses
            ingestedRowRepository.saveAll(rows);

            return FileUploadResponse.success(
                ingestedFile.getFileId(),
                ingestedFile.getFileName(),
                ingestedFile.getFileSize(),
                rows.size(),
                successfulTransactions
            );

        } catch (Exception e) {
            // Mark file as failed
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFile.setErrorMessage(e.getMessage());
            ingestedFileRepository.save(ingestedFile);

            return FileUploadResponse.failure("Failed to process CSV: " + e.getMessage(), List.of(e.getMessage()));
        }
    }

    @Transactional
    public FileUploadResponse processReceiptFile(MultipartFile file, Integer userId) {
        // TODO: Implement receipt processing with OCR
        // For now, return a placeholder response
        return FileUploadResponse.builder()
            .success(false)
            .message("Receipt processing not yet implemented")
            .build();
    }

    private List<IngestedRow> parseCsvToRows(MultipartFile file, Integer fileId) throws IOException {
        List<IngestedRow> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int rowNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (rowNumber == 1) {
                    // Skip header row
                    continue;
                }
                
                IngestedRow row = new IngestedRow(fileId, rowNumber, line);
                rows.add(row);
            }
        }
        
        return rows;
    }
}
