package com.ca_bank.member_service.models.dto;

import com.ca_bank.member_service.models.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Data Transfer Object for money transfer responses.
 * Contains the final receipt and updated balance information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponseDTO {
    
    /**
     * Internal database ID of the transaction.
     */
    private String transactionId;

    /**
     * User-friendly reference string for the transfer.
     */
    private String reference;

    /**
     * ID of the account money was sent from.
     */
    private Long fromAccountId;

    /**
     * ID of the account money was sent to.
     */
    private Long toAccountId;

    /**
     * The confirmed amount of the transfer.
     */
    private BigDecimal amount;

    /**
     * Optional note describing the purpose.
     */
    private String description;

    /**
     * Result of the transaction (e.g., COMPLETED, FAILED).
     */
    private TransactionStatus status;

    /**
     * Timestamp of when the transaction was finalized.
     */
    private Date timestamp;

    /**
     * Updated balance of the sender after the transfer.
     */
    private BigDecimal senderNewBalance;

    /**
     * Updated balance of the recipient after the transfer.
     */
    private BigDecimal recipientNewBalance;

    /**
     * Human-readable status message for the UI.
     */
    private String message;
}
