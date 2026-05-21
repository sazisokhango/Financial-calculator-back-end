# Contract: GET /api/investments/{id}

**Feature**: 006-investment-forecast
**Method**: GET
**Path**: `/api/investments/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description              |
|-----------|------|----------|--------------------------|
| id        | Long | Yes      | ID of the saved forecast |

---

## Responses

### 200 OK — Forecast found

```json
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
  "monthlyProjection": [
    { "month": 1, "startingBalance": 10000.00, "monthlyContribution": 2000.00, "interestEarned": 100.00, "endingBalance": 12100.00 },
    { "month": 2, "startingBalance": 12100.00, "monthlyContribution": 2000.00, "interestEarned": 121.00, "endingBalance": 14221.00 }
  ]
}
```

### 404 Not Found — Forecast does not exist

```json
{ "status": 404, "error": "Not Found", "message": "Investment forecast not found" }
```

---

## Scenarios

| Scenario                       | Status | Response                          |
|--------------------------------|--------|-----------------------------------|
| Forecast with given id exists  | 200    | Full InvestmentForecastResponse   |
| No forecast with given id      | 404    | Standard error shape              |
