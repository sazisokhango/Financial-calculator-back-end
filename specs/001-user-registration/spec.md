# Feature Specification: User Registration

**Feature Branch**: `001-user-registration`

**Created**: 2026-05-20

**Status**: Draft

**Input**: User Registration — POST /api/auth/register with firstName, lastName, email fields;
email must be unique and valid; returns 201 on success, 400 on validation failure or duplicate email

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — New User Registers Successfully (Priority: P1)

A first-time user provides their first name, last name, and email address to create an account.
After registering, their identity is stored and they can be found in the user list to begin
using the tax calculator.

**Why this priority**: Registration is the entry point to the entire application — no other
feature is accessible without a registered identity.

**Independent Test**: Send a registration request with valid first name, last name, and email.
The system stores the user and returns their profile with a generated ID. Delivering this
alone gives a working registration endpoint that the front-end can integrate against.

**Acceptance Scenarios**:

1. **Given** no account exists for the email `saziso@example.com`,
   **When** a user submits `firstName: "Saziso"`, `lastName: "Khango"`, `email: "saziso@example.com"`,
   **Then** the system creates the account and responds with `201 Created` containing the user's
   id, firstName, lastName, and email.

2. **Given** a valid registration request,
   **When** the user is created,
   **Then** the response body includes the system-generated `id` alongside the submitted fields.

---

### User Story 2 — Registration Rejected for Duplicate Email (Priority: P2)

A user who attempts to register with an email address already tied to an existing account
receives a clear error message, preventing duplicate identities in the system.

**Why this priority**: Without this guard, multiple users could share the same email, breaking
the identity model that the user-selection flow depends on.

**Independent Test**: Register a user with email `test@example.com`, then attempt to register
again with the same email. The second attempt must return a `400 Bad Request` with a message
indicating the email is already registered.

**Acceptance Scenarios**:

1. **Given** a user with email `test@example.com` is already registered,
   **When** a second registration request is submitted with `email: "test@example.com"`,
   **Then** the system responds with `400 Bad Request` and the message `"Email already registered"`.

---

### User Story 3 — Registration Rejected for Invalid or Missing Fields (Priority: P3)

A user who submits a registration form with missing or malformed data receives descriptive
validation feedback rather than a silent failure or server error.

**Why this priority**: Good input validation protects data integrity and gives the front-end
actionable feedback to display to the user.

**Independent Test**: Submit a registration request with each invalid scenario below and
confirm each returns `400 Bad Request`.

**Acceptance Scenarios**:

1. **Given** a registration request with `firstName` left blank,
   **When** submitted,
   **Then** the system responds with `400 Bad Request` indicating the field is required.

2. **Given** a registration request with `lastName` left blank,
   **When** submitted,
   **Then** the system responds with `400 Bad Request` indicating the field is required.

3. **Given** a registration request with `email` set to `"not-an-email"`,
   **When** submitted,
   **Then** the system responds with `400 Bad Request` indicating the email is invalid.

4. **Given** a registration request with all fields missing (empty body),
   **When** submitted,
   **Then** the system responds with `400 Bad Request`.

---

### Edge Cases

- What happens when `email` contains leading/trailing whitespace? → Should be treated as invalid or trimmed before validation.
- What happens when `firstName` or `lastName` contains only whitespace? → Must be rejected as blank.
- What happens when the same valid request is sent twice in rapid succession? → Second request must be rejected as duplicate email.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept a registration request containing `firstName`, `lastName`, and `email`.
- **FR-002**: The system MUST persist the user's identity upon successful registration.
- **FR-003**: The system MUST return the registered user's profile (including their generated id) upon success.
- **FR-004**: The system MUST reject registration when `firstName` is blank or missing.
- **FR-005**: The system MUST reject registration when `lastName` is blank or missing.
- **FR-006**: The system MUST reject registration when `email` is not a valid email format.
- **FR-007**: The system MUST reject registration when the submitted `email` is already registered.
- **FR-008**: All rejection responses MUST return `400 Bad Request` with a descriptive message.
- **FR-009**: Successful registration MUST return `201 Created`.

### Key Entities

- **User**: Represents a registered identity in the system. Key attributes: unique id, firstName,
  lastName, email (unique). This entity is the foundation for the user-selection flow and for
  scoping tax calculations to specific individuals.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A valid registration request completes and the new user is retrievable by id immediately after.
- **SC-002**: 100% of registration requests with a duplicate email are rejected with a `400` response.
- **SC-003**: 100% of registration requests with a missing or blank required field are rejected with a `400` response.
- **SC-004**: 100% of registration requests with an invalid email format are rejected with a `400` response.
- **SC-005**: All error responses from the registration endpoint conform to the standard error shape
  `{ status, error, message }`.

---

## Assumptions

- No password is required — authentication is out of scope for this feature. Identity is
  established by email uniqueness alone.
- Email comparison for uniqueness is case-insensitive (e.g., `User@Example.com` and
  `user@example.com` are treated as the same address).
- The `id` field is system-generated and must not be supplied in the request body.
- No email verification (OTP/confirmation link) is required at registration time.
- The registered user will be visible in `GET /api/user` immediately after registration
  (covered by the User Management feature).
