# Implementation Plan: Saved Calculations CRUD

**Branch**: `004-saved-calculations` | **Date**: 2026-05-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/004-saved-calculations/spec.md`

---

## Summary

Complete the CRUD lifecycle for saved tax calculations by adding four operations to the
existing `TaxCalculationService` and `TaxController`: list by user, get by id, update with
recalculation, and hard delete. No new entities or DTOs are required. A new
`TaxCalculationNotFoundException` handles 404 cases for calculations.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: `BigDecimal` for monetary values; no entities at API boundary; layered architecture; full recalculation on PUT

**Scale/Scope**: Single-user MVP; Angular front-end at localhost:4200

---

## Constitution Check

| Principle | Check | Status |
|-----------|-------|--------|
| I. REST API First | `GET /api/tax → 200`, `GET /api/tax/{id} → 200/404`, `PUT → 200/400/404`, `DELETE → 204/404` | ✅ |
| II. Layered Architecture | `TaxController → TaxCalculationService → TaxCalculationRepository`; `TaxCalculationResponse` DTO at boundary | ✅ |
| III. Strict Input Validation | PUT reuses `@Valid TaxCalculationRequest` — same annotations as POST | ✅ |
| IV. Consistent Error Handling | `TaxCalculationNotFoundException → GlobalExceptionHandler → 404` | ✅ |
| V. Tax Calculation Correctness | PUT recalculates via `SarsTaxCalculator` — same engine as Feature 3 | ✅ |

**Result**: All gates pass. No violations to justify.

---

## Project Structure

### Documentation (this feature)

```text
specs/004-saved-calculations/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── GET-api-tax.md
│   ├── GET-api-tax-id.md
│   ├── PUT-api-tax-id.md
│   └── DELETE-api-tax-id.md
└── tasks.md             ← Phase 2 output (/speckit-tasks — not yet created)
```

### Source Code

```text
src/main/java/com/psybergate/financialcalculator/
├── exception/
│   └── TaxCalculationNotFoundException.java    ← NEW
│   └── GlobalExceptionHandler.java             ← EXTEND: handle TaxCalculationNotFoundException → 404
├── repository/
│   └── TaxCalculationRepository.java           ← EXTEND: add findByUser(User user)
├── service/
│   └── TaxCalculationService.java              ← EXTEND: findAllByUser, findById, update, delete
└── controller/
    └── TaxController.java                      ← EXTEND: GET list, GET by id, PUT, DELETE

src/test/java/com/psybergate/financialcalculator/
└── tax/
    └── SavedCalculationsSpec.java              ← NEW: all 4 user story scenarios
```

**Reused from Feature 3** (no changes):
- `entity/TaxCalculation.java`
- `dto/TaxCalculationRequest.java` + `TaxCalculationResponse.java`
- `service/SarsTaxCalculator.java`

**Structure Decision**: Single Spring Boot project. All changes are extensions to existing
Feature 3 files except `TaxCalculationNotFoundException` (new) and `SavedCalculationsSpec`
(new test class to keep concerns separated from `TaxCalculationSpec`).

---

## Complexity Tracking

No constitution violations — complexity tracking not required.
