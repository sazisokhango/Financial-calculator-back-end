# Implementation Plan: Property Bond

**Branch**: `007-property-bond` | **Date**: 2026-05-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/007-property-bond/spec.md`

---

## Summary

Allow registered users to create, retrieve, update, and delete property bond repayment plans. Each plan is computed via declining-balance amortization and returns a 6-field summary breakdown plus a month-by-month projection. User identity resolved by `userEmail`. Full CRUD exposed via `/api/bonds`.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Spring Validation, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` with MockMvc | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: `BigDecimal` for all monetary and percentage values; no entities in API responses; Controller → Service → Repository layering enforced; calculation engine as injectable `@Service`; user identified by `userEmail` (not `userId`)

**Scale/Scope**: Single-user MVP; Angular front-end at `localhost:4200`

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I — REST API First | ✅ PASS | `POST /api/bonds` → 201; `GET /api/bonds` → 200; `GET /api/bonds/{id}` → 200; `PUT /api/bonds/{id}` → 200; `DELETE /api/bonds/{id}` → 204; 400/404 on errors — all match constitution endpoint list (v1.1.0) |
| II — Layered Architecture | ✅ PASS | `PropertyBondController` → `PropertyBondService` → `PropertyBondRepository`. Calculation delegated to `PropertyBondCalculator` (`@Service`). No business logic in controller. |
| III — Strict Input Validation | ✅ PASS | Jakarta Bean Validation on all inputs: `@NotBlank @Email` (userEmail), `@NotBlank` (title), `@NotNull @DecimalMin("0.00")` (initialAmount, monthlyContribution), `@NotNull @Min(1)` (termMonths), `@NotNull @DecimalMin("0.00") @DecimalMax("100.00")` (interestRate). Failures → 400. |
| IV — Consistent Error Handling | ✅ PASS | `GlobalExceptionHandler` extended with `PropertyBondNotFoundException` → 404 using the standard error shape. No new error structures introduced. |
| V — Tax Calculation Correctness | N/A | Tax-specific principle. Bond formula verified: month 1 = 1,200,000 × (11/12/100) = 11,000 interestCharged ✓; principalPaid = 12,000 − 11,000 = 1,000 ✓; endingBalance = 1,199,000 ✓. `BigDecimal` used throughout. |

**Post-design re-check**: ✅ All applicable principles satisfied. No violations.

---

## Project Structure

### Documentation (this feature)

```text
specs/007-property-bond/
├── plan.md                    # This file
├── research.md                # Phase 0 output
├── data-model.md              # Phase 1 output
├── quickstart.md              # Phase 1 output
├── contracts/
│   ├── POST-api-bonds.md
│   ├── GET-api-bonds.md
│   ├── GET-api-bonds-id.md
│   ├── PUT-api-bonds-id.md
│   └── DELETE-api-bonds-id.md
└── tasks.md                   # Phase 2 output (/speckit-tasks — not created here)
```

### Source Code (repository root)

```text
src/
├── main/java/com/psybergate/financialcalculator/
│   ├── controller/
│   │   └── PropertyBondController.java           (new)
│   ├── service/
│   │   ├── PropertyBondService.java              (new)
│   │   └── PropertyBondCalculator.java           (new)
│   ├── repository/
│   │   └── PropertyBondRepository.java           (new)
│   ├── entity/
│   │   ├── PropertyBond.java                     (new)
│   │   └── BondMonthlyProjection.java            (new)
│   ├── dto/
│   │   ├── PropertyBondRequest.java              (new)
│   │   ├── PropertyBondResponse.java             (new)
│   │   ├── BondForecastResultDto.java            (new)
│   │   └── BondMonthlyProjectionDto.java         (new)
│   └── exception/
│       ├── PropertyBondNotFoundException.java    (new)
│       └── GlobalExceptionHandler.java           (modified — add handler)
│
└── test/java/com/psybergate/financialcalculator/
    └── bond/
        └── PropertyBondSpec.java                 (new)
```

**Structure Decision**: Single Spring Boot project (existing). Property bond follows the identical Controller → Service → Repository layering established in all prior features. Two new JPA entities normalise the monthly projection into a child table. `GlobalExceptionHandler` receives one new handler method. No changes to any other existing class.

---

## Complexity Tracking

No constitution violations. No additional patterns introduced beyond what is already established in the project.
