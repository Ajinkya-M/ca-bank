package com.ca_bank.member_service.models.enums;

/**
 * Represents the type of a financial transaction.
 * Used to categorize different money movements.
 */
public enum TransactionType {
    /**
     * Member-to-member money transfer.
     */
    TRANSFER,
    
    /**
     * Money deposited into an account (future feature).
     */
    DEPOSIT,
    
    /**
     * Money withdrawn from an account (future feature).
     */
    WITHDRAWAL
}
