package com.team021.financial_nudger.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.domain.Transaction.TransactionType;
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

    public TransactionService(
            TransactionRepository transactionRepository,
            CategoryService categoryService,
            CategorizationService categorizationService,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.categorizationService = categorizationService;
        this.userRepository = userRepository;
    }

    // 1️⃣ MANUAL TRANSACTION
    public Transaction saveManualTransaction(ManualTransactionRequest request) {

        ensureUserExists(request.userId());

        List<String> categories = categoryService.getAvailableCategoryNames(request.userId());
        ClassificationResult result =
                categorizationService.classifyExpense(request.userId(), request.note(), categories);

        Integer categoryId = categoryService
                .getOrCreateUserCategoryByName(result.classifiedCategoryName(), request.userId())
                .getCategoryId();

        Transaction tx = new Transaction();
        tx.setUserId(request.userId());
        tx.setDate(request.date());
        tx.setAmount(request.amount());
        tx.setType(TransactionType.DEBIT);
        tx.setDescription(request.note());
        tx.setCategoryId(categoryId);
        tx.setCategoryConfidence(result.confidenceScore());
        tx.setAiCategorized(true);
        tx.setUserCategorized(false);

        return transactionRepository.save(tx);
    }

    // 2️⃣ PDF STATEMENT LINE
    @Transactional
    public void saveTransactionFromStatementLine(Integer userId, Integer fileId, String line) {

        if (line == null || line.isBlank()) return;
        ensureUserExists(userId);

        List<String> categories = categoryService.getAvailableCategoryNames(userId);
        ParsedStatement parsed = parseAmountAndType(line);

        String categoryName = parsed.type() == TransactionType.CREDIT
                ? "Transfer"
                : categorizationService
                    .classifyExpense(userId, line, categories)
                    .classifiedCategoryName();

        Integer categoryId = categoryService
                .getOrCreateUserCategoryByName(categoryName, userId)
                .getCategoryId();

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setFileId(fileId);
        tx.setDate(LocalDate.now());
        tx.setAmount(parsed.amount());
        tx.setType(parsed.type());
        tx.setDescription(line);
        tx.setCategoryId(categoryId);
        tx.setCategoryConfidence(BigDecimal.valueOf(0.75));
        tx.setAiCategorized(true);
        tx.setUserCategorized(false);

        transactionRepository.save(tx);
    }

    private void ensureUserExists(Integer userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
    }

    private ParsedStatement parseAmountAndType(String line) {
        Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*\\((Dr|Cr)\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(line);

        BigDecimal amount = BigDecimal.ZERO;
        TransactionType type = TransactionType.DEBIT;

        if (m.find()) {
            amount = new BigDecimal(m.group(1));
            type = m.group(2).equalsIgnoreCase("CR") ? TransactionType.CREDIT : TransactionType.DEBIT;
        }

        return new ParsedStatement(amount, type);
    }

    private record ParsedStatement(BigDecimal amount, TransactionType type) {}
}
