package com.ca_bank.member_service.security;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.enums.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtUtil class.
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        // Initialize JwtUtil with test configuration
        jwtUtil = new JwtUtil(
                "testSecretKeyForJWTSigningThatIsAtLeast32CharactersLong",
                900000L, // 15 minutes
                604800000L // 7 days
        );
        
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("hashedPassword")
                .status(MemberStatus.ACTIVE)
                .build();
    }
    
    @Test
    @DisplayName("Should generate valid access token")
    void generateAccessToken_ShouldReturnValidToken() {
        // When
        String token = jwtUtil.generateAccessToken(testMember);
        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }
    
    @Test
    @DisplayName("Should validate valid token")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateAccessToken(testMember);
        
        // When
        boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertThat(isValid).isTrue();
    }
    
    @Test
    @DisplayName("Should reject invalid token")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should reject malformed token")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "notavalidjwt";
        
        // When
        boolean isValid = jwtUtil.validateToken(malformedToken);
        
        // Then
        assertThat(isValid).isFalse();
    }
    
    @Test
    @DisplayName("Should extract email from token")
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        // Given
        String token = jwtUtil.generateAccessToken(testMember);
        
        // When
        String email = jwtUtil.getEmailFromToken(token);
        
        // Then
        assertThat(email).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("Should extract member ID from token")
    void getMemberIdFromToken_ShouldReturnCorrectId() {
        // Given
        String token = jwtUtil.generateAccessToken(testMember);
        
        // When
        Long memberId = jwtUtil.getMemberIdFromToken(token);
        
        // Then
        assertThat(memberId).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should generate unique refresh tokens")
    void generateRefreshToken_ShouldReturnUniqueTokens() {
        // When
        String token1 = jwtUtil.generateRefreshToken();
        String token2 = jwtUtil.generateRefreshToken();
        
        // Then
        assertThat(token1).isNotNull();
        assertThat(token2).isNotNull();
        assertThat(token1).isNotEqualTo(token2);
    }
    
    @Test
    @DisplayName("Should return correct expiration times")
    void getExpirationTimes_ShouldReturnConfiguredValues() {
        // Then
        assertThat(jwtUtil.getAccessTokenExpirationSeconds()).isEqualTo(900L);
        assertThat(jwtUtil.getRefreshTokenExpirationMs()).isEqualTo(604800000L);
    }
}
