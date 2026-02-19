# JWT Authentication - member-service

This guide explains how to use the JWT authentication feature in the CA Bank member-service.

## Quick Start

### 1. Start the Application
```bash
cd member-service
mvn spring-boot:run
```

### 2. Register a New Member
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecureP@ss123",
    "phone": "+1234567890"
  }'
```

**Response:**
```json
{
  "status": "success",
  "data": {
    "member": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

### 3. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecureP@ss123"
  }'
```

### 4. Access Protected Endpoints
Use the `accessToken` from login/register response:

```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 5. Refresh Token
When the access token expires (15 minutes), use the refresh token to get a new one:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

### 6. Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "550e8400-e29b-41d4-a716-446655440000"}'
```

---

## API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | No | Register new member |
| POST | `/api/v1/auth/login` | No | Login and get tokens |
| POST | `/api/v1/auth/refresh` | No | Refresh access token |
| POST | `/api/v1/auth/logout` | Yes | Revoke refresh token |
| GET | `/api/v1/auth/me` | Yes | Get current user info |

---

## Token Details

| Token | Expiry | Storage |
|-------|--------|---------|
| Access Token | 15 minutes | Client (memory/localStorage) |
| Refresh Token | 7 days | Client + Database |

---

## Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`@$!%*?&`)

---

## Configuration

Set these in `application.properties` or as environment variables:

```properties
jwt.secret=${JWT_SECRET}                    # 256-bit secret key
jwt.access-token-expiration=900000          # 15 minutes (ms)
jwt.refresh-token-expiration=604800000      # 7 days (ms)
```

> **Production**: Always set `JWT_SECRET` environment variable with a secure 256-bit key.
