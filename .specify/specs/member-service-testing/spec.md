# Specification: MemberService Unit Testing (Refined)

## 1. Summary
Establish a robust unit testing suite for the `MemberService` to ensure high code quality, reliability, and zero regressions. The focus is on verifying business logic and data mapping in isolation from external dependencies.

## 2. Test Objectives
- **Verify Persistence**: Confirm `createMember` interacts correctly with the `MemberRepository`.
- **Verify Mapping**: Ensure `getAllMembers` and `getMember` accurately convert `Member` entities to `MemberResponseDTO`.
- **Verify Null Safety**: Confirm proper handling of missing records.
- **Enforce Coverage**: Maintain strict code coverage standards using JaCoCo.

## 3. Acceptance Criteria
- [ ] **100% Method Coverage**: Every public/protected method in `MemberService` must be tested.
- [ ] **80%+ Line Coverage**: Minimum line coverage threshold for the service layer.
- [ ] **Mocking**: All repository dependencies must be mocked using Mockito.
- [ ] **Repeatability**: Tests must pass consistently without requiring an external database.
- [ ] **AssertJ**: Use fluent assertions for clear, readable test checks.

## 4. Detailed Requirements

### 4.1 Test Scenarios

#### 4.1.1 `Member createMember(Member member)`
- **Success Case**: Call `memberRepository.save(member)` and return the result.
- **Verification**: Use `verify()` to ensure the repository was called exactly once.

#### 4.1.2 `List<MemberResponseDTO> getAllMembers()`
- **Success Case**: Convert a list of `Member` entities from `memberRepository.findAll()` into DTOs.
- **Empty Case**: Return an empty list if no members are found.
- **Data Integrity**: Verify that DTO fields (first_name, last_name, etc.) match the entity fields.

#### 4.1.3 `MemberResponseDTO getMember(Long id)`
- **Found Case**: Return a populated DTO for an existing ID.
- **Not Found Case**: Return `null` for a non-existent ID.

## 5. Technical Stack
- **JUnit 5**: Testing framework.
- **Mockito**: Mocking framework.
- **AssertJ**: Assertion library.
- **JaCoCo**: Code coverage tool.
- **Lombok**: For builder and data access in tests.

---
**Created**: 2026-01-23  
**Status**: Finalized
