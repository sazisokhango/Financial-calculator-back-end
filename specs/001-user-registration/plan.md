# Implementation Plan: User Registration

**Branch**: `001-user-registration` | **Date**: 2026-05-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-user-registration/spec.md`

---

## Summary

Implement the `POST /api/auth/register` endpoint that accepts a `firstName`, `lastName`, and
`email`, persists the user to the `users` table, and returns a `UserResponse` DTO on success.
Email must be validated for format and uniqueness (case-insensitive). All rejections return
`400 Bad Request` in the standard error shape. No authentication is required.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Spring Validation, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: No entities in API responses; layered architecture enforced; email stored lowercase

**Scale/Scope**: Single-user MVP; Angular front-end at localhost:4200

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Check | Status |
|-----------|-------|--------|
| I. REST API First | `POST /api/auth/register → 201`, errors → `400`, standard error shape | ✅ |
| II. Layered Architecture | `AuthController → UserService → UserRepository`; DTOs at boundary; no entity in response | ✅ |
| III. Strict Input Validation | `@NotBlank` on firstName/lastName, `@Email` on email; duplicate email → 400 | ✅ |
| IV. Consistent Error Handling | `GlobalExceptionHandler` handles `MethodArgumentNotValidException` + duplicate exception | ✅ |
| V. Tax Calculation Correctness | N/A — this feature has no monetary fields | ✅ |

**Result**: All gates pass. No violations to justify.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-user-registration/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── POST-api-auth-register.md
└── tasks.md             ← Phase 2 output (/speckit-tasks — not yet created)
```

### Source Code

```text
src/main/java/com/psybergate/financialcalculator/
├── entity/
│   └── User.java                          ← JPA entity
├── dto/
│   ├── RegisterRequest.java               ← inbound DTO
│   └── UserResponse.java                  ← outbound DTO
├── repository/
│   └── UserRepository.java                ← Spring Data JPA repository
├── service/
│   └── UserService.java                   ← business logic, duplicate check
├── controller/
│   └── AuthController.java                ← POST /api/auth/register
└── exception/
    ├── EmailAlreadyRegisteredException.java
    └── GlobalExceptionHandler.java        ← already exists, extend for new exception

src/test/java/com/psybergate/financialcalculator/
└── registration/
    └── UserRegistrationSpec.java          ← all registration test scenarios
```

**Structure Decision**: Single Spring Boot project. Standard layered package layout per
constitution. Test class name follows SpecKit convention: `EntityNameSpec.java`.

---

## Complexity Tracking

No constitution violations — complexity tracking not required.
