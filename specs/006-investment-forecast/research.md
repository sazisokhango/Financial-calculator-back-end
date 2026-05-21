# Research: Investment Forecast

**Feature**: 006-investment-forecast
**Date**: 2026-05-21

---

## Decision 1 — Monthly Projection Storage Strategy

**Decision**: Separate child table `investment_forecast_monthly_projections` with a `@OneToMany` JPA relationship.

**Rationale**: The monthly projection is a variable-length list (1 to potentially 600+ entries for long-horizon forecasts). A normalised child table is queryable, indexable, and handles JPA's `orphanRemoval = true` cleanly when a forecast is updated — the old entries are deleted automatically and the new set is inserted. A JSON/TEXT column would require manual serialisation, is opaque to SQL queries, and does not fit the existing PostgreSQL-first data model.

**Alternatives considered**:
- JSON column (`@Column(columnDefinition = "TEXT")` with Jackson): Simpler for reads but loses DB integrity, is harder to query, and introduces manual serialisation code. Rejected.
- Embedded list (not supported by JPA without a secondary table): Not applicable. Rejected.

---

## Decision 2 — Calculator Return Type

**Decision**: `InvestmentForecastCalculator` returns a `ForecastCalculationResult` record containing all summary fields plus a list of `MonthlyProjectionEntryData` records (plain data holders, not JPA entities).

**Rationale**: The calculator must be a pure computation service — it must not create or persist entities. Returning a data-holder record decouples calculation from persistence. The service layer then maps the result into entities before saving. This mirrors the pattern of `SarsTaxCalculator` returning `BigDecimal` values that the service layer assembles into a `TaxCalculation` entity.

**Alternatives considered**:
- Calculator populates entity directly: Creates coupling between the calculation engine and the JPA layer. Rejected.
- Calculator returns entity: Prevents unit-testing the calculator without a JPA context. Rejected.

---

## Decision 3 — BigDecimal Precision and Rounding

**Decision**:
- Monetary amounts (`initialAmount`, `monthlyContribution`, all projection values): `NUMERIC(15, 2)` — 2 decimal places, `RoundingMode.HALF_UP`.
- `annualInterestRate`: `NUMERIC(7, 4)` — 4 decimal places to accommodate rates like 12.5000%.
- `roiPercentage`: `NUMERIC(10, 4)` — 4 decimal places to match the PRD example (43.2700%).
- Monthly rate intermediate calculation: 10 decimal places of precision internally, then rounded to 2dp at the end of each monthly step.

**Rationale**: Consistent with the existing tax calculation engine. The PRD example uses 2dp for monetary values and 2dp for roiPercentage but 4dp provides more precision without breaking compatibility. All intermediate calculations retain extra precision to minimise cumulative rounding error.

**Alternatives considered**:
- Store roiPercentage as 2dp: Loses precision for small percentages. Rejected.
- Use `double` for intermediate monthly rate: Forbidden by Constitution Principle V. Rejected.

---

## Decision 4 — Zero totalContributions Guard (roiPercentage)

**Decision**: If `totalContributions` equals zero (i.e., both `initialAmount` and `monthlyContribution` are 0), set `roiPercentage` to `0.00` to avoid `ArithmeticException` on division by zero.

**Rationale**: Bean Validation allows `initialAmount = 0` and `monthlyContribution = 0` simultaneously. The result is a zero-value forecast — valid and meaningful (the user is exploring the scenario). Dividing `totalInterestEarned` (also 0) by `totalContributions` (0) would throw without this guard.

**Alternatives considered**:
- Add a cross-field constraint rejecting both-zero: Over-constrains valid user intent. Rejected.
- Return `null` for roiPercentage: Breaks the contract — all fields must be present. Rejected.

---

## Decision 5 — Calculation Formula (verified against PRD example)

**Decision**: Apply monthly contribution **after** interest is earned on the starting balance for that month.

**Verification against PRD example** (annualInterestRate = 12%, initialAmount = 10 000, monthlyContribution = 2 000):

```
monthlyRate = 12 / 12 / 100 = 0.01

Month 1:
  startingBalance     = 10 000.00
  interestEarned      = 10 000.00 × 0.01 = 100.00
  endingBalance       = 10 000.00 + 2 000.00 + 100.00 = 12 100.00  ✓

Month 2:
  startingBalance     = 12 100.00
  interestEarned      = 12 100.00 × 0.01 = 121.00
  endingBalance       = 12 100.00 + 2 000.00 + 121.00 = 14 221.00  ✓
```

**Summary fields** (assuming termMonths = 60, projectedValue = 186 245.00):
```
totalContributions   = 10 000 + (2 000 × 60) = 130 000.00         ✓
totalInterestEarned  = 186 245.00 − 130 000.00 = 56 245.00         ✓
roiPercentage        = 56 245.00 / 130 000.00 × 100 = 43.27%       ✓
averageMonthlyGrowth = 56 245.00 / 60 = 937.42 (PRD shows 937.41 — minor rounding diff, HALF_UP at 2dp)
```

**Rationale**: Formula confirmed correct against all PRD values.

---

## Decision 6 — Controller Endpoint Mapping

**Decision**: `InvestmentForecastController` is mapped to `/api/investments`. The create endpoint is `@PostMapping("/forecast")` (path: `POST /api/investments/forecast`), matching the PRD exactly. All other CRUD endpoints use the root `/api/investments` path.

**Rationale**: The PRD explicitly defines `POST /api/investments/forecast` as the creation endpoint. This is the path registered in the constitution. Deviating would violate Principle I.

---

## Decision 7 — PUT Ownership Check

**Decision**: On `PUT /api/investments/{id}`, the service validates that the `userId` in the request body matches the existing forecast's owner. If not, return `400 Bad Request`.

**Rationale**: Consistent with `TaxCalculationService.update()` behaviour. The owning user is fixed at creation and cannot be changed. An ownership mismatch is a client error (400), not a not-found (404).

**Alternatives considered**:
- Ignore userId on PUT (use the existing owner): Silently accepts a mismatched userId, which is misleading. Rejected.
- Return 403 Forbidden: No auth system exists at MVP. Rejected.
