# Product Requirements Document
## South African Tax Calculator API

**Version**: 1.0.0
**Date**: 2026-05-20
**Status**: Approved

---

## 1. Overview

A Spring Boot RESTful API backed by PostgreSQL that enables users to register, manage their identity, perform South African tax calculations, persist those calculations for future reference, and model investment growth through compound-interest forecasting.

The application follows a simplified user-selection flow: users register once, then identify themselves by selecting their name from the user list — no password authentication.

---

## 2. Goals

- Provide a correct, SARS 2024/2025-compliant tax calculation engine
- Allow users to persist, retrieve, update, and delete their tax calculations
- Allow users to create, persist, and manage compound-interest investment forecasts
- Expose a clean REST API consumable by the Angular front-end
- Enforce strict input validation and return consistent error responses

---

## 3. Users & Flow

```
Register → Select Name (GET /api/user) → View / Create Calculations or Forecasts
```

1. User registers with first name, last name, and email
2. User sees all registered users and selects their own name
3. User creates tax calculations or investment forecasts scoped to their identity
4. User can view, update, and delete their own saved calculations and forecasts

---

## 4. Features

---

### Feature 1 — User Registration

**Endpoint**: `POST /api/auth/register`

**Request Body**:
| Field      | Type   | Required | Rules                         |
|------------|--------|----------|-------------------------------|
| firstName  | String | Yes      | Not blank                     |
| lastName   | String | Yes      | Not blank                     |
| email      | String | Yes      | Valid email format, unique    |

**Responses**:
| Status | Condition                                      |
|--------|------------------------------------------------|
| 201    | User successfully registered                   |
| 400    | Validation failed / empty fields               |
| 400    | Email already registered                       |

**Response Body (201)**:
```json
{
  "id": 1,
  "firstName": "Saziso",
  "lastName": "Khango",
  "email": "saziso@example.com"
}
```

