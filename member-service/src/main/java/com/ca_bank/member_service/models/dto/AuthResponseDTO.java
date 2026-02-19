package com.ca_bank.member_service.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses containing JWT tokens.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    
    /**
     * Member details (optional, included on login/register).
     */
    private MemberResponseDTO member;
    
    /**
     * JWT access token for API authentication.
     */
    private String accessToken;
    
    /**
     * Refresh token for obtaining new access tokens.
     */
    private String refreshToken;
    
    /**
     * Token type (always "Bearer").
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Access token expiry time in seconds.
     */
    private Long expiresIn;
}
