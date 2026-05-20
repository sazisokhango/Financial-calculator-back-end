# Quickstart: User Registration

**Feature**: 001-user-registration

Use these commands to manually verify the feature once implemented.

## Prerequisites

1. PostgreSQL running with the `financial_calculator` database and `tax_calc` user
2. Application started: `./mvnw spring-boot:run`
3. App available at `http://localhost:8080`

---

## Happy Path — Register a new user

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq
```

**Expected response (201)**:
```json
{
  "id": 1,
  "firstName": "Saziso",
  "lastName": "Khango",
  "email": "saziso@example.com"
}
```

---

## Duplicate Email — Should return 400

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Other","lastName":"User","email":"saziso@example.com"}' | jq
```

**Expected response (400)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered"
}
```

---

## Blank Field — Should return 400

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"","lastName":"Khango","email":"saziso@example.com"}' | jq
```

**Expected response (400)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "firstName: must not be blank"
}
```

---

## Invalid Email — Should return 400

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"not-valid"}' | jq
```

**Expected response (400)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "email: must be a well-formed email address"
}
```

---

## Run Tests

```bash
./mvnw test
```

All tests should pass with zero failures.
