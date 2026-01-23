package com.ca_bank.member_service;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.repositories.MemberRepository;

@SpringBootApplication
public class MemberServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MemberServiceApplication.class, args);
	}
}

@Configuration
class PopulateMembers implements CommandLineRunner {
	
	private final MemberRepository memberRepository;
	
	public PopulateMembers(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}
	
	@Override
	public void run(String... args) throws Exception {
		List.of(
			Member.builder()
				.firstName("Sarah")
				.lastName("Johnson")
				.email("sarah.johnson@email.com")
				.phone("5551234567")
				.build(),
			Member.builder()
				.firstName("Michael")
				.lastName("Chen")
				.email("michael.chen@email.com")
				.phone("5552345678")
				.build(),
			Member.builder()
				.firstName("Emily")
				.lastName("Rodriguez")
				.email("emily.rodriguez@email.com")
				.phone("5553456789")
				.build(),
			Member.builder()
				.firstName("David")
				.lastName("Williams")
				.email("david.williams@email.com")
				.phone("5554567890")
				.build(),
			Member.builder()
				.firstName("Jessica")
				.lastName("Brown")
				.email("jessica.brown@email.com")
				.phone("5555678901")
				.build()
		).forEach(newMember -> {
			memberRepository.findByEmail(newMember.getEmail())
					.ifPresentOrElse(existingMember -> {}, () -> memberRepository.save(newMember));

		});
	}
}
