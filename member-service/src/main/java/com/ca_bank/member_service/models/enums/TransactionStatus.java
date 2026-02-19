package com.ca_bank.member_service.models.enums;

/**
 * Represents the status of a money transfer transaction.
 * Used for audit trail and tracking transaction lifecycle.
 */
public enum TransactionStatus {
    /**
     * Transaction initiated but not yet completed.
     */
    PENDING,
    
    /**
     * Transaction successfully processed.
     */
    COMPLETED,
    
    /**
     * Transaction failed due to validation error, insufficient funds, etc.
     */
    FAILED,
    
    /**
     * Transaction was reversed (refund/dispute resolution).
     */
    REVERSED
}
