# Feature Specification: Saved Calculations CRUD

**Feature Branch**: `004-saved-calculations`

**Created**: 2026-05-20

**Status**: Draft

**Input**: Saved Calculations CRUD — GET /api/tax?userId={userId} returns all calculations
for a user; GET /api/tax/{id} returns a single calculation; PUT /api/tax/{id} updates
and recalculates; DELETE /api/tax/{id} deletes a calculation; returns 404 if calculation
not found.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — View All My Saved Calculations (Priority: P1)

A registered user retrieves the full list of tax calculations they have previously saved,
so they can review their calculation history at a glance.

**Why this priority**: This is the primary read path — the feature has no value if the
user cannot see their saved work. An empty list is valid when no calculations exist yet.

**Independent Test**: Save two calculations for a user, then retrieve their list.
The response contains exactly two records with all breakdown fields. A different user's
calculations are not included.

**Acceptance Scenarios**:

1. **Given** user 1 has two saved calculations and user 2 has one,
   **When** user 1 requests their list,
   **Then** the system responds with `200 OK` containing exactly two calculations
   belonging to user 1.

2. **Given** a user has no saved calculations,
   **When** they request their list,
   **Then** the system responds with `200 OK` and an empty list `[]`.

3. **Given** a non-existent userId is provided,
   **When** they request the list,
   **Then** the system responds with `404 Not Found` and message `"User not found"`.

---

### User Story 2 — View a Single Saved Calculation (Priority: P2)

A user retrieves one specific calculation by its id to see the full input and breakdown
detail for that record.

**Why this priority**: Needed by the front-end to display a calculation detail view
after the user selects an item from the list.

**Independent Test**: Save a calculation, capture its id, then retrieve it by id.
The response matches all inputs and breakdown fields exactly.

**Acceptance Scenarios**:

1. **Given** a calculation with id `5` exists,
   **When** a request is made for id `5`,
   **Then** the system responds with `200 OK` containing the full calculation record
   (all inputs + all 7 breakdown fields).

2. **Given** no calculation with id `999` exists,
   **When** a request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Calculation not found"`.

---

### User Story 3 — Update a Saved Calculation (Priority: P3)

A user edits a previously saved calculation — changing income figures, deductions, or
age — and the system recalculates the full tax breakdown and updates the saved record.

**Why this priority**: Users need to iterate on their tax estimates — e.g. adjusting a
salary figure after a pay rise, without deleting and re-creating from scratch.

**Independent Test**: Save a calculation with salary R500,000. Update it to salary
R600,000. The response shows the new breakdown values based on R600,000 salary, and
the original id is preserved.

**Acceptance Scenarios**:

1. **Given** a saved calculation with salary R500,000,
   **When** it is updated with salary R600,000 (same other fields),
   **Then** the system responds with `200 OK` containing updated breakdown values
   reflecting R600,000, and the record id is unchanged.

2. **Given** a saved calculation exists,
   **When** it is updated with an invalid field (e.g. negative salary),
   **Then** the system responds with `400 Bad Request`.

3. **Given** no calculation with id `999` exists,
   **When** an update request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Calculation not found"`.

---

### User Story 4 — Delete a Saved Calculation (Priority: P4)

A user removes a saved calculation they no longer need. The record is permanently deleted
and is no longer retrievable.

**Why this priority**: Completing the full CRUD lifecycle and allowing users to manage
their calculation history.

**Independent Test**: Save a calculation, delete it by id, then attempt to retrieve it.
The retrieval returns `404 Not Found`.

**Acceptance Scenarios**:

1. **Given** a calculation with id `3` exists,
   **When** a delete request is made for id `3`,
   **Then** the system responds with `204 No Content` and the record no longer exists.

2. **Given** no calculation with id `999` exists,
   **When** a delete request is made for id `999`,
   **Then** the system responds with `404 Not Found` and message `"Calculation not found"`.

---

### Edge Cases

- What if `userId` is omitted from `GET /api/tax`? → The parameter is required; respond `400 Bad Request`.
- Can a user retrieve or modify another user's calculation by guessing the id? → Out of scope for MVP — no cross-user ownership check is required at this stage.
- What happens if a calculation is deleted and then `GET /api/tax?userId=X` is called? → The deleted record must not appear in the list.
- Can `PUT /api/tax/{id}` change the `userId`? → No — the owning user is fixed at creation and cannot be changed via update.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST return all saved calculations for a given user when `GET /api/tax?userId={userId}` is called.
- **FR-002**: Each calculation in the list MUST include the full record: all inputs and all 7 breakdown fields.
- **FR-003**: The system MUST return an empty list (not an error) when the user has no saved calculations.
- **FR-004**: The system MUST return `404 Not Found` when the `userId` provided to the list endpoint does not match any registered user.
- **FR-005**: The system MUST return a single calculation record when `GET /api/tax/{id}` is called with a valid id.
- **FR-006**: The system MUST return `404 Not Found` with message `"Calculation not found"` when the requested id does not exist.
- **FR-007**: The system MUST accept updated inputs via `PUT /api/tax/{id}`, recompute the full tax breakdown using SARS 2024/2025 rules, and persist the updated record.
- **FR-008**: The system MUST return `200 OK` with the updated full record after a successful update.
- **FR-009**: The system MUST return `400 Bad Request` when an update request contains invalid inputs.
- **FR-010**: The system MUST return `404 Not Found` when an update is requested for a non-existent id.
- **FR-011**: The system MUST permanently delete a calculation when `DELETE /api/tax/{id}` is called for a valid id.
- **FR-012**: The system MUST return `204 No Content` after a successful deletion.
- **FR-013**: The system MUST return `404 Not Found` when a delete is requested for a non-existent id.

### Key Entities

- **TaxCalculation**: Already introduced in Feature 3. Same entity and response shape — all inputs + 7 breakdown fields. Updated in-place on PUT; removed permanently on DELETE.
- **User**: Already exists — used to scope the `GET /api/tax` list query.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of list requests for a valid userId return `200 OK` with only that user's calculations.
- **SC-002**: 100% of single-record requests for a valid id return `200 OK` with the complete record.
- **SC-003**: 100% of requests for non-existent ids or userIds return the correct `404` with the standard error shape.
- **SC-004**: After a successful `PUT`, the retrieved record reflects the new inputs and recalculated breakdown — not the original values.
- **SC-005**: After a successful `DELETE`, the record cannot be retrieved — `GET /api/tax/{id}` returns `404`.
- **SC-006**: A user's list does not include calculations belonging to other users.

---

## Assumptions

- No ownership-scoping check on `GET /api/tax/{id}`, `PUT /api/tax/{id}`, or `DELETE /api/tax/{id}` — any caller who knows the id can access or modify it. This is acceptable at MVP given the simplified no-auth user model.
- `PUT /api/tax/{id}` requires all the same input fields as `POST /api/tax` (title, age, etc.) plus `userId`. The owning user must still exist; the userId in the request body MUST match the existing calculation's owner.
- The tax recalculation on `PUT` uses the same SARS 2024/2025 engine from Feature 3 — no new calculation logic is introduced.
- The `GET /api/tax` endpoint requires `userId` as a mandatory query parameter — there is no "all calculations" endpoint without a user filter.
- Deletion is hard (permanent) — no soft-delete or recycle bin.
- `TaxCalculationNotFoundException` will be a new domain exception, separate from `UserNotFoundException`.
