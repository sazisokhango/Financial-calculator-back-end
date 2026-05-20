# Feature Specification: Tax Calculation Engine

**Feature Branch**: `003-tax-calculation`

**Created**: 2026-05-20

**Status**: Draft

**Input**: Tax Calculation Engine — POST /api/tax calculates South African income tax using
SARS 2024/2025 brackets and saves the result; inputs include title, description, salary,
interestIncome, dividend, capitalGain, bonus, retirementAnnuity, age, taxAlreadyPaid, userId;
numeric fields default to 0 and must be >= 0; response returns full tax breakdown:
totalIncome, totalDeductions, netTaxableIncome, taxBeforeRebate, rebate, taxAlreadyPaid,
finalTaxLiability; age determines primary/secondary/tertiary rebate.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Calculate and Save Tax for a Salaried Employee Under 65 (Priority: P1)

A registered user with only a salary enters their income details and receives a complete
tax breakdown. The calculation is saved to their account for future reference.

**Why this priority**: This is the most common use case — the core value of the application.
A salaried employee under 65 uses only the primary rebate and no additional income sources.

**Independent Test**: Submit a calculation for a user earning R500,000 salary, age 35, no
other income or deductions. Verify the response returns all 7 breakdown fields with
mathematically correct values, and the calculation is assigned an id confirming it was saved.

**Acceptance Scenarios**:

1. **Given** a registered user (id: 1), age 35, salary R500,000, all other fields zero,
   **When** they submit a tax calculation request,
   **Then** the system responds with `201 Created` containing all 7 breakdown fields:
   totalIncome, totalDeductions, netTaxableIncome, taxBeforeRebate, rebate,
   taxAlreadyPaid, finalTaxLiability — plus the saved record's id and the submitted inputs.

2. **Given** a calculation with a `title` of "My 2025 Tax",
   **When** it is submitted and saved,
   **Then** the saved record includes the title and is retrievable by the assigned id.

---

### User Story 2 — Calculate Tax for a User Over 65 (Secondary Rebate) (Priority: P2)

A user aged between 65 and 74 receives a higher rebate than a younger taxpayer, resulting
in a lower final tax liability for the same income.

**Why this priority**: Age-based rebate tiers are a non-negotiable SARS requirement.
Incorrect rebate application would produce legally wrong results.

**Independent Test**: Submit the same salary for a user aged 70 vs a user aged 35.
The user aged 70 must have a higher rebate and a lower final tax liability.

**Acceptance Scenarios**:

1. **Given** a user aged 70 and salary R500,000,
   **When** a calculation is submitted,
   **Then** the rebate equals Primary + Secondary (R17,235 + R9,444 = R26,679) and
   finalTaxLiability is lower than for an equivalent under-65 taxpayer.

2. **Given** a user aged 75 or older,
   **When** a calculation is submitted,
   **Then** the rebate equals Primary + Secondary + Tertiary (R17,235 + R9,444 + R3,145 = R29,824).

---

### User Story 3 — Calculate Tax with Multiple Income Sources and Deductions (Priority: P3)

A user with salary, interest income, dividends, capital gains, a bonus, and a retirement
annuity contribution receives a correct net taxable income after deductions, with tax
calculated on the reduced amount.

**Why this priority**: Real-world users have mixed income — this validates the full
calculation pipeline from gross income through deductions to final liability.

**Independent Test**: Submit salary R400,000, bonus R50,000, interestIncome R10,000,
retirementAnnuity R24,000, age 40. Verify totalIncome = R460,000, totalDeductions = R24,000,
netTaxableIncome = R436,000, and finalTaxLiability reflects the correct bracket calculation.

**Acceptance Scenarios**:

1. **Given** salary R400,000, bonus R50,000, interestIncome R10,000, retirementAnnuity R24,000, age 40,
   **When** a calculation is submitted,
   **Then** totalIncome = R460,000, totalDeductions = R24,000, netTaxableIncome = R436,000,
   and the remaining breakdown fields reflect SARS 2024/2025 brackets applied to R436,000.

2. **Given** a taxAlreadyPaid value of R20,000,
   **When** a calculation is submitted,
   **Then** finalTaxLiability = MAX(0, taxBeforeRebate − rebate − R20,000), and if this
   results in a negative number, finalTaxLiability is returned as 0.

---

### User Story 4 — Reject Invalid or Missing Inputs (Priority: P4)

A user who submits a calculation without a required field, or with a negative numeric value,
receives a clear validation error before any calculation is attempted.

