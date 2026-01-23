package com.ca_bank.member_service.service;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.dto.MemberResponseDTO;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("Should save and return a new member")
    void createMember_shouldSaveAndReturnMember() {
        // Given
        Member memberToSave = TestDataFactory.createMember();
        memberToSave.setId(null); // Simulate new member
        Member savedMember = TestDataFactory.createMember();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        Member result = memberService.createMember(memberToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(memberRepository, times(1)).save(memberToSave);
    }

    @Test
    @DisplayName("Should return a list of all member DTOs")
    void getAllMembers_shouldReturnDtoList() {
        // Given
        List<Member> mockMembers = TestDataFactory.createMemberList(3);
        when(memberRepository.findAll()).thenReturn(mockMembers);

        // When
        List<MemberResponseDTO> result = memberService.getAllMembers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getEmail()).isEqualTo("user1@test.com");
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no members found")
    void getAllMembers_shouldReturnEmptyListWhenNoneExist() {
        // Given
        when(memberRepository.findAll()).thenReturn(List.of());

        // When
        List<MemberResponseDTO> result = memberService.getAllMembers();

        // Then
        assertThat(result).isEmpty();
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return member DTO by valid ID")
    void getMember_withValidId_shouldReturnDto() {
        // Given
        Long id = 1L;
        Member member = TestDataFactory.createMember();
        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        // When
        MemberResponseDTO result = memberService.getMember(id);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(memberRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should return null for non-existent ID")
    void getMember_withInvalidId_shouldReturnNull() {
        // Given
        Long id = 99L;
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        // When
        MemberResponseDTO result = memberService.getMember(id);

        // Then
        assertThat(result).isNull();
        verify(memberRepository, times(1)).findById(id);
    }
}
