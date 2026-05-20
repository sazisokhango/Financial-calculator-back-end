# Data Model: Tax Calculation Engine

**Feature**: 003-tax-calculation
**Date**: 2026-05-20

---

## Entity: TaxCalculation

Persists all inputs and the full computed breakdown for one tax calculation, linked to
the owning User.

**Table name**: `tax_calculations`

### Input Fields

| Field              | Type       | Constraints                  | Notes                       |
|--------------------|------------|------------------------------|-----------------------------|
| id                 | Long       | PK, auto-generated           |                             |
| user               | User       | NOT NULL, ManyToOne          | FK → users.id               |
| title              | String     | NOT NULL, NOT BLANK          | Max 255 chars               |
| description        | String     | Nullable                     |                             |
| salary             | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| interestIncome     | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| dividend           | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| capitalGain        | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| bonus              | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| retirementAnnuity  | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |
| age                | Integer    | NOT NULL, >= 0               |                             |
| taxAlreadyPaid     | BigDecimal | NOT NULL, >= 0               | Defaults to 0 if omitted    |

### Computed Breakdown Fields (persisted)

| Field              | Type       | Notes                              |
|--------------------|------------|------------------------------------|
| totalIncome        | BigDecimal | salary + interest + div + cg + bonus |
| totalDeductions    | BigDecimal | retirementAnnuity                  |
| netTaxableIncome   | BigDecimal | MAX(0, totalIncome − deductions)   |
| taxBeforeRebate    | BigDecimal | SARS bracket applied to NTI        |
| rebate             | BigDecimal | Age-based rebate total             |
| finalTaxLiability  | BigDecimal | MAX(0, taxBefore − rebate − paid)  |

---

## DTOs

### TaxCalculationRequest (inbound)

| Field              | Type       | Required | Validation          | Default    |
|--------------------|------------|----------|---------------------|------------|
| userId             | Long       | Yes      | Not null            | —          |
| title              | String     | Yes      | @NotBlank           | —          |
| description        | String     | No       | —                   | null       |
| salary             | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| interestIncome     | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| dividend           | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| capitalGain        | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| bonus              | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| retirementAnnuity  | BigDecimal | No       | @DecimalMin("0.00") | 0          |
| age                | Integer    | Yes      | @NotNull, @Min(0)   | —          |
| taxAlreadyPaid     | BigDecimal | No       | @DecimalMin("0.00") | 0          |

### TaxCalculationResponse (outbound)

All input fields + id + userId + all 7 breakdown fields:

| Field              | Type       |
|--------------------|------------|
| id                 | Long       |
| userId             | Long       |
| title              | String     |
| description        | String     |
| salary             | BigDecimal |
| interestIncome     | BigDecimal |
| dividend           | BigDecimal |
| capitalGain        | BigDecimal |
| bonus              | BigDecimal |
| retirementAnnuity  | BigDecimal |
| age                | Integer    |
| taxAlreadyPaid     | BigDecimal |
| totalIncome        | BigDecimal |
| totalDeductions    | BigDecimal |
| netTaxableIncome   | BigDecimal |
| taxBeforeRebate    | BigDecimal |
| rebate             | BigDecimal |
| finalTaxLiability  | BigDecimal |

---

## Relationships

```
User ──< TaxCalculation
(one user owns many tax calculations)
```

The `TaxCalculation.user` field is a `@ManyToOne(fetch = LAZY)` to `User`.
`User` has no `@OneToMany` back-reference in this feature (added in Feature 4 if needed).

---

## Database Schema

```sql
CREATE TABLE tax_calculations (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL REFERENCES users(id),
    title                VARCHAR(255) NOT NULL,
    description          TEXT,
    salary               NUMERIC(15,2) NOT NULL DEFAULT 0,
    interest_income      NUMERIC(15,2) NOT NULL DEFAULT 0,
    dividend             NUMERIC(15,2) NOT NULL DEFAULT 0,
    capital_gain         NUMERIC(15,2) NOT NULL DEFAULT 0,
    bonus                NUMERIC(15,2) NOT NULL DEFAULT 0,
    retirement_annuity   NUMERIC(15,2) NOT NULL DEFAULT 0,
    age                  INTEGER NOT NULL,
    tax_already_paid     NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_income         NUMERIC(15,2) NOT NULL,
    total_deductions     NUMERIC(15,2) NOT NULL,
    net_taxable_income   NUMERIC(15,2) NOT NULL,
    tax_before_rebate    NUMERIC(15,2) NOT NULL,
    rebate               NUMERIC(15,2) NOT NULL,
    final_tax_liability  NUMERIC(15,2) NOT NULL
);
```
