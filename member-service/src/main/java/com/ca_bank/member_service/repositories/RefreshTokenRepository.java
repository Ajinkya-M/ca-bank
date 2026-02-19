package com.ca_bank.member_service.repositories;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity operations.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Find a refresh token by its token string.
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Find all refresh tokens for a member.
     */
    List<RefreshToken> findByMember(Member member);
    
    /**
     * Delete all refresh tokens for a member.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.member = :member")
    void deleteByMember(Member member);
    
    /**
     * Find all valid (non-revoked, non-expired) tokens for a member.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.member = :member AND rt.revoked = false")
    List<RefreshToken> findValidTokensByMember(Member member);
}
