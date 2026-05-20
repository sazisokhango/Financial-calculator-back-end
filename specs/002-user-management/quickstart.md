# Quickstart: User Management

**Feature**: 002-user-management

Use these commands to manually verify the feature once implemented.

## Prerequisites

1. PostgreSQL running with the `financial_calculator` database and `tax_calc` user
2. Application started: `./mvnw spring-boot:run`
3. At least one registered user (use the register endpoint first if needed)

---

## Setup — Register test users

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq

curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}' | jq
```

---

## Get All Users — Should return 200 with list

```bash
curl -s http://localhost:8080/api/user | jq
```

**Expected response (200)**:
```json
[
  { "id": 1, "firstName": "Saziso", "lastName": "Khango", "email": "saziso@example.com" },
  { "id": 2, "firstName": "John",   "lastName": "Doe",    "email": "john@example.com" }
]
```

---

## Get All Users (empty) — Should return 200 with empty list

```bash
# With no registered users:
curl -s http://localhost:8080/api/user | jq
```

**Expected response (200)**:
```json
[]
```

---

## Get User by ID — Should return 200 with user profile

```bash
curl -s http://localhost:8080/api/user/1 | jq
```

**Expected response (200)**:
```json
{ "id": 1, "firstName": "Saziso", "lastName": "Khango", "email": "saziso@example.com" }
```

---

## Get Non-Existent User — Should return 404

```bash
curl -s http://localhost:8080/api/user/999 | jq
```

**Expected response (404)**:
```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

## Run Tests

```bash
./mvnw test
```

All tests should pass with zero failures.
