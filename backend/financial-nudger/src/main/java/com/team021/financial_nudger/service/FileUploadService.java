package com.team021.financial_nudger.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.team021.financial_nudger.domain.IngestedFile;
import com.team021.financial_nudger.dto.FileUploadResponse;
import com.team021.financial_nudger.exception.FileProcessingException;
import com.team021.financial_nudger.repository.IngestedFileRepository;
import com.team021.financial_nudger.service.pdf.PdfExtractionService;

@Service
public class FileUploadService {

    private final IngestedFileRepository ingestedFileRepository;
    private final TransactionService transactionService;
    private final PdfExtractionService pdfExtractionService;

    public FileUploadService(
            IngestedFileRepository repo,
            TransactionService txService,
            PdfExtractionService pdfService
    ) {
        this.ingestedFileRepository = repo;
        this.transactionService = txService;
        this.pdfExtractionService = pdfService;
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

        int success = 0;
        List<String> errors = new ArrayList<>();

        try {
            String text = pdfExtractionService.extractTextFromPdf(file);
            String[] raw = (text == null ? "" : text).split("\r?\n");

            List<String> lines = new ArrayList<>();
            for (String r : raw) {
                if (r != null) {
                    String t = r.trim();
                    if (!t.isEmpty()) lines.add(t);
                }
            }

            if (lines.isEmpty()) {
                ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
                ingestedFile.setErrorMessage("No extractable text found in PDF");
                ingestedFileRepository.save(ingestedFile);
                return FileUploadResponse.builder()
                        .success(false)
                        .message("No extractable text found in PDF")
                        .fileId(ingestedFile.getFileId())
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .processedRows(0)
                        .successfulTransactions(0)
                        .failedTransactions(0)
                        .errors(errors)
                        .build();
            }

            for (String line : lines) {
                try {
                    transactionService.saveTransactionFromStatementLine(
                            userId, ingestedFile.getFileId(), line);
                    success++;
                } catch (Exception e) {
                    errors.add(line);
                }
            }

            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
            ingestedFileRepository.save(ingestedFile);

            return FileUploadResponse.builder()
                    .success(true)
                    .message("Statement processed")
                    .fileId(ingestedFile.getFileId())
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .processedRows(lines.size())
                    .successfulTransactions(success)
                    .failedTransactions(lines.size() - success)
                    .errors(errors)
                    .build();

        } catch (Exception e) {
            ingestedFile.setUploadStatus(IngestedFile.UploadStatus.FAILED);
            ingestedFile.setErrorMessage(e.getMessage());
            ingestedFileRepository.save(ingestedFile);
            throw new FileProcessingException(e.getMessage(), e);
        }
    }

    @Transactional
    public FileUploadResponse processReceiptFile(MultipartFile file, Integer userId) {
        IngestedFile ingestedFile = new IngestedFile();
        ingestedFile.setUserId(userId);
        ingestedFile.setFileName(file.getOriginalFilename());
        ingestedFile.setFileSize(file.getSize());
        ingestedFile.setMimeType(file.getContentType());
        ingestedFile.setFileType(IngestedFile.FileType.RECEIPT);
        ingestedFile.setUploadStatus(IngestedFile.UploadStatus.COMPLETED);
        ingestedFile = ingestedFileRepository.save(ingestedFile);

        return FileUploadResponse.builder()
                .success(true)
                .message("Receipt accepted (parsing not implemented)")
                .fileId(ingestedFile.getFileId())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .processedRows(0)
                .successfulTransactions(0)
                .failedTransactions(0)
                .build();
    }
}

