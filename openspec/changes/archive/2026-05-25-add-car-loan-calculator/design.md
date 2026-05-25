## Context

The Financial Calculator back-end follows a strict layered pattern per feature: Entity → Repository → Calculator (pure math, `@Service`) → Service → Controller with request/response DTOs. The Car Loan Calculator follows this same pattern. All monetary values use `BigDecimal` with `HALF_UP` rounding and 2 decimal scale on output.

**Key financial formulas** (from requirements):

```
Financed Amount = purchasePrice − initialDeposit + onceOffFee
Monthly Rate    = interestRate / 12 / 100

Base Instalment (PMT, excluding admin fee) — annuity with balloon:
  PMT = (P − BV / (1+r)^n) × [r(1+r)^n / ((1+r)^n − 1)]
  where P = financedAmount, BV = balloonPayment, r = monthlyRate, n = termMonths

Total Monthly Repayment = PMT + adminFee

Per month amortisation:
  interestCharged  = startingBalance × monthlyRate
  principalPaid    = PMT − interestCharged   (admin fee does NOT reduce principal)
  endingBalance    = startingBalance − principalPaid
  Final month only: endingBalance − balloonPayment (balloon settles remaining balance)

Zero-rate edge case: PMT = (P − BV) / n
Zero-financed-amount edge case: no repayment schedule, all summary fields = 0
```

## Goals / Non-Goals

**Goals:**
- Implement `CarLoanCalculator` handling the annuity+balloon formula and producing the full amortisation schedule
- Handle all three edge cases: zero interest rate, financed amount = 0 (deposit covers purchase), repayment too low to reduce balance (reject 400)
- Persist each calculation as `CarLoan` + `CarLoanMonthlyProjection` child entities
- Expose full CRUD at `/api/loans` (consistent with requirement document)
- Unit tests for `CarLoanCalculator`; `@SpringBootTest` integration tests for controller

**Non-Goals:**
- Variable-rate loans or interest rate changes mid-term
- User-scoped URL path (`/api/users/{userId}/car-loans`) — requirements specify `/api/loans`
- Frontend changes

## Decisions

**1. Monthly repayment = PMT + adminFee (admin fee on top, does not reduce principal)**  
Verified from the example response: `principalPaid = monthlyRepayment − interestCharged − adminFee`. Admin fee is a cost, not a capital repayment.

**2. Balloon payment adjusts the annuity PMT via present-value reduction**  
`PMT = (P − PV(balloon)) × annuity_factor` where `PV(balloon) = BV / (1+r)^n`. This matches standard South African vehicle finance practice. The balloon is then applied as a lump-sum deduction in month `n`.

**3. Use `BigDecimal` throughout — no `double` / `Math.pow()`**  
`BigDecimal.pow(n)` with scale 10 for all intermediate calculations to avoid floating-point drift on large loan amounts or long terms.

**4. "Repayment too low" guard in `CarLoanCalculator`**  
If `PMT ≤ interestCharged` in month 1 (i.e., the payment doesn't cover the first month's interest), throw an `IllegalArgumentException`. The service translates this to HTTP 400 with a descriptive message.

**5. API endpoints at `/api/loans` (not user-scoped path)**  
The requirements document specifies flat `/api/loans` endpoints. User association (if needed) follows the existing Property Bond pattern of a request field or query parameter — not a path variable. No userId path param for this feature.

**6. `CarLoanMonthlyProjection` stored as a child entity with `@OneToMany`**  
Mirrors `BondMonthlyProjection`. `CascadeType.ALL` + `orphanRemoval = true`. Ordered by `month_number ASC`.

## Risks / Trade-offs

- **Risk**: `(1+r)^n` for large `n` with high rates can be a very large number — intermediate scale must be sufficient.  
  **Mitigation**: Use scale=10 for intermediate `BigDecimal` operations; final scale=2 only for output fields.

- **Risk**: Balloon payment larger than remaining balance at final month could produce negative balance.  
  **Mitigation**: Validate `balloonPayment ≤ financedAmount` on input (as required). At runtime, clamp ending balance to 0.

- **Trade-off**: Storing up to 360 rows per loan adds write volume on create/update.  
  **Accepted**: Consistent with Property Bond; read-heavy after creation.
