# GET /api/bonds/{id}

**Feature**: 007-property-bond

## Summary

Retrieve a single property bond plan by its unique ID.

## Request

**Method**: `GET`
**Path**: `/api/bonds/{id}`

### Path Parameters

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| id        | Long | Yes      | Bond record ID     |

**Example**: `GET /api/bonds/1`

## Responses

### 200 OK

```json
{
  "id": 1,
  "userEmail": "user@example.com",
  "title": "Family Home Bond",
  "description": "Primary residence bond repayment",
  "initialAmount": 1200000.00,
  "monthlyContribution": 12000.00,
  "termMonths": 240,
  "interestRate": 11.00,
  "forecastResults": {
    "totalLoanAmount": 1200000.00,
    "totalRepayments": 2150000.00,
    "totalInterestPaid": 950000.00,
    "remainingBalance": 0.00,
    "estimatedPayoffMonth": 240,
    "fullyPaid": true
  },
  "monthlyProjection": [
    {
      "month": 1,
      "startingBalance": 1200000.00,
      "monthlyPayment": 12000.00,
      "interestCharged": 11000.00,
      "principalPaid": 1000.00,
      "endingBalance": 1199000.00
    }
  ]
}
```

### 404 Not Found

Bond with the given ID does not exist.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Property bond not found"
}
```
