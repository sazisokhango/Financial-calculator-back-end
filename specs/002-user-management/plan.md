# Implementation Plan: User Management

**Branch**: `002-user-management` | **Date**: 2026-05-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/002-user-management/spec.md`

## Summary

Add two read-only endpoints — `GET /api/user` (all users) and `GET /api/user/{id}` (single
user) — to support the front-end name-selection flow. No new entities or DTOs are needed;
this feature reuses `User` and `UserResponse` from Feature 1. A new `UserController`,
two new `UserService` methods, a `UserNotFoundException`, and a handler extension complete
the implementation.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: No entities in API responses; reuse existing `UserResponse` DTO; layered architecture

**Scale/Scope**: Single-user MVP; Angular front-end at localhost:4200

---

## Constitution Check

| Principle | Check | Status |
|-----------|-------|--------|
| I. REST API First | `GET /api/user → 200`, `GET /api/user/{id} → 200/404`, standard error shape | ✅ |
| II. Layered Architecture | `UserController → UserService → UserRepository`; `UserResponse` DTO at boundary | ✅ |
| III. Strict Input Validation | No request body — path variable `{id}` typed `Long`, Spring handles mismatch | ✅ |
| IV. Consistent Error Handling | `UserNotFoundException` → `GlobalExceptionHandler` → `404 Not Found` | ✅ |
| V. Tax Calculation Correctness | N/A — no monetary fields | ✅ |

**Result**: All gates pass. No violations to justify.

---

## Project Structure

### Documentation (this feature)

```text
specs/002-user-management/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── GET-api-user.md
│   └── GET-api-user-id.md
└── tasks.md             ← Phase 2 output (/speckit-tasks — not yet created)
```

### Source Code

```text
src/main/java/com/psybergate/financialcalculator/
├── controller/
│   └── UserController.java                    ← NEW: GET /api/user, GET /api/user/{id}
├── service/
│   └── UserService.java                       ← EXTEND: add findAll(), findById(Long)
└── exception/
    ├── UserNotFoundException.java             ← NEW: extends RuntimeException
    └── GlobalExceptionHandler.java            ← EXTEND: handle UserNotFoundException → 404

src/test/java/com/psybergate/financialcalculator/
└── user/
    └── UserManagementSpec.java                ← NEW: all user management test scenarios
```

**Reused from Feature 1** (no changes):
- `entity/User.java`
- `dto/UserResponse.java`
- `repository/UserRepository.java`

**Structure Decision**: Single Spring Boot project. Layered package layout per constitution.
Test class `UserManagementSpec.java` follows SpecKit naming convention.

---

## Complexity Tracking

No constitution violations — complexity tracking not required.
