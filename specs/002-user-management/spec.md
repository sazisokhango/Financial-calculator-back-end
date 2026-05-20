# Feature Specification: User Management

**Feature Branch**: `002-user-management`

**Created**: 2026-05-20

**Status**: Draft

**Input**: User Management — GET /api/user returns all registered users; GET /api/user/{id}
returns a single user by id; used by the front-end name-selection flow; returns 404 if user not found

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Browse All Registered Users (Priority: P1)

A user opens the application and is presented with a list of all registered names so they
can identify and select themselves before viewing or creating tax calculations.

**Why this priority**: This is the core of the user-selection flow described in the PRD.
Without a user list, users have no way to identify themselves after registration.

**Independent Test**: Call the all-users endpoint when at least one user is registered.
The response must contain a list with each user's id, firstName, lastName, and email.
When no users are registered the response is an empty list (not an error).

**Acceptance Scenarios**:

1. **Given** two users — Saziso Khango and John Doe — are registered,
   **When** a request is made to retrieve all users,
   **Then** the system responds with `200 OK` containing both users with their id, firstName,
   lastName, and email.

2. **Given** no users are registered,
   **When** a request is made to retrieve all users,
   **Then** the system responds with `200 OK` and an empty list `[]`.

---

### User Story 2 — View a Specific User by ID (Priority: P2)

After selecting a name from the list, the front-end fetches that user's full profile by id
to confirm identity before proceeding to the calculations view.

**Why this priority**: The front-end needs a reliable single-user lookup to establish the
active user's identity. Depends on the user list (US1) to obtain the id first.

**Independent Test**: Register a user, retrieve their id from the user list, then request
that user by id. The response matches the registered profile exactly.

**Acceptance Scenarios**:

1. **Given** user with id `1` exists (firstName: "Saziso", lastName: "Khango", email: "saziso@example.com"),
   **When** a request is made for user id `1`,
   **Then** the system responds with `200 OK` containing the user's id, firstName, lastName, and email.

2. **Given** no user with id `999` exists,
   **When** a request is made for user id `999`,
   **Then** the system responds with `404 Not Found` and the message `"User not found"`.

---

### Edge Cases

- What if the same endpoint is called with a non-numeric id (e.g., `/api/user/abc`)? → Should return `400 Bad Request` or Spring's default type mismatch error.
- What if the user list grows very large? → For MVP, no pagination is required; return all users.
- Can a user see other users' email addresses in the list? → Yes — the list is for identity selection and email is part of the profile. No access control is in scope for this feature.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST return a list of all registered users when the all-users endpoint is called.
- **FR-002**: Each user in the list MUST include their id, firstName, lastName, and email.
- **FR-003**: The system MUST return an empty list (not an error) when no users are registered.
- **FR-004**: The system MUST return a single user's full profile when requested by a valid id.
- **FR-005**: The system MUST return `404 Not Found` with message `"User not found"` when the requested id does not exist.
- **FR-006**: All successful responses MUST return `200 OK`.
- **FR-007**: The `User` entity MUST NOT be exposed directly — a response DTO MUST be used at the API boundary.

### Key Entities

- **User**: Already introduced in Feature 1. Same profile shape used here — id, firstName,
  lastName, email. No new fields required for this feature.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of requests to the all-users endpoint return `200 OK` regardless of how many users are registered (including zero).
- **SC-002**: 100% of requests for a valid user id return `200 OK` with the correct profile.
- **SC-003**: 100% of requests for a non-existent user id return `404 Not Found` with the standard error shape `{ status, error, message }`.
- **SC-004**: The user list returned by the all-users endpoint always reflects the current state of registered users.

---

## Assumptions

- No authentication or access control is required — any caller can retrieve the user list or a specific user profile.
- No pagination is required for the user list at MVP — all registered users are returned in a single response.
- The response shape for both endpoints uses the same `UserResponse` DTO introduced in Feature 1 (id, firstName, lastName, email).
- The `User` entity was created in Feature 1 and already persists to the database — this feature only adds read endpoints, no new data is persisted.
- Users are returned in no guaranteed order (insertion order or database default).
