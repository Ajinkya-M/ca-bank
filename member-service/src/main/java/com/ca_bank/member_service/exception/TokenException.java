package com.ca_bank.member_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when token validation fails (expired, revoked, invalid).
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenException extends RuntimeException {
    
    public TokenException(String message) {
        super(message);
    }
    
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
