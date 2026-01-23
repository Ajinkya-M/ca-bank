# Technical Plan: Service Layer Unit Testing

## Overview
**Feature**: Service Layer Unit Testing for Money Transfer Feature  
**Priority**: High  
**Estimated Effort**: 2-3 days  
**Created**: 2026-01-23

## Context & Requirements
This plan covers the implementation of comprehensive unit testing for the money transfer feature's service layer. Unit tests will use Mockito to mock repository dependencies and H2 database for integration tests.

### Functional Requirements
- [x] Set up H2 in-memory database for testing
- [x] Configure test profile with separate properties
- [x] Add JaCoCo for coverage reporting
- [x] Implement TransferService unit tests
- [x] Implement AccountService unit tests
- [x] Create test utilities and data factories
- [x] Achieve 80%+ code coverage on service layer

### Non-Functional Requirements
- Tests must run in isolation (no shared state)
- Tests must be fast (< 5 seconds total for unit tests)
- Clear naming conventions following Given-When-Then pattern

## Current State Analysis

### Existing Components
- **Spring Boot 3.4.12** with Java 17
- **spring-boot-starter-test** (includes JUnit 5, Mockito, AssertJ)
- **PostgreSQL** for production database
- **Lombok** for reducing boilerplate
- **MemberService** with basic CRUD operations

### What's Missing (To Be Added)
- H2 database dependency for testing
- JaCoCo plugin for coverage
- Test configuration (`application-test.properties`)
- Service layer unit tests
- Test utilities and data factories

## Implementation Plan

### Phase 1: Test Infrastructure Setup

#### Step 1.1: Add H2 Database Dependency
Add H2 to `pom.xml` with test scope:

```xml
<!-- H2 Database for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

#### Step 1.2: Add JaCoCo Plugin
Add JaCoCo Maven plugin for coverage reporting:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                        <includes>
                            <include>com.ca_bank.member_service.service.*</include>
                        </includes>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Step 1.3: Create Test Configuration
Create `src/test/resources/application-test.properties`:

```properties
# H2 In-Memory Database for Testing
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Settings for H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Disable Flyway/Liquibase if present
spring.flyway.enabled=false
spring.liquibase.enabled=false

# Logging
logging.level.org.springframework=WARN
logging.level.com.ca_bank=DEBUG
```

### Phase 2: Test Utilities & Factories

#### Step 2.1: Create Test Data Factory
Create `src/test/java/com/ca_bank/member_service/util/TestDataFactory.java`:

```java
package com.ca_bank.member_service.util;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.Account;
import com.ca_bank.member_service.models.Transaction;
import com.ca_bank.member_service.models.enums.AccountStatus;
import com.ca_bank.member_service.models.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataFactory {

    // ===== MEMBER FACTORY =====
    
    public static Member createMember() {
        return createMember(1L, "John", "Doe", "john.doe@test.com");
    }
    
    public static Member createMember(Long id, String firstName, String lastName, String email) {
        return Member.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone("1234567890")
                .build();
    }
    
    // ===== ACCOUNT FACTORY =====
    
    public static Account createAccount(Member member) {
        return createAccount(member, new BigDecimal("1000.00"));
    }
    
    public static Account createAccount(Member member, BigDecimal balance) {
        return Account.builder()
                .id(member.getId())
                .member(member)
                .accountNumber("ACC-" + member.getId() + "-001")
                .balance(balance)
                .currency("USD")
                .status(AccountStatus.ACTIVE)
                .dailyTransferLimit(new BigDecimal("50000.00"))
                .version(0L)
                .build();
    }
    
    public static Account createInactiveAccount(Member member) {
        Account account = createAccount(member);
        account.setStatus(AccountStatus.FROZEN);
        return account;
    }
    
    // ===== TRANSACTION FACTORY =====
    
    public static Transaction createTransaction(Account from, Account to, BigDecimal amount) {
        return Transaction.builder()
                .id(1L)
                .reference("REF-" + System.currentTimeMillis())
                .fromAccount(from)
                .toAccount(to)
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .description("Test transfer")
                .senderBalanceBefore(from.getBalance())
                .senderBalanceAfter(from.getBalance().subtract(amount))
                .recipientBalanceBefore(to.getBalance())
                .recipientBalanceAfter(to.getBalance().add(amount))
                .createdAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }
}
```

#### Step 2.2: Create Custom Assertions (Optional)
Create `src/test/java/com/ca_bank/member_service/util/TransferAssertions.java`:

```java
package com.ca_bank.member_service.util;

