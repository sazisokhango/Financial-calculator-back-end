# Feature Specification: Property Bond

**Feature Branch**: `007-property-bond`

**Created**: 2026-05-22

**Status**: Draft

**Input**: User description: "Property Bond — Allow users to calculate and forecast a property bond repayment plan. Full CRUD with monthly breakdown and repayment summary."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Calculate and Save Bond Repayment Plan (Priority: P1)

A registered user wants to model the repayment cost of a home loan. They submit their bond amount, monthly repayment, loan term, and interest rate. The system calculates the full repayment schedule and stores it so the user can refer back to it later.

**Why this priority**: This is the core value of the feature — without the ability to create and persist a bond calculation, all other stories are irrelevant.

**Independent Test**: Can be tested by submitting a valid bond request and verifying the 201 response contains correct `forecastResults` and `monthlyProjection` values.

**Acceptance Scenarios**:

1. **Given** a registered user with a valid email, **When** they submit a bond request with valid inputs, **Then** the system returns HTTP 201 with a full bond record including `id`, `forecastResults`, and `monthlyProjection`.
2. **Given** a valid bond request where `monthlyContribution` exceeds the interest charged each month, **When** the system calculates the schedule, **Then** `fullyPaid` is `true`, `remainingBalance` is `0.00`, and `estimatedPayoffMonth` reflects the month the balance reached zero.
3. **Given** a bond request where `monthlyContribution` is too low to reduce the principal, **When** the system calculates, **Then** `fullyPaid` is `false`, `remainingBalance` is the outstanding amount after `termMonths`, and `estimatedPayoffMonth` equals `termMonths`.
4. **Given** a request with a missing or blank `title`, **When** submitted, **Then** the system returns HTTP 400 with a validation error message.
5. **Given** a request with an `interestRate` outside 0–100, **When** submitted, **Then** the system returns HTTP 400.
6. **Given** a `userEmail` that does not match any registered user, **When** submitted, **Then** the system returns HTTP 404.

---

### User Story 2 - View All Bond Plans for a User (Priority: P2)

A registered user wants to view all their previously saved bond plans to compare different scenarios.

**Why this priority**: Read access is the most common post-creation action and is needed to make saved data useful.

**Independent Test**: Can be tested by creating one or more bond records for a user and then calling `GET /api/bonds?userEmail={email}` and verifying the correct records are returned.

**Acceptance Scenarios**:

1. **Given** a user with two saved bond plans, **When** they request all bonds by their email, **Then** the system returns HTTP 200 with an array of both full bond records.
2. **Given** a user with no saved bond plans, **When** they request all bonds, **Then** the system returns HTTP 200 with an empty array.
3. **Given** an unregistered `userEmail`, **When** used to list bonds, **Then** the system returns HTTP 404.

---

### User Story 3 - View a Single Bond Plan (Priority: P3)

A registered user wants to retrieve a specific saved bond plan by its ID.

**Why this priority**: Single-record retrieval supports detail views and is a prerequisite for update and delete flows.

**Independent Test**: Can be tested by creating a bond record, then calling `GET /api/bonds/{id}` and verifying the returned record matches the created one.

**Acceptance Scenarios**:

1. **Given** an existing bond `id`, **When** retrieved, **Then** the system returns HTTP 200 with the full bond record.
2. **Given** a non-existent bond `id`, **When** requested, **Then** the system returns HTTP 404.

---

### User Story 4 - Update a Bond Plan (Priority: P4)

A registered user wants to modify the inputs of a saved bond plan (e.g., change the interest rate or term) and have the forecast fully recalculated.

**Why this priority**: Users iterate on their bond scenarios; recalculation on update is essential for accuracy.

**Independent Test**: Can be tested by creating a bond, then updating it with different inputs and verifying the response contains freshly recalculated `forecastResults` and `monthlyProjection`.

**Acceptance Scenarios**:

1. **Given** an existing bond record, **When** updated with new valid inputs, **Then** the system returns HTTP 200 with fully recalculated results.
2. **Given** a non-existent bond `id`, **When** an update is attempted, **Then** the system returns HTTP 404.
3. **Given** an update request with invalid inputs, **When** submitted, **Then** the system returns HTTP 400 with validation error messages.

---

### User Story 5 - Delete a Bond Plan (Priority: P5)

A registered user wants to remove a bond plan they no longer need.

**Why this priority**: Cleanup capability rounds out full CRUD; lowest priority because it has no calculation complexity.

**Independent Test**: Can be tested by creating a bond, deleting it, and confirming a subsequent GET returns HTTP 404.

**Acceptance Scenarios**:

1. **Given** an existing bond `id`, **When** deleted, **Then** the system returns HTTP 204 and the record is no longer retrievable.
2. **Given** a non-existent bond `id`, **When** a delete is attempted, **Then** the system returns HTTP 404.

