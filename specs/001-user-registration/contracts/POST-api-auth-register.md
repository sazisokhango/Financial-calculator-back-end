# Contract: POST /api/auth/register

**Feature**: 001-user-registration
**Method**: POST
**Path**: `/api/auth/register`

---

## Request

### Headers

| Header         | Value              | Required |
|----------------|--------------------|----------|
| Content-Type   | application/json   | Yes      |

### Body

```json
{
  "firstName": "Saziso",
  "lastName": "Khango",
  "email": "saziso@example.com"
}
```

| Field      | Type   | Required | Validation                        |
|------------|--------|----------|-----------------------------------|
| firstName  | String | Yes      | Not blank                         |
| lastName   | String | Yes      | Not blank                         |
| email      | String | Yes      | Not blank, valid email format     |

---

## Responses

### 201 Created — Registration successful

```json
{
  "id": 1,
  "firstName": "Saziso",
  "lastName": "Khango",
  "email": "saziso@example.com"
}
```

### 400 Bad Request — Validation failure (blank fields or invalid email)

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "firstName: must not be blank"
}
```

### 400 Bad Request — Duplicate email

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered"
}
```

---

## Scenarios

| Scenario                              | Status | Response body               |
|---------------------------------------|--------|-----------------------------|
| Valid new registration                | 201    | UserResponse with id        |
| Empty body                            | 400    | Standard error shape        |
| `firstName` blank                     | 400    | Standard error shape        |
| `lastName` blank                      | 400    | Standard error shape        |
| `email` invalid format                | 400    | Standard error shape        |
| `email` already registered            | 400    | "Email already registered"  |
| `email` duplicate (case-insensitive)  | 400    | "Email already registered"  |
