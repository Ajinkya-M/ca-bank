# Technical Plan: Money Transfer Feature Implementation

## Overview
**Feature**: Member-to-Member Money Transfer System  
**Priority**: High  
**Estimated Effort**: 3-4 days  
**Created**: 2026-01-23  
**Specification Reference**: `.specify/specs/money-transfer/spec.md`

## Context & Requirements
CA Bank requires a secure money transfer system that enables members to transfer funds between their accounts. This is a core banking feature that must ensure atomic transactions, prevent negative balances, and maintain comprehensive audit trails.

### Functional Requirements
- [ ] Members can initiate transfers by specifying recipient, amount, and optional description
- [ ] System validates sufficient balance before processing
- [ ] Transfers are atomic (both debit and credit succeed together or both fail)
- [ ] System prevents negative balances at all times
- [ ] All transfers are logged with complete audit trail
- [ ] Members can check their current account balance
- [ ] Transfer confirmation provides transaction reference ID
- [ ] Appropriate error messages for all failure scenarios

### Non-Functional Requirements
- [ ] Transfer initiation: < 500ms (95th percentile)
- [ ] Balance inquiry: < 100ms (95th percentile)
- [ ] Handle concurrent transfers without data corruption (optimistic locking)
- [ ] 80%+ code coverage for service layer

## Current State Analysis

### Existing Components
| Component | File | Status |
|-----------|------|--------|
| Member Entity | `models/Member.java` | ✅ Exists |
| Member Repository | `repositories/MemberRepository.java` | ✅ Exists |
| Member Service | `service/MemberService.java` | ✅ Exists |
| Member Controller | `controller/MemberController.java` | ✅ Exists |
| Member DTOs | `dto/MemberRequestDTO.java`, `dto/MemberResponseDTO.java` | ✅ Exists |

### Gaps & Challenges
| Gap | Description |
|-----|-------------|
| No Account Entity | Members don't have associated bank accounts |
| No Transaction Tracking | No way to record money movements |
| No Transfer Logic | No service to handle atomic transfers |
| No Balance Validation | No mechanism to prevent overdrafts |
| Missing Test Infrastructure | No TestDataFactory or unit tests for money transfer |

## Proposed Solution

### Architecture Changes
```
┌─────────────────────────────────────────────────────────────────┐
│                        API Layer                                 │
│  TransferController (POST /transfers, GET /accounts/{id}/balance)│
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                     Service Layer                                │
│  TransferService (@Transactional)  │  AccountService             │
│  - transfer()                       │  - createAccount()          │
│  - validateAmount()                 │  - getBalance()             │
│  - checkIdempotency()               │  - credit() / debit()       │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                   Repository Layer                               │
│  AccountRepository  │  TransactionRepository  │  MemberRepository│
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                     Database Layer                               │
│  accounts (with @Version)  │  transactions  │  members           │
└─────────────────────────────────────────────────────────────────┘
```

### Database Schema Changes
```sql
-- accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT UNIQUE NOT NULL REFERENCES members(id),
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    daily_transfer_limit DECIMAL(15,2) DEFAULT 50000.00,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(50) UNIQUE NOT NULL,
    from_account_id BIGINT NOT NULL REFERENCES accounts(id),
    to_account_id BIGINT NOT NULL REFERENCES accounts(id),
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'TRANSFER',
    idempotency_key VARCHAR(100) UNIQUE,
    sender_balance_before DECIMAL(15,2),
    sender_balance_after DECIMAL(15,2),
    recipient_balance_before DECIMAL(15,2),
    recipient_balance_after DECIMAL(15,2),
    error_message VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_from_account ON transactions(from_account_id);
CREATE INDEX idx_to_account ON transactions(to_account_id);
```

### API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transfers` | Initiate money transfer |
| GET | `/api/v1/accounts/{memberId}/balance` | Get account balance |
| POST | `/api/v1/accounts/{memberId}` | Create account for member |

## Implementation Phases

### Phase 1: Database Layer (Enums & Entities)
**Timeline**: 30 minutes  
**Files to Create**:

| # | File Path | Description |
|---|-----------|-------------|
| 1 | `models/enums/AccountStatus.java` | ACTIVE, FROZEN, CLOSED |
| 2 | `models/enums/TransactionStatus.java` | PENDING, COMPLETED, FAILED, REVERSED |
| 3 | `models/enums/TransactionType.java` | TRANSFER, DEPOSIT, WITHDRAWAL |
| 4 | `models/Account.java` | Account entity with @Version for optimistic locking |
| 5 | `models/Transaction.java` | Transaction entity with audit fields |

### Phase 2: Repository Layer
**Timeline**: 15 minutes  
**Files to Create**:

| # | File Path | Key Methods |
|---|-----------|-------------|
| 1 | `repositories/AccountRepository.java` | `findByMemberId()`, `findByAccountNumber()` |
| 2 | `repositories/TransactionRepository.java` | `findByIdempotencyKey()`, `findByReference()` |

### Phase 3: DTO Layer
**Timeline**: 20 minutes  
**Files to Create**:

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `dto/TransferRequestDTO.java` | Transfer request payload |
| 2 | `dto/TransferResponseDTO.java` | Transfer response with balances |
| 3 | `dto/BalanceResponseDTO.java` | Balance inquiry response |

### Phase 4: Exception Handling
**Timeline**: 30 minutes  
**Files to Create**:

