package com.ca_bank.member_service.models;

import com.ca_bank.member_service.models.enums.TransactionStatus;
import com.ca_bank.member_service.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity representing a financial transaction (money transfer).
 * Maintains complete audit trail including balance snapshots.
 * Supports idempotency to prevent duplicate transactions.
 */
@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_from_account", columnList = "from_account_id"),
           @Index(name = "idx_to_account", columnList = "to_account_id"),
           @Index(name = "idx_reference", columnList = "reference", unique = true)
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique transaction reference (e.g., REF-ABC123XYZ).
     */
    @Column(name = "reference", unique = true, nullable = false)
    private String reference;

    /**
     * Account that sent the money.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    /**
     * Account that received the money.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    /**
     * Amount transferred.
     * Uses BigDecimal for precise monetary calculations.
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Optional description/note for the transfer.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Transaction status (PENDING, COMPLETED, FAILED, REVERSED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    /**
     * Transaction type (TRANSFER, DEPOSIT, WITHDRAWAL).
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type = TransactionType.TRANSFER;

    /**
     * Client-provided idempotency key to prevent duplicate transactions.
     * If the same key is sent twice, the second request returns the original result.
     */
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    /**
     * Sender's balance before the transaction (audit trail).
     */
    @Column(name = "sender_balance_before", precision = 15, scale = 2)
    private BigDecimal senderBalanceBefore;

    /**
     * Sender's balance after the transaction (audit trail).
     */
    @Column(name = "sender_balance_after", precision = 15, scale = 2)
    private BigDecimal senderBalanceAfter;

    /**
     * Recipient's balance before the transaction (audit trail).
     */
    @Column(name = "recipient_balance_before", precision = 15, scale = 2)
    private BigDecimal recipientBalanceBefore;

    /**
     * Recipient's balance after the transaction (audit trail).
     */
    @Column(name = "recipient_balance_after", precision = 15, scale = 2)
    private BigDecimal recipientBalanceAfter;

    /**
     * Error message if transaction failed.
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Timestamp when transaction was created.
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Date createdAt;

    /**
     * Timestamp when transaction was completed (success or failure).
     */
    @Column(name = "completed_at")
    private Date completedAt;
}
