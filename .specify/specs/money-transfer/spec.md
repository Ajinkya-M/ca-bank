# Specification: Money Transfer Between Member Accounts

## Summary
This feature enables CA Bank members to securely transfer money from their account to another member's account. The system ensures atomic transactions, validates sufficient balance, maintains comprehensive audit trails, and provides real-time balance updates. This is a core banking feature that enhances member-to-member money movement capabilities.

## User Story
**As a** CA Bank member  
**I want** to transfer money from my account to another member's account  
**So that** I can conveniently send money to other members without external payment systems

## Acceptance Criteria
- [ ] Members can initiate a transfer by specifying recipient, amount, and optional description
- [ ] System validates sufficient balance before processing transfer
- [ ] Transfers are atomic (both debit and credit succeed together or both fail)
- [ ] System prevents negative balances at all times
- [ ] All transfers are logged with complete audit trail
- [ ] Members can view their transfer history (sent and received)
- [ ] Members can check their current account balance
- [ ] Transfer confirmation provides transaction reference ID
- [ ] System handles concurrent transfers without data corruption
- [ ] Appropriate error messages for all failure scenarios

## Detailed Requirements

###functional Requirements

1. **Account Creation and Management**
   - Description: Each member must have an associated account with a balance
   - Input: Member ID (existing member)
   - Output: Account entity with initial balance (default: 0.00)
   - Validation:
     - One account per member
     - Balance must be non-negative decimal (2 decimal places)
     - Account must be active to perform transfers

