# Contract: GET /api/tax/{id}

**Feature**: 004-saved-calculations
**Method**: GET
**Path**: `/api/tax/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description          |
|-----------|------|----------|----------------------|
| id        | Long | Yes      | Calculation id       |

---

## Responses

### 200 OK — Found

```json
{
  "id": 1, "userId": 1, "title": "My 2025 Tax",
  "salary": 500000.00, "age": 35,
  "totalIncome": 500000.00, "totalDeductions": 0.00,
  "netTaxableIncome": 500000.00, "taxBeforeRebate": 117507.00,
  "rebate": 17235.00, "taxAlreadyPaid": 0.00, "finalTaxLiability": 100272.00
}
```

### 404 Not Found

```json
{ "status": 404, "error": "Not Found", "message": "Calculation not found" }
```

---

## Scenarios

| Scenario              | Status | Response body                  |
|-----------------------|--------|--------------------------------|
| Valid id, exists      | 200    | Full TaxCalculationResponse    |
| Valid id, not found   | 404    | Standard error shape           |
