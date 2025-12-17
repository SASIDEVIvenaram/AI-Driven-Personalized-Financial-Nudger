package com.team021.financial_nudger.service.pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * Service responsible for extracting raw text from a PDF using PDFBox.
 * If PDFBox fails (scanned/corrupted PDF), falls back to OCR via Tesseract.
 */
@Service
public class PdfExtractionService {

    private final Tesseract tesseract;
    private final boolean ocrEnabled;

    public PdfExtractionService(@Value("${tesseract.datapath:}") String configuredPath) {
        Tesseract temp = null;
        boolean enabled = false;
        String resolved = resolveTessdataPath(configuredPath);
        try {
            if (resolved != null) {
                temp = new Tesseract();
                temp.setDatapath(resolved);
                temp.setLanguage("eng");
                enabled = true;
                File dp = new File(resolved);
                System.out.println("✅ Tesseract OCR initialized. Datapath exists=" + dp.exists() + " → " + resolved);
                if (!new File(dp, "eng.traineddata").exists()) {
                    System.out.println("⚠️  eng.traineddata missing. Place it under: " + dp.getAbsolutePath());
                }
            } else {
                System.out.println("⚠️  No valid tessdata path found. OCR disabled. Set tesseract.datapath or install Tesseract.");
            }
        } catch (Exception e) {
            System.out.println("⚠️  Tesseract OCR not available (install from https://github.com/UB-Mannheim/tesseract/wiki): " + e.getMessage());
        }
        this.tesseract = temp;
        this.ocrEnabled = enabled;
    }

    private String resolveTessdataPath(String configured) {
        try {
            List<String> candidates = new ArrayList<>();
            if (configured != null && !configured.isBlank()) {
                candidates.add(configured);
            }
            String env = System.getenv("TESSDATA_PREFIX");
            if (env != null && !env.isBlank()) {
                if (env.toLowerCase().endsWith("tessdata") || env.endsWith("/") || env.endsWith("\\")) {
                    candidates.add(env);
                } else {
                    candidates.add(env + (env.contains("/") ? "/tessdata" : "\\tessdata"));
                }
            }
            candidates.add("C:/Program Files/Tesseract-OCR/tessdata");
            candidates.add("C:/Program Files (x86)/Tesseract-OCR/tessdata");
            String userHome = System.getProperty("user.home");
            candidates.add(userHome + "/AppData/Local/Programs/Tesseract-OCR/tessdata");
            // Project-local override: place a tessdata folder at repo root
            candidates.add(new File("").getAbsolutePath() + "/tessdata");

            for (String c : candidates) {
                try {
                    if (c == null || c.isBlank()) continue;
                    File f = new File(c);
                    if (f.exists() && f.isDirectory()) {
                        return f.getAbsolutePath();
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Attempts to extract text from a PDF.
     * First tries PDFBox; if that fails or returns empty, uses OCR via Tesseract.
     */
    public String extractTextFromPdf(MultipartFile pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // If PDFBox extracted text, use it
            if (text != null && !text.trim().isEmpty()) {
                return text;
            }

            // Fallback to OCR for scanned PDFs
            System.out.println("⚠️ PDFBox returned no text. Attempting OCR fallback...");
            try {
                return extractTextViaOcr(document);
            } catch (Throwable t) {
                System.out.println("❌ OCR crashed: " + t);
                return "";
            }

        } catch (Exception e) {
            System.out.println(
                    "⚠️ PDFBox failed: " + e.getMessage() + ". Attempting OCR fallback..."
            );
            try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
                try {
                    return extractTextViaOcr(document);
                } catch (Throwable t) {
                    System.out.println("❌ OCR crashed: " + t);
                    return "";
                }
            } catch (IOException ocrError) {
                System.out.println("⚠️ OCR also failed: " + ocrError.getMessage());
                return "";
            }
        }
    }

    /**
     * Extract text from PDF pages using OCR (Tesseract).
     * Converts each PDF page to an image and runs OCR on it.
     * Returns empty string if OCR is not available.
     */
    private String extractTextViaOcr(PDDocument document) {
        if (!ocrEnabled || tesseract == null) {
            System.out.println("⚠️  OCR is not available. Install Tesseract-OCR from: https://github.com/UB-Mannheim/tesseract/wiki");
            return "";
        }

        StringBuilder fullText = new StringBuilder();
        try {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            System.out.println("🔍 Running OCR on " + pageCount + " page(s)...");

            int pages = Math.min(pageCount, 5);
            int[] dpis = new int[] {150, 200, 300};

            for (int i = 0; i < pages; i++) {
                String pageBest = "";
                for (int dpi : dpis) {
                    try {
                        BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                        String text = tesseract.doOCR(image);
                        if (text != null && text.trim().length() > pageBest.length()) {
                            pageBest = text.trim();
                        }
                        if (pageBest.length() > 20) break; // good enough
                    } catch (TesseractException te) {
                        System.out.println("❌ OCR error on page " + i + " @" + dpi + "dpi: " + te.getMessage());
                    }
                }
                fullText.append(pageBest).append("\n");
            }

            String result = fullText.toString().trim();
            System.out.println("✅ OCR extracted " + result.length() + " characters");
            return result;

        } catch (IOException e) {
            System.out.println("❌ OCR rendering failed: " + e.getMessage());
            return "";
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            System.out.println("❌ Tesseract native library issue: " + e.getMessage());
            return "";
        } catch (Throwable t) {
            System.out.println("❌ OCR crashed: " + t);
            return "";
        }
    }
}