2. **Money Transfer Initiation**
   - Description: Authenticated member can transfer money to another member's account
   - Input:
     - From Account ID (authenticated member's account)
     - To Member ID or Account Number
     - Transfer Amount (decimal, positive)
     - Description/Notes (optional, max 500 characters)
   - Output: Transfer confirmation with transaction ID, timestamp, updated balances
   - Validation:
     - Amount must be positive and greater than 0.01
     - Amount must have at most 2 decimal places
     - Sender account must have sufficient balance (amount + any fees)
     - Recipient member/account must exist and be active
     - Cannot transfer to own account
     - Maximum transfer amount per transaction: 10,000.00 (configurable)

3. **Balance Validation**
   - Description: Verify sender has sufficient funds before processing
   - Input: Account ID, Transfer Amount
   - Output: Boolean (sufficient/insufficient)
   - Validation:
     - Current balance >= transfer amount
     - Account for any pending/locked funds
     - Check account status is ACTIVE

4. **Transaction Processing (Atomic)**
   - Description: Execute debit and credit as single atomic transaction
    - Input: Validated transfer request
   - Output: Completed transaction record
   - Validation:
     - Use database transaction (@Transactional)
     - Debit sender account
     - Credit recipient account
     - Create transaction records for both sides
     - Rollback if any step fails
     - Update last transaction timestamp

5. **Transaction History**
   - Description: Members can view their transfer history
   - Input: Member ID, pagination params (page, size), date range (optional), filter (sent/received/all)
   - Output: List of transactions with details
   - Validation:
     - Only show transactions for authenticated member
     - Support pagination (default page size: 20)
     - Sort by date descending (most recent first)

6. **Balance Inquiry**
   - Description: Members can check their current account balance
   - Input: Member ID (authenticated)
   - Output: Current balance, available balance, last updated timestamp
   - Validation:
     - Only authenticated member can view their own balance
     - Return real-time balance (not cached)

### Business Rules

1. **Minimum Transfer Amount**: 1.00 (one unit of currency)
2. **Maximum Transfer Amount**: 10,000.00 per transaction (configurable via properties)
3. **Daily Transfer Limit**: 50,000.00 per account per day (configurable)
4. **Account Balance**: Must never go below 0.00 (prevent overdrafts)
5. **Supported Currency**: Single currency (USD/default currency) for initial version
6. **Transfer Fees**: None for member-to-member transfers (can be added later)
7. **Account Status**: Only ACTIVE accounts can send or receive transfers
8. **Duplicate Prevention**: Use idempotency keys to prevent duplicate transfers from retries
9. **Transaction Reference**: Generate unique transaction reference for each transfer
10. **Audit Retention**: All transaction records must be retained indefinitely

### Edge Cases

| Scenario | Expected Behavior |
|----------|-------------------|
| Concurrent transfers from same account | Use pessimistic locking or optimistic locking with retry; ensure balance never goes negative |
| Transfer to non-existent member | Return 404 with clear error message "Recipient member not found" |
| Transfer to inactive account | Return 400 with error "Recipient account is not active" |
| Insufficient balance | Return 400 with error "Insufficient funds. Available: {balance}, Required: {amount}" |
| Negative or zero amount | Return 400 with error "Transfer amount must be positive" |
| Transfer to self | Return 400 with error "Cannot transfer to your own account" |
| Amount exceeds maximum limit | Return 400 with error "Amount exceeds maximum transfer limit of {max}" |
| Network failure during transaction | Transaction rolls back; no money is moved; return 500 with retry instruction |
| Duplicate transaction (idempotency) | Return original transaction result without processing again |
| Account locked/frozen | Return 403 with error "Account is temporarily locked" |

## API Specification

### Endpoint 1: Initiate Money Transfer
```http
POST /api/v1/transfers
Content-Type: application/json
# Note: Authentication will be added in Phase 2 (JWT)
```

### Request Schema
```json
{
  "fromAccountId": 123,
  "toMemberId": 456,
  "amount": 250.50,
  "description": "Payment for dinner",
  "idempotencyKey": "unique-client-generated-key-12345"
}
```

### Success Response Schema (201 Created)
```json
{
  "status": "success",
  "data": {
    "transactionId": "TXN-20260122-001",
    "reference": "REF-ABC123XYZ",
    "fromAccountId": 123,
    "toAccountId": 789,
    "amount": 250.50,
    "description": "Payment for dinner",
    "status": "COMPLETED",
    "timestamp": "2026-01-22T16:20:30Z",
    "senderNewBalance": 749.50,
    "recipientNewBalance": 1250.50
  },
  "message": "Transfer completed successfully"
}
```

### Endpoint 2: Get Transfer History
```http
GET /api/v1/transfers?memberId={memberId}&page=0&size=20&type=all&startDate=2026-01-01&endDate=2026-01-31
# Note: memberId will be extracted from JWT token in Phase 2
```

### Response Schema (200 OK)
```json
{
  "status": "success",
  "data": {
    "transfers": [
      {
        "transactionId": "TXN-20260122-001",
        "reference": "REF-ABC123XYZ",
        "type": "SENT",
        "otherParty": {
          "memberId": 456,
          "name": "John Doe"
        },
        "amount": 250.50,
        "description": "Payment for dinner",
        "status": "COMPLETED",
        "timestamp": "2026-01-22T16:20:30Z",
        "balanceAfter": 749.50
      }
    ],
    "pagination": {
      "currentPage": 0,
      "totalPages": 5,
      "totalElements": 87,
      "pageSize": 20
    }
  },
  "message": "Transfer history retrieved successfully"
}
```

### Endpoint 3: Get Account Balance
```http
GET /api/v1/accounts/{memberId}/balance
# Note: Will use authenticated member ID from JWT in Phase 2
```

### Response Schema (200 OK)
```json
{
  "status": "success",
  "data": {
    "accountId": 123,
    "currentBalance": 749.50,
    "availableBalance": 749.50,
    "currency": "USD",
    "lastTransactionDate": "2026-01-22T16:20:30Z",
    "accountStatus": "ACTIVE"
  },
  "message": "Balance retrieved successfully"
}
```

### Endpoint 4: Get Transfer Details
```http
GET /api/v1/transfers/{transactionId}?memberId={memberId}
# Note: Will verify ownership via JWT in Phase 2
```

### Response Schema (200 OK)
```json
{
  "status": "success",
  "data": {
    "transactionId": "TXN-20260122-001",
    "reference": "REF-ABC123XYZ",
    "fromAccount": {
      "accountId": 123,
      "memberId": 100,
      "memberName": "Jane Smith"
    },
    "toAccount": {
      "accountId": 789,
      "memberId": 456,
      "memberName": "John Doe"
    },
    "amount": 250.50,
    "description": "Payment for dinner",
    "status": "COMPLETED",
    "timestamp": "2026-01-22T16:20:30Z"
  },
  "message": "Transfer details retrieved successfully"
}
```

### Error Responses
| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Invalid input (negative amount, exceeds limit, self-transfer, etc.) |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Account locked/frozen or unauthorized access to other member's data |
| 404 | Not Found | Recipient member/account not found or transaction ID doesn't exist |
| 409 | Conflict | Insufficient funds or concurrent transaction conflict |
| 422 | Unprocessable Entity | Validation errors in request payload |
| 500 | Internal Server Error | Database error, transaction rollback, or unexpected system error |
| 503 | Service Unavailable | System maintenance or temporary service disruption |

## Data Model

### Entity: Account
```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "member_id", unique = true, nullable = false)
    private Member member;
    
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber; // Generated: ACC-{memberId}-{random}
    
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "currency", nullable = false)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE; // ACTIVE, FROZEN, CLOSED
    
    @Column(name = "daily_transfer_limit", precision = 15, scale = 2)
    private BigDecimal dailyTransferLimit = new BigDecimal("50000.00");
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // For optimistic locking
}
```

### Entity: Transaction
```java
@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_from_account", columnList = "from_account_id"),
           @Index(name = "idx_to_account", columnList = "to_account_id"),
           @Index(name = "idx_reference", columnList = "reference", unique = true),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "reference", unique = true, nullable = false)
    private String reference; // REF-{timestamp}-{random}
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status; // PENDING, COMPLETED, FAILED, REVERSED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type = TransactionType.TRANSFER; // TRANSFER, DEPOSIT, WITHDRAWAL
    
    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
    
    @Column(name = "sender_balance_before", precision = 15, scale = 2)
    private BigDecimal senderBalanceBefore;
    
    @Column(name = "sender_balance_after", precision = 15, scale = 2)
    private BigDecimal senderBalanceAfter;
    
    @Column(name = "recipient_balance_before", precision = 15, scale = 2)
    private BigDecimal recipientBalanceBefore;
    
    @Column(name = "recipient_balance_after", precision = 15, scale = 2)
    private BigDecimal recipientBalanceAfter;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
```

### Relationships
- Account has a one-to-one relationship with Member
- Transaction has a many-to-one relationship with Account (from_account)
- Transaction has a many-to-one relationship with Account (to_account)
- Each Member can have multiple Transactions (both sent and received)

## Security Requirements

### Phase 1 (Current Implementation - No Authentication)
- **Authentication**: None required initially
  - `fromMemberId` passed explicitly in transfer requests
  - `memberId` passed as path/query parameter for balance and history queries
  - **Note**: This is for development/testing only; JWT will be added in Phase 2
- **Authorization**: Basic validation only
  - Verify member exists before processing
  - No cross-member data access restrictions yet
- **Data Protection**: 
  - Use prepared statements to prevent SQL injection
  - Input validation for all requests
  - Never log sensitive data

### Phase 2 (Future - JWT Authentication)
- **Authentication**: JWT-based authentication required for all endpoints
  - Member ID extracted from JWT claims
  - Token validation on every request
  - Token refresh mechanism
- **Authorization**: 
  - Members can only initiate transfers from their own account
  - Members can only view their own account balance and transaction history
  - Admins can view all transactions (future enhancement)
- **Data Protection**: 
  - Use HTTPS for all API communication
  - Encrypt sensitive data in transaction logs
  - Never log full account numbers or auth tokens

### Audit (Both Phases)
- Log all transfer attempts (success and failure) with member ID, amount, timestamp
- Log all balance inquiries
- Log suspicious activities (multiple failed attempts, unusual amounts)
- Retain audit logs for minimum 7 years (compliance requirement)

## Testing Requirements

### Testing Strategy Overview
The project uses a comprehensive testing approach following the test pyramid:
- **Unit Tests**: Primary focus - test service layer with mocked dependencies
- **Integration Tests**: Test repository layer with H2 database
- **API Tests**: Test controller endpoints with MockMvc

### Test Database Configuration
- **H2 In-Memory Database** for all tests
- Automatic schema creation from JPA entities
- Test data isolation between test methods
- Fast test execution (no external database dependency)

#### Test Configuration (`application-test.properties`)
```properties
# H2 In-Memory Database for Testing
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Settings for H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Initialize test data
spring.sql.init.mode=embedded
```

### Service Layer Unit Tests (Required)

#### TransferService Tests
```java
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private TransferService transferService;

    // Test Cases Required:
    
    // 1. Successful transfer
    @Test
    void transfer_WithValidRequest_ShouldDebitSenderAndCreditRecipient()
    
    // 2. Insufficient balance
    @Test
    void transfer_WithInsufficientBalance_ShouldThrowInsufficientFundsException()
    
    // 3. Transfer to non-existent member
    @Test
    void transfer_ToNonExistentMember_ShouldThrowMemberNotFoundException()
    
    // 4. Transfer to self
    @Test  
    void transfer_ToSelf_ShouldThrowInvalidTransferException()
    
    // 5. Negative/zero amount
    @Test
    void transfer_WithNegativeAmount_ShouldThrowValidationException()
    
    // 6. Amount exceeds limit
    @Test
    void transfer_ExceedingMaxLimit_ShouldThrowLimitExceededException()
    
    // 7. Inactive account
    @Test
    void transfer_FromInactiveAccount_ShouldThrowAccountNotActiveException()
    
    // 8. Concurrent transfer handling
    @Test
    void transfer_WithOptimisticLockException_ShouldRetryOrFail()
}
```

#### AccountService Tests
```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private AccountService accountService;

    // Test Cases Required:
    
    @Test
    void getBalance_ForExistingAccount_ShouldReturnCurrentBalance()
    
    @Test
    void getBalance_ForNonExistentMember_ShouldThrowNotFoundException()
    
    @Test
    void createAccount_ForNewMember_ShouldCreateWithZeroBalance()
    
    @Test
    void createAccount_ForMemberWithExistingAccount_ShouldThrowDuplicateException()
    
    @Test
    void creditAccount_WithValidAmount_ShouldIncreaseBalance()
    
    @Test
    void debitAccount_WithValidAmount_ShouldDecreaseBalance()
    
    @Test
    void debitAccount_WithInsufficientBalance_ShouldThrowException()
}
```

#### TransactionHistoryService Tests
```java
@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private TransactionHistoryService transactionHistoryService;

    @Test
    void getHistory_ForMember_ShouldReturnPaginatedResults()
    
    @Test
    void getHistory_WithDateRange_ShouldFilterByDates()
    
    @Test
    void getHistory_FilterBySent_ShouldOnlyReturnOutgoingTransfers()
    
    @Test
    void getHistory_FilterByReceived_ShouldOnlyReturnIncomingTransfers()
    
    @Test
    void getTransactionDetails_ForOwnTransaction_ShouldReturnDetails()
}
```

### Integration Tests (Repository Layer)
```java
@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByMemberId_ShouldReturnAccount()
    
    @Test
    void findByAccountNumber_ShouldReturnAccount()
    
    @Test
    void updateBalance_ShouldPersistNewBalance()
}

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Test
    void findByFromAccountOrToAccount_ShouldReturnAllMemberTransactions()
    
    @Test
    void findByCreatedAtBetween_ShouldFilterByDateRange()
    
    @Test
    void findByIdempotencyKey_ShouldReturnExistingTransaction()
}
```

### Controller/API Tests
```java
@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TransferService transferService;

    @Test
    void initiateTransfer_WithValidRequest_ShouldReturn201()
    
    @Test
    void initiateTransfer_WithInvalidRequest_ShouldReturn400()
    
    @Test
    void getBalance_ForExistingMember_ShouldReturn200()
    
    @Test
    void getTransferHistory_WithPagination_ShouldReturnPaginatedResults()
}
```

### Test Coverage Requirements
- **Minimum Coverage**: 80% for service layer classes
- **Critical Paths**: 100% coverage for:
  - `TransferService.transfer()` method
  - Balance validation logic
  - Transaction rollback scenarios
- **Tools**: JaCoCo for coverage reporting

### Test Data Setup
```java
@TestConfiguration
class TestDataConfig {
    
    // Create test members with accounts for testing
    public static Member createTestMember(String firstName, String email) {
        return Member.builder()
            .firstName(firstName)
            .lastName("Test")
            .email(email)
            .phone("1234567890")
            .build();
    }
    
    public static Account createTestAccount(Member member, BigDecimal balance) {
        return Account.builder()
            .member(member)
            .accountNumber("ACC-" + member.getId() + "-TEST")
            .balance(balance)
            .status(AccountStatus.ACTIVE)
            .currency("USD")
            .build();
    }
}
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# Run only service layer tests
./mvnw test -Dtest="*ServiceTest"

# Run integration tests
./mvnw test -Dtest="*RepositoryTest"
```

## Performance Requirements
- **Response Time**: 
  - Transfer initiation: < 500ms (95th percentile) due to database transactions
  - Balance inquiry: < 100ms (95th percentile)
  - Transaction history: < 200ms (95th percentile)
- **Throughput**: Support 100 concurrent transfers per second
- **Concurrency**: Handle 1000 concurrent users
- **Data Volume**: 
  - Expect 10,000 transactions per day initially
  - Plan for 100 million transactions over 5 years
  - Implement partitioning/archiving strategy for old transactions

## UI/UX Considerations
- Transfer confirmation should clearly show: amount, recipient name, sender's new balance
- Use loading indicators during transfer processing (may take few seconds)
- Show clear error messages for all failure scenarios
- Transaction history should be easy to filter and search
- Balance should be prominently displayed
- Consider adding transfer templates for frequent recipients (future)
- Mobile-responsive design for API consumers building UIs

## Dependencies

### Phase 1 (Current)
- Spring Data JPA (for ORM)
- PostgreSQL or MySQL database (production)
- **H2 Database** (testing - in-memory)
- Spring Boot Validation (for input validation)
- Lombok (for reducing boilerplate)
- Spring Boot Actuator (for monitoring)
- **JUnit 5 & Mockito** (service layer unit tests)
- **JaCoCo** (test coverage reporting)

### Phase 2 (JWT Authentication)
- Spring Security (for authentication/authorization)
- JWT library (io.jsonwebtoken:jjwt) for token management

### Optional/Future
- Database migration tool (Flyway or Liquibase)
- UUID Generator for transaction references

## Out of Scope
- **Inter-bank transfers** (only internal member-to-member for now)
- **Currency conversion** (single currency only)
- **Scheduled/recurring transfers** (manual transfers only)
- **Transfer fees/charges** (free transfers for now)
- **Transfer limits per recipient** (only sender limits)
- **Transfer approval workflows** (instant transfers only)
- **Overdraft facility** (strict no negative balance)
- **Joint accounts** (one account per member)
- **Business accounts** (individual members only)
- **International transfers/SWIFT** (domestic only)

## Open Questions
- [ ] Should we implement pessimistic or optimistic locking for concurrent transfers?
- [ ] What should be the default daily transfer limit? (Currently suggested: 50,000)
- [ ] Should we send email/SMS notifications for successful transfers?
- [ ] Do we need a transfer reversal/refund mechanism for disputes?
- [ ] Should there be a minimum account balance requirement (e.g., 10.00)?
- [ ] What level of auditing detail is required for compliance?
- [ ] Should we rate-limit transfer API to prevent abuse?

## References
- Constitution: `.specify/memory/constitution.md`
- Current Member Entity: `member-service/src/main/java/com/ca_bank/member_service/models/Member.java`
- Banking Security Best Practices: [ISO 27001, PCI-DSS]
- ACID Transaction Requirements: [Database Transaction Design]

---
**Created**: 2026-01-22  
**Last Updated**: 2026-01-23  
**Status**: Draft
