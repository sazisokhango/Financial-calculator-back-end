# Contract: POST /api/investments/forecast

**Feature**: 006-investment-forecast
**Method**: POST
**Path**: `/api/investments/forecast`

---

## Request

### Headers

| Header       | Value              |
|--------------|--------------------|
| Content-Type | application/json   |

### Body

| Field               | Type       | Required | Validation                                  |
|---------------------|------------|----------|---------------------------------------------|
| userId              | Long       | Yes      | Must reference an existing registered user  |
| title               | String     | Yes      | Not blank                                   |
| description         | String     | No       | May be null or omitted                      |
| initialAmount       | BigDecimal | Yes      | >= 0                                        |
| monthlyContribution | BigDecimal | Yes      | >= 0                                        |
| termMonths          | Integer    | Yes      | > 0                                         |
| annualInterestRate  | BigDecimal | Yes      | Between 0 and 100 inclusive                 |

```json
{
  "userId": 1,
  "title": "Retirement Growth Plan",
  "description": "Long-term monthly investment",
  "initialAmount": 10000.00,
  "monthlyContribution": 2000.00,
  "termMonths": 60,
  "annualInterestRate": 12.00
}
```

---

## Responses

### 201 Created — Forecast saved successfully

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
    {
      "month": 1,
      "startingBalance": 10000.00,
      "monthlyContribution": 2000.00,
      "interestEarned": 100.00,
      "endingBalance": 12100.00
    },
    {
      "month": 2,
      "startingBalance": 12100.00,
      "monthlyContribution": 2000.00,
      "interestEarned": 121.00,
      "endingBalance": 14221.00
    }
  ]
}
```

### 400 Bad Request — Validation failure

```json
{ "status": 400, "error": "Bad Request", "message": "termMonths: must be greater than or equal to 1" }
```

### 404 Not Found — User does not exist

```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

## Scenarios

| Scenario                             | Status | Response                          |
|--------------------------------------|--------|-----------------------------------|
| Valid inputs, existing user          | 201    | Full InvestmentForecastResponse   |
| Blank title                          | 400    | Standard error shape              |
| Negative initialAmount               | 400    | Standard error shape              |
| termMonths = 0                       | 400    | Standard error shape              |
| annualInterestRate = 101             | 400    | Standard error shape              |
| annualInterestRate = 0               | 201    | Forecast with all interestEarned = 0 |
| initialAmount = 0, monthlyContribution = 0 | 201 | Forecast with projectedValue = 0 |
| Non-existent userId                  | 404    | Standard error shape              |
