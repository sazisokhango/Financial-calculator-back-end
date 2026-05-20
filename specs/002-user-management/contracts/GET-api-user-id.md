# Contract: GET /api/user/{id}

**Feature**: 002-user-management
**Method**: GET
**Path**: `/api/user/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| id        | Long | Yes      | The user's numeric id |

---

## Responses

### 200 OK — User found

```json
{
  "id": 1,
  "firstName": "Saziso",
  "lastName": "Khango",
  "email": "saziso@example.com"
}
```

### 404 Not Found — User does not exist

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```

---

## Scenarios

| Scenario                   | Status | Response body               |
|----------------------------|--------|-----------------------------|
| Valid id, user exists      | 200    | UserResponse                |
| Valid id, user not found   | 404    | Standard error shape        |
