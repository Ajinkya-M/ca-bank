package com.ca_bank.member_service.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API response wrapper.
 * 
 * @param <T> type of data payload
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Response status: "success" or "error"
     */
    private String status;
    
    /**
     * Response data payload
     */
    private T data;
    
    /**
     * Human-readable message
     */
    private String message;
    
    /**
     * Create a success response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .message(message)
                .build();
    }
    
    /**
     * Create a success response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }
    
    /**
     * Create an error response with message.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .build();
    }
}
