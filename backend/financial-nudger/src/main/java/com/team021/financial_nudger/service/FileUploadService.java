package com.team021.financial_nudger.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.domain.IngestedFile;
import com.team021.financial_nudger.dto.ExtractedFinancialData;
import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.exception.FileProcessingException;
import com.team021.financial_nudger.repository.IngestedFileRepository;
import com.team021.financial_nudger.service.llm.LLMExtractionService;
import com.team021.financial_nudger.service.llm.StatementParserService;
import com.team021.financial_nudger.service.pdf.PdfExtractionService;

import net.sourceforge.tess4j.Tesseract;

@Service
public class FileUploadService {

    private final IngestedFileRepository ingestedFileRepository;
    private final TransactionService transactionService;
    private final PdfExtractionService pdfExtractionService;
    private final StatementParserService statementParserService;
    private final CategoryService categoryService;
    private final LLMExtractionService llmExtractionService;

    public FileUploadService(IngestedFileRepository ingestedFileRepository,
                             TransactionService transactionService,
                             PdfExtractionService pdfExtractionService,
                             StatementParserService statementParserService,
                             CategoryService categoryService,
                             LLMExtractionService llmExtractionService) {
        this.ingestedFileRepository = ingestedFileRepository;
        this.transactionService = transactionService;
        this.pdfExtractionService = pdfExtractionService;
        this.statementParserService = statementParserService;
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

        } catch (Exception e) {
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
            // 🔹 Step 1: Try normal text extraction
            String rawText = pdfExtractionService.extractTextFromPdf(file);
            System.out.println("📄 Extracted PDF text length: " + (rawText != null ? rawText.length() : 0));

            // 🔹 Step 2: If empty, fallback to OCR (for scanned PDFs)
            if (rawText == null || rawText.isBlank()) {
                System.out.println("⚠️ PDF text is empty. Using OCR fallback...");
                File tempFile = File.createTempFile("ocr_", ".pdf");
                file.transferTo(tempFile);

                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("C:\\Users\\91944\\AppData\\Local\\Programs\\Tesseract-OCR\\tessdata"); // path to tessdata folder
                tesseract.setLanguage("eng");
                rawText = tesseract.doOCR(tempFile);

                tempFile.deleteOnExit();
                System.out.println("🧠 OCR text extracted length: " + (rawText != null ? rawText.length() : 0));
            }

            if (rawText == null || rawText.isBlank()) {
                throw new FileProcessingException("Bank statement text is empty even after OCR.");
            }

            // 🔹 Step 3: Parse with Gemini
            List<ExtractedTransactionDto> extractedTransactions =
                    statementParserService.extractTransactions(rawText, userId);

            List<String> errors = transactionService.savePdfTransactions(userId, fileId, extractedTransactions);

            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

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
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("PDF extraction failed: " + e.getMessage(), e);
        } catch (Exception e) {
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException("Transaction parsing/saving failed: " + e.getMessage(), e);
        }
    }
}