| # | File Path | HTTP Status |
|---|-----------|-------------|
| 1 | `exception/AccountNotFoundException.java` | 404 |
| 2 | `exception/MemberNotFoundException.java` | 404 |
| 3 | `exception/InsufficientFundsException.java` | 400 |
| 4 | `exception/InvalidTransferException.java` | 400 |
| 5 | `exception/TransferLimitExceededException.java` | 400 |
| 6 | `exception/AccountNotActiveException.java` | 400 |
| 7 | `exception/AccountAlreadyExistsException.java` | 400 |
| 8 | `exception/GlobalExceptionHandler.java` | Centralized error handling |

### Phase 5: Service Layer
**Timeline**: 1 hour  
**Files to Create**:

| # | File Path | Key Methods |
|---|-----------|-------------|
| 1 | `service/AccountService.java` | `createAccount()`, `getBalance()`, `credit()`, `debit()` |
| 2 | `service/TransferService.java` | `transfer()` with validation and atomicity |

**TransferService.transfer() Logic**:
1. Validate amount (> 0, ≤ 10,000)
2. Check self-transfer
3. Check idempotency key
4. Fetch sender and recipient members
5. Fetch accounts and validate status
6. Validate sufficient balance
7. Execute atomic transfer (debit + credit)
8. Create transaction record
9. Return response with new balances

### Phase 6: Controller Layer
**Timeline**: 30 minutes  
**Files to Create**:

| # | File Path | Endpoints |
|---|-----------|-----------|
| 1 | `controller/TransferController.java` | Transfer and balance endpoints |

### Phase 7: Testing
**Timeline**: 1.5 hours  
**Files to Create**:

| # | File Path | Test Cases |
|---|-----------|------------|
| 1 | `test/util/TestDataFactory.java` | Factory for Member, Account, Transaction |
| 2 | `test/service/MemberServiceTest.java` | 5+ test cases |
| 3 | `test/service/AccountServiceTest.java` | 7+ test cases |
| 4 | `test/service/TransferServiceTest.java` | 8+ test cases |

**Test Coverage Targets**:
- TransferService: 100% method coverage
- AccountService: 100% method coverage
- MemberService: 100% method coverage (already done)

## Testing Strategy

### Unit Tests (Mockito)
| Service | Test Scenarios |
|---------|---------------|
| TransferService | Successful transfer, insufficient balance, self-transfer, negative amount, exceeds limit, inactive account, idempotency |
| AccountService | Get balance, create account, duplicate account, credit, debit, debit with insufficient funds |

### Test Data Factory Methods
```java
createMember(Long id, String firstName, String lastName, String email)
createAccount(Member member, BigDecimal balance)
createTransaction(Account from, Account to, BigDecimal amount)
```

## Dependencies & Risks

### Dependencies
- [x] Spring Boot 3.4.12
- [x] Spring Data JPA
- [x] PostgreSQL Driver
- [x] Lombok
- [x] JUnit 5 & Mockito (via spring-boot-starter-test)
- [x] JaCoCo (for coverage reporting)

### Risks
| Risk | Impact | Mitigation |
|------|--------|------------|
| Concurrent transfers causing race conditions | High | Use @Version for optimistic locking |
| Database connection issues | Medium | Use connection pooling, proper error handling |
| Test coverage not meeting 80% | Medium | Write comprehensive unit tests first |

## Success Metrics
- [ ] All unit tests passing
- [ ] 80%+ code coverage on service layer
- [ ] Transfer endpoint returns 201 on success
- [ ] Balance endpoint returns current balance
- [ ] Error scenarios return appropriate HTTP status codes

## File Creation Checklist

### Phase 1: Enums & Entities (5 files)
- [x] `models/enums/AccountStatus.java` ✅ Done
- [x] `models/enums/TransactionStatus.java` ✅ Done
- [x] `models/enums/TransactionType.java` ✅ Done
- [x] `models/Account.java` ✅ Done
- [x] `models/Transaction.java` ✅ Done

### Phase 2: Repositories (2 files)
- [x] `repositories/AccountRepository.java` ✅ Done
- [x] `repositories/TransactionRepository.java` ✅ Done

### Phase 3: DTOs (3 files)
- [x] `dto/TransferRequestDTO.java` ✅ Done
- [x] `dto/TransferResponseDTO.java` ✅ Done
- [ ] `dto/BalanceResponseDTO.java`

### Phase 4: Exceptions (8 files)
- [ ] `exception/AccountNotFoundException.java`
- [ ] `exception/MemberNotFoundException.java`
- [ ] `exception/InsufficientFundsException.java`
- [ ] `exception/InvalidTransferException.java`
- [ ] `exception/TransferLimitExceededException.java`
- [ ] `exception/AccountNotActiveException.java`
- [ ] `exception/AccountAlreadyExistsException.java`
- [ ] `exception/GlobalExceptionHandler.java`

### Phase 5: Services (2 files)
- [ ] `service/AccountService.java`
- [ ] `service/TransferService.java`

### Phase 6: Controllers (1 file)
- [ ] `controller/TransferController.java`

### Phase 7: Tests (4 files)
- [ ] `test/util/TestDataFactory.java`
- [ ] `test/service/MemberServiceTest.java`
- [ ] `test/service/AccountServiceTest.java`
- [ ] `test/service/TransferServiceTest.java`

**Total Files to Create**: 25 files

---
**Author**: Antigravity AI  
**Specification**: `.specify/specs/money-transfer/spec.md`  
**Status**: Draft - Ready for Implementation
