package com.team021.financial_nudger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.dto.ManualTransactionRequest;
import com.team021.financial_nudger.dto.TransactionFeedbackRequest;
import com.team021.financial_nudger.repository.TransactionRepository;
import com.team021.financial_nudger.service.TransactionFeedbackService;
import com.team021.financial_nudger.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService; // 1. New field for the service
    private final TransactionFeedbackService transactionFeedbackService;

    public TransactionController(TransactionRepository transactionRepository,
                                 TransactionService transactionService,
                                 TransactionFeedbackService transactionFeedbackService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.transactionFeedbackService = transactionFeedbackService;
    }

    // 3. NEW ENDPOINT: Handles manual entry and LLM categorization
    @PostMapping("/manual")
    public ResponseEntity<Transaction> addManualTransaction(@RequestBody @Valid ManualTransactionRequest req) {
        // Delegate the complex logic to the service
        Transaction newTransaction = transactionService.saveManualTransaction(req);
        return ResponseEntity.ok(newTransaction);
    }

    @PostMapping("/{transactionId}/feedback")
    public ResponseEntity<Transaction> submitFeedback(@PathVariable Integer transactionId,
                                                      @RequestBody @Valid TransactionFeedbackRequest request) {
        if (!transactionId.equals(request.transactionId())) {
            throw new IllegalArgumentException("Transaction ID mismatch between path and payload");
        }
        Transaction updated = transactionFeedbackService.applyFeedback(request);
        return ResponseEntity.ok(updated);
    }

    // Existing GET methods (kept for completeness)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Integer userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/uncategorized/{userId}")
    public ResponseEntity<List<Transaction>> getUncategorizedTransactions(@PathVariable Integer userId) {
        // You'll need to make sure the repository method 'findUncategorizedTransactionsByUserId'
        // correctly filters transactions where categoryId is null.
        List<Transaction> transactions = transactionRepository.findUncategorizedTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(@PathVariable Integer categoryId) {
        List<Transaction> transactions = transactionRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(transactions);
    }
}