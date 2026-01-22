package com.ca_bank.member_service.controller;

import com.ca_bank.member_service.models.dto.MemberResponseDTO;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/client")
@Slf4j
public class ClientController {

    private final RestTemplate restTemplate;

    @Value("${service.account.members}")
    private String membersEndPoint;

    /**
     * GET request example - Fetch all members
     */
    @GetMapping
    public List<MemberResponseDTO> getMembersData() {
        try {
            String fullUrl = this.restTemplate.getUriTemplateHandler().expand(this.membersEndPoint).toString();
            log.info("Fetching members from external service at URL: {}", fullUrl);

            MemberResponseDTO[] memberArray = restTemplate.getForObject(this.membersEndPoint, MemberResponseDTO[].class);
            
            if (memberArray != null) {
                log.info("Successfully retrieved {} members", memberArray.length);
                return Arrays.asList(memberArray);
            } else {
                log.warn("Received null response from members endpoint");
                return List.of();
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error while fetching members: {} - {}", e.getStatusCode(), e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error while fetching members", e);
            return List.of();
        }
    }

    /**
     * GET request example - Fetch a single member by ID
     */
    @GetMapping("/{id}")
    public MemberResponseDTO getMemberById(@PathVariable Long id) {
        try {
            log.info("Fetching member with ID: {}", id);
            return restTemplate.getForObject(this.membersEndPoint + "/" + id, MemberResponseDTO.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error while fetching member {}: {} - {}", id, e.getStatusCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while fetching member {}", id, e);
            return null;
        }
    }

    /**
     * POST request example - Create a new member
     * Example usage:
     * restTemplate.postForObject("/members", newMember, MemberResponseDTO.class);
     */
    // Uncomment and modify as needed:
    /*
    @PostMapping
    public MemberResponseDTO createMember(@RequestBody MemberRequestDTO memberRequest) {
        try {
            log.info("Creating new member via external service...");
            return restTemplate.postForObject("/members", memberRequest, MemberResponseDTO.class);
        } catch (HttpClientErrorException e) {
            log.error("HTTP Error while creating member: {} - {}", e.getStatusCode(), e.getMessage());
            return null;
        }
    }
    */

    // Additional examples of RestTemplate methods:
    
    /**
     * PUT request example - Update a member
     * restTemplate.put("/members/{id}", updatedMember, id);
     */
    
    /**
     * DELETE request example - Delete a member
     * restTemplate.delete("/members/{id}", id);
     */
    
    /**
     * Exchange method - For more control over request/response
     * ResponseEntity<MemberResponseDTO> response = restTemplate.exchange(
     *     "/members/{id}",
     *     HttpMethod.GET,
     *     null,
     *     MemberResponseDTO.class,
     *     id
     * );
     */




}
