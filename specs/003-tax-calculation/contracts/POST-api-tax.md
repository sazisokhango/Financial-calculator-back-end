# Contract: POST /api/tax

**Feature**: 003-tax-calculation
**Method**: POST
**Path**: `/api/tax`

---

## Request

### Headers

| Header       | Value            | Required |
|--------------|------------------|----------|
| Content-Type | application/json | Yes      |

### Body

```json
{
  "userId": 1,
  "title": "My 2025 Tax",
  "description": "Annual tax estimate",
  "salary": 500000.00,
  "interestIncome": 0,
  "dividend": 0,
  "capitalGain": 0,
  "bonus": 0,
  "retirementAnnuity": 0,
  "age": 35,
  "taxAlreadyPaid": 0
}
```

| Field             | Type       | Required | Validation          |
|-------------------|------------|----------|---------------------|
| userId            | Long       | Yes      | Must exist          |
| title             | String     | Yes      | Not blank           |
| description       | String     | No       | —                   |
| salary            | BigDecimal | No       | >= 0, default 0     |
| interestIncome    | BigDecimal | No       | >= 0, default 0     |
| dividend          | BigDecimal | No       | >= 0, default 0     |
| capitalGain       | BigDecimal | No       | >= 0, default 0     |
| bonus             | BigDecimal | No       | >= 0, default 0     |
| retirementAnnuity | BigDecimal | No       | >= 0, default 0     |
| age               | Integer    | Yes      | >= 0                |
| taxAlreadyPaid    | BigDecimal | No       | >= 0, default 0     |

---

## Responses

### 201 Created — Calculation saved successfully

```json
{
  "id": 1,
  "userId": 1,
  "title": "My 2025 Tax",
  "description": "Annual tax estimate",
  "salary": 500000.00,
  "interestIncome": 0.00,
  "dividend": 0.00,
  "capitalGain": 0.00,
  "bonus": 0.00,
  "retirementAnnuity": 0.00,
  "age": 35,
  "taxAlreadyPaid": 0.00,
  "totalIncome": 500000.00,
  "totalDeductions": 0.00,
  "netTaxableIncome": 500000.00,
  "taxBeforeRebate": 117507.00,
  "rebate": 17235.00,
  "finalTaxLiability": 100272.00
}
```

### 400 Bad Request — Validation failure

```json
{ "status": 400, "error": "Bad Request", "message": "title: must not be blank" }
```

### 400 Bad Request — Negative numeric value

```json
{ "status": 400, "error": "Bad Request", "message": "salary: must be greater than or equal to 0.00" }
```

### 404 Not Found — User does not exist

```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

## Scenarios

| Scenario                              | Status | Notes                                 |
|---------------------------------------|--------|---------------------------------------|
| Valid request, salary only            | 201    | Full breakdown returned               |
| Valid request, multiple income fields | 201    | totalIncome = sum of all income fields |
| Valid request, retirementAnnuity > 0  | 201    | totalDeductions reflected in NTI      |
| Valid request, age 65–74              | 201    | Secondary rebate applied              |
| Valid request, age 75+                | 201    | Tertiary rebate applied               |
| taxAlreadyPaid exceeds tax owed       | 201    | finalTaxLiability = 0 (not negative)  |
| Missing title                         | 400    | Standard error shape                  |
| Negative salary                       | 400    | Standard error shape                  |
| Non-existent userId                   | 404    | Standard error shape                  |
