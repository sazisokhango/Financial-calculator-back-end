## Context

`CarLoanController.findAll()` currently maps to `GET /api/loans` with no filtering — it returns every loan in the database. The `CarLoan` entity already has a `@ManyToOne User user` field (nullable), and `CarLoanRepository` already contains `findAllByUserEmail(String)` (unused). The `User` entity uses a `Long id` primary key, consistent with how `TaxController` scopes its list endpoint (`@RequestParam Long userId`).

## Goals / Non-Goals

**Goals:**
- `GET /api/loans?userId=<Long>` returns only loans belonging to that user
- Unknown `userId` returns 404 with standard error body
- Consistent with `TaxController` pattern (`@RequestParam Long userId`)

**Non-Goals:**
- Changing the create (`POST`) endpoint to associate loans with users — out of scope
- Paginating results
- Removing `findAllByUserEmail` from the repository (harmless, leave it)

## Decisions

**1. `@RequestParam Long userId` (not path variable)**  
Matches the requirement and aligns with `TaxController`'s existing `getAllByUser` pattern. Flat `/api/loans?userId=X` is simpler than nesting.

**2. `CarLoanRepository.findAllByUser_Id(Long userId)` derived query**  
Spring Data JPA resolves `user_id` via the `user` association automatically. No need for a `@Query` annotation.

**3. 404 when userId not found**  
Consistent with how `PropertyBondService` and `TaxCalculationService` handle unknown users — throw `UserNotFoundException`, handled by the global exception handler.

**4. Existing loans with `user = null` are excluded**  
Loans created before this change (or without user association) will not appear in any user's list. This is acceptable — those loans are still accessible by id via `GET /api/loans/{id}`.

## Risks / Trade-offs

- **Risk**: `GET /api/loans` callers (e.g., any existing frontend code) will break — the parameter is now required.  
  **Mitigation**: Marked as BREAKING in proposal; coordinate with frontend team.

- **Trade-off**: Loans created without a `userId` become invisible in the list endpoint.  
  **Accepted**: Out of scope to retrofit; individual GET by id still works.