import com.ca_bank.member_service.models.Account;
import com.ca_bank.member_service.models.Transaction;
import com.ca_bank.member_service.models.enums.TransactionStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferAssertions {

    public static void assertTransferCompleted(Transaction transaction) {
        assertThat(transaction).isNotNull();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(transaction.getCompletedAt()).isNotNull();
    }
    
    public static void assertBalanceDeducted(Account account, BigDecimal originalBalance, BigDecimal amount) {
        assertThat(account.getBalance())
            .isEqualByComparingTo(originalBalance.subtract(amount));
    }
    
    public static void assertBalanceIncreased(Account account, BigDecimal originalBalance, BigDecimal amount) {
        assertThat(account.getBalance())
            .isEqualByComparingTo(originalBalance.add(amount));
    }
}
```

### Phase 3: Service Layer Unit Tests

#### Step 3.1: TransferService Unit Tests
Create `src/test/java/com/ca_bank/member_service/service/TransferServiceTest.java`:

```java
package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.*;
import com.ca_bank.member_service.models.Account;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.Transaction;
import com.ca_bank.member_service.models.dto.TransferRequestDTO;
import com.ca_bank.member_service.models.dto.TransferResponseDTO;
import com.ca_bank.member_service.models.enums.AccountStatus;
import com.ca_bank.member_service.repositories.AccountRepository;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.repositories.TransactionRepository;
import com.ca_bank.member_service.util.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private Member senderMember;
    private Member recipientMember;
    private Account senderAccount;
    private Account recipientAccount;

    @BeforeEach
    void setUp() {
        // Set up test data
        senderMember = TestDataFactory.createMember(1L, "John", "Sender", "john@test.com");
        recipientMember = TestDataFactory.createMember(2L, "Jane", "Recipient", "jane@test.com");
        senderAccount = TestDataFactory.createAccount(senderMember, new BigDecimal("1000.00"));
        recipientAccount = TestDataFactory.createAccount(recipientMember, new BigDecimal("500.00"));
    }

    @Nested
    @DisplayName("Successful Transfer Tests")
    class SuccessfulTransferTests {

        @Test
        @DisplayName("Should successfully transfer money between accounts")
        void transfer_WithValidRequest_ShouldDebitSenderAndCreditRecipient() {
            // Given
            BigDecimal transferAmount = new BigDecimal("250.00");
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), transferAmount, "Test payment"
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));
            when(memberRepository.findById(recipientMember.getId())).thenReturn(Optional.of(recipientMember));
            when(accountRepository.findByMemberId(senderMember.getId())).thenReturn(Optional.of(senderAccount));
            when(accountRepository.findByMemberId(recipientMember.getId())).thenReturn(Optional.of(recipientAccount));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            TransferResponseDTO response = transferService.transfer(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getAmount()).isEqualByComparingTo(transferAmount);
            
            // Verify balances updated
            verify(accountRepository, times(2)).save(any(Account.class));
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should transfer exact amount without precision loss")
        void transfer_WithDecimalAmount_ShouldPreservePrecision() {
            // Given
            BigDecimal transferAmount = new BigDecimal("123.45");
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), transferAmount, null
            );

            when(memberRepository.findById(any())).thenReturn(Optional.of(senderMember), Optional.of(recipientMember));
            when(accountRepository.findByMemberId(senderMember.getId())).thenReturn(Optional.of(senderAccount));
            when(accountRepository.findByMemberId(recipientMember.getId())).thenReturn(Optional.of(recipientAccount));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // When
            TransferResponseDTO response = transferService.transfer(request);

            // Then
            assertThat(response.getAmount()).isEqualByComparingTo("123.45");
        }
    }

    @Nested
    @DisplayName("Validation Failure Tests")
    class ValidationFailureTests {

        @Test
        @DisplayName("Should throw exception when sender has insufficient balance")
        void transfer_WithInsufficientBalance_ShouldThrowInsufficientFundsException() {
            // Given
            BigDecimal transferAmount = new BigDecimal("2000.00"); // More than 1000 balance
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), transferAmount, null
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));
            when(memberRepository.findById(recipientMember.getId())).thenReturn(Optional.of(recipientMember));
            when(accountRepository.findByMemberId(senderMember.getId())).thenReturn(Optional.of(senderAccount));
            when(accountRepository.findByMemberId(recipientMember.getId())).thenReturn(Optional.of(recipientAccount));

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("Should throw exception when recipient member not found")
        void transfer_ToNonExistentMember_ShouldThrowMemberNotFoundException() {
            // Given
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), 999L, new BigDecimal("100.00"), null
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(MemberNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw exception when transferring to self")
        void transfer_ToSelf_ShouldThrowInvalidTransferException() {
            // Given
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), senderMember.getId(), new BigDecimal("100.00"), null
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(InvalidTransferException.class)
                    .hasMessageContaining("Cannot transfer to your own account");
        }

        @Test
        @DisplayName("Should throw exception for negative transfer amount")
        void transfer_WithNegativeAmount_ShouldThrowValidationException() {
            // Given
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), new BigDecimal("-100.00"), null
            );

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(InvalidTransferException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should throw exception for zero transfer amount")
        void transfer_WithZeroAmount_ShouldThrowValidationException() {
            // Given
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), BigDecimal.ZERO, null
            );

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(InvalidTransferException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("Should throw exception when amount exceeds maximum limit")
        void transfer_ExceedingMaxLimit_ShouldThrowLimitExceededException() {
            // Given
            BigDecimal excessiveAmount = new BigDecimal("15000.00"); // Exceeds 10000 limit
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), excessiveAmount, null
            );

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(TransferLimitExceededException.class)
                    .hasMessageContaining("exceeds maximum");
        }
    }

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should throw exception when sender account is inactive")
        void transfer_FromInactiveAccount_ShouldThrowAccountNotActiveException() {
            // Given
            senderAccount.setStatus(AccountStatus.FROZEN);
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), new BigDecimal("100.00"), null
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));
            when(memberRepository.findById(recipientMember.getId())).thenReturn(Optional.of(recipientMember));
            when(accountRepository.findByMemberId(senderMember.getId())).thenReturn(Optional.of(senderAccount));

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(AccountNotActiveException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should throw exception when recipient account is inactive")
        void transfer_ToInactiveAccount_ShouldThrowAccountNotActiveException() {
            // Given
            recipientAccount.setStatus(AccountStatus.CLOSED);
            TransferRequestDTO request = new TransferRequestDTO(
                    senderMember.getId(), recipientMember.getId(), new BigDecimal("100.00"), null
            );

            when(memberRepository.findById(senderMember.getId())).thenReturn(Optional.of(senderMember));
            when(memberRepository.findById(recipientMember.getId())).thenReturn(Optional.of(recipientMember));
            when(accountRepository.findByMemberId(senderMember.getId())).thenReturn(Optional.of(senderAccount));
            when(accountRepository.findByMemberId(recipientMember.getId())).thenReturn(Optional.of(recipientAccount));

            // When/Then
            assertThatThrownBy(() -> transferService.transfer(request))
                    .isInstanceOf(AccountNotActiveException.class);
        }
    }
}
```

#### Step 3.2: AccountService Unit Tests
Create `src/test/java/com/ca_bank/member_service/service/AccountServiceTest.java`:

```java
package com.ca_bank.member_service.service;

