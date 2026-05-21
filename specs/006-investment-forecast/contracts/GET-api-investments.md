# Contract: GET /api/investments?userId={userId}

**Feature**: 006-investment-forecast
**Method**: GET
**Path**: `/api/investments`

---

## Request

### Query Parameters

| Parameter | Type | Required | Description                          |
|-----------|------|----------|--------------------------------------|
| userId    | Long | Yes      | Return forecasts for this user only  |

---

## Responses

### 200 OK — Forecasts found

```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "Retirement Growth Plan",
    "description": "Long-term monthly investment",
    "initialAmount": 10000.00,
    "monthlyContribution": 2000.00,
    "termMonths": 60,
    "annualInterestRate": 12.00,
    "forecastResults": {
      "projectedValue": 186245.00,
      "totalContributions": 130000.00,
      "totalInterestEarned": 56245.00,
      "roiPercentage": 43.27,
      "averageMonthlyGrowth": 937.42
    },
    "monthlyProjection": [ ... ]
  }
]
```

### 200 OK — No forecasts for user

```json
[]
```

### 404 Not Found — User does not exist

```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

## Scenarios

| Scenario                          | Status | Response                              |
|-----------------------------------|--------|---------------------------------------|
| User has one or more forecasts    | 200    | Array of InvestmentForecastResponse   |
| User has no forecasts             | 200    | `[]`                                  |
| Non-existent userId               | 404    | Standard error shape                  |
| userId omitted from request       | 400    | Standard error shape (Spring MVC binding error) |
