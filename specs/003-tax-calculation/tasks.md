# Tasks: Tax Calculation Engine

**Input**: Design documents from `specs/003-tax-calculation/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story. US2/US3/US4 require no new source files — they add test scenarios to the spec class after the core is built in US1.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1–US4 maps to spec.md user stories

---

## Phase 1: Setup

- [x] T001 Verify all existing tests (14) pass with `./mvnw test` from project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Entity, DTOs, repository, and the SARS calculator service — shared by all four user stories. No story work begins until this phase is complete.

⚠️ **CRITICAL**: All Phase 3–6 tasks depend on this phase.

- [x] T002 Create `TaxCalculation` JPA entity in `src/main/java/com/psybergate/financialcalculator/entity/TaxCalculation.java` — `@Entity`, `@Table(name="tax_calculations")`, Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long PK auto), `user` (`@ManyToOne(fetch=LAZY) @JoinColumn(name="user_id") User`), `title` (String NOT NULL), `description` (String nullable), `salary`/`interestIncome`/`dividend`/`capitalGain`/`bonus`/`retirementAnnuity`/`taxAlreadyPaid` (BigDecimal NOT NULL), `age` (Integer NOT NULL), `totalIncome`/`totalDeductions`/`netTaxableIncome`/`taxBeforeRebate`/`rebate`/`finalTaxLiability` (BigDecimal NOT NULL)
- [x] T003 [P] Create `TaxCalculationRequest` DTO in `src/main/java/com/psybergate/financialcalculator/dto/TaxCalculationRequest.java` — Lombok `@Data @NoArgsConstructor @AllArgsConstructor`; fields: `userId` (Long `@NotNull`), `title` (String `@NotBlank`), `description` (String), `salary`/`interestIncome`/`dividend`/`capitalGain`/`bonus`/`retirementAnnuity`/`taxAlreadyPaid` (BigDecimal each `@DecimalMin("0.00")`), `age` (Integer `@NotNull @Min(0)`)
- [x] T004 [P] Create `TaxCalculationResponse` DTO in `src/main/java/com/psybergate/financialcalculator/dto/TaxCalculationResponse.java` — Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long), `userId` (Long), `title`, `description`, all input BigDecimal/Integer fields, plus `totalIncome`, `totalDeductions`, `netTaxableIncome`, `taxBeforeRebate`, `rebate`, `finalTaxLiability` (all BigDecimal)
- [x] T005 Create `TaxCalculationRepository` interface in `src/main/java/com/psybergate/financialcalculator/repository/TaxCalculationRepository.java` extending `JpaRepository<TaxCalculation, Long>`
- [x] T006 Create `SarsTaxCalculator` in `src/main/java/com/psybergate/financialcalculator/service/SarsTaxCalculator.java` as `@Service` — declare all SARS 2024/2025 constants as named `BigDecimal` fields: bracket thresholds (237100, 370500, 512800, 673000, 857900, 1817000), base taxes (0, 42678, 77362, 121475, 179147, 251258, 644489), rates (18%, 26%, 31%, 36%, 39%, 41%, 45%), rebates (PRIMARY=17235, SECONDARY=9444, TERTIARY=3145); implement `BigDecimal calculateTax(BigDecimal taxableIncome)` applying progressive brackets; implement `BigDecimal calculateRebate(int age)` returning primary/primary+secondary/primary+secondary+tertiary based on age thresholds 65 and 75

**Checkpoint**: All shared infrastructure ready. US1–US4 phases can begin.

---

## Phase 3: User Story 1 — Salaried Employee Under 65 (Priority: P1) 🎯 MVP

**Goal**: `POST /api/tax` saves a tax calculation for a registered user and returns `201 Created` with all 7 breakdown fields correctly computed.

**Independent Test**: `POST /api/tax` with salary=500000, age=35, userId=valid → `201`, `taxBeforeRebate=117507`, `rebate=17235`, `finalTaxLiability=100272`, response includes `id`.

- [x] T007 [US1] Create `TaxCalculationService` in `src/main/java/com/psybergate/financialcalculator/service/TaxCalculationService.java` — inject `UserRepository`, `TaxCalculationRepository`, `SarsTaxCalculator`; implement `TaxCalculationResponse save(TaxCalculationRequest request)`: (1) look up `User` by `request.getUserId()`, throw `UserNotFoundException` if absent; (2) normalise null numeric fields to `BigDecimal.ZERO`; (3) compute `totalIncome`, `totalDeductions`, `netTaxableIncome = MAX(0, totalIncome - totalDeductions)`; (4) call `sarsTaxCalculator.calculateTax(netTaxableIncome)` → `taxBeforeRebate`; (5) call `sarsTaxCalculator.calculateRebate(age)` → `rebate`; (6) compute `finalTaxLiability = MAX(0, taxBeforeRebate - rebate - taxAlreadyPaid)`; (7) build and save `TaxCalculation` entity; (8) map saved entity to `TaxCalculationResponse` and return
- [x] T008 [US1] Create `TaxController` in `src/main/java/com/psybergate/financialcalculator/controller/TaxController.java` — `@RestController @RequestMapping("/api/tax")`; `@PostMapping` method accepting `@Valid @RequestBody TaxCalculationRequest`, delegates to `taxCalculationService.save(request)`, returns `ResponseEntity` with `201 Created`
- [x] T009 [US1] Write US1 test scenarios in `src/test/java/com/psybergate/financialcalculator/tax/TaxCalculationSpec.java` — `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")` with `@BeforeEach` clearing repos: (1) POST salary=500000, age=35, valid userId → assert 201, `taxBeforeRebate=117507.00`, `rebate=17235.00`, `finalTaxLiability=100272.00`, response has `id`; (2) POST with title "My 2025 Tax" → assert title present in response

