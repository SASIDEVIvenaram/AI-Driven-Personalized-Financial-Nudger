package com.team021.financial_nudger.service.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service responsible for physically extracting all text content from an uploaded PDF file.
 * This raw text is then passed to the LLM for structured parsing.
 */
@Service
public class PdfExtractionService {

    /**
     * Extracts all raw text content from a PDF file using Apache PDFBox.
     * * @param pdfFile The uploaded PDF file (MultipartFile).
     * @return A single String containing all text extracted from the PDF.
     * @throws IOException If the file cannot be read or PDFBox fails to process it.
     */
    public String extractTextFromPdf(MultipartFile pdfFile) throws IOException {
        
        // Use a try-with-resources block to ensure the PDDocument is closed, preventing memory leaks
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            
            // PDFTextStripper is the core class for sequential text extraction
            PDFTextStripper stripper = new PDFTextStripper();
            
            // stripper.setSortByPosition(true); // Optional: can sometimes help with table data
            
            // Extract the text from the entire document
            String text = stripper.getText(document);
            return text;
            
        } catch (IOException e) {
            // Re-throw with a descriptive message to aid in debugging file-related issues
            throw new IOException("Failed to extract text from PDF using PDFBox. " + e.getMessage(), e);
        }
    }
}