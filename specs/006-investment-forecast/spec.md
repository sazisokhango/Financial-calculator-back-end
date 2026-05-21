# Feature Specification: Investment Forecast

**Feature Branch**: `006-investment-forecast`

**Created**: 2026-05-21

**Status**: Draft

**Input**: Investment Forecast — POST /api/investments/forecast creates a compound-interest forecast persisted per user; full CRUD via GET /api/investments, GET /api/investments/{id}, PUT /api/investments/{id}, DELETE /api/investments/{id}; forecast results include a summary breakdown and a month-by-month projection.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Create an Investment Forecast (Priority: P1)

A registered user submits investment details and receives a calculated forecast showing how their investment will grow over time, with the forecast saved for future reference.

**Why this priority**: This is the core value of the feature — without creation, no other operation has meaning. A user must be able to get a forecast before they can view, update, or delete it.

**Independent Test**: Submit a valid forecast request with a known initial amount, monthly contribution, term, and interest rate. Verify the response contains a correct summary breakdown and a monthly projection array with the correct number of entries.

**Acceptance Scenarios**:

1. **Given** a registered user provides valid forecast inputs,
   **When** they submit the forecast request,
   **Then** the system responds with `201 Created` containing an `id`, the forecast summary (`projectedValue`, `totalContributions`, `totalInterestEarned`, `roiPercentage`, `averageMonthlyGrowth`), and a `monthlyProjection` array with one entry per month in the term.

2. **Given** a user submits a request with an invalid field (e.g., negative `initialAmount`),
   **When** the system validates the input,
   **Then** the system responds with `400 Bad Request` and includes validation error messages identifying the failing fields.

3. **Given** a user submits a request referencing a `userId` that does not exist,
   **When** the system processes the request,
   **Then** the system responds with `404 Not Found` and message `"User not found"`.

4. **Given** a user submits a request with `title` left blank,
   **When** the system validates the input,
   **Then** the system responds with `400 Bad Request`.

---

### User Story 2 — View All My Saved Forecasts (Priority: P2)

A registered user retrieves the full list of investment forecasts they have previously created, so they can compare or revisit past projections.

**Why this priority**: The list view is the primary navigation surface — users need to see what they have saved before they can act on any individual forecast.

**Independent Test**: Create two forecasts for one user and one for a different user. Retrieve the first user's list. The response contains exactly two records, both belonging to the first user.

**Acceptance Scenarios**:

1. **Given** user A has two saved forecasts and user B has one,
   **When** user A requests their forecast list,
   **Then** the system responds with `200 OK` containing exactly two forecasts belonging to user A.

2. **Given** a user has no saved forecasts,
   **When** they request their list,
   **Then** the system responds with `200 OK` and an empty array `[]`.

3. **Given** a non-existent `userId` is provided,
   **When** they request the list,
   **Then** the system responds with `404 Not Found` and message `"User not found"`.

---

### User Story 3 — View a Single Saved Forecast (Priority: P3)

A user retrieves one specific forecast by its id to review the full input details, summary results, and month-by-month projection.

**Why this priority**: Required for any detail view in the front-end after a user selects a forecast from the list.

**Independent Test**: Create a forecast, capture its `id`, then retrieve it by `id`. The response matches all submitted inputs and the full forecast results.

**Acceptance Scenarios**:

1. **Given** a forecast with id `5` exists,
   **When** a request is made for id `5`,
   **Then** the system responds with `200 OK` containing the full forecast record (all inputs + summary + monthly projection).

2. **Given** no forecast with id `999` exists,
   **When** a request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Investment forecast not found"`.

---

### User Story 4 — Update a Saved Forecast (Priority: P4)

A user edits a previously saved forecast — adjusting contribution amounts, interest rate, or term — and the system recalculates the full projection and updates the saved record.

**Why this priority**: Users need to iterate on their projections — e.g., modelling a salary increase by raising the monthly contribution — without deleting and recreating from scratch.

**Independent Test**: Create a forecast with a 12-month term. Update it to a 24-month term. The response shows a `monthlyProjection` array with 24 entries and updated summary values. The original `id` is preserved.

**Acceptance Scenarios**:

1. **Given** a saved forecast with `termMonths` of 12,
   **When** it is updated with `termMonths` of 24 (same other fields),
   **Then** the system responds with `200 OK` containing recalculated summary results and a 24-entry `monthlyProjection`. The record `id` is unchanged.

2. **Given** a saved forecast exists,
   **When** it is updated with an invalid field (e.g., `annualInterestRate` of 150),
   **Then** the system responds with `400 Bad Request`.

3. **Given** no forecast with id `999` exists,
   **When** an update request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Investment forecast not found"`.

---

### User Story 5 — Delete a Saved Forecast (Priority: P5)

A user permanently removes a saved forecast they no longer need.

**Why this priority**: Completes the full CRUD lifecycle and allows users to manage their forecast history.

**Independent Test**: Create a forecast, delete it by `id`, then attempt to retrieve it. The retrieval returns `404 Not Found`.

**Acceptance Scenarios**:

1. **Given** a forecast with id `3` exists,
   **When** a delete request is made for id `3`,
   **Then** the system responds with `204 No Content` and the record no longer exists.

2. **Given** no forecast with id `999` exists,
   **When** a delete request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Investment forecast not found"`.

---

### Edge Cases

