package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.AuthenticationException;
import com.ca_bank.member_service.exception.EmailAlreadyExistsException;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.RefreshToken;
import com.ca_bank.member_service.models.dto.*;
import com.ca_bank.member_service.models.enums.MemberStatus;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private RefreshTokenService refreshTokenService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private AuthService authService;
    
    private Member testMember;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
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
        
        registerRequest = RegisterRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .password("SecureP@ss123")
                .phone("+1234567890")
                .build();
        
        loginRequest = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("SecureP@ss123")
                .build();
        
        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-123")
                .member(testMember)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
    }
    
    // ==================== Registration Tests ====================
    
    @Test
    @DisplayName("Should register new member successfully")
    void register_WithValidData_ShouldReturnAuthResponse() {
        // Given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);
        when(jwtUtil.generateAccessToken(any(Member.class))).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(refreshTokenService.createRefreshToken(any(Member.class))).thenReturn(refreshToken);
        
        // When
        AuthResponseDTO response = authService.register(registerRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getMember().getEmail()).isEqualTo("test@example.com");
        
        verify(memberRepository).save(any(Member.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));
        
        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
        
        verify(memberRepository, never()).save(any(Member.class));
    }
    
    // ==================== Login Tests ====================
    
    @Test
    @DisplayName("Should login member with valid credentials")
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));
        when(jwtUtil.generateAccessToken(any(Member.class))).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(refreshTokenService.createRefreshToken(any(Member.class))).thenReturn(refreshToken);
        
        // When
        AuthResponseDTO response = authService.login(loginRequest);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
    }
    
    @Test
    @DisplayName("Should throw exception with invalid credentials")
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        
        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid email or password");
    }
    
    @Test
    @DisplayName("Should throw exception when account is inactive")
    void login_WithInactiveAccount_ShouldThrowException() {
        // Given
        testMember.setStatus(MemberStatus.INACTIVE);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(testMember));
        
        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Account is not active");
    }
    
    // ==================== Refresh Token Tests ====================
    
    @Test
    @DisplayName("Should refresh token successfully")
    void refreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Given
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-123");
        when(refreshTokenService.validateRefreshToken(anyString())).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken(any(Member.class))).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpirationSeconds()).thenReturn(900L);
        
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token("new-refresh-token")
                .member(testMember)
                .build();
        when(refreshTokenService.createRefreshToken(any(Member.class))).thenReturn(newRefreshToken);
        
        // When
        AuthResponseDTO response = authService.refreshToken(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        
        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
    }
    
    // ==================== Logout Tests ====================
    
    @Test
    @DisplayName("Should logout and revoke refresh token")
    void logout_ShouldRevokeRefreshToken() {
        // Given
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-123");
        
        // When
        authService.logout(request);
        
        // Then
        verify(refreshTokenService).revokeRefreshToken("refresh-token-123");
    }
    
    // ==================== Get Member Tests ====================
    
    @Test
    @DisplayName("Should get member by email")
    void getMemberByEmail_ShouldReturnMemberResponse() {
        // Given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));
        
        // When
        MemberResponseDTO response = authService.getMemberByEmail("test@example.com");
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("Should throw exception when member not found")
    void getMemberByEmail_WhenNotFound_ShouldThrowException() {
        // Given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authService.getMemberByEmail("unknown@example.com"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Member not found");
    }
}