---

### Edge Cases

- What happens when `monthlyContribution` equals exactly the first month's interest? — The principal is never reduced; `fullyPaid` must be `false` and `endingBalance` grows or stays flat.
- What happens when `initialAmount` is `0`? — All interest charges are `0`, all `principalPaid` equals `monthlyContribution`, balance reaches zero in month 1; `fullyPaid` is `true`.
- What happens when `interestRate` is `0`? — `monthlyRate` is `0`, all interest charges are `0`; principal reduces by `monthlyContribution` each month.
- What happens when `termMonths` is very large (e.g., 360)? — The system must process all months without error; `monthlyProjection` contains 360 entries.
- What happens if `principalPaid` in a given month would produce a negative `endingBalance`? — `endingBalance` must be clamped to `0` and the projection stops reducing further; `estimatedPayoffMonth` is recorded at that month.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow a registered user to create a property bond repayment plan by submitting bond inputs; the plan is calculated and persisted.
- **FR-002**: System MUST identify the requesting user by `userEmail`; the email must match a registered user or the request is rejected with HTTP 404.
- **FR-003**: System MUST validate all inputs: `title` not blank; `initialAmount` >= 0; `monthlyContribution` >= 0; `termMonths` > 0; `interestRate` between 0 and 100 inclusive. Failures return HTTP 400 with validation error messages.
- **FR-004**: System MUST calculate a month-by-month projection for every month from 1 to `termMonths` using the defined formula, clamping `endingBalance` to a minimum of `0`.
- **FR-005**: System MUST compute summary results: `totalLoanAmount`, `totalRepayments`, `totalInterestPaid`, `remainingBalance`, `estimatedPayoffMonth`, and `fullyPaid`.
- **FR-006**: System MUST return the full bond record (inputs + `forecastResults` + `monthlyProjection`) in every create, retrieve, and update response.
- **FR-007**: System MUST allow retrieval of all bond plans belonging to a user, filtered by `userEmail`.
- **FR-008**: System MUST allow retrieval of a single bond plan by its unique identifier.
- **FR-009**: System MUST allow a user to update a bond plan; the forecast MUST be fully recalculated from the new inputs on every update.
- **FR-010**: System MUST allow a user to delete a bond plan by its unique identifier.
- **FR-011**: System MUST return HTTP 404 when a bond plan is not found by ID.
- **FR-012**: System MUST use high-precision decimal arithmetic for all monetary and rate calculations to avoid rounding errors.

### Key Entities

- **BondPlan**: Represents a saved property bond scenario — stores inputs (`title`, `description`, `initialAmount`, `monthlyContribution`, `termMonths`, `interestRate`, `userEmail`) and computed results (`forecastResults`, `monthlyProjection`).
- **BondForecastResult**: The summary output of a bond calculation — `totalLoanAmount`, `totalRepayments`, `totalInterestPaid`, `remainingBalance`, `estimatedPayoffMonth`, `fullyPaid`.
- **BondMonthlyProjection**: A single month entry in the amortization schedule — `month`, `startingBalance`, `monthlyPayment`, `interestCharged`, `principalPaid`, `endingBalance`.
- **User**: An existing registered user — linked to a bond plan via `userEmail`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create, retrieve, update, and delete a property bond plan without encountering unexpected errors.
- **SC-002**: Bond calculation results are mathematically correct — the sum of all `principalPaid` values equals `totalLoanAmount − remainingBalance` and the sum of all `interestCharged` values equals `totalInterestPaid`.
- **SC-003**: All five CRUD operations respond within acceptable time for any term up to 360 months.
- **SC-004**: Invalid inputs are rejected 100% of the time with a descriptive HTTP 400 response; no invalid data is persisted.
- **SC-005**: Requests referencing an unregistered `userEmail` are rejected 100% of the time with HTTP 404; no orphaned records are created.
- **SC-006**: All edge cases (zero interest rate, zero initial amount, underpayment scenario) return correct and consistent results without system errors.

## Assumptions

- Users are already registered; this feature does not handle registration — it depends on the existing User Registration feature (Feature 1).
- User identity is resolved by `userEmail` at the time of each request, consistent with the pattern established in the Investment Forecast feature (Feature 5).
- `monthlyContribution` is the fixed repayment amount provided by the user, not calculated by the system (the system does not auto-calculate a recommended repayment). This models the scenario where a user knows their repayment amount.
- The `monthlyProjection` array is always persisted and returned in full with every response — no pagination or on-demand lazy loading.
- All monetary values are stored and returned with sufficient decimal precision to avoid rounding drift over long terms.
- Description field is optional and may be omitted or null without error.
- The bond plan is permanently deleted when `DELETE` is called — no soft-delete or archiving.
