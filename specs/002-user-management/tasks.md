# Tasks: User Management

**Input**: Design documents from `specs/002-user-management/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2)

---

## Phase 1: Setup

**Purpose**: Confirm baseline passes before adding any code.

- [ ] T001 Verify all existing tests pass with `./mvnw test` from project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Exception infrastructure shared by both user stories. Must be complete before any story work begins.

⚠️ **CRITICAL**: Both Phase 3 and Phase 4 depend on this phase.

- [ ] T002 Create `UserNotFoundException` in `src/main/java/com/psybergate/financialcalculator/exception/UserNotFoundException.java` extending `RuntimeException` with a single `String message` constructor
- [ ] T003 Update `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` to handle `UserNotFoundException` → return `404 Not Found` with body `{ "status": 404, "error": "Not Found", "message": "User not found" }`

**Checkpoint**: Exception infrastructure ready — both user story phases can now begin.

---

## Phase 3: User Story 1 — Browse All Registered Users (Priority: P1) 🎯 MVP

**Goal**: `GET /api/user` returns `200 OK` with a list of all registered users, or an empty list when none exist.

**Independent Test**: `GET /api/user` with two registered users → `200` with array of two `UserResponse` objects. `GET /api/user` with no users → `200` with `[]`.

- [ ] T004 [US1] Add `findAll()` method to `UserService` in `src/main/java/com/psybergate/financialcalculator/service/UserService.java` — call `userRepository.findAll()`, map each `User` to `UserResponse` using builder, return `List<UserResponse>`
- [ ] T005 [US1] Create `UserController` in `src/main/java/com/psybergate/financialcalculator/controller/UserController.java` with `@RestController`, `@RequestMapping("/api/user")` — add `GET` method mapped to `""` that calls `userService.findAll()` and returns `ResponseEntity<List<UserResponse>>` with `200 OK`
- [ ] T006 [US1] Write `GET /api/user` test scenarios in `src/test/java/com/psybergate/financialcalculator/user/UserManagementSpec.java` — use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, `@BeforeEach` clears repo: (1) register two users then `GET /api/user` → assert `200` and array size 2 with correct fields; (2) no users registered → assert `200` and empty array `[]`

**Checkpoint**: `GET /api/user` returns correct list. User Story 1 independently verified.

---

## Phase 4: User Story 2 — View a Specific User by ID (Priority: P2)

**Goal**: `GET /api/user/{id}` returns `200 OK` with the user profile, or `404 Not Found` with standard error shape when the id does not exist.

**Independent Test**: Register a user, get their id from the list, `GET /api/user/{id}` → `200` with correct profile. `GET /api/user/999` → `404` with `message: "User not found"`.

- [ ] T007 [US2] Add `findById(Long id)` method to `UserService` in `src/main/java/com/psybergate/financialcalculator/service/UserService.java` — call `userRepository.findById(id)`, map to `UserResponse` if present, throw `UserNotFoundException("User not found")` if absent
- [ ] T008 [US2] Add `GET /{id}` endpoint to `UserController` in `src/main/java/com/psybergate/financialcalculator/controller/UserController.java` — method accepts `@PathVariable Long id`, delegates to `userService.findById(id)`, returns `ResponseEntity<UserResponse>` with `200 OK`
- [ ] T009 [US2] Write `GET /api/user/{id}` test scenarios in `src/test/java/com/psybergate/financialcalculator/user/UserManagementSpec.java`: (1) register a user, retrieve their id, `GET /api/user/{id}` → assert `200` with correct firstName, lastName, email; (2) `GET /api/user/999` → assert `404`, `status: 404`, `error: "Not Found"`, `message: "User not found"`

**Checkpoint**: Both endpoints fully functional. All user stories verified.

---

## Phase 5: Polish & Cross-Cutting Concerns

- [ ] T010 Run `./mvnw test` from project root — confirm all tests (Feature 1 + Feature 2) pass with zero failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS both user stories
- **Phase 3 (US1)**: Depends on Phase 2 — implement first
- **Phase 4 (US2)**: Depends on Phase 3 (shares `UserController` file) — implement after US1
- **Phase 5 (Polish)**: Depends on all stories complete

### Within Each Story

- Service method before controller endpoint (controller delegates to service)
- Implementation before tests

### Parallel Opportunities

- T002 + T003: Create exception class and update handler (separate files) — run together
- T004 + (start T007 in parallel if different developer) — both are service methods in same file, so sequential

---

## Parallel Example: Phase 2

```
T002 — Create UserNotFoundException        (parallel)
T003 — Update GlobalExceptionHandler       (parallel)
```

---

## Implementation Strategy

### MVP (User Story 1 only)

1. Phase 1: Verify baseline
2. Phase 2: Exception infrastructure
3. Phase 3: US1 — `GET /api/user`
4. **STOP and VALIDATE**: list endpoint works end-to-end

### Full Feature Delivery

1. Setup → Foundational → US1 → US2 → Polish
2. `./mvnw test` after each phase checkpoint

---

## Notes

- [P] tasks = different files, no shared dependencies
- `UserController` is created in Phase 3 (US1) and extended in Phase 4 (US2) — must be sequential
- `UserManagementSpec.java` accumulates scenarios for both US1 and US2
- No new entity or DTO — `User` entity and `UserResponse` DTO reused from Feature 1
- `UserService` already exists from Feature 1 — extend it, do not replace it
