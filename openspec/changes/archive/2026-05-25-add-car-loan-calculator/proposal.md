## Why

The financial calculator has no vehicle financing tool. South African users need to evaluate car loan affordability — including once-off fees, admin fees, and balloon payments — before committing to a purchase. The backend must calculate and persist full loan repayment plans with month-by-month breakdowns.

## What Changes

- Introduce a **Car Loan Calculator** feature: entity, DTOs, calculator (pure math), service, and controller
- Inputs: `purchasePrice`, `initialDeposit`, `onceOffFee`, `adminFee`, `balloonPayment`, `termMonths`, `interestRate`, `title`, `description`
- Financed amount formula: `purchasePrice − initialDeposit + onceOffFee`
- Monthly repayment computed via the annuity formula adjusted for balloon payment; admin fee added on top each month
- Response includes a forecast summary (financedAmount, monthlyRepayment, totalRepayments, totalInterestPaid, totalFeesPaid, balloonPayment, remainingBalance, estimatedPayoffMonth, fullyPaid) and a month-by-month projection
- Full CRUD endpoints at `/api/loans`
- Edge cases handled: deposit covers full price (no schedule), balloon payment at final month, monthly repayment too low to reduce balance (reject with 400)

## Capabilities

### New Capabilities
- `car-loan-calculator`: Calculate and persist a car loan repayment plan — financed amount (price − deposit + once-off fee), fixed monthly instalment (annuity formula with balloon adjustment + admin fee), full amortisation schedule with interest, admin fee, and principal per month, and balloon settlement in the final month.

### Modified Capabilities
<!-- None -->

## Impact

- **New code**: `CarLoan` entity, `CarLoanMonthlyProjection` entity, `CarLoanRepository`, `CarLoanRequest` / `CarLoanResponse` / `CarLoanMonthlyProjectionDto` / `ForecastResultDto` DTOs, `CarLoanCalculator` (pure math), `CarLoanService`, `CarLoanController`
- **Database**: New `car_loans` and `car_loan_monthly_projections` tables via JPA
- **API**: `/api/loans` — POST, GET (list), GET (by id), PUT, DELETE
- **Tests**: Unit tests for `CarLoanCalculator`; `@SpringBootTest` integration tests for all controller endpoints
- **No breaking changes** to existing features
