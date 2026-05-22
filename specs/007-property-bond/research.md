# Research: Property Bond

**Feature**: 007-property-bond | **Date**: 2026-05-22

---

## Decision 1 — User Identity: userEmail vs userId

**Decision**: Use `userEmail` (String) to identify the requesting user in all bond requests and query parameters.

**Rationale**: PR #8 (`bufix/fixing-get-by-id-to-email`) explicitly migrated user lookup to email-based identification. `UserRepository.findByEmailIgnoreCase(String)` is already available. The spec and PRD both specify `userEmail`. This is the forward direction for the project.

**Alternatives considered**: `userId` (Long) — used in InvestmentForecast (Feature 5). Rejected because it contradicts the migration direction established in PR #8 and the PRD for this feature.

---

## Decision 2 — Calculation: amortization formula

**Decision**: Standard declining-balance amortization with clamping.

```
monthlyRate       = interestRate / 12 / 100
interestCharged   = startingBalance × monthlyRate   (HALF_UP, scale 2)
principalPaid     = monthlyContribution − interestCharged
```

If `principalPaid > startingBalance` (final month over-payment):
- `principalPaid` is capped to `startingBalance`
- actual payment for that month = `startingBalance + interestCharged`
- `endingBalance` = 0
- projection stops contributing further entries (balance is 0)

**Summary fields**:
```
totalLoanAmount      = initialAmount
totalRepayments      = sum of actual payments made up to payoff
totalInterestPaid    = sum of all interestCharged values
remainingBalance     = MAX(0, endingBalance at termMonths)
estimatedPayoffMonth = first month where endingBalance ≤ 0; else termMonths
fullyPaid            = remainingBalance == 0
```

**Rationale**: Matches the PRD example (month 1: 1,200,000 × 11%/12 = 11,000 interestCharged, 1,000 principalPaid, 1,199,000 endingBalance ✓). Capping prevents negative balances in the final month.

**Alternatives considered**: Fixed-payment formula for exact bond repayment — would auto-calculate the monthly payment from principal, rate, and term. Rejected because the spec explicitly states `monthlyContribution` is user-supplied; the system does not auto-calculate the recommended repayment amount.

---

## Decision 3 — Persistence: denormalized summary on parent entity

**Decision**: Store all six summary result fields (`totalLoanAmount`, `totalRepayments`, `totalInterestPaid`, `remainingBalance`, `estimatedPayoffMonth`, `fullyPaid`) as columns on the `property_bonds` table. Monthly projection rows stored in a child table `bond_monthly_projections`.

**Rationale**: Matches the established pattern from `InvestmentForecast` (Feature 5) and `TaxCalculation` (Feature 3–4). No additional abstraction layers introduced beyond what the constitution permits.

**Alternatives considered**: Storing results in a separate summary table — adds a join for no benefit at this scale. Storing projection as JSON column — loses queryability and diverges from project pattern. Both rejected.

---

## Decision 4 — Calculator: dedicated @Service

**Decision**: `PropertyBondCalculator` (@Service) encapsulates the amortization loop, returning a `BondCalculationResult` record with the full projection and summary values. Called by `PropertyBondService`.

**Rationale**: Constitution Principle II mandates calculation as injectable `@Service`. Consistent with `InvestmentForecastCalculator` / `SarsTaxCalculator` pattern.

---

## Decision 5 — Naming conventions

| Layer      | Class name                      | Notes                              |
|------------|---------------------------------|------------------------------------|
| Controller | `PropertyBondController`        | `@RequestMapping("/api/bonds")`    |
| Service    | `PropertyBondService`           | orchestration + user lookup        |
| Calculator | `PropertyBondCalculator`        | pure calculation, no DB access     |
| Repository | `PropertyBondRepository`        | `JpaRepository<PropertyBond, Long>`|
| Entity     | `PropertyBond`                  | table `property_bonds`             |
| Entity     | `BondMonthlyProjection`         | table `bond_monthly_projections`   |
| DTO        | `PropertyBondRequest`           | request body                       |
| DTO        | `PropertyBondResponse`          | response body                      |
| DTO        | `BondForecastResultDto`         | nested summary object              |
| DTO        | `BondMonthlyProjectionDto`      | nested projection entry            |
| Exception  | `PropertyBondNotFoundException` | → 404 via GlobalExceptionHandler   |
| Test       | `PropertyBondSpec`              | `src/test/.../bond/`               |
