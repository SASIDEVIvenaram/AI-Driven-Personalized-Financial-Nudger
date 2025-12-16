package com.team021.financial_nudger.service.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for extracting raw text from a PDF using PDFBox.
 * If PDFBox fails (scanned/corrupted PDF), it returns an empty string
 * so that OCR fallback can be applied by the caller.
 */
@Service
public class PdfExtractionService {

    /**
     * Attempts to extract text from a PDF.
     * Returns empty string if PDFBox cannot extract text,
     * allowing OCR fallback to run.
     */
    public String extractTextFromPdf(MultipartFile pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);

        } catch (Exception e) {
            // IMPORTANT: Do NOT throw exception here
            // Let OCR fallback handle scanned or broken PDFs
            System.out.println(
                    "⚠️ PDFBox text extraction failed. Falling back to OCR. Reason: " + e.getMessage()
            );
            return "";
        }
    }
}
