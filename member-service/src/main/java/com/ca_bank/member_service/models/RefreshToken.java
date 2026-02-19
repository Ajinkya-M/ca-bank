package com.ca_bank.member_service.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;

/**
 * Entity representing a refresh token for JWT authentication.
 * Used for generating new access tokens without requiring re-authentication.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique refresh token string.
     */
    @Column(name = "token", unique = true, nullable = false)
    private String token;

    /**
     * The member this refresh token belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Expiration time of this refresh token.
     */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /**
     * Whether this token has been revoked (e.g., on logout).
     */
    @Builder.Default
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    /**
     * Timestamp when the token was created.
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private Date createdAt;

    /**
     * Check if this refresh token is expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
