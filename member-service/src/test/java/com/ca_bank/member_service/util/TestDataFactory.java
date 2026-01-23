package com.ca_bank.member_service.util;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.dto.MemberResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {

    public static Member createMember() {
        return Member.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("1234567890")
                .build();
    }

    public static List<Member> createMemberList(int size) {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            members.add(Member.builder()
                    .id((long) i)
                    .firstName("User" + i)
                    .lastName("Last" + i)
                    .email("user" + i + "@test.com")
                    .phone("000000000" + i)
                    .build());
        }
        return members;
    }

    public static MemberResponseDTO createMemberResponseDTO() {
        return MemberResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@test.com")
                .phone("1234567890")
                .build();
    }
}
