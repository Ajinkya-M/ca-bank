package com.ca_bank.member_service.controller;

import java.util.List;

import com.ca_bank.member_service.models.dto.MemberResponseDTO;
import org.springframework.web.bind.annotation.*;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memeberService;

    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memeberService.createMember(member);
    }

    @GetMapping
    public List<MemberResponseDTO> getAllMembers() {
        return memeberService.getAllMembers();
    }

    @GetMapping("/{id}")
    public MemberResponseDTO getMember(@PathVariable Long id) {
        return memeberService.getMember(id);
    }
}
