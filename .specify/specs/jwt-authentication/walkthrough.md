# JWT Authentication Feature - Walkthrough

## Summary

Successfully implemented JWT-based authentication for the CA Bank member-service. Members can now register, login, and access protected endpoints using JWT tokens.

## Files Created/Modified

### Dependencies
- **[pom.xml](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/pom.xml)** - Added Spring Security, Validation, and JJWT dependencies

### Models & Entities
| File | Description |
|------|-------------|
| [Member.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/Member.java) | Added `password` and `status` fields |
| [MemberStatus.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/enums/MemberStatus.java) | New enum for account status |
| [RefreshToken.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/RefreshToken.java) | New entity for refresh tokens |

### DTOs
| File | Description |
|------|-------------|
| [LoginRequestDTO.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/dto/LoginRequestDTO.java) | Login request validation |
| [RegisterRequestDTO.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/dto/RegisterRequestDTO.java) | Registration with validation |
| [AuthResponseDTO.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/dto/AuthResponseDTO.java) | Token response format |
| [RefreshTokenRequestDTO.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/dto/RefreshTokenRequestDTO.java) | Refresh token request |
| [ApiResponse.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/models/dto/ApiResponse.java) | Standardized API wrapper |

### Security Components
| File | Description |
|------|-------------|
| [JwtUtil.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/security/JwtUtil.java) | Token generation & validation |
| [JwtAuthenticationFilter.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/security/JwtAuthenticationFilter.java) | Request filter for JWT |
| [SecurityConfig.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/security/SecurityConfig.java) | Spring Security config |
| [CustomUserDetailsService.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/security/CustomUserDetailsService.java) | User loading service |

### Services & Controllers
| File | Description |
|------|-------------|
| [AuthService.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/service/AuthService.java) | Auth business logic |
| [RefreshTokenService.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/service/RefreshTokenService.java) | Token management |
| [AuthController.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/controller/AuthController.java) | REST endpoints |

### Exception Handling
| File | Description |
|------|-------------|
| [AuthenticationException.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/exception/AuthenticationException.java) | Auth failures |
| [TokenException.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/exception/TokenException.java) | Token errors |
| [EmailAlreadyExistsException.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/exception/EmailAlreadyExistsException.java) | Duplicate email |
| [GlobalExceptionHandler.java](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/java/com/ca_bank/member_service/exception/GlobalExceptionHandler.java) | Global handler |

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | ❌ | Register new member |
| POST | `/api/v1/auth/login` | ❌ | Login & get tokens |
| POST | `/api/v1/auth/refresh` | ❌ | Refresh access token |
| POST | `/api/v1/auth/logout` | ✅ | Revoke refresh token |
| GET | `/api/v1/auth/me` | ✅ | Get current user |

---

## Testing Results

All unit tests passed:
- **JwtUtilTest** - 8 tests (token generation, validation, claim extraction)
- **AuthServiceTest** - 8 tests (register, login, refresh, logout)
- **RefreshTokenServiceTest** - 7 tests (create, validate, revoke)

```
mvn test "-Dtest=JwtUtilTest,AuthServiceTest,RefreshTokenServiceTest"
Exit code: 0
```

---

## Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"SecureP@ss123","phone":"+1234567890"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecureP@ss123"}'
```

### Access Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer {accessToken}"
```

---

## Configuration

JWT settings in [application.properties](file:///d:/Ajinkya/workspace/springboot-projects/ca-bank/member-service/src/main/resources/application.properties):
```properties
jwt.secret=${JWT_SECRET:mySecretKeyForJWTSigningThatIsAtLeast32CharactersLong}
jwt.access-token-expiration=900000    # 15 minutes
jwt.refresh-token-expiration=604800000 # 7 days
```

> [!IMPORTANT]
> Set `JWT_SECRET` environment variable in production with a secure 256-bit key.
