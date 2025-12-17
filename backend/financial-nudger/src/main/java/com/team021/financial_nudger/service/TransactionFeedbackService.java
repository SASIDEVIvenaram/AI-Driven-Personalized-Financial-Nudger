package com.team021.financial_nudger.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.domain.TransactionCategoryFeedback;
import com.team021.financial_nudger.dto.TransactionFeedbackRequest;
import com.team021.financial_nudger.exception.ResourceNotFoundException;
import com.team021.financial_nudger.repository.TransactionCategoryFeedbackRepository;
import com.team021.financial_nudger.repository.TransactionRepository;

@Service
public class TransactionFeedbackService {

    private final TransactionRepository transactionRepository;
    private final TransactionCategoryFeedbackRepository feedbackRepository;
    private final CategoryService categoryService;

    public TransactionFeedbackService(TransactionRepository transactionRepository,
                                      TransactionCategoryFeedbackRepository feedbackRepository,
                                      CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.feedbackRepository = feedbackRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public Transaction applyFeedback(TransactionFeedbackRequest request) {
        Transaction transaction = transactionRepository.findById(Objects.requireNonNull(request.transactionId()))
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + request.transactionId()));

        if (!transaction.getUserId().equals(request.userId())) {
            throw new IllegalArgumentException("Transaction does not belong to this user");
        }

        Integer newCategoryId = categoryService
                .getOrCreateUserCategoryByName(request.correctedCategoryName(), request.userId())
                .getCategoryId();
        Integer oldCategoryId = transaction.getCategoryId();

        transaction.setCategoryId(newCategoryId);
        transaction.setUserCategorized(true);
        transaction.setAiCategorized(false);
        transaction.setCategoryConfidence(null);
        Transaction saved = transactionRepository.save(transaction);

        TransactionCategoryFeedback feedback = new TransactionCategoryFeedback(
                transaction.getTransactionId(),
                request.userId(),
                oldCategoryId,
                newCategoryId
        );
        feedbackRepository.save(feedback);

        return saved;
    }
}

