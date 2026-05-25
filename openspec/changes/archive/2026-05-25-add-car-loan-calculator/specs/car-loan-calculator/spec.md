## ADDED Requirements

### Requirement: Calculate financed amount
The system SHALL derive the financed amount as `purchasePrice − initialDeposit + onceOffFee`. If the resulting financed amount is zero or less, the system SHALL return an empty repayment schedule and all summary monetary fields set to zero.

#### Scenario: Standard financed amount
- **WHEN** purchasePrice=500000, initialDeposit=80000, onceOffFee=1500
- **THEN** financedAmount SHALL equal 421500.00

#### Scenario: Deposit covers full purchase price with no once-off fee
- **WHEN** initialDeposit equals purchasePrice and onceOffFee is 0
- **THEN** financedAmount SHALL be 0.00 and no monthly projection entries SHALL be returned

---

### Requirement: Calculate monthly repayment using annuity formula with balloon adjustment
The system SHALL compute the base monthly instalment (PMT) using the standard annuity formula adjusted for the balloon payment: `PMT = (P − BV/(1+r)^n) × [r(1+r)^n / ((1+r)^n − 1)]` where P = financedAmount, BV = balloonPayment, r = monthly interest rate, n = termMonths. The total monthly repayment returned SHALL equal `PMT + adminFee`. For a zero interest rate the system SHALL use `PMT = (P − BV) / n`.

#### Scenario: Standard calculation with balloon payment
- **WHEN** financedAmount=420000, balloonPayment=90000, interestRate=11, termMonths=72, adminFee=69
- **THEN** monthlyRepayment SHALL equal PMT + 69 where PMT is derived from the annuity+balloon formula, and totalRepayments = monthlyRepayment × 72

#### Scenario: Zero interest rate loan
- **WHEN** interestRate=0, financedAmount=120000, balloonPayment=0, termMonths=12
- **THEN** monthlyRepayment SHALL equal 10000.00 + adminFee and totalInterestPaid SHALL equal 0.00

#### Scenario: Monthly repayment too low to reduce balance
- **WHEN** the computed PMT does not exceed the first-month interest charge (i.e. PMT ≤ financedAmount × monthlyRate)
- **THEN** the system SHALL return HTTP 400 with the message "Monthly repayment amount is too low to reduce the loan balance."

---

### Requirement: Produce month-by-month amortisation schedule
The system SHALL produce one projection entry per month from 1 to termMonths. Each entry SHALL contain: month, startingBalance, monthlyRepayment, interestCharged, adminFee, principalPaid, endingBalance. interestCharged SHALL be `startingBalance × monthlyRate` (rounded 2 dp HALF_UP). principalPaid SHALL be `PMT − interestCharged` (admin fee does NOT reduce principal). endingBalance SHALL be `startingBalance − principalPaid`. In the final month the system SHALL additionally deduct the balloonPayment from the ending balance, clamped to a minimum of 0.00.

#### Scenario: Month 1 entry values
- **WHEN** financedAmount=420000, monthlyRate=11/12/100, PMT=8431 (illustrative), adminFee=69
- **THEN** month 1 entry: startingBalance=420000.00, interestCharged=3850.00, principalPaid=4581.00, endingBalance=415419.00, adminFee=69.00, monthlyRepayment=8500.00

#### Scenario: Month 2 opening balance
- **WHEN** month 1 endingBalance=415419.00
- **THEN** month 2 startingBalance SHALL equal 415419.00

#### Scenario: Final month balloon settlement
- **WHEN** balloonPayment=90000 and the loan reaches the final month
- **THEN** the final endingBalance SHALL be reduced by the balloon payment amount, resulting in remainingBalance=0.00 when fully paid

#### Scenario: Final month closing balance with no balloon
- **WHEN** balloonPayment=0 and the loan runs to term
- **THEN** the final entry endingBalance SHALL be 0.00 (any rounding remainder absorbed into the final payment)

---

