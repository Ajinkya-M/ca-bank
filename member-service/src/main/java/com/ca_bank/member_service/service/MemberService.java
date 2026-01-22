package com.ca_bank.member_service.service;

import java.util.List;

import com.ca_bank.member_service.models.dto.MemberResponseDTO;
import org.springframework.stereotype.Service;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.repositories.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public List<MemberResponseDTO> getAllMembers() {
        return this.memberRepository.findAll().stream().map(MemberResponseDTO::fromEntity).toList();
    }


    public MemberResponseDTO getMember(Long id) {
        return this.memberRepository.findById(id).map(MemberResponseDTO::fromEntity).orElse(null);
    }
}
