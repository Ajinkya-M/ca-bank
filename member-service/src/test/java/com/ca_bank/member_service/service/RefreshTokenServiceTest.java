package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.TokenException;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.RefreshToken;
import com.ca_bank.member_service.models.enums.MemberStatus;
import com.ca_bank.member_service.repositories.RefreshTokenRepository;
import com.ca_bank.member_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RefreshTokenService.
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private RefreshTokenService refreshTokenService;
    
    private Member testMember;
    private RefreshToken refreshToken;
    
    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .status(MemberStatus.ACTIVE)
                .build();
        
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-123")
                .member(testMember)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
    }
    
    @Test
    @DisplayName("Should create refresh token successfully")
    void createRefreshToken_ShouldReturnToken() {
        // Given
        when(jwtUtil.generateRefreshToken()).thenReturn("new-token-uuid");
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        
        // When
        RefreshToken result = refreshTokenService.createRefreshToken(testMember);
        
        // Then
        assertThat(result).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
    
    @Test
    @DisplayName("Should validate valid token")
    void validateRefreshToken_WithValidToken_ShouldReturnToken() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123"))
                .thenReturn(Optional.of(refreshToken));
        
        // When
        RefreshToken result = refreshTokenService.validateRefreshToken("refresh-token-123");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("refresh-token-123");
    }
    
    @Test
    @DisplayName("Should throw exception for non-existent token")
    void validateRefreshToken_WithNonExistentToken_ShouldThrowException() {
        // Given
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("unknown-token"))
                .isInstanceOf(TokenException.class)
                .hasMessage("Refresh token not found");
    }
    
    @Test
    @DisplayName("Should throw exception for revoked token")
    void validateRefreshToken_WithRevokedToken_ShouldThrowException() {
        // Given
        refreshToken.setRevoked(true);
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        
        // When & Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("refresh-token-123"))
                .isInstanceOf(TokenException.class)
                .hasMessage("Refresh token has been revoked");
    }
    
    @Test
    @DisplayName("Should throw exception for expired token")
    void validateRefreshToken_WithExpiredToken_ShouldThrowException() {
        // Given
        refreshToken.setExpiryDate(Instant.now().minusSeconds(100));
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        
        // When & Then
        assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("refresh-token-123"))
                .isInstanceOf(TokenException.class)
                .hasMessage("Refresh token has expired");
    }
    
    @Test
    @DisplayName("Should revoke token successfully")
    void revokeRefreshToken_ShouldSetRevokedTrue() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123"))
                .thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        
        // When
        refreshTokenService.revokeRefreshToken("refresh-token-123");
        
        // Then
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshToken);
    }
    
    @Test
    @DisplayName("Should throw exception when revoking non-existent token")
    void revokeRefreshToken_WithNonExistentToken_ShouldThrowException() {
        // Given
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> refreshTokenService.revokeRefreshToken("unknown-token"))
                .isInstanceOf(TokenException.class)
                .hasMessage("Refresh token not found");
    }
    
    @Test
    @DisplayName("Should delete tokens by member")
    void deleteByMember_ShouldCallRepository() {
        // When
        refreshTokenService.deleteByMember(testMember);
        
        // Then
        verify(refreshTokenRepository).deleteByMember(testMember);
    }
}
