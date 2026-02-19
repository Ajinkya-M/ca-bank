package com.ca_bank.member_service.repositories;

import com.ca_bank.member_service.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Transaction entity.
 * Spring Data JPA automatically implements CRUD operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transaction by idempotency key.
     * Used to prevent duplicate transactions when client retries.
     * 
     * @param idempotencyKey the client-provided idempotency key
     * @return Optional containing the transaction if already processed
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    /**
     * Find transaction by reference number.
     * Used for transaction lookup and verification.
     * 
     * @param reference the unique transaction reference (e.g., REF-ABC123)
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByReference(String reference);
}
