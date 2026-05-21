<!--
SYNC IMPACT REPORT
==================
Version change: (new) → 1.0.0
Added sections:
  - Core Principles (I–V)
  - Technology Stack
  - API & Development Standards
  - Governance
Modified principles: N/A (initial ratification)
Removed sections: N/A
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ aligned (Spring Boot / Java / PostgreSQL context reflected)
  - .specify/templates/spec-template.md ✅ aligned (no mandatory section changes required)
  - .specify/templates/tasks-template.md ✅ aligned (task categories reflect layered architecture)
Follow-up TODOs: none — all placeholders resolved

---

Version change: 1.0.0 → 1.0.1 (PATCH — 2026-05-21)
Added: /api/investments endpoint definitions to Principle I and API Standards section (Feature 5 Investment Forecast)
Modified principles: Principle I (endpoint list extended), API & Development Standards (base paths extended)
Removed sections: N/A
Templates requiring updates: none — no structural principle changes
-->

# Financial Calculator Back-End Constitution

## Core Principles

### I. REST API First

Every feature MUST be exposed exclusively through a RESTful HTTP API.
HTTP methods and status codes are non-negotiable:

- `POST   /api/auth/register`          → `201 Created`
- `GET    /api/user`                   → `200 OK`
- `GET    /api/user/{id}`              → `200 OK`
- `POST   /api/tax`                    → `201 Created`
- `GET    /api/tax`                    → `200 OK`
- `GET    /api/tax/{id}`               → `200 OK`
- `PUT    /api/tax/{id}`               → `200 OK`
- `DELETE /api/tax/{id}`               → `204 No Content`
- `POST   /api/investments/forecast`   → `201 Created`
- `GET    /api/investments`            → `200 OK`
- `GET    /api/investments/{id}`       → `200 OK`
- `PUT    /api/investments/{id}`       → `200 OK`
- `DELETE /api/investments/{id}`       → `204 No Content`
- Resource not found                   → `404 Not Found`
- Invalid request body                 → `400 Bad Request`

All error responses MUST follow this exact JSON shape:

```json
{ "status": 404, "error": "Not Found", "message": "Descriptive message here" }
```

No endpoint MAY return a different error structure.

### II. Layered Architecture (NON-NEGOTIABLE)

The codebase MUST follow a strict three-layer separation:

```
Controller  →  Service  →  Repository
```

- **Controller**: HTTP boundary only — maps requests/responses, delegates to service.
  No business logic permitted.
- **Service**: Business logic, validation orchestration, tax calculation, exception handling.
- **Repository**: Data access only — no business logic permitted.

DTOs (`*Request`, `*Response`) MUST be used at the API boundary.
JPA entities MUST NOT be exposed directly in controller responses.

### III. Strict Input Validation

All incoming data MUST be validated using Jakarta Bean Validation before it reaches
the service layer:

- `firstName`, `lastName`, `title` MUST be present and NOT blank.
- `email` MUST be a valid email format and MUST be unique across all users.
- All numeric tax inputs (salary, interestIncome, dividend, capitalGain, bonus,
  retirementAnnuity, taxAlreadyPaid) MUST default to `0` when omitted and MUST be `>= 0`.
- `age` MUST be present and MUST be `>= 0`.

Validation failures MUST produce a `400 Bad Request` response.
No invalid data may reach the service layer.

### IV. Consistent Error Handling

A single, centralised `@RestControllerAdvice` (`GlobalExceptionHandler`) MUST intercept
all unhandled exceptions and translate them into the standard error JSON shape.
No controller method MAY return raw exception messages or stack traces to the client.

Specific cases:
- Duplicate email on registration → `400 Bad Request`
- Resource not found → `404 Not Found`
- Bean validation failure → `400 Bad Request`

### V. Tax Calculation Correctness (NON-NEGOTIABLE)

All tax calculations MUST be SARS 2024/2025 compliant.

- `BigDecimal` MUST be used for ALL monetary values — `double` and `float` are forbidden.
- Tax brackets, rebate thresholds, and rebate amounts MUST be stored as named `BigDecimal`
  constants — no magic numbers in logic.
- Calculation steps MUST follow the exact order defined in the PRD:
  Total Income → Total Deductions → Net Taxable Income → Tax Before Rebate →
  Rebate (age-based) → Final Tax Liability.
- `MAX(0, ...)` MUST be applied to Net Taxable Income and Final Tax Liability.
- The full breakdown (all 7 fields) MUST be returned in every tax response.

---

## Technology Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.0
- **Build Tool**: Maven
- **Database**: PostgreSQL
  - Database name: `financial_calculator`
  - Username: `tax_calc`
  - Password: `password`
- **Test Database**: H2 in-memory (`@ActiveProfiles("test")` on all test classes)
- **Lombok**: Required — use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Base package**: `com.psybergate.financialcalculator`

No alternative language, framework, or database may be substituted without a
constitution amendment.

---

## API & Development Standards

- API base paths: `/api/auth`, `/api/user`, `/api/tax`, `/api/investments`
- All monetary fields serialised as `BigDecimal` (JSON number with 2 decimal places).
- CORS MUST be configured to allow requests from `http://localhost:4200`.
- A `README.md` MUST be maintained at the project root explaining how to run the application.
- Complexity MUST be justified. Any pattern beyond Controller → Service → Repository
  (e.g., additional abstraction layers, event systems) requires explicit documentation
  in the feature plan.
- The tax calculation engine MUST be implemented as an injectable Spring `@Service`
  (`TaxCalculationService` or equivalent) — not as static utility methods.
- Refer to `specs/PRD.md` for originating product requirements.

---

## Governance

This constitution supersedes all other development practices for this project.
Amendments require:

1. A documented reason for the change.
2. An updated version number following semantic versioning:
   - **MAJOR**: removal or redefinition of a principle.
   - **MINOR**: new principle or section added.
   - **PATCH**: clarifications or wording fixes.
3. `LAST_AMENDED_DATE` updated to the date of the amendment.

All implementation plans, task lists, and code reviews MUST verify compliance with
these principles before proceeding.
Runtime development guidance is in `CLAUDE.md` (updated per active feature).

**Version**: 1.0.1 | **Ratified**: 2026-05-20 | **Last Amended**: 2026-05-21
