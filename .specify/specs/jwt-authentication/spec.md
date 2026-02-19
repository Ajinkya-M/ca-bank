# Specification: JWT-Based User Authentication

## Summary
Implement JWT (JSON Web Token) based authentication for the CA Bank member-service. This feature enables members to securely authenticate using email and password, receive JWT access and refresh tokens, and access protected API endpoints. The implementation follows industry-standard security practices using Spring Security.

## User Story
**As a** bank member  
**I want** to securely authenticate with my credentials and receive a JWT token  
**So that** I can access protected banking features without re-authenticating on each request

## Acceptance Criteria
- [ ] Members can register with email, password, and personal details
- [ ] Members can login with email and password to receive JWT tokens
- [ ] Access tokens expire after 15 minutes
- [ ] Refresh tokens expire after 7 days and can generate new access tokens
- [ ] Protected endpoints return 401 Unauthorized without valid token
- [ ] Protected endpoints accept Bearer token in Authorization header
- [ ] Passwords are stored using BCrypt hashing
- [ ] Invalid credentials return appropriate error messages
- [ ] Token refresh endpoint works without requiring re-authentication
- [ ] Logout invalidates refresh tokens

## Detailed Requirements

### Functional Requirements

1. **Member Registration**
   - Description: Allow new members to register with credentials
   - Input: firstName, lastName, email, password, phone
   - Output: Member details with JWT tokens
   - Validation: 
     - Email must be unique and valid format
     - Password minimum 8 characters with complexity requirements
     - Required fields: firstName, lastName, email, password

2. **Member Login**
   - Description: Authenticate members and issue JWT tokens
   - Input: email, password
   - Output: Access token, refresh token, token expiry, member details
   - Validation:
     - Email must exist in system
     - Password must match stored hash

3. **Token Refresh**
   - Description: Generate new access token using refresh token
   - Input: Refresh token
   - Output: New access token, new refresh token
   - Validation: Refresh token must be valid and not expired/revoked

4. **Protected Endpoint Access**
   - Description: Validate JWT on protected endpoints
   - Input: Bearer token in Authorization header
   - Output: Allow or deny access
   - Validation: Token must be valid, not expired, and properly signed

5. **Logout**
   - Description: Invalidate user's refresh token
   - Input: Refresh token
   - Output: Confirmation message
   - Validation: Token must belong to authenticated user

### Business Rules
1. Only ACTIVE members can authenticate
2. Failed login attempts should be logged for security monitoring
3. Refresh tokens can only be used once (rotation policy)
4. Access tokens cannot be revoked (rely on short expiry)
5. Member email serves as the unique identifier for authentication

### Edge Cases
| Scenario | Expected Behavior |
|----------|-------------------|
| Expired access token | Return 401 with "Token expired" message |
| Invalid token format | Return 401 with "Invalid token" message |
| Inactive member login attempt | Return 403 with "Account inactive" message |
| Duplicate email registration | Return 409 with "Email already exists" message |
| Missing Authorization header | Return 401 with "No token provided" message |
| Expired refresh token | Return 401, require re-authentication |
| Tampered token (invalid signature) | Return 401 with "Invalid token" message |

## API Specification

### 1. Register Member
```http
POST /api/v1/auth/register
Content-Type: application/json
```

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecureP@ss123",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "status": "success",
  "data": {
    "member": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "phone": "+1234567890"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Registration successful"
}
```

### 2. Login
```http
POST /api/v1/auth/login
Content-Type: application/json
```

**Request:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecureP@ss123"
}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "member": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Login successful"
}
```

### 3. Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Token refreshed successfully"
}
```

### 4. Logout
```http
POST /api/v1/auth/logout
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": null,
  "message": "Logged out successfully"
}
```

### 5. Get Current User (Protected)
```http
GET /api/v1/auth/me
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890"
  },
  "message": "Current user retrieved"
}
```

### Error Responses
| Status Code | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Invalid request body or validation failure |
| 401 | Unauthorized | Missing, invalid, or expired token |
| 403 | Forbidden | Account inactive or insufficient permissions |
| 409 | Conflict | Email already registered |
| 500 | Internal Server Error | Unexpected server error |

## Data Model

### Entity: Member (Updated)
```java
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String firstName;
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt hashed
    
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    @CreationTimestamp
    private Date createdAt;
    
    @UpdateTimestamp
    private Date updatedAt;
}
```

### Entity: RefreshToken (New)
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    private boolean revoked = false;
    
    @CreationTimestamp
    private Date createdAt;
}
```

### Enum: MemberStatus (New)
```java
public enum MemberStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    PENDING_VERIFICATION
}
```

### Relationships
- Member has one-to-many RefreshToken (multiple devices/sessions)
- RefreshToken belongs to Member

## Security Requirements
- **Authentication**: JWT-based with access and refresh tokens
- **Password Storage**: BCrypt with strength 10+
- **Token Signing**: HMAC-SHA256 with secure secret key (min 256 bits)
- **Authorization**: Role-based (future enhancement - currently all authenticated members have equal access)
- **Data Protection**: 
  - Passwords never returned in API responses
  - Tokens transmitted only over HTTPS (enforce in production)
- **Audit**: Log all authentication events (login, logout, failed attempts)

## Performance Requirements
- **Response Time**: < 200ms for authentication endpoints (95th percentile)
- **Token Generation**: < 50ms
- **Token Validation**: < 10ms
- **Throughput**: Support 100+ concurrent login requests
- **Concurrency**: Handle concurrent refresh token rotations correctly

## Dependencies
- Spring Security 6.x
- jjwt-api, jjwt-impl, jjwt-jackson (JJWT 0.12.x)
- BCrypt (included in Spring Security)
- PostgreSQL for refresh token storage

## Out of Scope
- OAuth2 / Social Login (Google, Facebook, etc.)
- Two-Factor Authentication (2FA)
- Password reset / forgot password flow
- Email verification on registration
- Role/Permission-based authorization
- Rate limiting on auth endpoints
- Account lockout after failed attempts

## Open Questions
- [x] Token expiry times: Access = 15min, Refresh = 7 days (decided)
- [x] Refresh token storage: Database (decided)
- [ ] Should we support multiple concurrent sessions per member?
- [ ] Should we notify members of new login activity?

## References
- [JWT.io](https://jwt.io) - JWT documentation
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---
**Created**: 2026-01-29  
**Last Updated**: 2026-01-29  
**Status**: Draft
