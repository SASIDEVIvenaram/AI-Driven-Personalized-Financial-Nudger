package com.team021.financial_nudger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Use * for brevity

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.dto.ManualTransactionRequest; // Import the new DTO
import com.team021.financial_nudger.repository.TransactionRepository;
import com.team021.financial_nudger.service.TransactionService; // Import the new Service

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService; // 1. New field for the service

    // 2. Update constructor to inject TransactionService
    public TransactionController(TransactionRepository transactionRepository, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    // 3. NEW ENDPOINT: Handles manual entry and LLM categorization
    @PostMapping("/manual")
    public ResponseEntity<Transaction> addManualTransaction(@RequestBody @Valid ManualTransactionRequest req) {
        // Delegate the complex logic to the service
        Transaction newTransaction = transactionService.saveManualTransaction(req);
        return ResponseEntity.ok(newTransaction);
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