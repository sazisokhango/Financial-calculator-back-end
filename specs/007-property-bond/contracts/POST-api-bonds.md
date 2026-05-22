# POST /api/bonds

**Feature**: 007-property-bond

## Summary

Create a new property bond repayment plan for a registered user. The bond is calculated and fully persisted.

## Request

**Method**: `POST`
**Path**: `/api/bonds`
**Content-Type**: `application/json`

### Request Body

```json
{
  "userEmail": "user@example.com",
  "title": "Family Home Bond",
  "description": "Primary residence bond repayment",
  "initialAmount": 1200000.00,
  "monthlyContribution": 12000.00,
  "termMonths": 240,
  "interestRate": 11.00
}
```

| Field               | Type       | Required | Validation                          |
|---------------------|------------|----------|-------------------------------------|
| userEmail           | String     | Yes      | Not blank, valid email format       |
| title               | String     | Yes      | Not blank                           |
| description         | String     | No       | —                                   |
| initialAmount       | BigDecimal | Yes      | >= 0                                |
| monthlyContribution | BigDecimal | Yes      | >= 0                                |
| termMonths          | Integer    | Yes      | > 0 (min 1)                         |
| interestRate        | BigDecimal | Yes      | >= 0 and <= 100                     |

## Responses

### 201 Created

Bond successfully created.

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
    },
    {
      "month": 2,
      "startingBalance": 1199000.00,
      "monthlyPayment": 12000.00,
      "interestCharged": 10990.00,
      "principalPaid": 1010.00,
      "endingBalance": 1197990.00
    }
  ]
}
```

### 400 Bad Request

Validation failure.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "title: must not be blank"
}
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
