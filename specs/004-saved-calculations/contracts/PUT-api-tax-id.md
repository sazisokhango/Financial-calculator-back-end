# Contract: PUT /api/tax/{id}

**Feature**: 004-saved-calculations
**Method**: PUT
**Path**: `/api/tax/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required |
|-----------|------|----------|
| id        | Long | Yes      |

### Body

Same shape as `POST /api/tax`:

```json
{
  "userId": 1,
  "title": "Updated Tax",
  "salary": 600000.00,
  "age": 35
}
```

`userId` is validated (user must exist) but does not change the calculation's owner.

---

## Responses

### 200 OK — Updated with recalculated breakdown

```json
{
  "id": 1, "userId": 1, "title": "Updated Tax",
  "salary": 600000.00, "age": 35,
  "totalIncome": 600000.00, "totalDeductions": 0.00,
  "netTaxableIncome": 600000.00, "taxBeforeRebate": 148627.00,
  "rebate": 17235.00, "taxAlreadyPaid": 0.00, "finalTaxLiability": 131392.00
}
```

### 400 Bad Request — Validation failure

```json
{ "status": 400, "error": "Bad Request", "message": "salary: must be greater than or equal to 0.00" }
```

### 404 Not Found — Calculation does not exist

```json
{ "status": 404, "error": "Not Found", "message": "Calculation not found" }
```

---

## Scenarios

| Scenario                        | Status | Notes                              |
|---------------------------------|--------|------------------------------------|
| Valid update                    | 200    | New breakdown computed and saved   |
| Negative field                  | 400    | Standard error shape               |
| id not found                    | 404    | Standard error shape               |
