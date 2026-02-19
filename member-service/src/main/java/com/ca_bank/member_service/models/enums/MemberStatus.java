package com.ca_bank.member_service.models.enums;

/**
 * Enum representing the status of a bank member account.
 */
public enum MemberStatus {
    /**
     * Member account is active and can perform all operations.
     */
    ACTIVE,
    
    /**
     * Member account is inactive (voluntary deactivation).
     */
    INACTIVE,
    
    /**
     * Member account is suspended due to policy violation or security concerns.
     */
    SUSPENDED,
    
    /**
     * Member has registered but not yet verified their email/identity.
     */
    PENDING_VERIFICATION
}
