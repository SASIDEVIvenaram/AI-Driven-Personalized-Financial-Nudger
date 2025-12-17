package com.team021.financial_nudger.controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team021.financial_nudger.repository.TransactionCategoryFeedbackRepository;
import com.team021.financial_nudger.repository.TransactionRepository;

@RestController
@RequestMapping("/api/ml")
public class MlDataExportController {

    private final TransactionRepository transactionRepository;
    private final TransactionCategoryFeedbackRepository feedbackRepository;

    public MlDataExportController(TransactionRepository transactionRepository,
                                  TransactionCategoryFeedbackRepository feedbackRepository) {
        this.transactionRepository = transactionRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Export user-corrected transactions for ML model retraining.
     * Returns CSV-like data: description, corrected_category
     */
    @GetMapping("/export/feedback")
        public ResponseEntity<List<FeedbackExportDto>> exportFeedbackData() {
        var feedbacks = feedbackRepository.findAll();

        var exportData = feedbacks.stream()
            .flatMap(feedback -> transactionRepository
                .findById(Objects.requireNonNull(feedback.getTransactionId()))
                .stream()
                .map(transaction -> new FeedbackExportDto(
                    transaction.getDescription(),
                    feedback.getNewCategoryId(),
                    transaction.getMerchantName()
                )))
            .collect(Collectors.toList());

        return ResponseEntity.ok(exportData);
        }

    public record FeedbackExportDto(
            String description,
            Integer categoryId,
            String merchantName
    ) {}
}
