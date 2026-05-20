# Contract: GET /api/tax?userId={userId}

**Feature**: 004-saved-calculations
**Method**: GET
**Path**: `/api/tax`

---

## Request

### Query Parameters

| Parameter | Type | Required | Description                    |
|-----------|------|----------|--------------------------------|
| userId    | Long | Yes      | Return calculations for this user |

---

## Responses

### 200 OK — Calculations found

```json
[
  {
    "id": 1, "userId": 1, "title": "My 2025 Tax",
    "salary": 500000.00, "age": 35,
    "totalIncome": 500000.00, "totalDeductions": 0.00,
    "netTaxableIncome": 500000.00, "taxBeforeRebate": 117507.00,
    "rebate": 17235.00, "taxAlreadyPaid": 0.00, "finalTaxLiability": 100272.00
  }
]
```

### 200 OK — No calculations for user

```json
[]
```

### 404 Not Found — User does not exist

```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

## Scenarios

| Scenario                          | Status | Response                    |
|-----------------------------------|--------|-----------------------------|
| User has calculations             | 200    | Array of TaxCalculationResponse |
| User has no calculations          | 200    | `[]`                        |
| userId does not exist             | 404    | Standard error shape        |
