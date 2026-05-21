# Contract: PUT /api/investments/{id}

**Feature**: 006-investment-forecast
**Method**: PUT
**Path**: `/api/investments/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description                    |
|-----------|------|----------|--------------------------------|
| id        | Long | Yes      | ID of the forecast to update   |

### Headers

| Header       | Value            |
|--------------|------------------|
| Content-Type | application/json |

### Body

Same fields as `POST /api/investments/forecast`. All fields required. `userId` must match the existing forecast's owner.

```json
{
  "userId": 1,
  "title": "Updated Retirement Plan",
  "description": "Revised after pay rise",
  "initialAmount": 10000.00,
  "monthlyContribution": 3000.00,
  "termMonths": 60,
  "annualInterestRate": 12.00
}
```

---

## Responses

### 200 OK — Forecast updated and recalculated

```json
{
  "id": 1,
  "userId": 1,
  "title": "Updated Retirement Plan",
  "description": "Revised after pay rise",
  "initialAmount": 10000.00,
  "monthlyContribution": 3000.00,
  "termMonths": 60,
  "annualInterestRate": 12.00,
  "forecastResults": {
    "projectedValue": 226445.00,
    "totalContributions": 190000.00,
    "totalInterestEarned": 36445.00,
    "roiPercentage": 19.18,
    "averageMonthlyGrowth": 607.42
  },
  "monthlyProjection": [ ... ]
}
```

### 400 Bad Request — Validation failure or ownership mismatch

```json
{ "status": 400, "error": "Bad Request", "message": "annualInterestRate: must be less than or equal to 100" }
```

### 404 Not Found — Forecast does not exist

```json
{ "status": 404, "error": "Not Found", "message": "Investment forecast not found" }
```

---

## Scenarios

| Scenario                                       | Status | Response                          |
|------------------------------------------------|--------|-----------------------------------|
| Valid update, existing forecast, matching user | 200    | Updated InvestmentForecastResponse with recalculated results |
| Invalid field (e.g., termMonths = 0)           | 400    | Standard error shape              |
| userId does not match existing owner           | 400    | Standard error shape              |
| Forecast id does not exist                     | 404    | Standard error shape              |
