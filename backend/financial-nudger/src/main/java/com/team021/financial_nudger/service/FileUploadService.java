package com.team021.financial_nudger.service;

import java.io.IOException;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.repository.IngestedFileRepository;
import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.service.llm.StatementParserService;
import com.team021.financial_nudger.domain.IngestedFile;
import com.team021.financial_nudger.exception.FileProcessingException;
import com.team021.financial_nudger.service.pdf.PdfExtractionService;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.dto.ExtractedFinancialData;
import com.team021.financial_nudger.service.llm.LLMExtractionService;
import com.team021.financial_nudger.service.CategoryService;

@Service
public class FileUploadService {
    private final IngestedFileRepository ingestedFileRepository;
    private final TransactionService transactionService;
    private final PdfExtractionService pdfExtractionService;
    private final StatementParserService statementParserService; // LLM text-to-DTO
    private final CategoryService categoryService; // For category names
    private final LLMExtractionService llmExtractionService; // LLM multimodal (receipts)

    public FileUploadService(IngestedFileRepository ingestedFileRepository,
                             TransactionService transactionService,
                             PdfExtractionService pdfExtractionService,
                             StatementParserService statementParserService,
                             CategoryService categoryService, // New
                             LLMExtractionService llmExtractionService) { // New
        this.ingestedFileRepository = ingestedFileRepository;
        this.transactionService = transactionService;
        this.pdfExtractionService = pdfExtractionService;
        this.statementParserService = statementParserService;
        this.categoryService = categoryService;
        this.llmExtractionService = llmExtractionService;
    }
    @Transactional
    public FileUploadResponse processReceiptFile(MultipartFile file, Integer userId) throws Exception {
        // 1. Initialize and Save IngestedFile metadata
        IngestedFile ingestedFile = new IngestedFile();
        ingestedFile.setUserId(userId);
        ingestedFile.setFileName(file.getOriginalFilename());
        ingestedFile.setFileSize(file.getSize());
        ingestedFile.setMimeType(file.getContentType());
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.PROCESSING);
        ingestedFile = ingestedFileRepository.save(ingestedFile);
        Integer fileId = ingestedFile.getFileId();

        try {
            // 2. GET CATEGORIES for LLM
            List<String> validCategories = categoryService.getAvailableCategoryNames(userId);

            // 3. LLM EXTRACTION & CATEGORIZATION (Multimodal call)
            // This service handles the image/text analysis and returns a single DTO
            ExtractedFinancialData extractedData = llmExtractionService.extractAndCategorizeReceipt(
                    file, userId, validCategories);

            // 4. MAP CATEGORY NAME to ID and SAVE TRANSACTION
            List<String> errors = transactionService.saveReceiptTransaction(
                    userId,
                    fileId,
                    extractedData
            );

            // 5. Update File Status to SUCCESS
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

            // 6. Build SUCCESS RESPONSE
            return FileUploadResponse.builder()
                    .success(true)
                    .message("Receipt processed and transaction created.")
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .processedRows(1)
                    .successfulTransactions(errors.isEmpty() ? 1 : 0)
                    .failedTransactions(errors.isEmpty() ? 0 : 1)
                    .errors(errors)
                    .processedAt(java.time.Instant.now())
                    .build();

        } catch (Exception e) {
            // Handle any failure during LLM call or transaction saving
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("Receipt processing failed: " + e.getMessage(), e);
        }
    }
    @Transactional
    public FileUploadResponse processPdfFile(MultipartFile file, Integer userId) {

        IngestedFile ingestedFile = new IngestedFile();
        // 1. Initialize and Save IngestedFile metadata
        ingestedFile.setUserId(userId);
        ingestedFile.setFileName(file.getOriginalFilename());
        ingestedFile.setFileSize(file.getSize());
        ingestedFile.setMimeType(file.getContentType());
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.PROCESSING);
        ingestedFile = ingestedFileRepository.save(ingestedFile);
        Integer fileId = ingestedFile.getFileId();

        try {
            // 2. Extract Raw Text (Phase 1: Basic text extraction from PDF)
            String rawText = pdfExtractionService.extractTextFromPdf(file);

            // 3. LLM Structured Extraction (Phase 2: LLM turns raw text into clean DTOs)
            List<ExtractedTransactionDto> extractedTransactions =
                    statementParserService.extractTransactions(rawText, userId);

            // 4. Final Processing and Persistence (Phase 3: Categorization and Saving)
            // This is assumed to be a method in TransactionService that handles the DTOs
            List<String> errors = transactionService.savePdfTransactions(
                    userId,
                    fileId,
                    extractedTransactions
            );

            // 5. Update File Status to SUCCESS
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

            // 6. Build SUCCESS RESPONSE
            return FileUploadResponse.builder()
                    .success(true)
                    .message("PDF file processed successfully.")
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .processedRows(extractedTransactions.size())
                    .successfulTransactions(extractedTransactions.size() - errors.size())
                    .failedTransactions(errors.size())
                    .errors(errors)
                    .processedAt(java.time.Instant.now())
                    .build();


        } catch (IOException e) {
            // Handle Extraction Failure
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("PDF extraction failed.", e);
        } catch (Exception e) {
            // Handle LLM Parsing or Transaction Saving Failure
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("Transaction parsing/saving failed: " + e.getMessage(), e);
        }
    }
}

