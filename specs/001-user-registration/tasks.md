# Tasks: User Registration

**Input**: Design documents from `specs/001-user-registration/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks are grouped by user story to enable independent implementation
and testing of each story.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)

---

## Phase 1: Setup

**Purpose**: Confirm project baseline before implementing anything.

- [ ] T001 Verify project compiles and existing tests pass with `./mvnw test` from project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core structures shared by all three user stories — entity, DTOs, repository,
and exception infrastructure. No user story work begins until this phase is complete.

⚠️ **CRITICAL**: All Phase 3/4/5 tasks depend on this phase completing first.

- [ ] T002 Create `User` JPA entity in `src/main/java/com/psybergate/financialcalculator/entity/User.java` with fields: `id` (Long, auto-generated), `firstName` (String, NOT NULL), `lastName` (String, NOT NULL), `email` (String, NOT NULL, UNIQUE) — use `@Entity`, `@Table(name="users")`, Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- [ ] T003 [P] Create `RegisterRequest` DTO in `src/main/java/com/psybergate/financialcalculator/dto/RegisterRequest.java` with fields `firstName`, `lastName`, `email` (all String) — use Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- [ ] T004 [P] Create `UserResponse` DTO in `src/main/java/com/psybergate/financialcalculator/dto/UserResponse.java` with fields `id` (Long), `firstName`, `lastName`, `email` (all String) — use Lombok `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- [ ] T005 Create `UserRepository` interface in `src/main/java/com/psybergate/financialcalculator/repository/UserRepository.java` extending `JpaRepository<User, Long>` — add method `Optional<User> findByEmailIgnoreCase(String email)`
- [ ] T006 Create `EmailAlreadyRegisteredException` in `src/main/java/com/psybergate/financialcalculator/exception/EmailAlreadyRegisteredException.java` extending `RuntimeException` with a message constructor
- [ ] T007 Update `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` to handle `EmailAlreadyRegisteredException` → return `400 Bad Request` with `{ "status": 400, "error": "Bad Request", "message": "Email already registered" }`

**Checkpoint**: Foundation ready — all three user story phases can now begin.

---

## Phase 3: User Story 1 — New User Registers Successfully (Priority: P1) 🎯 MVP

**Goal**: A valid registration request persists the user and returns `201 Created` with the user's full profile including their generated id.

**Independent Test**: `POST /api/auth/register` with `{"firstName":"Saziso","lastName":"Khango","email":"saziso@example.com"}` → `201` with id, firstName, lastName, email in body.

