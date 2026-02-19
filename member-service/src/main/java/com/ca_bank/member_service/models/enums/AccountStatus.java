package com.ca_bank.member_service.models.enums;

/**
 * Represents the status of a bank account.
 * Used to control whether an account can participate in transfers.
 */
public enum AccountStatus {
    /**
     * Account is operational and can send/receive transfers.
     */
    ACTIVE,
    
    /**
     * Account is temporarily locked and cannot transact.
     */
    FROZEN,
    
    /**
     * Account is permanently closed.
     */
    CLOSED
}
