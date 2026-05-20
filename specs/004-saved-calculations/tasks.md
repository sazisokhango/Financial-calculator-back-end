# Tasks: Saved Calculations CRUD

**Input**: Design documents from `specs/004-saved-calculations/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story. All source changes are extensions to existing
Feature 3 files; `SavedCalculationsSpec.java` is a new test class keeping scenarios separated.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1–US4 maps to spec.md user stories

---

## Phase 1: Setup

- [x] T001 Verify all 26 existing tests pass with `./mvnw test` from project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Exception class, handler extension, and repository query needed by all four stories.

⚠️ **CRITICAL**: All Phase 3–6 tasks depend on this phase.

- [x] T002 [P] Create `TaxCalculationNotFoundException` in `src/main/java/com/psybergate/financialcalculator/exception/TaxCalculationNotFoundException.java` extending `RuntimeException` with a single `String message` constructor
- [x] T003 [P] Update `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` to handle `TaxCalculationNotFoundException` → return `404 Not Found` with body `{ "status": 404, "error": "Not Found", "message": ex.getMessage() }`
- [x] T004 Add `List<TaxCalculation> findByUser(User user)` method to `TaxCalculationRepository` in `src/main/java/com/psybergate/financialcalculator/repository/TaxCalculationRepository.java` (Spring Data JPA derived query — no body needed)

**Checkpoint**: Shared infrastructure ready — all user story phases can begin.

---

## Phase 3: User Story 1 — View All Saved Calculations (Priority: P1) 🎯 MVP

**Goal**: `GET /api/tax?userId={userId}` returns all calculations for a user as `200 OK`, empty list if none, `404` if userId unknown.

**Independent Test**: Save two calculations for user A and one for user B. `GET /api/tax?userId=A` → 200 with exactly 2 records. `GET /api/tax?userId=B` → 200 with 1 record. `GET /api/tax?userId=999` → 404.

- [x] T005 [US1] Add `findAllByUser(Long userId)` to `TaxCalculationService` in `src/main/java/com/psybergate/financialcalculator/service/TaxCalculationService.java` — look up `User` by userId, throw `UserNotFoundException("User not found")` if absent, call `taxCalculationRepository.findByUser(user)`, map each `TaxCalculation` to `TaxCalculationResponse` using existing `toResponse()`, return `List<TaxCalculationResponse>`
- [x] T006 [US1] Add `GET /api/tax` endpoint to `TaxController` in `src/main/java/com/psybergate/financialcalculator/controller/TaxController.java` — `@GetMapping` with `@RequestParam Long userId`, delegates to `taxCalculationService.findAllByUser(userId)`, returns `ResponseEntity<List<TaxCalculationResponse>>` with `200 OK`
- [x] T007 [US1] Write US1 test scenarios in `src/test/java/com/psybergate/financialcalculator/tax/SavedCalculationsSpec.java` — `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`, `@BeforeEach` clears both repos: (1) save two calculations for userA, `GET /api/tax?userId=userA` → 200, array size 2; (2) user with no calculations → 200, `[]`; (3) `GET /api/tax?userId=999` → 404, `message: "User not found"`; (4) save calc for userA and userB, verify `GET /api/tax?userId=userA` does NOT include userB's record

**Checkpoint**: List endpoint verified end-to-end. US1 independently validated.

---

## Phase 4: User Story 2 — View Single Calculation (Priority: P2)

**Goal**: `GET /api/tax/{id}` returns `200 OK` with the full record, or `404` if the id does not exist.

**Independent Test**: Save a calculation, capture its id, `GET /api/tax/{id}` → 200 with all breakdown fields matching. `GET /api/tax/999` → 404 with `message: "Calculation not found"`.

- [x] T008 [US2] Add `findById(Long id)` to `TaxCalculationService` in `src/main/java/com/psybergate/financialcalculator/service/TaxCalculationService.java` — call `taxCalculationRepository.findById(id)`, throw `TaxCalculationNotFoundException("Calculation not found")` if absent, return `toResponse(entity)`
- [x] T009 [US2] Add `GET /api/tax/{id}` endpoint to `TaxController` in `src/main/java/com/psybergate/financialcalculator/controller/TaxController.java` — `@GetMapping("/{id}")` with `@PathVariable Long id`, delegates to `taxCalculationService.findById(id)`, returns `ResponseEntity<TaxCalculationResponse>` with `200 OK`
- [x] T010 [US2] Write US2 test scenarios in `src/test/java/com/psybergate/financialcalculator/tax/SavedCalculationsSpec.java`: (1) save a calculation, `GET /api/tax/{id}` → 200, verify `id`, `title`, `finalTaxLiability` match; (2) `GET /api/tax/999999` → 404, `status: 404`, `error: "Not Found"`, `message: "Calculation not found"`

**Checkpoint**: Single-record read verified. US2 independently validated.

---

## Phase 5: User Story 3 — Update a Saved Calculation (Priority: P3)

**Goal**: `PUT /api/tax/{id}` recalculates with new inputs and returns `200 OK` with the updated breakdown. The original id is preserved.

**Independent Test**: Save calculation with salary=500,000. `PUT /api/tax/{id}` with salary=600,000 → 200, `taxBeforeRebate=152867.00`, `finalTaxLiability=135632.00`, `id` unchanged.

- [x] T011 [US3] Add `update(Long id, TaxCalculationRequest request)` to `TaxCalculationService` in `src/main/java/com/psybergate/financialcalculator/service/TaxCalculationService.java` — find existing by id (throw `TaxCalculationNotFoundException` if absent); validate user by request.userId (throw `UserNotFoundException` if absent); normalise null numerics to ZERO; recompute all 7 breakdown fields using `SarsTaxCalculator`; update all fields on the existing entity (keep same `user` reference); save and return `toResponse()`
- [x] T012 [US3] Add `PUT /api/tax/{id}` endpoint to `TaxController` in `src/main/java/com/psybergate/financialcalculator/controller/TaxController.java` — `@PutMapping("/{id}")` with `@PathVariable Long id` and `@Valid @RequestBody TaxCalculationRequest`, delegates to `taxCalculationService.update(id, request)`, returns `ResponseEntity<TaxCalculationResponse>` with `200 OK`
- [x] T013 [US3] Write US3 test scenarios in `src/test/java/com/psybergate/financialcalculator/tax/SavedCalculationsSpec.java`: (1) save salary=500000/age=35, PUT with salary=600000/age=35 → 200, `taxBeforeRebate=152867.00`, `finalTaxLiability=135632.00`, same `id`; (2) PUT with negative salary → 400; (3) PUT to non-existent id 999999 → 404 `message: "Calculation not found"`

**Checkpoint**: Update with recalculation verified. US3 independently validated.

---

## Phase 6: User Story 4 — Delete a Saved Calculation (Priority: P4)

**Goal**: `DELETE /api/tax/{id}` returns `204 No Content` and permanently removes the record. Subsequent `GET /api/tax/{id}` returns `404`.

**Independent Test**: Save a calculation, `DELETE /api/tax/{id}` → 204. Then `GET /api/tax/{id}` → 404. `DELETE /api/tax/999` → 404.

- [x] T014 [US4] Add `delete(Long id)` to `TaxCalculationService` in `src/main/java/com/psybergate/financialcalculator/service/TaxCalculationService.java` — verify existence with `taxCalculationRepository.existsById(id)`, throw `TaxCalculationNotFoundException("Calculation not found")` if absent, call `taxCalculationRepository.deleteById(id)`
- [x] T015 [US4] Add `DELETE /api/tax/{id}` endpoint to `TaxController` in `src/main/java/com/psybergate/financialcalculator/controller/TaxController.java` — `@DeleteMapping("/{id}")` with `@PathVariable Long id`, delegates to `taxCalculationService.delete(id)`, returns `ResponseEntity<Void>` with `204 No Content`
- [x] T016 [US4] Write US4 test scenarios in `src/test/java/com/psybergate/financialcalculator/tax/SavedCalculationsSpec.java`: (1) save a calculation, `DELETE /api/tax/{id}` → 204; (2) after delete, `GET /api/tax/{id}` → 404; (3) `DELETE /api/tax/999999` → 404 `message: "Calculation not found"`

**Checkpoint**: Full CRUD lifecycle complete. All 4 user stories verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T017 Run `./mvnw test` from project root — confirm all tests (Features 1–4) pass with zero failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories
- **Phase 3 (US1)**: Depends on Phase 2 — first story
- **Phase 4 (US2)**: Depends on Phase 2 — requires `TaxCalculationNotFoundException` from foundational
- **Phase 5 (US3)**: Depends on Phase 2 + Phase 4 (shares `TaxController` file added in US2's T009)
- **Phase 6 (US4)**: Depends on Phase 2 + Phase 4 (shares `TaxController` file)
- **Phase 7 (Polish)**: Depends on all stories complete

### Within Phase 2

- T002 + T003 can run in parallel (separate files)
- T004 depends on neither — also parallel with T002 and T003

### Within Each User Story

- Service method before controller endpoint (controller delegates to service)
- Tests after implementation in each phase

### Parallel Opportunities

```
Phase 2:
  T002 — TaxCalculationNotFoundException     [P]
  T003 — GlobalExceptionHandler update       [P]
  T004 — TaxCalculationRepository method     [P]
```

---

## Implementation Strategy

### MVP (US1 only)

1. Verify baseline → Phase 2 → Phase 3 → validate `GET /api/tax?userId=X` works

### Full Feature Delivery

Setup → Foundational → US1 → US2 → US3 → US4 → Polish (sequential — `TaxController` is shared)

---

## Notes

- All 4 user story phases extend the same `TaxController` file — must run sequentially
- `SavedCalculationsSpec.java` is a new test class (separate from `TaxCalculationSpec.java`)
- `SavedCalculationsSpec.@BeforeEach` must clear both `taxCalculationRepository` AND `userRepository` (FK constraint, same pattern as other specs)
- The `update()` method keeps the existing `user` reference from the persisted entity — `userId` in PUT body only validates user existence, does not change ownership
- `toResponse()` already exists in `TaxCalculationService` — reuse it in all four new methods
