# GET /api/bonds

**Feature**: 007-property-bond

## Summary

Retrieve all property bond plans belonging to a registered user, identified by email.

## Request

**Method**: `GET`
**Path**: `/api/bonds`

### Query Parameters

| Parameter | Type   | Required | Description                     |
|-----------|--------|----------|---------------------------------|
| userEmail | String | Yes      | Email of the registered user    |

**Example**: `GET /api/bonds?userEmail=user@example.com`

## Responses

### 200 OK

Returns an array of full bond records (may be empty if user has no bonds).

```json
[
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
    "monthlyProjection": [...]
  }
]
```

### 404 Not Found

`userEmail` does not match a registered user.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found"
}
```
