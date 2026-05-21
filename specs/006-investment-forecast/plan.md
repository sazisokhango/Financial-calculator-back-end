# Implementation Plan: Investment Forecast

**Branch**: `006-investment-forecast` | **Date**: 2026-05-21 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/006-investment-forecast/spec.md`

---

## Summary

Allow registered users to create, retrieve, update, and delete investment forecasts. Each forecast is computed using monthly compound interest and returns a 5-field summary breakdown plus a month-by-month projection array. Persisted per user in a normalised two-table schema; full CRUD is exposed via `/api/investments`.

---

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.5.0, Spring Data JPA, Spring Validation, Lombok

**Storage**: PostgreSQL (main) | H2 in-memory (test — `@ActiveProfiles("test")`)

**Testing**: JUnit 5 via `spring-boot-starter-test` with MockMvc | `./mvnw test`

**Target Platform**: Linux server, port 8080

**Project Type**: REST web-service

**Performance Goals**: N/A for MVP

**Constraints**: `BigDecimal` for all monetary and percentage values; no entities in API responses; Controller → Service → Repository layering enforced; calculation engine as injectable `@Service`

**Scale/Scope**: Single-user MVP; Angular front-end at `localhost:4200`

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I — REST API First | ✅ PASS | `POST /api/investments/forecast` → 201; `GET /api/investments` → 200; `GET /api/investments/{id}` → 200; `PUT /api/investments/{id}` → 200; `DELETE /api/investments/{id}` → 204; 400/404 on errors — all match constitution endpoint list |
| II — Layered Architecture | ✅ PASS | `InvestmentForecastController` → `InvestmentForecastService` → `InvestmentForecastRepository`. Calculation delegated to `InvestmentForecastCalculator` (`@Service`). No business logic in controller. |
| III — Strict Input Validation | ✅ PASS | Jakarta Bean Validation on all inputs: `@NotBlank` (title), `@NotNull @DecimalMin("0.00")` (initialAmount, monthlyContribution), `@NotNull @Min(1)` (termMonths), `@NotNull @DecimalMin("0.00") @DecimalMax("100.00")` (annualInterestRate). Validation failure → 400. |
| IV — Consistent Error Handling | ✅ PASS | `GlobalExceptionHandler` extended with `InvestmentForecastNotFoundException` → 404 using the standard error shape. No new error structures introduced. |
| V — Tax Calculation Correctness | N/A | Tax-specific principle. Investment formula verified against PRD example: month 1 interest = 10 000 × 0.01 = 100.00 ✓; endingBalance = 12 100.00 ✓. `BigDecimal` used throughout. |

**Post-design re-check**: ✅ All applicable principles satisfied. No violations.

---

## Project Structure

### Documentation (this feature)

```text
specs/006-investment-forecast/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   ├── POST-api-investments-forecast.md
│   ├── GET-api-investments.md
│   ├── GET-api-investments-id.md
│   ├── PUT-api-investments-id.md
│   └── DELETE-api-investments-id.md
└── tasks.md             # Phase 2 output (/speckit-tasks — not created here)
```

### Source Code (repository root)

```text
src/
├── main/java/com/psybergate/financialcalculator/
│   ├── controller/
│   │   └── InvestmentForecastController.java        (new)
│   ├── service/
│   │   ├── InvestmentForecastService.java            (new)
│   │   └── InvestmentForecastCalculator.java         (new)
│   ├── repository/
│   │   └── InvestmentForecastRepository.java         (new)
│   ├── entity/
│   │   ├── InvestmentForecast.java                   (new)
│   │   └── MonthlyProjectionEntry.java               (new)
│   ├── dto/
│   │   ├── InvestmentForecastRequest.java            (new)
│   │   ├── InvestmentForecastResponse.java           (new)
│   │   ├── ForecastResultDto.java                    (new)
│   │   └── MonthlyProjectionEntryDto.java            (new)
│   └── exception/
│       ├── InvestmentForecastNotFoundException.java  (new)
│       └── GlobalExceptionHandler.java               (modified — add handler for InvestmentForecastNotFoundException)
│
└── test/java/com/psybergate/financialcalculator/
    └── investment/
        └── InvestmentForecastSpec.java               (new)
```

**Structure Decision**: Single Spring Boot project (existing). Investment forecast follows the identical Controller → Service → Repository layering as the tax feature. Two new JPA entities normalise the monthly projection into a child table. No changes to any existing class beyond the exception handler addition.
