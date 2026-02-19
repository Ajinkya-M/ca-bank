package com.ca_bank.member_service.repositories;

import com.ca_bank.member_service.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Account entity.
 * Spring Data JPA automatically implements CRUD operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    /**
     * Find account by member ID.
     * Used to retrieve a member's account for transfers and balance inquiries.
     * 
     * @param memberId the ID of the member
     * @return Optional containing the account if found
     */
    Optional<Account> findByMemberId(Long memberId);
    
    /**
     * Find account by account number.
     * Supports future feature for transfers using account numbers.
     * 
     * @param accountNumber the unique account number
     * @return Optional containing the account if found
     */
    Optional<Account> findByAccountNumber(String accountNumber);
}