**Error Body (400)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered"
}
```

---

### Feature 2 — User Management

#### Get All Users
**Endpoint**: `GET /api/user`
**Description**: Returns all registered users. Used by the front-end to display a name selection list.

**Response (200)**:
```json
[
  { "id": 1, "firstName": "Saziso", "lastName": "Khango", "email": "saziso@example.com" },
  { "id": 2, "firstName": "John",   "lastName": "Doe",    "email": "john@example.com" }
]
```

#### Get Single User
**Endpoint**: `GET /api/user/{id}`

**Response (200)**:
```json
{ "id": 1, "firstName": "Saziso", "lastName": "Khango", "email": "saziso@example.com" }
```

**Response (404)**:
```json
{ "status": 404, "error": "Not Found", "message": "User not found" }
```

---

### Feature 3 — Tax Calculation Engine

#### Calculation Input Fields
| Field              | Type           | Required | Default | Rules         |
|--------------------|----------------|----------|---------|---------------|
| title              | String         | Yes      | —       | Not blank     |
| description        | String         | No       | —       | —             |
| salary             | BigDecimal     | No       | 0       | >= 0          |
| interestIncome     | BigDecimal     | No       | 0       | >= 0          |
| dividend           | BigDecimal     | No       | 0       | >= 0          |
| capitalGain        | BigDecimal     | No       | 0       | >= 0          |
| bonus              | BigDecimal     | No       | 0       | >= 0          |
| retirementAnnuity  | BigDecimal     | No       | 0       | >= 0          |
| age                | Integer        | Yes      | —       | >= 0          |
| taxAlreadyPaid     | BigDecimal     | No       | 0       | >= 0          |

#### Calculation Logic

**Step 1 — Total Gross Income**
```
Total Income = Salary + Interest Income + Dividend + Capital Gain + Bonus
```

**Step 2 — Total Deductions**
```
Total Deductions = Retirement Annuity
```

**Step 3 — Net Taxable Income**
```
Net Taxable Income = MAX(0, Total Income - Total Deductions)
```

**Step 4 — Tax Before Rebate (SARS 2024/2025 Brackets)**

| Taxable Income             | Rate                                      |
|----------------------------|-------------------------------------------|
| R0 – R237,100              | 18%                                       |
| R237,101 – R370,500        | R42,678 + 26% of amount over R237,100     |
| R370,501 – R512,800        | R77,362 + 31% of amount over R370,500     |
| R512,801 – R673,000        | R121,475 + 36% of amount over R512,800    |
| R673,001 – R857,900        | R179,147 + 39% of amount over R673,000    |
| R857,901 – R1,817,000      | R251,258 + 41% of amount over R857,900    |
| R1,817,001+                | R644,489 + 45% of amount over R1,817,000  |

**Step 5 — Rebates (SARS 2024/2025)**

| Rebate    | Age Condition | Amount    |
|-----------|---------------|-----------|
| Primary   | All taxpayers | R17,235   |
| Secondary | Age 65–74     | R9,444    |
| Tertiary  | Age 75+       | R3,145    |

Rebate applied:
- Age < 65: Primary rebate only
- Age 65–74: Primary + Secondary
- Age 75+: Primary + Secondary + Tertiary

**Step 6 — Final Tax Liability**
```
Final Tax Liability = MAX(0, Tax Before Rebate - Rebate - Tax Already Paid)
```

#### Calculation Response
```json
{
  "totalIncome":       450000.00,
  "totalDeductions":   24000.00,
  "netTaxableIncome":  426000.00,
  "taxBeforeRebate":   109598.00,
  "rebate":            17235.00,
  "taxAlreadyPaid":    5000.00,
  "finalTaxLiability": 87363.00
}
```

---

### Feature 4 — Saved Calculations (CRUD)

All endpoints are scoped to a specific user via `userId` in the request body or path.

#### Save Calculation
**Endpoint**: `POST /api/tax`

**Request Body**: All calculation input fields (Feature 3) plus `userId`.

**Response (201)**: Saved calculation with full tax breakdown + `id`.

**Response (400)**: Validation failure.

#### View All Calculations (for a user)
**Endpoint**: `GET /api/tax?userId={userId}`

**Response (200)**: Array of saved calculations belonging to that user.

#### View Single Calculation
**Endpoint**: `GET /api/tax/{id}`

**Response (200)**: Single saved calculation.

**Response (404)**: Calculation not found.

#### Update Calculation
**Endpoint**: `PUT /api/tax/{id}`

**Request Body**: Same as POST — tax is recalculated on save.

**Response (200)**: Updated calculation with recalculated tax breakdown.

**Response (404)**: Calculation not found.

#### Delete Calculation
**Endpoint**: `DELETE /api/tax/{id}`

**Response (204)**: Deleted successfully.

**Response (404)**: Calculation not found.

---

### Feature 5 — Investment Forecast (CRUD)

Allow users to create a compound-interest investment forecast, persist it, and manage it with full CRUD operations. Each forecast is scoped to a registered user.

#### Input Fields

| Field                | Type       | Required | Rules                        |
|----------------------|------------|----------|------------------------------|
| userId               | Long       | Yes      | Must reference an existing user |
| title                | String     | Yes      | Not blank                    |
| description          | String     | No       | —                            |
| initialAmount        | BigDecimal | Yes      | >= 0                         |
| monthlyContribution  | BigDecimal | Yes      | >= 0                         |
| termMonths           | Integer    | Yes      | > 0                          |
| annualInterestRate   | BigDecimal | Yes      | Between 0 and 100 (inclusive)|

#### Calculation Logic

**Monthly rate**
```
monthlyRate = annualInterestRate / 12 / 100
```

**Per-month projection** (iterate month 1 → termMonths)
```
startingBalance(1)   = initialAmount
interestEarned(m)    = startingBalance(m) × monthlyRate
endingBalance(m)     = startingBalance(m) + monthlyContribution + interestEarned(m)
startingBalance(m+1) = endingBalance(m)
```

**Summary results**
```
projectedValue       = endingBalance(termMonths)
totalContributions   = initialAmount + (monthlyContribution × termMonths)
totalInterestEarned  = projectedValue − totalContributions
roiPercentage        = (totalInterestEarned / totalContributions) × 100
averageMonthlyGrowth = totalInterestEarned / termMonths
```

`BigDecimal` MUST be used for all monetary and percentage values.

#### Create Forecast

**Endpoint**: `POST /api/investments/forecast`

**Response (201)**: Full forecast record including `id`, `forecastResults`, and complete `monthlyProjection` array.

**Response (400)**: Validation failure.

**Response (404)**: `userId` does not match a registered user.

**Response body (201)**:
```json
{
  "id": 1,
  "title": "Retirement Growth Plan",
  "description": "Long-term monthly investment",
  "userId": 1,
  "forecastResults": {
    "projectedValue": 186245.00,
    "totalContributions": 130000.00,
    "totalInterestEarned": 56245.00,
    "roiPercentage": 43.27,
    "averageMonthlyGrowth": 937.41
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

#### View All Forecasts (for a user)

**Endpoint**: `GET /api/investments?userId={userId}`

**Response (200)**: Array of all saved forecasts belonging to that user (full records including `monthlyProjection`).

**Response (404)**: `userId` does not match a registered user.

#### View Single Forecast

**Endpoint**: `GET /api/investments/{id}`

**Response (200)**: Full forecast record.

**Response (404)**: Forecast not found.

#### Update Forecast

**Endpoint**: `PUT /api/investments/{id}`

**Request Body**: Same fields as POST. The forecast is fully recalculated using the new inputs and the updated record is persisted.

**Response (200)**: Updated forecast with recalculated `forecastResults` and `monthlyProjection`.

**Response (400)**: Validation failure.

**Response (404)**: Forecast not found.

#### Delete Forecast

**Endpoint**: `DELETE /api/investments/{id}`

**Response (204)**: Deleted successfully.

**Response (404)**: Forecast not found.

---

## 5. Standard Error Response

All error responses MUST follow this exact JSON shape:

```json
{ "status": 404, "error": "Not Found", "message": "Descriptive message here" }
```

No endpoint may return a different error structure.

---

## 6. Technical Requirements

| Concern         | Decision                                              |
|-----------------|-------------------------------------------------------|
| Language        | Java 17                                               |
| Framework       | Spring Boot 3.5.0                                     |
| Build           | Maven                                                 |
| Database        | PostgreSQL — DB: `financial_calculator`, user: `tax_calc` |
| Test Database   | H2 in-memory                                          |
| Numeric Types   | `BigDecimal` for all monetary values                  |
| Architecture    | Controller → Service → Repository (3-layer strict)    |
| DTOs            | Entities must NOT be exposed directly in responses    |
| Lombok          | Required — use `@Data`, `@Builder`, `@NoArgsConstructor`, etc. |
| Validation      | Jakarta Bean Validation (`@NotBlank`, `@Min`, etc.)   |
| Error Handling  | Single `@RestControllerAdvice` global exception handler |
| Base Package    | `com.psybergate.financialcalculator`                  |

---

## 7. Feature Roadmap

| # | Feature                    | Priority |
|---|----------------------------|----------|
| 1 | User Registration          | P1       |
| 2 | User Management            | P2       |
| 3 | Tax Calculation Engine     | P3       |
| 4 | Saved Calculations CRUD    | P4       |
| 5 | Investment Forecast (CRUD) | P5       |

Each feature will be developed on its own branch following the SDD workflow:
`/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`
