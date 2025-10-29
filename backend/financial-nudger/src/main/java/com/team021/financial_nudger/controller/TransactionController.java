package com.team021.financial_nudger.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team021.financial_nudger.domain.Transaction;
import com.team021.financial_nudger.repository.TransactionRepository;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Integer userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/uncategorized/{userId}")
    public ResponseEntity<List<Transaction>> getUncategorizedTransactions(@PathVariable Integer userId) {
        List<Transaction> transactions = transactionRepository.findUncategorizedTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCategory(@PathVariable Integer categoryId) {
        List<Transaction> transactions = transactionRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(transactions);
    }
}
