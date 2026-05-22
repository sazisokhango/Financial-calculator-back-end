# PUT /api/bonds/{id}

**Feature**: 007-property-bond

## Summary

Update an existing property bond plan. The bond is fully recalculated from the new inputs and the updated record is persisted.

## Request

**Method**: `PUT`
**Path**: `/api/bonds/{id}`
**Content-Type**: `application/json`

### Path Parameters

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| id        | Long | Yes      | Bond record ID     |

### Request Body

Same fields as `POST /api/bonds`.

```json
{
  "userEmail": "user@example.com",
  "title": "Updated Bond Title",
  "description": "Revised repayment plan",
  "initialAmount": 1200000.00,
  "monthlyContribution": 15000.00,
  "termMonths": 240,
  "interestRate": 11.00
}
```

## Responses

### 200 OK

Updated bond record with fully recalculated `forecastResults` and `monthlyProjection`.

```json
{
  "id": 1,
  "userEmail": "user@example.com",
  "title": "Updated Bond Title",
  "description": "Revised repayment plan",
  "initialAmount": 1200000.00,
  "monthlyContribution": 15000.00,
  "termMonths": 240,
  "interestRate": 11.00,
  "forecastResults": {
    "totalLoanAmount": 1200000.00,
    "totalRepayments": 1950000.00,
    "totalInterestPaid": 750000.00,
    "remainingBalance": 0.00,
    "estimatedPayoffMonth": 180,
    "fullyPaid": true
  },
  "monthlyProjection": [...]
}
```

### 400 Bad Request

Validation failure.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "termMonths: must be greater than or equal to 1"
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
