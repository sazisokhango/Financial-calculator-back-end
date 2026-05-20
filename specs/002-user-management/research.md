# Research: User Management

**Feature**: 002-user-management
**Date**: 2026-05-20

---

## Decision 1 — Reuse Existing Entity and DTO

**Decision**: No new entity or DTO is introduced. The `User` JPA entity and `UserResponse`
DTO created in Feature 1 are used directly.

**Rationale**: The data shape is identical (id, firstName, lastName, email). Adding new
classes would duplicate structure with no benefit.

**Alternatives considered**: None — reuse is the only sensible option.

---

## Decision 2 — Dedicated UserController

**Decision**: Create a new `UserController` mapped to `/api/user`, separate from
`AuthController` (`/api/auth`).

**Rationale**: Keeps the auth boundary (`/api/auth`) focused on registration/login concerns
while `/api/user` handles identity reads. Follows single-responsibility within the controller
layer and matches the PRD's endpoint definitions.

**Alternatives considered**:
- Merge into `AuthController` — rejected because it violates single-responsibility and
  mixes auth concerns with read concerns.

---

## Decision 3 — Extend UserService

**Decision**: Add `findAll()` and `findById(Long id)` methods to the existing `UserService`
from Feature 1.

**Rationale**: `UserService` already owns `UserRepository` and the `User → UserResponse`
mapping logic. Extending it avoids a second service class for the same entity.

**Alternatives considered**:
- Separate `UserQueryService` — rejected as over-engineering for two read methods.

---

## Decision 4 — UserNotFoundException for 404

**Decision**: Create a new `UserNotFoundException` extending `RuntimeException`. The
`GlobalExceptionHandler` maps it to `404 Not Found` with message `"User not found"`.

**Rationale**: Consistent with the existing pattern (`EmailAlreadyRegisteredException → 400`).
Domain exceptions keep the service layer clean and the handler centralised.

**Alternatives considered**:
- Return `Optional<UserResponse>` and let the controller return 404 — rejected because it
  puts response-code logic in the controller, violating the layered architecture principle.

---

## Decision 5 — Empty List on No Users

**Decision**: `GET /api/user` returns `200 OK` with `[]` when no users exist.

**Rationale**: An empty list is a valid, expected state during development and early usage.
Returning a `404` would be semantically incorrect — the resource (the collection) exists,
it is just empty.

**Alternatives considered**: None — `200 []` is the standard REST convention for empty collections.
