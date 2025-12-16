package com.team021.financial_nudger.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.domain.Transaction.TransactionType;
import com.team021.financial_nudger.dto.ExtractedFinancialData;
import com.team021.financial_nudger.dto.ManualTransactionRequest;
import com.team021.financial_nudger.exception.ResourceNotFoundException;
import com.team021.financial_nudger.repository.TransactionRepository;
import com.team021.financial_nudger.repository.UserRepository;
import com.team021.financial_nudger.service.llm.CategorizationService;
import com.team021.financial_nudger.service.llm.ClassificationResult;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategorizationService categorizationService;
        private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryService categoryService,
                                                          CategorizationService categorizationService,
                                                          UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.categorizationService = categorizationService;
                this.userRepository = userRepository;
    }

    // =========================================================
    // 1️⃣ MANUAL TRANSACTION (USER INPUT)
    // =========================================================
    public Transaction saveManualTransaction(ManualTransactionRequest request) {

                ensureUserExists(request.userId());

        List<String> validCategories =
                categoryService.getAvailableCategoryNames(request.userId());

        ClassificationResult result = categorizationService.classifyExpense(
                request.userId(),
                request.note(),
                validCategories
        );

        Integer categoryId = categoryService
                .getCategoryByName(result.classifiedCategoryName(), request.userId())
                .getCategoryId();

        Transaction transaction = new Transaction();
        transaction.setUserId(request.userId());
        transaction.setDate(request.date());
        transaction.setAmount(request.amount());
        transaction.setType(TransactionType.DEBIT);
        transaction.setDescription(request.note());
        transaction.setCategoryId(categoryId);
        transaction.setCategoryConfidence(result.confidenceScore());
        transaction.setAiCategorized(true);
        transaction.setUserCategorized(false);

        return transactionRepository.save(transaction);
    }

    // =========================================================
    // 2️⃣ RECEIPT TRANSACTION (GEMINI IMAGE FLOW – KEEP)
    // =========================================================
    @Transactional
    public List<String> saveReceiptTransaction(
            Integer userId,
            Integer fileId,
            ExtractedFinancialData extractedData) {

        List<String> errors = new ArrayList<>();

        try {
            ensureUserExists(userId);
            Integer categoryId = categoryService
                    .getCategoryByName(extractedData.classifiedCategoryName(), userId)
                    .getCategoryId();

            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setFileId(fileId);
            transaction.setDate(extractedData.date());
            transaction.setAmount(extractedData.amount());
            transaction.setType(extractedData.type());
            transaction.setDescription(extractedData.description());
            transaction.setMerchantName(extractedData.merchantName());
            transaction.setCategoryId(categoryId);
            transaction.setCategoryConfidence(extractedData.confidenceScore());
            transaction.setAiCategorized(true);
            transaction.setUserCategorized(false);

            transactionRepository.save(transaction);

        } catch (Exception e) {
            errors.add("Failed to save receipt transaction: " + e.getMessage());
        }

        return errors;
    }

    // =========================================================
    // 3️⃣ BANK STATEMENT LINE (PDF / OCR → fastText)
    // =========================================================
    @Transactional
    public void saveTransactionFromStatementLine(
            Integer userId,
            Integer fileId,
            String statementLine) {

        if (statementLine == null || statementLine.isBlank()) return;

                ensureUserExists(userId);

        List<String> validCategories =
                categoryService.getAvailableCategoryNames(userId);

        ParsedStatement parsed = parseAmountAndType(statementLine);

        String categoryName;
        if (parsed.type() == TransactionType.CREDIT) {
            categoryName = resolveCreditCategory(validCategories);
        } else {
            ClassificationResult result = categorizationService.classifyExpense(
                    userId,
                    statementLine,
                    validCategories
            );
            categoryName = result.classifiedCategoryName();
        }

        Integer categoryId = categoryService
                .getCategoryByName(categoryName, userId)
                .getCategoryId();

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setFileId(fileId);
                transaction.setDate(LocalDate.now());          // Can be improved later
                transaction.setAmount(parsed.amount());        // Parsed from line when possible
                transaction.setType(parsed.type());
        transaction.setDescription(statementLine);
        transaction.setCategoryId(categoryId);
                transaction.setCategoryConfidence(BigDecimal.valueOf(0.75)); // statement heuristic confidence
        transaction.setAiCategorized(true);
        transaction.setUserCategorized(false);

        transactionRepository.save(transaction);
    }

        private void ensureUserExists(Integer userId) {
                if (userId == null || !userRepository.existsById(userId)) {
                        throw new ResourceNotFoundException("User not found: " + userId);
                }
        }

        private ParsedStatement parseAmountAndType(String line) {
                if (line == null) {
                        return new ParsedStatement(BigDecimal.ZERO, TransactionType.DEBIT);
                }

                Pattern pattern = Pattern.compile("([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)\\s*\\((Dr|Cr)\\)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(line);

                BigDecimal amount = BigDecimal.ZERO;
                TransactionType type = TransactionType.DEBIT;

                if (matcher.find()) {
                        String amtStr = matcher.group(1).replace(",", "");
                        try {
                                amount = new BigDecimal(amtStr);
                        } catch (NumberFormatException ignored) { }

                        String drcr = matcher.group(2).toUpperCase();
                        type = drcr.equals("CR") ? TransactionType.CREDIT : TransactionType.DEBIT;
                }

                return new ParsedStatement(amount, type);
        }

        private String resolveCreditCategory(List<String> availableCategories) {
                if (availableCategories == null || availableCategories.isEmpty()) {
                        return "Miscellaneous";
                }

                String target = findCategory(availableCategories, "Transfers");
                if (target != null) return target;

                target = findCategory(availableCategories, "Miscellaneous");
                if (target != null) return target;

                return availableCategories.get(0);
        }

        private String findCategory(List<String> availableCategories, String name) {
                for (String candidate : availableCategories) {
                        if (candidate.equalsIgnoreCase(name)) {
                                return candidate;
                        }
                }
                return null;
        }

        private record ParsedStatement(BigDecimal amount, TransactionType type) { }
}
