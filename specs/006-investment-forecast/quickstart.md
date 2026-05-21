# Quickstart: Investment Forecast

**Feature**: 006-investment-forecast
**Date**: 2026-05-21

---

## Prerequisites

- PostgreSQL running with database `financial_calculator`, user `tax_calc`, password `password`
- Java 17 installed
- Maven wrapper available (`./mvnw`)

---

## Running the Application

```bash
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`.

---

## Running the Tests

```bash
./mvnw test
```

The `InvestmentForecastSpec` test class uses H2 in-memory (`@ActiveProfiles("test")`). No PostgreSQL required for tests.

---

## Manual Smoke Test

### 1. Register a user

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq .
```

Note the `id` returned (e.g., `1`).

### 2. Create an investment forecast

```bash
curl -s -X POST http://localhost:8080/api/investments/forecast \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Retirement Growth Plan",
    "description": "Long-term monthly investment",
    "initialAmount": 10000.00,
    "monthlyContribution": 2000.00,
    "termMonths": 60,
    "annualInterestRate": 12.00
  }' | jq .
```

**Expected**: `201 Created`. `monthlyProjection` array contains 60 entries.
Verify: `monthlyProjection[0].interestEarned == 100.00`, `monthlyProjection[0].endingBalance == 12100.00`.

Note the returned `id` (e.g., `1`).

### 3. Get all forecasts for user

```bash
curl -s http://localhost:8080/api/investments?userId=1 | jq '.[].forecastResults'
```

**Expected**: `200 OK` with one-item array.

### 4. Get single forecast

```bash
curl -s http://localhost:8080/api/investments/1 | jq .
```

**Expected**: `200 OK` with full record including `monthlyProjection`.

### 5. Update the forecast (increase monthly contribution)

```bash
curl -s -X PUT http://localhost:8080/api/investments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Updated Retirement Plan",
    "initialAmount": 10000.00,
    "monthlyContribution": 3000.00,
    "termMonths": 60,
    "annualInterestRate": 12.00
  }' | jq '.forecastResults'
```

**Expected**: `200 OK`. `projectedValue` is higher than original. `id` is unchanged.

### 6. Delete the forecast

```bash
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/investments/1
```

**Expected**: `204`.

### 7. Confirm deletion

```bash
curl -s http://localhost:8080/api/investments/1 | jq .
```

**Expected**: `404 Not Found`, `"message": "Investment forecast not found"`.

---

## Validation Error Smoke Tests

```bash
# termMonths = 0 (must be > 0) → 400
curl -s -X POST http://localhost:8080/api/investments/forecast \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Test","initialAmount":1000,"monthlyContribution":100,"termMonths":0,"annualInterestRate":10}' | jq .

# annualInterestRate = 101 (max is 100) → 400
curl -s -X POST http://localhost:8080/api/investments/forecast \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Test","initialAmount":1000,"monthlyContribution":100,"termMonths":12,"annualInterestRate":101}' | jq .

# Non-existent userId → 404
curl -s -X POST http://localhost:8080/api/investments/forecast \
  -H "Content-Type: application/json" \
  -d '{"userId":99999,"title":"Test","initialAmount":1000,"monthlyContribution":100,"termMonths":12,"annualInterestRate":10}' | jq .
```
