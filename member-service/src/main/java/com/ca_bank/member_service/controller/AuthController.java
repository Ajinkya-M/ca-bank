package com.ca_bank.member_service.controller;

import com.ca_bank.member_service.models.dto.*;
import com.ca_bank.member_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Register a new member.
     * 
     * @param request registration request
     * @return authentication response with tokens
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }
    
    /**
     * Authenticate a member.
     * 
     * @param request login request
     * @return authentication response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
    
    /**
     * Refresh access token using refresh token.
     * 
     * @param request refresh token request
     * @return authentication response with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }
    
    /**
     * Logout (revoke refresh token).
     * Requires authentication.
     * 
     * @param request refresh token to revoke
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
    
    /**
     * Get current authenticated user details.
     * Requires authentication.
     * 
     * @param userDetails authenticated user
     * @return member details
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDTO member = authService.getMemberByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(member, "Current user retrieved"));
    }
}
