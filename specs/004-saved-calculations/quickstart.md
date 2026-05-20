# Quickstart: Saved Calculations CRUD

**Feature**: 004-saved-calculations

---

## Prerequisites

1. PostgreSQL running — `financial_calculator` DB, user `tax_calc`
2. App started: `./mvnw spring-boot:run`
3. Register a user and save a calculation (use Feature 3 endpoints)

---

## Setup

```bash
# Register user (note returned id)
USER_ID=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq '.id')

# Save a calculation (note returned id)
CALC_ID=$(curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"title\":\"My 2025 Tax\",\"salary\":500000,\"age\":35}" | jq '.id')
```

---

## GET /api/tax?userId={userId} — List all calculations

```bash
curl -s "http://localhost:8080/api/tax?userId=$USER_ID" | jq
```

**Expected (200)**: Array containing the saved calculation.

---

## GET /api/tax?userId={userId} — Empty list

```bash
# With a user that has no calculations:
curl -s "http://localhost:8080/api/tax?userId=$USER_ID" | jq
```

**Expected (200)**: `[]`

---

## GET /api/tax/{id} — Single calculation

```bash
curl -s "http://localhost:8080/api/tax/$CALC_ID" | jq
```

**Expected (200)**: Full calculation with all breakdown fields.

---

## GET /api/tax/999 — Not found

```bash
curl -s "http://localhost:8080/api/tax/999" | jq
```

**Expected (404)**: `{ "status": 404, "error": "Not Found", "message": "Calculation not found" }`

---

## PUT /api/tax/{id} — Update salary to R600,000

```bash
curl -s -X PUT "http://localhost:8080/api/tax/$CALC_ID" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"title\":\"Updated Tax\",\"salary\":600000,\"age\":35}" | jq
```

**Expected (200)**: Updated breakdown — `taxBeforeRebate: 152867.00`, `finalTaxLiability: 135632.00`

---

## DELETE /api/tax/{id} — Delete calculation

```bash
curl -s -X DELETE "http://localhost:8080/api/tax/$CALC_ID" -w "%{http_code}"
```

**Expected**: `204`

```bash
# Verify it's gone:
curl -s "http://localhost:8080/api/tax/$CALC_ID" | jq
```

**Expected (404)**: `{ "status": 404, "error": "Not Found", "message": "Calculation not found" }`

---

## Run Tests

```bash
./mvnw test
```

All tests (Features 1–4) should pass with zero failures.
