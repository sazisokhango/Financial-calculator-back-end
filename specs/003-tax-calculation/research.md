# Research: Tax Calculation Engine

**Feature**: 003-tax-calculation
**Date**: 2026-05-20

---

## Decision 1 — Dedicated SarsTaxCalculator Service

**Decision**: Implement a separate `SarsTaxCalculator` Spring `@Service` that holds all
SARS 2024/2025 bracket and rebate constants as named `BigDecimal` fields, and exposes a
single `calculate(TaxCalculationRequest)` method returning a breakdown record.

**Rationale**: Constitution Principle V mandates that bracket/rebate values are stored as
named constants (no magic numbers). Isolating calculation logic in its own service makes
it independently testable and keeps `TaxCalculationService` focused on persistence and
user-lookup concerns.

**Alternatives considered**:
- Static utility class — rejected because static methods cannot be injected or mocked in
  tests, making the constraint hard to verify.
- Constants in `TaxCalculationService` directly — rejected because it conflates business
  logic with persistence orchestration.

---

## Decision 2 — SARS 2024/2025 Bracket Constants

All values stored as named `BigDecimal` fields in `SarsTaxCalculator`:

**Tax Brackets**:

| Bracket | Base Tax | Rate | Threshold |
|---------|----------|------|-----------|
| 1       | 0        | 18%  | 0         |
| 2       | 42,678   | 26%  | 237,100   |
| 3       | 77,362   | 31%  | 370,500   |
| 4       | 121,475  | 36%  | 512,800   |
| 5       | 179,147  | 39%  | 673,000   |
| 6       | 251,258  | 41%  | 857,900   |
| 7       | 644,489  | 45%  | 1,817,000 |

**Rebates**:

| Rebate    | Amount  | Age Condition |
|-----------|---------|---------------|
| Primary   | 17,235  | All           |
| Secondary | 9,444   | Age 65–74     |
| Tertiary  | 3,145   | Age 75+       |

**Verified test values (for spec acceptance scenarios)**:

- Salary R500,000, age 35 → taxBeforeRebate = R117,507 | rebate = R17,235 | finalTaxLiability = R100,272
- Salary R500,000, age 70 → taxBeforeRebate = R117,507 | rebate = R26,679 | finalTaxLiability = R90,828
- Salary R500,000, age 75 → taxBeforeRebate = R117,507 | rebate = R29,824 | finalTaxLiability = R87,683
- netTaxableIncome R436,000, age 40 → taxBeforeRebate = R97,667 | rebate = R17,235 | finalTaxLiability = R80,432

---

## Decision 3 — BigDecimal for All Monetary Values

**Decision**: All monetary fields use `BigDecimal`. Arithmetic uses `BigDecimal.add()`,
`subtract()`, `multiply()`. Division uses `RoundingMode.HALF_UP` with scale 2.

**Rationale**: Constitution Principle V explicitly forbids `double` and `float` for monetary
values. `BigDecimal` is the only acceptable type for SARS calculations where cent-level
precision matters.

**Alternatives considered**: None — mandated by constitution.

---

## Decision 4 — Nullable Numeric Fields Default to Zero

**Decision**: In `TaxCalculationRequest`, all numeric income/deduction fields except `age`
are annotated with `@DecimalMin("0.00")` and a `@Builder.Default` / constructor default of
`BigDecimal.ZERO`. Jackson deserialises absent JSON fields as `null`; the service normalises
null → `BigDecimal.ZERO` before calculating.

**Rationale**: Spec FR-003/FR-004 require defaulting to 0 when omitted. Using `@NotNull` would
force clients to always send every field. Normalising in the service keeps the DTO lenient
and the logic explicit.

**Alternatives considered**:
- `@NotNull @DecimalMin` on all fields — rejected because it breaks the "omit = 0" contract.

---

## Decision 5 — TaxCalculation Entity Stores Inputs + Computed Breakdown

**Decision**: The `TaxCalculation` JPA entity stores all input fields and all 7 computed
breakdown fields. No recalculation happens on read — the persisted breakdown is the source
of truth.

**Rationale**: Enables Feature 4 (saved calculations CRUD) to return consistent data without
recomputing. Also means tax bracket changes never retroactively alter historical records.

**Alternatives considered**:
- Store inputs only, recalculate on read — rejected because it would silently change
  historical records if bracket constants are ever updated.

---

## Decision 6 — User Ownership via @ManyToOne

**Decision**: `TaxCalculation` has a `@ManyToOne` relationship to `User`. The service looks
up the `User` by `userId` (throws `UserNotFoundException` if absent) before saving.

**Rationale**: The spec requires `userId` to reference an existing user (FR-001, FR-014).
The relationship enables future ownership-scoped queries in Feature 4.

**Alternatives considered**:
- Store only `userId` as a Long column — rejected because it loses referential integrity
  and makes the join for Feature 4 queries awkward.
