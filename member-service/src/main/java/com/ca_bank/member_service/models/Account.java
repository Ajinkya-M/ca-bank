package com.ca_bank.member_service.models;

import com.ca_bank.member_service.models.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity representing a bank account associated with a member.
 * Each member can have exactly one account.
 * Uses @Version for optimistic locking to handle concurrent transfers.
 */
@Entity
@Table(name = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The member who owns this account.
     * One-to-one relationship ensures each member has exactly one account.
     */
    @OneToOne
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;

    /**
     * Unique account number (e.g., ACC-1-A8B3C2D1).
     */
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    /**
     * Current account balance.
     * Uses BigDecimal for precise monetary calculations.
     */
    @Builder.Default
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Currency code (default: USD).
     */
    @Builder.Default
    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    /**
     * Account status controlling transaction eligibility.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Maximum amount that can be transferred per day.
     */
    @Builder.Default
    @Column(name = "daily_transfer_limit", precision = 15, scale = 2)
    private BigDecimal dailyTransferLimit = new BigDecimal("50000.00");

    /**
     * Version field for optimistic locking.
     * Prevents concurrent update conflicts during transfers.
     */
    @Version
    private Long version;

    /**
     * Timestamp when the account was created.
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Date createdAt;

    /**
     * Timestamp when the account was last updated.
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;
}
