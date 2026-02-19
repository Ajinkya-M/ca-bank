package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.AuthenticationException;
import com.ca_bank.member_service.exception.EmailAlreadyExistsException;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.RefreshToken;
import com.ca_bank.member_service.models.dto.*;
import com.ca_bank.member_service.models.enums.MemberStatus;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling authentication operations (register, login, refresh, logout).
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Register a new member.
     *
     * @param request registration request DTO
     * @return authentication response with tokens
     */
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if email already exists
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        
        // Create new member with hashed password
        Member member = Member.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .status(MemberStatus.ACTIVE)
                .build();
        
        Member savedMember = memberRepository.save(member);
        logger.info("New member registered: {}", savedMember.getEmail());
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedMember);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedMember);
        
        return AuthResponseDTO.builder()
                .member(MemberResponseDTO.fromEntity(savedMember))
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationSeconds())
                .build();
    }
    
    /**
     * Authenticate a member and return tokens.
     *
     * @param request login request DTO
     * @return authentication response with tokens
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            if (!authentication.isAuthenticated()) {
                throw new AuthenticationException("Invalid credentials");
            }
        } catch (DisabledException e) {
            throw new AuthenticationException("Account is disabled");
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }
        
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        
        // Check member status
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }
        
        logger.info("Member logged in: {}", member.getEmail());
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(member);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(member);
        
        return AuthResponseDTO.builder()
                .member(MemberResponseDTO.fromEntity(member))
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationSeconds())
                .build();
    }
    
    /**
     * Refresh access token using a valid refresh token.
     *
     * @param refreshTokenRequest refresh token request DTO
     * @return authentication response with new tokens
     */
    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest) {
        // Validate and get refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        
        // Revoke the old refresh token (rotation policy)
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());
        
        Member member = refreshToken.getMember();
        
        // Generate new tokens
        String accessToken = jwtUtil.generateAccessToken(member);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(member);
        
        logger.info("Token refreshed for member: {}", member.getEmail());
        
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpirationSeconds())
                .build();
    }
    
    /**
     * Logout by revoking the refresh token.
     *
     * @param refreshTokenRequest refresh token request DTO
     */
    @Transactional
    public void logout(RefreshTokenRequestDTO refreshTokenRequest) {
        refreshTokenService.revokeRefreshToken(refreshTokenRequest.getRefreshToken());
        logger.info("Member logged out, refresh token revoked");
    }
    
    /**
     * Get member by email.
     *
     * @param email member email
     * @return member response DTO
     */
    public MemberResponseDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Member not found"));
        return MemberResponseDTO.fromEntity(member);
    }
}
