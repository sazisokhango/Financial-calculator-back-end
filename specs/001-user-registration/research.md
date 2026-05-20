# Research: User Registration

**Feature**: 001-user-registration
**Date**: 2026-05-20

---

## Decision 1 — Email Uniqueness Check

**Decision**: Query the database for an existing user with the same email (case-insensitive)
before persisting. Throw a domain exception on collision; the `GlobalExceptionHandler`
translates it to `400 Bad Request`.

**Rationale**: Simple pre-insert check is sufficient at MVP scale. A unique constraint on
the `email` column in the database provides an additional safety net.

**Alternatives considered**:
- Unique DB constraint only (catch `DataIntegrityViolationException`) — rejected because
  the error message surfaced would be technical, not user-friendly.
- Optimistic locking — overkill for a registration flow.

---

## Decision 2 — Email Case Normalisation

**Decision**: Store email in lowercase. Validate uniqueness against the lowercased value.

**Rationale**: Per the spec assumption, `User@Example.com` and `user@example.com` must be
treated as the same address. Normalising to lowercase at write time is the simplest approach
and avoids case-insensitive query complexity at read time.

**Alternatives considered**:
- Case-insensitive DB query (`LOWER(email) = LOWER(?)`) at registration time — functional
  but adds query complexity every time; normalisation is cheaper long-term.

---

## Decision 3 — Response Shape

**Decision**: Return a `UserResponse` DTO containing `id`, `firstName`, `lastName`, `email`.
The JPA `User` entity is never returned directly.

**Rationale**: Mandated by Constitution Principle II (DTOs at API boundary). Decouples the
persistence model from the API contract, allowing either to evolve independently.

**Alternatives considered**: None — constitution mandates DTOs.

---

## Decision 4 — Validation Strategy

**Decision**: Use Jakarta Bean Validation annotations (`@NotBlank`, `@Email`) on the
`RegisterRequest` DTO and `@Valid` in the controller method signature. The
`GlobalExceptionHandler` catches `MethodArgumentNotValidException` and maps it to `400`.

**Rationale**: Declarative validation keeps the controller thin (HTTP boundary only per
Constitution Principle II) and the service free of defensive checks for these invariants.

**Alternatives considered**:
- Manual validation in the service layer — rejected because it conflicts with the
  layered architecture mandate (service should handle business logic, not HTTP validation).

---

## Decision 5 — No Password / Authentication

**Decision**: No password field. No Spring Security authentication chain for this endpoint.
The `SecurityConfig` permits all requests.

**Rationale**: Per spec assumption — authentication is explicitly out of scope. Identity is
email-unique only. Future features may introduce auth without breaking this contract.

**Alternatives considered**: None — explicitly out of scope per PRD.
