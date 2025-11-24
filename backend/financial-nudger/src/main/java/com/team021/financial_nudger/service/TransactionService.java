package com.team021.financial_nudger.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.domain.Transaction.TransactionType;
import com.team021.financial_nudger.dto.ExtractedFinancialData;
import com.team021.financial_nudger.dto.ExtractedTransactionDto;
import com.team021.financial_nudger.dto.ManualTransactionRequest;
import com.team021.financial_nudger.repository.TransactionRepository;
import com.team021.financial_nudger.service.llm.CategorizationService;
import com.team021.financial_nudger.service.llm.ClassificationResult;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategorizationService categorizationService;
  
    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                              CategorizationService categorizationService) 
    { 
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.categorizationService = categorizationService;
    }

    /**
     * Handles the full lifecycle of a manual transaction entry, including LLM categorization.
     */
    public Transaction saveManualTransaction(ManualTransactionRequest request) {

        // 1. Get available categories for LLM
        List<String> validCategories = categoryService.getAvailableCategoryNames(request.userId());

        // 2. Call LLM for categorization
        ClassificationResult result = categorizationService.classifyExpense(
                request.userId(),
                request.note(),
                validCategories
        );

        // 3. Find the Category ID based on the LLM's result
        Integer categoryId = categoryService.getCategoryByName(result.classifiedCategoryName(), request.userId())
                .getCategoryId();

        // 4. Create and populate the Transaction entity
        Transaction transaction = new Transaction();
        transaction.setUserId(request.userId());
        transaction.setDate(request.date());
        transaction.setAmount(request.amount());
        transaction.setType(TransactionType.DEBIT); // Assuming manual entry is initially an expense (DEBIT)
        transaction.setDescription(request.note());
        transaction.setCategoryId(categoryId);
        transaction.setCategoryConfidence(result.confidenceScore());
        transaction.setAiCategorized(true);
        transaction.setUserCategorized(false); // AI categorized it, not the user directly

        return transactionRepository.save(transaction);
    }

    /**
     * Handles the saving of a single transaction extracted and categorized by the Multimodal LLM (Receipts).
     * @return A list of errors (will be empty on success).
     */
    @Transactional
    public List<String> saveReceiptTransaction(Integer userId, Integer fileId, ExtractedFinancialData extractedData) {
        List<String> errors = new ArrayList<>();

        try {
            // 1. Map Category Name to ID
            Integer categoryId = categoryService.getCategoryByName(
                    extractedData.classifiedCategoryName(), userId).getCategoryId();

            // 2. Create and Save Transaction
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setFileId(fileId);

            transaction.setDate(extractedData.date());
            transaction.setAmount(extractedData.amount());

            // Set fields directly from the extracted, categorized DTO
            transaction.setType(extractedData.type());
            transaction.setDescription(extractedData.description());
            transaction.setMerchantName(extractedData.merchantName());

            transaction.setCategoryId(categoryId);
            transaction.setCategoryConfidence(extractedData.confidenceScore());
            transaction.setAiCategorized(true);
            transaction.setUserCategorized(false); // AI categorized it

            transactionRepository.save(transaction);

        } catch (Exception e) {
            errors.add("Failed to save receipt transaction: " + e.getMessage());
            // Log the exception
        }

        return errors;
    }

    @Transactional
    public List<String> savePdfTransactions(Integer userId, Integer fileId, List<ExtractedTransactionDto> extractedTransactions) {

        List<String> errors = new ArrayList<>();

        List<String> validCategories = categoryService.getAvailableCategoryNames(userId);

        for (ExtractedTransactionDto dto : extractedTransactions) {
            try {
                ClassificationResult result = categorizationService.classifyExpense(
                        userId,
                        buildPdfTransactionPrompt(dto),
                        validCategories
                );

                Integer categoryId = categoryService.getCategoryByName(result.classifiedCategoryName(), userId).getCategoryId();

                Transaction transaction = new Transaction();
                transaction.setUserId(userId);
                transaction.setDate(dto.date());
                transaction.setAmount(dto.amount());

                // CRITICAL: Map the DTO's TransactionType enum to your Transaction model's enum
                transaction.setType(Transaction.TransactionType.valueOf(dto.type().name()));

                transaction.setDescription(dto.description());
                transaction.setFileId(fileId);
                transaction.setCategoryId(categoryId);
                transaction.setCategoryConfidence(result.confidenceScore());
                transaction.setAiCategorized(true);
                transaction.setUserCategorized(false);

                transactionRepository.save(transaction);

            } catch (Exception e) {
                // Collect errors
                errors.add("Failed to process transaction: " + dto.description() + ". Error: " + e.getMessage());
                // Log the exception
            }
        }

        return errors;
    }

    private String buildPdfTransactionPrompt(ExtractedTransactionDto dto) {
        return """
                Description: %s
                Amount: %s
                Type: %s
                """.formatted(
                dto.description(),
                dto.amount(),
                dto.type().name()
        );
    }
}