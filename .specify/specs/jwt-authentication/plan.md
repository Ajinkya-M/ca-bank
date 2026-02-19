# JWT-Based User Authentication

Implement JWT authentication for the CA Bank member-service, enabling secure member login with email/password and token-based API access.

## User Review Required

> [!IMPORTANT]
> **Breaking Change**: The `Member` entity will be modified to add `password` and `status` fields. Database migration or schema update will be required.

> [!IMPORTANT]
> **New Dependencies**: Spring Security and JJWT will be added. All existing endpoints will require authentication by default unless explicitly whitelisted.

## Proposed Changes

### Dependencies

#### [MODIFY] [pom.xml](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/pom.xml)

Add the following dependencies:
- `spring-boot-starter-security` - Spring Security framework
- `spring-boot-starter-validation` - Bean validation for DTOs
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.6) - JWT token handling

---

### Models & Entities

#### [MODIFY] [Member.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/Member.java)

Add fields:
- `password` (String) - BCrypt hashed password
- `status` (MemberStatus enum) - Account status (ACTIVE, INACTIVE, SUSPENDED)
- Make `email` unique and non-null

#### [NEW] MemberStatus.java
`member-service/src/main/java/com/ca_bank/member_service/models/enums/MemberStatus.java`

Enum with values: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`

#### [NEW] RefreshToken.java
`member-service/src/main/java/com/ca_bank/member_service/models/RefreshToken.java`

Entity for storing refresh tokens with:
- `token` (unique String)
- `member` (ManyToOne relationship)
- `expiryDate` (Instant)
- `revoked` (boolean)

---

### DTOs

#### [NEW] LoginRequestDTO.java
`member-service/src/main/java/com/ca_bank/member_service/models/dto/LoginRequestDTO.java`

Fields: `email`, `password` with validation annotations

#### [NEW] RegisterRequestDTO.java
`member-service/src/main/java/com/ca_bank/member_service/models/dto/RegisterRequestDTO.java`

Fields: `firstName`, `lastName`, `email`, `password`, `phone` with validation

#### [NEW] AuthResponseDTO.java
`member-service/src/main/java/com/ca_bank/member_service/models/dto/AuthResponseDTO.java`

Fields: `member`, `accessToken`, `refreshToken`, `tokenType`, `expiresIn`

#### [NEW] RefreshTokenRequestDTO.java
`member-service/src/main/java/com/ca_bank/member_service/models/dto/RefreshTokenRequestDTO.java`

Fields: `refreshToken`

---

### Repository Layer

#### [NEW] RefreshTokenRepository.java
`member-service/src/main/java/com/ca_bank/member_service/repositories/RefreshTokenRepository.java`

Methods:
- `findByToken(String token)`
- `findByMember(Member member)`
- `deleteByMember(Member member)`

---

### Security Components

#### [NEW] JwtUtil.java
`member-service/src/main/java/com/ca_bank/member_service/security/JwtUtil.java`

JWT token utility class:
- `generateAccessToken(Member member)` - Creates 15-min access token
- `generateRefreshToken()` - Creates random refresh token string
- `validateToken(String token)` - Validates token signature and expiry
- `getEmailFromToken(String token)` - Extracts email claim

#### [NEW] JwtAuthenticationFilter.java
`member-service/src/main/java/com/ca_bank/member_service/security/JwtAuthenticationFilter.java`

Filter extending `OncePerRequestFilter`:
- Extracts Bearer token from Authorization header
- Validates token and sets SecurityContext
- Continues filter chain

#### [NEW] SecurityConfig.java
`member-service/src/main/java/com/ca_bank/member_service/security/SecurityConfig.java`

Spring Security configuration:
- Disable CSRF (stateless API)
- Permit: `/api/v1/auth/**`, `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- Require authentication for all other endpoints
- Configure JWT filter in security filter chain

#### [NEW] CustomUserDetailsService.java
`member-service/src/main/java/com/ca_bank/member_service/security/CustomUserDetailsService.java`

Implements `UserDetailsService`:
- `loadUserByUsername(email)` - Loads Member and returns UserDetails

---

### Service Layer

#### [NEW] AuthService.java
`member-service/src/main/java/com/ca_bank/member_service/service/AuthService.java`

Authentication business logic:
- `register(RegisterRequestDTO)` - Register new member with hashed password
- `login(LoginRequestDTO)` - Authenticate and return tokens
- `refreshToken(String refreshToken)` - Rotate and generate new tokens
- `logout(String refreshToken)` - Revoke refresh token

#### [NEW] RefreshTokenService.java
`member-service/src/main/java/com/ca_bank/member_service/service/RefreshTokenService.java`

Refresh token management:
- `createRefreshToken(Member member)` - Create and persist new token
- `validateRefreshToken(String token)` - Check existence and expiry
- `revokeRefreshToken(String token)` - Mark as revoked
- `deleteByMember(Member member)` - Remove all tokens for member

---

### Controller Layer

#### [NEW] AuthController.java
`member-service/src/main/java/com/ca_bank/member_service/controller/AuthController.java`

Endpoints:
- `POST /api/v1/auth/register` - Register new member
- `POST /api/v1/auth/login` - Login and get tokens
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout (protected)
- `GET /api/v1/auth/me` - Get current user (protected)

---

### Exception Handling

#### [NEW] AuthenticationException.java
`member-service/src/main/java/com/ca_bank/member_service/exception/AuthenticationException.java`

Custom exception for auth failures with HTTP status mapping

#### [NEW] TokenException.java
`member-service/src/main/java/com/ca_bank/member_service/exception/TokenException.java`

Custom exception for token validation failures

---

### Configuration

#### [MODIFY] application.properties/yml

Add JWT configuration properties:
```properties
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-for-jwt-signing}
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

---

## Verification Plan

### Automated Tests

#### Unit Tests (New)

**1. JwtUtilTest.java**
- Test token generation produces valid JWT
- Test token validation with valid/expired/malformed tokens
- Test email extraction from token

**2. AuthServiceTest.java**
- Test registration with valid data
- Test registration with duplicate email (expect exception)
- Test login with valid credentials
- Test login with invalid credentials (expect exception)
- Test login with inactive account (expect exception)
- Test token refresh with valid refresh token
- Test token refresh with expired/revoked token (expect exception)
- Test logout revokes refresh token

**3. RefreshTokenServiceTest.java**
- Test creation of refresh token
- Test validation of valid/expired/revoked tokens
- Test revocation of token

**4. SecurityIntegrationTest.java**
- Test protected endpoint without token returns 401
- Test protected endpoint with valid token returns 200
- Test protected endpoint with expired token returns 401
- Test public endpoints without token returns 200

**Run Tests Command:**
```bash
cd d:\Ajinkya\workspace\springboot-projects\ca-bank\member-service
mvn test
```

**Coverage Report:**
```bash
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

---

### Manual Verification

After implementation, verify using curl or Postman:

**1. Register a new member:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com","password":"SecureP@ss123","phone":"+1234567890"}'
```
Expected: 201 status with member data and tokens

**2. Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecureP@ss123"}'
```
Expected: 200 status with tokens

**3. Access protected endpoint:**
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer {accessToken}"
```
Expected: 200 status with user data

**4. Access without token:**
```bash
curl -X GET http://localhost:8080/api/v1/auth/me
```
Expected: 401 Unauthorized

**5. Refresh token:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"{refreshToken}"}'
```
Expected: 200 status with new tokens

---

## Files to Create/Modify Summary

| Action | File |
|--------|------|
| MODIFY | `pom.xml` |
| MODIFY | `Member.java` |
| MODIFY | `application.properties` |
| NEW | `MemberStatus.java` |
| NEW | `RefreshToken.java` |
| NEW | `LoginRequestDTO.java` |
| NEW | `RegisterRequestDTO.java` |
| NEW | `AuthResponseDTO.java` |
| NEW | `RefreshTokenRequestDTO.java` |
| NEW | `RefreshTokenRepository.java` |
| NEW | `JwtUtil.java` |
| NEW | `JwtAuthenticationFilter.java` |
| NEW | `SecurityConfig.java` |
| NEW | `CustomUserDetailsService.java` |
| NEW | `AuthService.java` |
| NEW | `RefreshTokenService.java` |
| NEW | `AuthController.java` |
| NEW | `AuthenticationException.java` |
| NEW | `TokenException.java` |
| NEW | `JwtUtilTest.java` |
| NEW | `AuthServiceTest.java` |
| NEW | `RefreshTokenServiceTest.java` |
| NEW | `SecurityIntegrationTest.java` |

**Total: ~22 files (3 modified, ~19 new)**