- [ ] T008 [US1] Implement `UserService` in `src/main/java/com/psybergate/financialcalculator/service/UserService.java` with method `UserResponse register(RegisterRequest request)` — lowercase the email before saving, map `User` entity to `UserResponse` DTO, inject `UserRepository`
- [ ] T009 [US1] Implement `AuthController` in `src/main/java/com/psybergate/financialcalculator/controller/AuthController.java` with `@RestController`, `@RequestMapping("/api/auth")` — add `POST /register` method that accepts `@RequestBody RegisterRequest`, delegates to `UserService`, returns `ResponseEntity` with `201 Created` status
- [ ] T010 [US1] Write happy-path test scenario in `src/test/java/com/psybergate/financialcalculator/registration/UserRegistrationSpec.java` — use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`: POST valid request → assert status 201, response body contains id, firstName, lastName, email

**Checkpoint**: `POST /api/auth/register` with valid data returns `201` and persists user. User Story 1 independently verified.

---

## Phase 4: User Story 2 — Duplicate Email Rejected (Priority: P2)

**Goal**: A registration attempt with an already-registered email returns `400 Bad Request` with message `"Email already registered"`.

**Independent Test**: Register `test@example.com`, then register again with same email → second call returns `400` with correct error message.

- [ ] T011 [US2] Add duplicate-email check to `UserService.register()` in `src/main/java/com/psybergate/financialcalculator/service/UserService.java` — call `userRepository.findByEmailIgnoreCase(email)` before saving; if present, throw `EmailAlreadyRegisteredException`
- [ ] T012 [US2] Write duplicate-email test scenario in `src/test/java/com/psybergate/financialcalculator/registration/UserRegistrationSpec.java` — register same email twice → assert second call returns `400` with `message: "Email already registered"`. Also test case-insensitive duplicate (`USER@example.com` vs `user@example.com`) → same `400`

**Checkpoint**: Duplicate email (including case variants) consistently returns `400`. User Story 2 independently verified.

---

## Phase 5: User Story 3 — Invalid / Missing Fields Rejected (Priority: P3)

**Goal**: Blank fields and invalid email format each return `400 Bad Request` with a field-level error message before reaching the service layer.

**Independent Test**: POST with blank `firstName` → `400`; blank `lastName` → `400`; invalid email format → `400`; empty body → `400`.

- [ ] T013 [US3] Add Bean Validation annotations to `RegisterRequest` in `src/main/java/com/psybergate/financialcalculator/dto/RegisterRequest.java` — annotate `firstName` and `lastName` with `@NotBlank`, annotate `email` with `@NotBlank` and `@Email`
- [ ] T014 [US3] Add `@Valid` annotation to the `@RequestBody RegisterRequest` parameter in `AuthController.register()` in `src/main/java/com/psybergate/financialcalculator/controller/AuthController.java`
- [ ] T015 [US3] Verify `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` correctly handles `MethodArgumentNotValidException` → extracts first field error and returns `400` with `{ "status": 400, "error": "Bad Request", "message": "<field>: <message>" }`
- [ ] T016 [US3] Write validation test scenarios in `src/test/java/com/psybergate/financialcalculator/registration/UserRegistrationSpec.java` — assert `400` for: blank firstName, blank lastName, invalid email format `"not-an-email"`, whitespace-only firstName, empty request body

**Checkpoint**: All invalid inputs return `400` with correct error shape. All three user stories verified. Full feature functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and cleanup across all stories.

- [ ] T017 Run all quickstart `curl` commands from `specs/001-user-registration/quickstart.md` against the running app and confirm each expected response matches
- [ ] T018 Run `./mvnw test` from project root — confirm all tests pass with zero failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories
- **Phase 3 (US1)**: Depends on Phase 2 — first story to implement
- **Phase 4 (US2)**: Depends on Phase 2 — builds on UserService from Phase 3
- **Phase 5 (US3)**: Depends on Phase 2 — adds annotations to DTOs/Controller from Phase 3
- **Phase 6 (Polish)**: Depends on all stories complete

### Within Phase 2 (Foundational)

- T002 first (entity needed by T005)
- T003 and T004 can run in parallel [P]
- T005 depends on T002
- T006 can run in parallel with T005 [P]
- T007 depends on T006

### Within Each User Story

- Tests written last (after implementation is in place)
- Service before controller (controller delegates to service)

### Parallel Opportunities

- T003 + T004 can run in parallel (independent files)
- T006 + T005 can run in parallel (independent files)

---

## Parallel Example: Phase 2 Foundational

```
Start immediately:
  T002 — Create User entity

After T002:
  T003 — Create RegisterRequest DTO   (parallel)
  T004 — Create UserResponse DTO      (parallel)
  T005 — Create UserRepository        (parallel after T002)
  T006 — Create EmailAlreadyRegisteredException (parallel)

After T006:
  T007 — Update GlobalExceptionHandler
```

---

## Implementation Strategy

### MVP (User Story 1 only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: `POST /api/auth/register` happy path works end-to-end
5. Demo to front-end team

### Full Feature Delivery

1. Setup → Foundational → US1 → US2 → US3 → Polish
2. Each story independently testable before moving to the next
3. Run `./mvnw test` after each phase checkpoint

---

## Notes

- [P] tasks = different files, no shared dependencies
- [Story] label maps each task to its user story for traceability
- Test class `UserRegistrationSpec.java` accumulates all scenarios across US1/US2/US3
- Email lowercase normalisation happens in `UserService`, not in the entity setter
- `GlobalExceptionHandler` already exists in the scaffold — extend it, do not replace it
