package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.TokenException;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.RefreshToken;
import com.ca_bank.member_service.repositories.RefreshTokenRepository;
import com.ca_bank.member_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for managing refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    
    /**
     * Create a new refresh token for the given member.
     *
     * @param member the member to create token for
     * @return the created RefreshToken entity
     */
    @Transactional
    public RefreshToken createRefreshToken(Member member) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtUtil.generateRefreshToken())
                .member(member)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();
        
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        logger.info("Created refresh token for member: {}", member.getEmail());
        return savedToken;
    }
    
    /**
     * Validate a refresh token and return the associated entity.
     *
     * @param token refresh token string
     * @return RefreshToken entity if valid
     * @throws TokenException if token is invalid, expired, or revoked
     */
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));
        
        if (refreshToken.isRevoked()) {
            throw new TokenException("Refresh token has been revoked");
        }
        
        if (refreshToken.isExpired()) {
            throw new TokenException("Refresh token has expired");
        }
        
        return refreshToken;
    }
    
    /**
     * Revoke a refresh token.
     *
     * @param token refresh token string to revoke
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found"));
        
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info("Revoked refresh token for member: {}", refreshToken.getMember().getEmail());
    }
    
    /**
     * Delete all refresh tokens for a member.
     *
     * @param member the member whose tokens should be deleted
     */
    @Transactional
    public void deleteByMember(Member member) {
        refreshTokenRepository.deleteByMember(member);
        logger.info("Deleted all refresh tokens for member: {}", member.getEmail());
    }
}