### Requirement: Return forecast summary
The system SHALL include a forecastResults object in the response containing: financedAmount, monthlyRepayment, totalRepayments, totalInterestPaid, totalFeesPaid, balloonPayment, remainingBalance, estimatedPayoffMonth, fullyPaid. totalRepayments SHALL equal the sum of all monthly repayments. totalInterestPaid SHALL equal the sum of all interestCharged entries. totalFeesPaid SHALL equal adminFee × termMonths. estimatedPayoffMonth SHALL be the month number at which the balance first reaches 0. fullyPaid SHALL be true when remainingBalance equals 0.00.

#### Scenario: Fully paid loan
- **WHEN** the loan balance reaches 0 at or before the final month
- **THEN** fullyPaid=true, remainingBalance=0.00, estimatedPayoffMonth= the month balance reached 0

#### Scenario: Loan not fully settled
- **WHEN** the remaining balance after the final month is greater than 0
- **THEN** fullyPaid=false and remainingBalance reflects the outstanding amount

---

### Requirement: Persist and retrieve car loan calculations
The system SHALL save each submitted car loan calculation and return all persisted fields. Users SHALL be able to list all saved loans and retrieve a single loan by id, including the full monthly projection.

#### Scenario: Create returns 201 with full response
- **WHEN** POST /api/loans is called with valid inputs
- **THEN** the system SHALL respond with HTTP 201 Created and a body containing id, all input fields, forecastResults, and the complete monthlyProjection array

#### Scenario: List all loans
- **WHEN** GET /api/loans is called
- **THEN** the system SHALL return HTTP 200 with a JSON array of all saved loan calculations

#### Scenario: Get by id
- **WHEN** GET /api/loans/{id} is called with an existing id
- **THEN** the system SHALL return HTTP 200 with the full loan detail including all projection entries

#### Scenario: Get by id not found
- **WHEN** GET /api/loans/{id} is called with a non-existent id
- **THEN** the system SHALL return HTTP 404 Not Found

#### Scenario: Update loan
- **WHEN** PUT /api/loans/{id} is called with updated inputs
- **THEN** the system SHALL recalculate and return HTTP 200 with the updated loan and fresh projection

#### Scenario: Delete loan
- **WHEN** DELETE /api/loans/{id} is called
- **THEN** the system SHALL return HTTP 204 No Content and the loan SHALL no longer be retrievable

---

### Requirement: Input validation
The system SHALL validate all inputs and return HTTP 400 Bad Request with descriptive error messages for any violation.

#### Scenario: Empty title
- **WHEN** title is blank or missing
- **THEN** the system SHALL return HTTP 400 with a message indicating title is required

#### Scenario: Purchase price not positive
- **WHEN** purchasePrice is 0 or negative
- **THEN** the system SHALL return HTTP 400

#### Scenario: Initial deposit exceeds purchase price
- **WHEN** initialDeposit is greater than purchasePrice
- **THEN** the system SHALL return HTTP 400

#### Scenario: Balloon payment exceeds financed amount
- **WHEN** balloonPayment is greater than financedAmount
- **THEN** the system SHALL return HTTP 400

#### Scenario: Term not positive
- **WHEN** termMonths is less than 1
- **THEN** the system SHALL return HTTP 400

#### Scenario: Interest rate out of range
- **WHEN** interestRate is less than 0 or greater than 100
- **THEN** the system SHALL return HTTP 400

#### Scenario: Negative fee fields
- **WHEN** onceOffFee, adminFee, or balloonPayment is negative
- **THEN** the system SHALL return HTTP 400

---

### Requirement: Error responses include timestamp and message
The system SHALL return error responses as a JSON object containing: httpStatus, message, and timestamp (ISO-8601).

#### Scenario: Validation error response body
- **WHEN** any validation fails and HTTP 400 is returned
- **THEN** the response body SHALL contain a message field describing the violation and a timestamp field

#### Scenario: Not found error response body
- **WHEN** a resource is not found and HTTP 404 is returned
- **THEN** the response body SHALL contain a message field and a timestamp field