**Checkpoint**: Core tax calculation and save working end-to-end. US1 verified.

---

## Phase 4: User Story 2 — Age-Based Rebate Tiers (Priority: P2)

**Goal**: The same salary produces different rebates and liabilities for age 70 (secondary rebate) and age 75+ (tertiary rebate). No new source files — only new test scenarios.

**Independent Test**: POST salary=500000, age=70 → `rebate=26679.00`, `finalTaxLiability=90828.00`. POST salary=500000, age=75 → `rebate=29824.00`, `finalTaxLiability=87683.00`.

- [x] T010 [US2] Add age-rebate test scenarios to `src/test/java/com/psybergate/financialcalculator/tax/TaxCalculationSpec.java`: (1) age=70 → assert `rebate=26679.00`, `finalTaxLiability=90828.00`; (2) age=75 → assert `rebate=29824.00`, `finalTaxLiability=87683.00`; (3) age=64 → assert `rebate=17235.00` (primary only); (4) assert age=70 finalTaxLiability strictly less than age=35 finalTaxLiability for same salary

**Checkpoint**: All three rebate tiers verified. US2 independently validated.

---

## Phase 5: User Story 3 — Multiple Income Sources and Deductions (Priority: P3)

**Goal**: Mixed income fields are summed correctly, retirement annuity reduces net taxable income, and `taxAlreadyPaid` reduces the final liability. No new source files.

**Independent Test**: salary=400000, bonus=50000, interestIncome=10000, retirementAnnuity=24000, age=40 → `totalIncome=460000.00`, `totalDeductions=24000.00`, `netTaxableIncome=436000.00`, `taxBeforeRebate=97667.00`, `finalTaxLiability=80432.00`.

- [x] T011 [US3] Add multi-income test scenarios to `src/test/java/com/psybergate/financialcalculator/tax/TaxCalculationSpec.java`: (1) salary=400000 + bonus=50000 + interestIncome=10000 + retirementAnnuity=24000, age=40 → assert `totalIncome=460000.00`, `totalDeductions=24000.00`, `netTaxableIncome=436000.00`, `taxBeforeRebate=97667.00`, `finalTaxLiability=80432.00`; (2) retirementAnnuity > totalIncome (e.g. salary=5000, retirementAnnuity=10000) → assert `netTaxableIncome=0.00`; (3) taxAlreadyPaid=200000, salary=500000, age=35 → assert `finalTaxLiability=0.00` (not negative)

**Checkpoint**: Deductions, multi-income, and floor-at-zero all verified. US3 independently validated.

---

## Phase 6: User Story 4 — Reject Invalid or Missing Inputs (Priority: P4)

**Goal**: Missing `title`, negative numeric fields, and non-existent `userId` each return the correct error response before any calculation runs.

**Independent Test**: POST with no title → 400. POST with salary=-1 → 400. POST with userId=999 → 404 with `message: "User not found"`.

- [x] T012 [US4] Add validation test scenarios to `src/test/java/com/psybergate/financialcalculator/tax/TaxCalculationSpec.java`: (1) missing title → assert 400 standard error shape; (2) salary=-1000 → assert 400; (3) retirementAnnuity=-1 → assert 400; (4) age=null → assert 400; (5) userId=999999 → assert 404, `status=404`, `error="Not Found"`, `message="User not found"`

**Checkpoint**: All validation paths confirmed. All 4 user stories verified. Full feature functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [x] T013 Run `./mvnw test` from project root — confirm all tests (Features 1, 2, 3) pass with zero failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user stories
- **Phase 3 (US1)**: Depends on Phase 2 — first and largest story, creates all source files
- **Phase 4 (US2)**: Depends on Phase 3 (needs `TaxCalculationSpec.java` to exist) — tests only
- **Phase 5 (US3)**: Depends on Phase 3 — tests only
- **Phase 6 (US4)**: Depends on Phase 3 — tests only
- **Phase 7 (Polish)**: Depends on all stories complete

### Within Phase 2

- T002 first (entity needed by T005, T006 uses no entity)
- T003, T004, T005, T006 can all run in parallel after T002

### Within Phase 3

- T007 (service) before T008 (controller delegates to service)
- T009 (test) after T008

### Phases 4–6

- Each adds only new test methods to `TaxCalculationSpec.java` — must run sequentially (same file)

### Parallel Opportunities

```
Phase 2 after T002:
  T003 — TaxCalculationRequest DTO     [P]
  T004 — TaxCalculationResponse DTO    [P]
  T005 — TaxCalculationRepository      [P]
  T006 — SarsTaxCalculator             [P]
```

---

## Implementation Strategy

### MVP (US1 only — Phases 1–3)

1. Verify baseline → build foundational layer → implement service + controller → verify test
2. At this point `POST /api/tax` works end-to-end for the common case

### Full Feature Delivery

Setup → Foundational → US1 → US2 → US3 → US4 → Polish

---

## Notes

- `SarsTaxCalculator` bracket computation: find the highest bracket threshold ≤ taxableIncome, apply `baseTax + rate × (taxableIncome - threshold)`
- `BigDecimal.ZERO.max(value)` implements `MAX(0, value)` cleanly
- `TaxCalculationSpec.java` accumulates all scenarios across US1–US4; use `@BeforeEach userRepository.deleteAll()` + `taxCalculationRepository.deleteAll()` to reset between tests
- Null normalisation (`null → ZERO`) happens in `TaxCalculationService.save()`, not in the entity or DTO
- The `SarsTaxCalculator` service itself can be separately unit-tested with plain `new SarsTaxCalculator()` if needed — no Spring context required for those assertions
