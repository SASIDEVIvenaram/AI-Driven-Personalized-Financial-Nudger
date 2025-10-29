package com.team021.financial_nudger.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.team021.financial_nudger.domain.IngestedRow;
import com.team021.financial_nudger.domain.Transaction;

@Service
public class CsvParsingService {

    // Common date formats in bank statements
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy")
    );

    // Patterns to detect transaction type
    private static final Pattern DEBIT_PATTERNS = Pattern.compile(
        "(?i)(debit|dr|withdrawal|payment|purchase|transfer.*out)"
    );
    private static final Pattern CREDIT_PATTERNS = Pattern.compile(
        "(?i)(credit|cr|deposit|salary|refund|transfer.*in)"
    );

    public Transaction parseRowToTransaction(IngestedRow row, Integer userId) {
        try {
            String rawData = row.getRawData();
            String[] columns = parseCsvLine(rawData);
            
            if (columns.length < 3) {
                throw new IllegalArgumentException("Insufficient columns in CSV row");
            }

            // Extract basic transaction data
            LocalDate date = parseDate(columns[0]);
            BigDecimal amount = parseAmount(columns[1]);
            Transaction.TransactionType type = determineTransactionType(columns[2], amount);
            String description = extractDescription(columns);
            String channel = determineChannel(description);
            String merchantName = extractMerchantName(description);

            // Create transaction
            Transaction transaction = new Transaction(
                userId,
                date,
                amount,
                type,
                description,
                channel,
                "INR"
            );
            
            transaction.setMerchantName(merchantName);
            transaction.setIsAiCategorized(false);
            transaction.setIsUserCategorized(false);

            return transaction;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse row " + row.getRowNumber() + ": " + e.getMessage(), e);
        }
    }

    private String[] parseCsvLine(String line) {
        // Simple CSV parsing - handles basic cases
        // For production, consider using a proper CSV library like OpenCSV
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        columns.add(current.toString().trim());
        
        return columns.toArray(new String[columns.size()]);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date is required");
        }

        dateStr = dateStr.trim();
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }

    private BigDecimal parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }

        // Remove currency symbols and clean the string
        String cleaned = amountStr.replaceAll("[₹$€£,]", "").trim();
        
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse amount: " + amountStr);
        }
    }

    private Transaction.TransactionType determineTransactionType(String description, BigDecimal amount) {
        if (description == null) {
            return amount.compareTo(BigDecimal.ZERO) < 0 ? Transaction.TransactionType.DEBIT : Transaction.TransactionType.CREDIT;
        }

        String desc = description.toLowerCase();
        
        if (DEBIT_PATTERNS.matcher(desc).find()) {
            return Transaction.TransactionType.DEBIT;
        } else if (CREDIT_PATTERNS.matcher(desc).find()) {
            return Transaction.TransactionType.CREDIT;
        } else {
            // Default based on amount
            return amount.compareTo(BigDecimal.ZERO) < 0 ? Transaction.TransactionType.DEBIT : Transaction.TransactionType.CREDIT;
        }
    }

    private String extractDescription(String[] columns) {
        // Description is usually in the 3rd column or later
        if (columns.length > 2) {
            return columns[2];
        }
        return "";
    }

    private String determineChannel(String description) {
        if (description == null) return "CSV";
        
        String desc = description.toLowerCase();
        
        if (desc.contains("upi")) return "UPI";
        if (desc.contains("pos") || desc.contains("card")) return "CARD";
        if (desc.contains("neft")) return "NEFT";
        if (desc.contains("imps")) return "IMPS";
        if (desc.contains("wallet")) return "WALLET";
        
        return "CSV";
    }

    private String extractMerchantName(String description) {
        if (description == null) return null;
        
        // Simple merchant extraction - can be enhanced with regex patterns
        String desc = description.toLowerCase();
        
        // Common merchant patterns
        if (desc.contains("swiggy")) return "Swiggy";
        if (desc.contains("zomato")) return "Zomato";
        if (desc.contains("uber")) return "Uber";
        if (desc.contains("ola")) return "Ola";
        if (desc.contains("netflix")) return "Netflix";
        if (desc.contains("amazon")) return "Amazon";
        if (desc.contains("flipkart")) return "Flipkart";
        
        // Extract from UPI patterns
        if (desc.contains("upi")) {
            String[] parts = desc.split(" ");
            for (String part : parts) {
                if (part.contains("@")) {
                    return part.split("@")[0];
                }
            }
        }
        
        return null;
    }
}