- What if `userId` is omitted from `GET /api/investments`? → The parameter is required; respond `400 Bad Request`.
- What if `annualInterestRate` is `0`? → The forecast is valid — the projection shows pure contribution growth with no interest. Each month's `interestEarned` is `0`.
- What if `initialAmount` is `0`? → Valid — the forecast models growth from monthly contributions alone.
- What if `monthlyContribution` is `0`? → Valid — the forecast models growth of the initial lump sum only.
- What if `termMonths` is `1`? → Valid — the projection contains a single entry.
- Can a user retrieve or modify another user's forecast by guessing the id? → Out of scope for MVP — no cross-user ownership check is required at this stage.
- Can `PUT /api/investments/{id}` change the owning user? → No — the `userId` in the request body MUST match the existing forecast's owner.
- What happens if a forecast is deleted and then `GET /api/investments?userId=X` is called? → The deleted record must not appear in the list.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept a forecast creation request containing `userId`, `title`, `initialAmount`, `monthlyContribution`, `termMonths`, and `annualInterestRate`, and persist the result.
- **FR-002**: `description` is optional and may be blank or omitted; all other input fields are required.
- **FR-003**: The system MUST validate all inputs and respond with `400 Bad Request` (including field-level error messages) when any validation rule is violated:
  - `title`: not blank
  - `initialAmount`: >= 0
  - `monthlyContribution`: >= 0
  - `termMonths`: > 0
  - `annualInterestRate`: between 0 and 100 inclusive
- **FR-004**: The system MUST respond with `404 Not Found` and message `"User not found"` when the provided `userId` does not reference a registered user.
- **FR-005**: The system MUST calculate and return a forecast summary containing: `projectedValue`, `totalContributions`, `totalInterestEarned`, `roiPercentage`, and `averageMonthlyGrowth`.
- **FR-006**: The system MUST calculate and return a `monthlyProjection` array with one entry per month in `termMonths`. Each entry contains: `month` (1-based index), `startingBalance`, `monthlyContribution`, `interestEarned`, and `endingBalance`.
- **FR-007**: The system MUST respond with `201 Created` and the full saved forecast record (including `id`) after a successful creation.
- **FR-008**: The system MUST return all saved forecasts for a given user when `GET /api/investments?userId={userId}` is called.
- **FR-009**: The system MUST return an empty array (not an error) when the user has no saved forecasts.
- **FR-010**: The system MUST return `404 Not Found` with message `"User not found"` when the `userId` provided to the list endpoint does not match a registered user.
- **FR-011**: The system MUST return a single full forecast record when `GET /api/investments/{id}` is called with a valid id.
- **FR-012**: The system MUST return `404 Not Found` with message `"Investment forecast not found"` when the requested id does not exist.
- **FR-013**: The system MUST accept updated inputs via `PUT /api/investments/{id}`, recompute the full forecast using the same compound-interest rules, and persist the updated record.
- **FR-014**: The system MUST return `200 OK` with the updated full record after a successful update.
- **FR-015**: The system MUST return `400 Bad Request` when an update request contains invalid inputs.
- **FR-016**: The system MUST return `404 Not Found` when an update is requested for a non-existent id.
- **FR-017**: The system MUST permanently delete a forecast when `DELETE /api/investments/{id}` is called for a valid id.
- **FR-018**: The system MUST return `204 No Content` after a successful deletion.
- **FR-019**: The system MUST return `404 Not Found` when a delete is requested for a non-existent id.

### Key Entities

- **InvestmentForecast**: Represents a saved investment forecast belonging to a user. Stores all input fields (`title`, `description`, `initialAmount`, `monthlyContribution`, `termMonths`, `annualInterestRate`) plus the calculated summary results. Linked to a `User`.
- **ForecastResult**: The summary outcome of the forecast calculation — `projectedValue`, `totalContributions`, `totalInterestEarned`, `roiPercentage`, `averageMonthlyGrowth`. Stored as part of the forecast record.
- **MonthlyProjectionEntry**: One row in the month-by-month projection — `month`, `startingBalance`, `monthlyContribution`, `interestEarned`, `endingBalance`. The full array is stored with the forecast.
- **User**: Already exists — used to scope and validate forecast ownership.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of creation requests with valid inputs receive `201 Created` with a complete forecast record containing the correct number of monthly projection entries.
- **SC-002**: 100% of creation requests with invalid inputs receive `400 Bad Request` with field-level error messages.
- **SC-003**: 100% of list requests for a valid `userId` return `200 OK` containing only that user's forecasts.
- **SC-004**: 100% of requests for non-existent ids or `userId` values return the correct `404` response with the standard error shape.
- **SC-005**: After a successful `PUT`, the retrieved record reflects the new inputs and the recalculated projection — not the original values.
- **SC-006**: After a successful `DELETE`, the record cannot be retrieved — `GET /api/investments/{id}` returns `404`.
- **SC-007**: A user's list does not include forecasts belonging to other users.
- **SC-008**: A forecast with `annualInterestRate` of `0` is accepted and returns a valid projection where every `interestEarned` entry is `0`.

---

## Assumptions

- No ownership-scoping check on `GET /api/investments/{id}`, `PUT /api/investments/{id}`, or `DELETE /api/investments/{id}` — any caller who knows the id can access or modify it. Acceptable at MVP given the simplified no-auth user model.
- `PUT /api/investments/{id}` requires all the same input fields as the creation request. The `userId` in the request body MUST match the existing forecast's owner — the owning user cannot be changed via update.
- `GET /api/investments` requires `userId` as a mandatory query parameter — there is no "all forecasts" endpoint without a user filter.
- Deletion is permanent (hard delete) — no soft-delete or recycle bin.
- Monthly projection entries are stored alongside the forecast and returned in every response; there is no separate endpoint to fetch them.
- `InvestmentForecastNotFoundException` will be a new domain exception, separate from `UserNotFoundException`.
- All monetary values and percentages use `BigDecimal` precision throughout — rounding follows the same standards as the existing tax calculation engine.
- `annualInterestRate` of `0` and `100` are both treated as valid boundary values.
