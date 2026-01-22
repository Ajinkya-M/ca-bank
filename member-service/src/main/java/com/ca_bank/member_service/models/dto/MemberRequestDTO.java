package com.ca_bank.member_service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRequestDTO {
    private String firstName;
    private String lastName;

    private String email;
    private String phone;
}