**Why this priority**: Invalid inputs must be stopped at the boundary — attempting to
calculate tax on negative salary or a missing user would produce nonsensical results.

**Acceptance Scenarios**:

1. **Given** a request with no `title`,
   **When** submitted,
   **Then** the system responds with `400 Bad Request`.

2. **Given** a request with `salary: -5000`,
   **When** submitted,
   **Then** the system responds with `400 Bad Request`.

3. **Given** a request with a `userId` that does not exist,
   **When** submitted,
   **Then** the system responds with `404 Not Found` with message `"User not found"`.

---

### Edge Cases

- What if all income fields are zero and age is 25? → netTaxableIncome = 0, taxBeforeRebate = 0, finalTaxLiability = 0. All 7 fields still returned.
- What if retirementAnnuity exceeds totalIncome? → netTaxableIncome = MAX(0, ...) = 0, not negative.
- What if taxAlreadyPaid exceeds tax owed? → finalTaxLiability = MAX(0, ...) = 0, not negative. No refund is calculated.
- What if `description` is omitted? → Optional field, stored as null/empty, no error.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept a calculation request linked to a registered user by `userId`.
- **FR-002**: The system MUST require a non-blank `title` for every calculation.
- **FR-003**: Numeric income fields (salary, interestIncome, dividend, capitalGain, bonus, taxAlreadyPaid) MUST default to 0 when omitted and MUST be rejected if negative.
- **FR-004**: The retirementAnnuity field MUST default to 0 when omitted and MUST be rejected if negative.
- **FR-005**: `age` MUST be provided and MUST be >= 0.
- **FR-006**: The system MUST compute totalIncome = salary + interestIncome + dividend + capitalGain + bonus.
- **FR-007**: The system MUST compute totalDeductions = retirementAnnuity.
- **FR-008**: The system MUST compute netTaxableIncome = MAX(0, totalIncome − totalDeductions).
- **FR-009**: The system MUST apply SARS 2024/2025 progressive tax brackets to netTaxableIncome to produce taxBeforeRebate.
- **FR-010**: The system MUST apply the correct age-based rebate: primary only (age < 65), primary + secondary (age 65–74), primary + secondary + tertiary (age >= 75).
- **FR-011**: The system MUST compute finalTaxLiability = MAX(0, taxBeforeRebate − rebate − taxAlreadyPaid).
- **FR-012**: The system MUST persist the calculation (all inputs + full breakdown) linked to the user.
- **FR-013**: The response MUST return `201 Created` with all 7 breakdown fields plus the saved record's id and all submitted inputs.
- **FR-014**: The system MUST return `404 Not Found` if the provided `userId` does not match any registered user.

### Key Entities

- **TaxCalculation**: Represents a saved tax calculation. Key attributes: id, title, description,
  all income inputs, retirementAnnuity, age, taxAlreadyPaid, the 7 computed breakdown fields,
  and a link to the owning User. Created on save, immutable until Feature 4 (update/delete).
- **User**: Already exists — linked to TaxCalculation via userId (many calculations per user).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of valid calculation requests return `201 Created` with all 7 breakdown fields present and non-null.
- **SC-002**: Tax figures for a R500,000 salary, age 35, match the expected SARS 2024/2025 values exactly (verifiable against published SARS tables).
- **SC-003**: The age-based rebate is applied correctly in 100% of cases: under-65 gets only primary; 65–74 gets primary + secondary; 75+ gets all three.
- **SC-004**: finalTaxLiability is never negative — MAX(0, …) is enforced in 100% of cases.
- **SC-005**: 100% of requests with missing title, negative numeric fields, or non-existent userId are rejected before calculation.
- **SC-006**: Every saved calculation is retrievable by its assigned id immediately after creation.

---

## Assumptions

- SARS 2024/2025 tax brackets and rebate amounts are fixed constants — they do not change within this feature's lifecycle.
- The `description` field is optional and may be null or empty; no validation is applied to it.
- `userId` must refer to an existing registered user — the calculation is always user-scoped.
- Only one deduction type (retirement annuity) is supported in this version; additional deductions are out of scope.
- Dividend, capital gain, and interest income are treated as ordinary taxable income for simplicity — SARS-specific exemptions (e.g., interest exemption, CGT inclusion rate) are out of scope for this version.
- The calculation result is saved automatically on every successful POST — there is no separate "save" step.
- Tax year is fixed at 2024/2025 — no multi-year support is required.
