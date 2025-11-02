package com.team021.financial_nudger.service.llm;
import com.team021.financial_nudger.domain.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.team021.financial_nudger.dto.ExtractedFinancialData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LLMExtractionService {

    // Inject the Gemini API client here

    /**
     * Sends the receipt image/PDF to the LLM for structured data extraction and categorization.
     */
    public ExtractedFinancialData extractAndCategorizeReceipt(
            MultipartFile receiptFile,
            Integer userId,
            List<String> validCategories) {

        // 1. Convert MultipartFile to a format usable by the LLM (e.g., base64 or byte array)

        // 2. Build the prompt (e.g., "Analyze this image. The user's categories are [X, Y, Z].
        //    Extract date, amount, merchant, and classify the expense into one of the categories.")

        // 3. Call the Gemini API (multimodal request)

        // 4. Parse the structured JSON output from the LLM into the ExtractedFinancialData DTO

        // --- Mocking the LLM response for now ---
        return new ExtractedFinancialData(
                LocalDate.now(),
                new BigDecimal("45.50"),
                "Starbucks Coffee",
                "Payment for a latte",
                "Food & Dining", // The category name classified by the LLM
                new BigDecimal("0.95"),
                Transaction.TransactionType.DEBIT
        );
    }
}