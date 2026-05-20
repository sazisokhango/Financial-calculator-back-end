# Quickstart: Tax Calculation Engine

**Feature**: 003-tax-calculation

---

## Prerequisites

1. PostgreSQL running — `financial_calculator` DB, user `tax_calc`
2. App started: `./mvnw spring-boot:run`
3. A registered user (register one first if needed)

---

## Setup — Register a user

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq
# Note the returned "id" — use it as userId below
```

---

## Happy Path — Salary only, age 35

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "My 2025 Tax",
    "salary": 500000,
    "age": 35
  }' | jq
```

**Expected (201)**:
```json
{
  "id": 1,
  "userId": 1,
  "title": "My 2025 Tax",
  "salary": 500000.00,
  "totalIncome": 500000.00,
  "totalDeductions": 0.00,
  "netTaxableIncome": 500000.00,
  "taxBeforeRebate": 117507.00,
  "rebate": 17235.00,
  "taxAlreadyPaid": 0.00,
  "finalTaxLiability": 100272.00
}
```

---

## Age 65–74 — Secondary rebate applied

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "Senior Tax", "salary": 500000, "age": 70}' | jq
```

**Expected**: `rebate: 26679.00`, `finalTaxLiability: 90828.00`

---

## Age 75+ — All three rebates

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "Tertiary Tax", "salary": 500000, "age": 75}' | jq
```

**Expected**: `rebate: 29824.00`, `finalTaxLiability: 87683.00`

---

## Multiple income sources + retirement annuity

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Mixed Income",
    "salary": 400000,
    "bonus": 50000,
    "interestIncome": 10000,
    "retirementAnnuity": 24000,
    "age": 40
  }' | jq
```

**Expected**: `totalIncome: 460000.00`, `totalDeductions: 24000.00`, `netTaxableIncome: 436000.00`, `taxBeforeRebate: 97667.00`, `finalTaxLiability: 80432.00`

---

## Validation — Missing title → 400

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "salary": 500000, "age": 35}' | jq
```

**Expected**: `{ "status": 400, "error": "Bad Request", "message": "title: must not be blank" }`

---

## Validation — Negative salary → 400

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "Test", "salary": -1000, "age": 35}' | jq
```

**Expected**: `{ "status": 400, "error": "Bad Request" }`

---

## Non-existent userId → 404

```bash
curl -s -X POST http://localhost:8080/api/tax \
  -H "Content-Type: application/json" \
  -d '{"userId": 999, "title": "Test", "age": 35}' | jq
```

**Expected**: `{ "status": 404, "error": "Not Found", "message": "User not found" }`

---

## Run Tests

```bash
./mvnw test
```

All tests (Features 1, 2, 3) should pass with zero failures.
