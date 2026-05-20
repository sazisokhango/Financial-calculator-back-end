# Contract: GET /api/user

**Feature**: 002-user-management
**Method**: GET
**Path**: `/api/user`

---

## Request

No request body. No query parameters.

---

## Responses

### 200 OK — Users found

```json
[
  {
    "id": 1,
    "firstName": "Saziso",
    "lastName": "Khango",
    "email": "saziso@example.com"
  },
  {
    "id": 2,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  }
]
```

### 200 OK — No users registered

```json
[]
```

---

## Scenarios

| Scenario                  | Status | Response body            |
|---------------------------|--------|--------------------------|
| One or more users exist   | 200    | Array of UserResponse    |
| No users registered       | 200    | Empty array `[]`         |
