package com.ca_bank.member_service.models.dto;

import com.ca_bank.member_service.models.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDTO {
    private Long id;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String email;
    private String phone;


    public static MemberResponseDTO fromEntity(Member member) {
        return MemberResponseDTO.builder()
                .id(member.getId())
                .firstName(member.getFirstName()) // No change needed here, @JsonProperty handles JSON serialization
                .lastName(member.getLastName()) // No change needed here, @JsonProperty handles JSON serialization
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }

    // You might also want a method to convert DTO to Entity for updates or creation
    // This depends on your service layer's design
}
