# Data Model: Property Bond

**Feature**: 007-property-bond | **Date**: 2026-05-22

---

## Entities

### PropertyBond

**Table**: `property_bonds`

| Column               | Java Type    | DB Type                    | Nullable | Notes                         |
|----------------------|--------------|----------------------------|----------|-------------------------------|
| id                   | Long         | BIGSERIAL PK               | No       | Auto-generated                |
| user_id              | Long (FK)    | BIGINT FK → users.id       | No       | ManyToOne, LAZY fetch         |
| title                | String       | VARCHAR(255) NOT NULL      | No       |                               |
| description          | String       | TEXT                       | Yes      |                               |
| initial_amount       | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | Bond/loan principal           |
| monthly_contribution | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | Fixed monthly repayment       |
| term_months          | Integer      | INTEGER NOT NULL           | No       | Loan term in months           |
| interest_rate        | BigDecimal   | NUMERIC(7,4) NOT NULL      | No       | Annual interest rate (0–100)  |
| total_loan_amount    | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | = initialAmount               |
| total_repayments     | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | Sum of all payments made      |
| total_interest_paid  | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | Sum of interestCharged        |
| remaining_balance    | BigDecimal   | NUMERIC(15,2) NOT NULL     | No       | Balance after term (≥ 0)      |
| estimated_payoff_month | Integer    | INTEGER NOT NULL           | No       | First month balance = 0       |
| fully_paid           | Boolean      | BOOLEAN NOT NULL           | No       | remainingBalance == 0         |

**Relationships**:
- `@ManyToOne(fetch = FetchType.LAZY)` → `User` via `user_id`
- `@OneToMany(mappedBy = "bond", cascade = CascadeType.ALL, orphanRemoval = true)` → `BondMonthlyProjection` ordered by `month ASC`

---

### BondMonthlyProjection

**Table**: `bond_monthly_projections`

| Column               | Java Type  | DB Type                        | Nullable | Notes                          |
|----------------------|------------|--------------------------------|----------|--------------------------------|
| id                   | Long       | BIGSERIAL PK                   | No       | Auto-generated                 |
| bond_id              | Long (FK)  | BIGINT FK → property_bonds.id  | No       | ManyToOne, LAZY fetch          |
| month_number         | Integer    | INTEGER NOT NULL               | No       | 1-based month index            |
| starting_balance     | BigDecimal | NUMERIC(15,2) NOT NULL         | No       |                                |
| monthly_payment      | BigDecimal | NUMERIC(15,2) NOT NULL         | No       | Actual payment (may be partial in last month) |
| interest_charged     | BigDecimal | NUMERIC(15,2) NOT NULL         | No       |                                |
| principal_paid       | BigDecimal | NUMERIC(15,2) NOT NULL         | No       |                                |
| ending_balance       | BigDecimal | NUMERIC(15,2) NOT NULL         | No       | ≥ 0 (clamped)                  |

**Relationships**:
- `@ManyToOne(fetch = FetchType.LAZY)` → `PropertyBond` via `bond_id`

---

## DTOs

### PropertyBondRequest

Used for POST (create) and PUT (update) request bodies.

| Field               | Type       | Validation                                          |
|---------------------|------------|-----------------------------------------------------|
| userEmail           | String     | `@NotBlank`, `@Email`                               |
| title               | String     | `@NotBlank`                                         |
| description         | String     | Optional — no constraints                           |
| initialAmount       | BigDecimal | `@NotNull`, `@DecimalMin("0.00")`                   |
| monthlyContribution | BigDecimal | `@NotNull`, `@DecimalMin("0.00")`                   |
| termMonths          | Integer    | `@NotNull`, `@Min(1)`                               |
| interestRate        | BigDecimal | `@NotNull`, `@DecimalMin("0.00")`, `@DecimalMax("100.00")` |

---

### PropertyBondResponse

Returned for all successful create, retrieve, and update operations.

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
  "monthlyProjection": [...]
}
```

---

### BondForecastResultDto

| Field                | Type       |
|----------------------|------------|
| totalLoanAmount      | BigDecimal |
| totalRepayments      | BigDecimal |
| totalInterestPaid    | BigDecimal |
| remainingBalance     | BigDecimal |
| estimatedPayoffMonth | Integer    |
| fullyPaid            | Boolean    |

---

### BondMonthlyProjectionDto

| Field            | Type       |
|------------------|------------|
| month            | Integer    |
| startingBalance  | BigDecimal |
| monthlyPayment   | BigDecimal |
| interestCharged  | BigDecimal |
| principalPaid    | BigDecimal |
| endingBalance    | BigDecimal |

---

## Calculation Algorithm

```
monthlyRate = interestRate / 12 / 100   [scale 10, HALF_UP]

For m = 1 to termMonths:
  if startingBalance == 0:
    record zeroed entry and continue

  interestCharged = startingBalance × monthlyRate  [scale 2, HALF_UP]
  principalPaid   = monthlyContribution − interestCharged

  if principalPaid >= startingBalance:        // final (over)payment
    principalPaid   = startingBalance
    actualPayment   = startingBalance + interestCharged
    endingBalance   = 0
    estimatedPayoffMonth = m (if not set yet)
  else:
    actualPayment   = monthlyContribution
    endingBalance   = startingBalance − principalPaid  [scale 2, HALF_UP]

  record: {m, startingBalance, actualPayment, interestCharged, principalPaid, endingBalance}
  startingBalance = endingBalance

Summary:
  totalLoanAmount      = initialAmount
  totalRepayments      = Σ actualPayment  [scale 2, HALF_UP]
  totalInterestPaid    = Σ interestCharged [scale 2, HALF_UP]
  remainingBalance     = MAX(0, endingBalance at termMonths)
  estimatedPayoffMonth = first m where endingBalance == 0, else termMonths
  fullyPaid            = remainingBalance == 0
```
