# Implementation Plan: Tax Calculation Engine

**Branch**: `003-tax-calculation` | **Date**: 2026-05-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-tax-calculation/spec.md`

---

## Summary

Implement `POST /api/tax` which accepts tax inputs scoped to a registered user, computes
a full SARS 2024/2025 tax breakdown, persists the result, and returns `201 Created` with
all 7 breakdown fields. A dedicated `SarsTaxCalculator` service holds all SARS bracket
and rebate constants. `BigDecimal` is used for all monetary arithmetic.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Spring Validation, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: `BigDecimal` for all monetary values; all bracket/rebate values as named constants; no entities in API responses; layered architecture

**Scale/Scope**: Single-user MVP; Angular front-end at localhost:4200

---

## Constitution Check

| Principle | Check | Status |
|-----------|-------|--------|
| I. REST API First | `POST /api/tax → 201`; validation → 400; user not found → 404; standard error shape | ✅ |
| II. Layered Architecture | `TaxController → TaxCalculationService → TaxCalculationRepository`; DTOs at boundary | ✅ |
| III. Strict Input Validation | `@NotBlank` on title, `@DecimalMin("0.00")` on all monetary fields, `@NotNull @Min(0)` on age | ✅ |
| IV. Consistent Error Handling | `GlobalExceptionHandler` handles validation + `UserNotFoundException` → 404 | ✅ |
| V. Tax Calculation Correctness | `SarsTaxCalculator` holds named constants; `BigDecimal` throughout; all 7 fields returned | ✅ |

**Result**: All gates pass. No violations to justify.

---

## Project Structure

### Documentation (this feature)

```text
specs/003-tax-calculation/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── POST-api-tax.md
└── tasks.md             ← Phase 2 output (/speckit-tasks — not yet created)
```

### Source Code

```text
src/main/java/com/psybergate/financialcalculator/
├── entity/
│   └── TaxCalculation.java                    ← NEW: JPA entity, all inputs + breakdown
├── dto/
│   ├── TaxCalculationRequest.java             ← NEW: inbound DTO with validation
│   └── TaxCalculationResponse.java            ← NEW: outbound DTO (full breakdown)
├── repository/
│   └── TaxCalculationRepository.java          ← NEW: JpaRepository<TaxCalculation, Long>
├── service/
│   ├── SarsTaxCalculator.java                 ← NEW: SARS constants + calculation logic
│   └── TaxCalculationService.java             ← NEW: user lookup, calculate, persist
└── controller/
    └── TaxController.java                     ← NEW: POST /api/tax

src/test/java/com/psybergate/financialcalculator/
└── tax/
    └── TaxCalculationSpec.java                ← NEW: all 4 user story scenarios
```

**Reused from Features 1 & 2** (no changes):
- `entity/User.java` — referenced via `@ManyToOne` in `TaxCalculation`
- `repository/UserRepository.java` — used to validate `userId`
- `exception/UserNotFoundException.java` — thrown when userId not found
- `exception/GlobalExceptionHandler.java` — already handles `UserNotFoundException`

**Structure Decision**: Single Spring Boot project, standard layered package layout.
`SarsTaxCalculator` is a second `@Service` in the service package — justified by the
constitution's requirement that bracket constants are in a named, dedicated component.

---

## Complexity Tracking

| Pattern | Why Needed | Simpler Alternative Rejected Because |
|---------|------------|--------------------------------------|
| Dedicated `SarsTaxCalculator` service | Constitution V mandates named constants in an isolated component | Putting constants in `TaxCalculationService` conflates persistence orchestration with calculation logic and makes unit testing of bracket math harder |
