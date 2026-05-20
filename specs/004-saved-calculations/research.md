# Research: Saved Calculations CRUD

**Feature**: 004-saved-calculations
**Date**: 2026-05-20

---

## Decision 1 — Extend Existing TaxCalculationService

**Decision**: Add four new methods to the existing `TaxCalculationService`:
`findAllByUser(Long userId)`, `findById(Long id)`, `update(Long id, TaxCalculationRequest)`,
`delete(Long id)`. No second service class is introduced.

**Rationale**: All CRUD operations share the same entity, repository, and `toResponse()`
helper already present in the service. Splitting into a second class for read/write
operations would add indirection with no benefit at this scale.

**Alternatives considered**:
- Separate `TaxCalculationQueryService` — rejected as premature abstraction for four methods.

---

## Decision 2 — Repository findByUser Query

**Decision**: Add `List<TaxCalculation> findByUser(User user)` to `TaxCalculationRepository`.
The service validates the user exists via `UserRepository` first (throwing `UserNotFoundException`),
then passes the `User` entity to the repository query.

**Rationale**: Spring Data JPA derives `findByUser` directly from the `@ManyToOne User user`
field — no custom JPQL needed. Validating the user first gives a clean 404 with a user-friendly
message before attempting the list query.

**Alternatives considered**:
- `findByUser_Id(Long id)` — functional but less readable than passing the entity.
- `@Query("SELECT t FROM TaxCalculation t WHERE t.user.id = :userId")` — unnecessary when
  derived methods work.

---

## Decision 3 — TaxCalculationNotFoundException

**Decision**: Create `TaxCalculationNotFoundException` extending `RuntimeException`.
Add a handler in `GlobalExceptionHandler` → `404 Not Found` with message from the exception.

**Rationale**: Consistent with the existing `UserNotFoundException → 404` pattern established
in Feature 2. Keeps domain exceptions explicit and the handler centralised (Constitution IV).

**Alternatives considered**: None — the pattern is mandated by the constitution.

---

## Decision 4 — PUT Reuses TaxCalculationRequest; userId Ignored for Ownership

**Decision**: `PUT /api/tax/{id}` accepts the same `TaxCalculationRequest` body as `POST`.
The service looks up the calculation by id, then re-uses the existing `User` from the
persisted record (ownership is immutable after creation). The `userId` field in the PUT body
is used only to validate the user still exists; it does not change the calculation's owner.

**Rationale**: Keeps the API surface consistent (one DTO for create and update). Immutable
ownership is the safest MVP choice — preventing accidental or malicious reassignment without
adding an ownership-change flow not described in the PRD.

**Alternatives considered**:
- Separate `TaxCalculationUpdateRequest` without `userId` — cleaner but adds a new class
  for marginal benefit; deferred to a future improvement.
- Allow ownership change via PUT — rejected; not in PRD scope.

---

## Decision 5 — Full Recalculation on PUT

**Decision**: `update()` normalises null inputs to zero, recomputes all 7 breakdown fields
via `SarsTaxCalculator` (same logic as `save()`), and overwrites the entity in place.
No new calculation logic is introduced.

**Rationale**: Re-using the same SARS engine for PUT ensures consistency between creation
and update. Spec FR-007 explicitly requires recalculation on update.

**Alternatives considered**: None — reuse is the only sensible approach.

---

## Decision 6 — Hard Delete

**Decision**: `DELETE /api/tax/{id}` calls `taxCalculationRepository.deleteById(id)`.
No soft-delete flag or audit trail is maintained.

**Rationale**: The spec assumption explicitly states "Deletion is hard (permanent) — no
soft-delete or recycle bin." Soft-delete would require schema changes and filtering logic
not in scope.

**Alternatives considered**: None — scope constraint documented in spec.
