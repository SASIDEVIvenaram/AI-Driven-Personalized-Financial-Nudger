package com.team021.financial_nudger.exception;

/**
 * Custom runtime exception for failures during the file ingestion pipeline
 * (e.g., PDF extraction via PDFBox, or structured data extraction via LLM).
 * * Extending RuntimeException ensures that Spring's @Transactional logic
 * will automatically roll back the transaction upon this exception.
 */
public class FileProcessingException extends RuntimeException {

    // Constructor to create the exception with only a message
    public FileProcessingException(String message) {
        super(message);
    }

    // Constructor to wrap an underlying exception (e.g., IOException from PDFBox)
    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}