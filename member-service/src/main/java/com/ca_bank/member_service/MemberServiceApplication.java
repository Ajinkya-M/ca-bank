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
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@gmail.com")
				.phone("1234567890")
				.build(),
			Member.builder()
				.firstName("Jane")
				.lastName("Doe")
				.email("jane.doe@gmail.com")
				.phone("1234567890")
				.build(),
			Member.builder()
				.firstName("Bob")
				.lastName("Doe")
				.email("bob.doe@gmail.com")
				.phone("1234567890")
				.build(),
			Member.builder()
				.firstName("Alice")
				.lastName("Doe")
				.email("alice.does@gmail.com")
				.phone("1234567891")
				.build()
		).forEach(newMember -> {
			memberRepository.findByEmail(newMember.getEmail())
					.ifPresentOrElse(existingMember -> {}, () -> memberRepository.save(newMember));

		});
	}
}
