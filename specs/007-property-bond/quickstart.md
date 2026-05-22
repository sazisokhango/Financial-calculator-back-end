# Quickstart: Property Bond

**Feature**: 007-property-bond | **Date**: 2026-05-22

## Prerequisites

- Application running on `http://localhost:8080`
- A registered user (run `POST /api/auth/register` first if needed)

## 1. Register a user (if needed)

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}' | jq
```

## 2. Create a property bond

```bash
curl -s -X POST http://localhost:8080/api/bonds \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "saziso@example.com",
    "title": "Family Home Bond",
    "description": "Primary residence bond repayment",
    "initialAmount": 1200000.00,
    "monthlyContribution": 12000.00,
    "termMonths": 240,
    "interestRate": 11.00
  }' | jq
```

Expected: HTTP 201 with `forecastResults` and `monthlyProjection` array.

## 3. Get all bonds for a user

```bash
curl -s "http://localhost:8080/api/bonds?userEmail=saziso@example.com" | jq
```

## 4. Get a specific bond

```bash
curl -s http://localhost:8080/api/bonds/1 | jq
```

## 5. Update a bond

```bash
curl -s -X PUT http://localhost:8080/api/bonds/1 \
  -H "Content-Type: application/json" \
  -d '{
    "userEmail": "saziso@example.com",
    "title": "Family Home Bond (Updated)",
    "initialAmount": 1200000.00,
    "monthlyContribution": 15000.00,
    "termMonths": 240,
    "interestRate": 11.00
  }' | jq
```

## 6. Delete a bond

```bash
curl -s -X DELETE http://localhost:8080/api/bonds/1 -w "%{http_code}"
```

Expected: `204`

## Run tests

```bash
./mvnw test -pl . -Dtest=PropertyBondSpec
```

## Build

```bash
./mvnw clean package -DskipTests
```