import com.ca_bank.member_service.exception.AccountAlreadyExistsException;
import com.ca_bank.member_service.exception.AccountNotFoundException;
import com.ca_bank.member_service.exception.InsufficientFundsException;
import com.ca_bank.member_service.exception.MemberNotFoundException;
import com.ca_bank.member_service.models.Account;
import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.dto.BalanceResponseDTO;
import com.ca_bank.member_service.repositories.AccountRepository;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.util.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AccountService accountService;

    private Member testMember;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testMember = TestDataFactory.createMember();
        testAccount = TestDataFactory.createAccount(testMember, new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Get Balance Tests")
    class GetBalanceTests {

        @Test
        @DisplayName("Should return balance for existing account")
        void getBalance_ForExistingAccount_ShouldReturnCurrentBalance() {
            // Given
            when(accountRepository.findByMemberId(testMember.getId()))
                    .thenReturn(Optional.of(testAccount));

            // When
            BalanceResponseDTO response = accountService.getBalance(testMember.getId());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentBalance()).isEqualByComparingTo("1000.00");
            assertThat(response.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should throw exception for non-existent member")
        void getBalance_ForNonExistentMember_ShouldThrowNotFoundException() {
            // Given
            when(accountRepository.findByMemberId(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> accountService.getBalance(999L))
                    .isInstanceOf(AccountNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Create Account Tests")
    class CreateAccountTests {

        @Test
        @DisplayName("Should create account with zero balance for new member")
        void createAccount_ForNewMember_ShouldCreateWithZeroBalance() {
            // Given
            when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
            when(accountRepository.findByMemberId(testMember.getId())).thenReturn(Optional.empty());
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
                Account saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // When
            Account account = accountService.createAccount(testMember.getId());

            // Then
            assertThat(account).isNotNull();
            assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(account.getMember()).isEqualTo(testMember);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("Should throw exception if member already has account")
        void createAccount_ForMemberWithExistingAccount_ShouldThrowDuplicateException() {
            // Given
            when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));
            when(accountRepository.findByMemberId(testMember.getId())).thenReturn(Optional.of(testAccount));

            // When/Then
            assertThatThrownBy(() -> accountService.createAccount(testMember.getId()))
                    .isInstanceOf(AccountAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-existent member")
        void createAccount_ForNonExistentMember_ShouldThrowException() {
            // Given
            when(memberRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> accountService.createAccount(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Credit/Debit Tests")
    class CreditDebitTests {

        @Test
        @DisplayName("Should increase balance when crediting account")
        void creditAccount_WithValidAmount_ShouldIncreaseBalance() {
            // Given
            BigDecimal creditAmount = new BigDecimal("500.00");
            BigDecimal originalBalance = testAccount.getBalance();
            when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Account updated = accountService.credit(testAccount.getId(), creditAmount);

            // Then
            assertThat(updated.getBalance())
                    .isEqualByComparingTo(originalBalance.add(creditAmount));
        }

        @Test
        @DisplayName("Should decrease balance when debiting account")
        void debitAccount_WithValidAmount_ShouldDecreaseBalance() {
            // Given
            BigDecimal debitAmount = new BigDecimal("300.00");
            BigDecimal originalBalance = testAccount.getBalance();
            when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Account updated = accountService.debit(testAccount.getId(), debitAmount);

            // Then
            assertThat(updated.getBalance())
                    .isEqualByComparingTo(originalBalance.subtract(debitAmount));
        }

        @Test
        @DisplayName("Should throw exception when debiting more than balance")
        void debitAccount_WithInsufficientBalance_ShouldThrowException() {
            // Given
            BigDecimal excessiveDebit = new BigDecimal("2000.00"); // More than 1000 balance
            when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

            // When/Then
            assertThatThrownBy(() -> accountService.debit(testAccount.getId(), excessiveDebit))
                    .isInstanceOf(InsufficientFundsException.class);
        }
    }
}
```

#### Step 3.3: MemberService Unit Tests (Update Existing)
Create/Update `src/test/java/com/ca_bank/member_service/service/MemberServiceTest.java`:

```java
package com.ca_bank.member_service.service;

import com.ca_bank.member_service.models.Member;
import com.ca_bank.member_service.models.dto.MemberResponseDTO;
import com.ca_bank.member_service.repositories.MemberRepository;
import com.ca_bank.member_service.util.TestDataFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService Unit Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = TestDataFactory.createMember();
    }

    @Test
    @DisplayName("Should create and return new member")
    void createMember_WithValidData_ShouldSaveAndReturn() {
        // Given
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        // When
        Member result = memberService.createMember(testMember);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(testMember.getFirstName());
        verify(memberRepository).save(testMember);
    }

    @Test
    @DisplayName("Should return all members as DTOs")
    void getAllMembers_ShouldReturnListOfDTOs() {
        // Given
        Member member2 = TestDataFactory.createMember(2L, "Jane", "Doe", "jane@test.com");
        when(memberRepository.findAll()).thenReturn(Arrays.asList(testMember, member2));

        // When
        List<MemberResponseDTO> result = memberService.getAllMembers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");
    }

    @Test
    @DisplayName("Should return member by ID")
    void getMember_WithExistingId_ShouldReturnDTO() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // When
        MemberResponseDTO result = memberService.getMember(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should return null for non-existent member")
    void getMember_WithNonExistentId_ShouldReturnNull() {
        // Given
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        MemberResponseDTO result = memberService.getMember(999L);

        // Then
        assertThat(result).isNull();
    }
}
```

### Phase 4: Directory Structure

After implementation, the test directory structure will be:

```
src/test/
├── java/
│   └── com/
│       └── ca_bank/
│           └── member_service/
│               ├── service/
│               │   ├── TransferServiceTest.java
│               │   ├── AccountServiceTest.java
│               │   └── MemberServiceTest.java
│               └── util/
│                   ├── TestDataFactory.java
│                   └── TransferAssertions.java
└── resources/
    └── application-test.properties
```

## Running Tests

### Commands

```bash
# Run all tests
./mvnw test

# Run with coverage report (generates in target/site/jacoco/)
./mvnw test jacoco:report

# Run only service layer tests
./mvnw test -Dtest="*ServiceTest"

# Run specific test class
./mvnw test -Dtest="TransferServiceTest"

# Run tests with verbose output
./mvnw test -Dtest="*ServiceTest" -DtrimStackTrace=false
```

### Viewing Coverage Report
After running `./mvnw test jacoco:report`, open:
```
target/site/jacoco/index.html
```

## Definition of Done

- [ ] H2 dependency added to pom.xml
- [ ] JaCoCo plugin configured in pom.xml
- [ ] application-test.properties created
- [ ] TestDataFactory created with member/account/transaction factories
- [ ] TransferServiceTest with all test cases (8+ tests)
- [ ] AccountServiceTest with all test cases (7+ tests)
- [ ] MemberServiceTest updated (4+ tests)
- [ ] All tests passing
- [ ] Coverage report generated
- [ ] 80%+ coverage on service layer classes

## Dependencies Summary

| Dependency | Purpose | Scope |
|------------|---------|-------|
| spring-boot-starter-test | JUnit 5, Mockito, AssertJ | test |
| h2 | In-memory database | test |
| jacoco-maven-plugin | Coverage reporting | plugin |

---
**Created**: 2026-01-23  
**Last Updated**: 2026-01-23  
**Status**: Ready for Implementation
