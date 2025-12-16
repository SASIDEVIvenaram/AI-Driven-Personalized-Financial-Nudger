package com.team021.financial_nudger.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.domain.IngestedFile;
import com.team021.financial_nudger.dto.ExtractedFinancialData;
import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.exception.FileProcessingException;
import com.team021.financial_nudger.repository.IngestedFileRepository;
import com.team021.financial_nudger.service.llm.LLMExtractionService;
import com.team021.financial_nudger.service.pdf.PdfExtractionService;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class FileUploadService {

    private final IngestedFileRepository ingestedFileRepository;
    private final TransactionService transactionService;
    private final PdfExtractionService pdfExtractionService;

    private final CategoryService categoryService;
    private final LLMExtractionService llmExtractionService;

    public FileUploadService(IngestedFileRepository ingestedFileRepository,
                             TransactionService transactionService,
                             PdfExtractionService pdfExtractionService,
                             CategoryService categoryService,
                             LLMExtractionService llmExtractionService) {
        this.ingestedFileRepository = ingestedFileRepository;
        this.transactionService = transactionService;
        this.pdfExtractionService = pdfExtractionService;
        this.categoryService = categoryService;
        this.llmExtractionService = llmExtractionService;
    }

    @Transactional
    public FileUploadResponse processReceiptFile(MultipartFile file, Integer userId) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("No file provided or file is empty.");
        }
        if (userId == null) {
            throw new FileProcessingException("User ID is required for file processing.");
        }

        IngestedFile ingestedFile = new IngestedFile();
        ingestedFile.setUserId(userId);
        ingestedFile.setFileName(file.getOriginalFilename());
        ingestedFile.setFileSize(file.getSize());
        ingestedFile.setMimeType(file.getContentType());
        ingestedFile.setFileType(IngestedFile.FileType.RECEIPT);
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.PROCESSING);
        ingestedFile = ingestedFileRepository.save(ingestedFile);
        Integer fileId = ingestedFile.getFileId();

        try {
            List<String> validCategories = categoryService.getAvailableCategoryNames(userId);
            if (validCategories == null) validCategories = new ArrayList<>();

            List<ExtractedFinancialData> extractedDataList =
                    llmExtractionService.extractAndCategorizeReceipt(file, userId, validCategories);

            if (extractedDataList == null || extractedDataList.isEmpty()) {
                throw new FileProcessingException("Receipt extraction returned no data.");
            }

            List<String> allErrors = new ArrayList<>();
            int successCount = 0;

            for (ExtractedFinancialData data : extractedDataList) {
                List<String> errors = transactionService.saveReceiptTransaction(userId, fileId, data);
                if (errors.isEmpty()) successCount++;
                else allErrors.addAll(errors);
            }

            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

            return FileUploadResponse.builder()
                    .success(true)
                    .message("Receipt processed successfully. " + successCount + " transaction(s) saved.")
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .processedRows(extractedDataList.size())
                    .successfulTransactions(successCount)
                    .failedTransactions(allErrors.size())
                    .errors(allErrors)
                    .processedAt(java.time.Instant.now())
                    .build();

        } catch (RuntimeException e) {
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFile.setErrorMessage(e.getMessage());
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("Receipt processing failed: " + e.getMessage(), e);
        }
    }
    @Transactional
    public FileUploadResponse processPdfFile(MultipartFile file, Integer userId) {

        IngestedFile ingestedFile = new IngestedFile();
        ingestedFile.setUserId(userId);
        ingestedFile.setFileName(file.getOriginalFilename());
        ingestedFile.setFileSize(file.getSize());
        ingestedFile.setMimeType(file.getContentType());
        ingestedFile.setFileType(IngestedFile.FileType.STATEMENT);
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.PROCESSING);
        ingestedFile = ingestedFileRepository.save(ingestedFile);

        Integer fileId = ingestedFile.getFileId();

        try {
            // 1️⃣ Extract text using PDFBox
            String rawText = pdfExtractionService.extractTextFromPdf(file);
            System.out.println("📄 Extracted PDF text length: " + (rawText != null ? rawText.length() : 0));

            // 2️⃣ OCR fallback
            if (rawText == null || rawText.isBlank()) {
                System.out.println("⚠️ PDF text empty, using OCR...");
                File tempFile = null;
                try {
                    tempFile = File.createTempFile("ocr_", ".pdf");
                    file.transferTo(Objects.requireNonNull(tempFile));

                    Tesseract tesseract = new Tesseract();
                    tesseract.setDatapath("C:\\Users\\91944\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata");
                    tesseract.setLanguage("eng");

                    rawText = tesseract.doOCR(tempFile);
                } catch (java.io.IOException | TesseractException ocrEx) {
                    throw new FileProcessingException("OCR processing failed: " + ocrEx.getMessage(), ocrEx);
                } finally {
                    if (tempFile != null) {
                        tempFile.deleteOnExit();
                    }
                }
            }

            if (rawText == null || rawText.isBlank()) {
                throw new FileProcessingException("Statement text empty even after OCR");
            }

            // 3️⃣ Split into lines (simple & reliable)
            String[] lines = rawText.split("\\r?\\n");

            int success = 0;
            List<String> errors = new ArrayList<>();

            for (String line : lines) {
                if (line.length() < 10) continue; // skip noise

                try {
                    transactionService.saveTransactionFromStatementLine(
                            userId,
                            fileId,
                            line
                    );
                    success++;
                } catch (Exception ex) {
                    errors.add("Failed line: " + line);
                }
            }

            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

            return FileUploadResponse.builder()
                    .success(true)
                    .message("PDF processed successfully using offline ML")
                    .fileId(fileId)
                    .fileName(file.getOriginalFilename())
                    .processedRows(lines.length)
                    .successfulTransactions(success)
                    .failedTransactions(errors.size())
                    .errors(errors)
                    .processedAt(java.time.Instant.now())
                    .build();

        } catch (RuntimeException e) {
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFile.setErrorMessage(e.getMessage());
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("PDF processing failed: " + e.getMessage(), e);
        }
    }

}
