# Tasks: Property Bond

**Input**: Design documents from `specs/007-property-bond/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story. All new source files follow the identical Controller → Service → Repository layering as prior features. `PropertyBondSpec.java` mirrors `InvestmentForecastSpec.java`.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no shared dependencies)
- **[Story]**: US1–US5 maps to spec.md user stories

---

## Phase 1: Setup

**Purpose**: Verify baseline before adding any new code.

- [x] T001 Run `./mvnw test` from the project root and confirm all existing tests pass with zero failures

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: New entities, DTOs, exception class, handler extension, repository, and calculation engine — all required by every user story phase.

⚠️ **CRITICAL**: All Phase 3–7 tasks depend on this phase being complete.

- [x] T002 [P] Create `PropertyBondNotFoundException` in `src/main/java/com/psybergate/financialcalculator/exception/PropertyBondNotFoundException.java` — extends `RuntimeException`, single constructor `(String message)` that calls `super(message)`

- [x] T003 [P] Extend `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` — add one new `@ExceptionHandler` method: `PropertyBondNotFoundException` → `ResponseEntity` with `404 Not Found` and body `error(404, "Not Found", ex.getMessage())`

- [x] T004 [P] Create `PropertyBond` entity in `src/main/java/com/psybergate/financialcalculator/entity/PropertyBond.java` — `@Entity @Table(name="property_bonds")`, `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long, `@Id @GeneratedValue(strategy=IDENTITY)`), `user` (User, `@ManyToOne(fetch=LAZY) @JoinColumn(name="user_id", nullable=false)`), `title` (String, `@Column(nullable=false)`), `description` (String, `@Column(columnDefinition="TEXT")`), `initialAmount` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `monthlyContribution` (BigDecimal, same), `termMonths` (Integer, `@Column(nullable=false)`), `interestRate` (BigDecimal, `@Column(nullable=false, precision=7, scale=4)`), `totalLoanAmount` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `totalRepayments` (BigDecimal, same), `totalInterestPaid` (BigDecimal, same), `remainingBalance` (BigDecimal, same), `estimatedPayoffMonth` (Integer, `@Column(nullable=false)`), `fullyPaid` (Boolean, `@Column(nullable=false)`), `monthlyProjection` (`List<BondMonthlyProjection>`, `@OneToMany(mappedBy="bond", cascade=CascadeType.ALL, orphanRemoval=true)`, `@OrderBy("month_number ASC")`, initialised with `@Builder.Default` to `new ArrayList<>()`)

- [x] T005 [P] Create `BondMonthlyProjection` entity in `src/main/java/com/psybergate/financialcalculator/entity/BondMonthlyProjection.java` — `@Entity @Table(name="bond_monthly_projections")`, `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long, `@Id @GeneratedValue(strategy=IDENTITY)`), `bond` (PropertyBond, `@ManyToOne(fetch=LAZY) @JoinColumn(name="bond_id", nullable=false)`), `month` (Integer, `@Column(name="month_number", nullable=false)`), `startingBalance` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `monthlyPayment` (BigDecimal, same), `interestCharged` (BigDecimal, same), `principalPaid` (BigDecimal, same), `endingBalance` (BigDecimal, same)

- [x] T006 [P] Create `BondForecastResultDto` in `src/main/java/com/psybergate/financialcalculator/dto/BondForecastResultDto.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `totalLoanAmount` (BigDecimal), `totalRepayments` (BigDecimal), `totalInterestPaid` (BigDecimal), `remainingBalance` (BigDecimal), `estimatedPayoffMonth` (Integer), `fullyPaid` (Boolean)

- [x] T007 [P] Create `BondMonthlyProjectionDto` in `src/main/java/com/psybergate/financialcalculator/dto/BondMonthlyProjectionDto.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `month` (Integer), `startingBalance` (BigDecimal), `monthlyPayment` (BigDecimal), `interestCharged` (BigDecimal), `principalPaid` (BigDecimal), `endingBalance` (BigDecimal)

- [x] T008 [P] Create `PropertyBondRequest` in `src/main/java/com/psybergate/financialcalculator/dto/PropertyBondRequest.java` — `@Data @NoArgsConstructor @AllArgsConstructor`; fields: `userEmail` (String, `@NotBlank @Email`), `title` (String, `@NotBlank`), `description` (String, no constraint), `initialAmount` (BigDecimal, `@NotNull @DecimalMin("0.00")`), `monthlyContribution` (BigDecimal, `@NotNull @DecimalMin("0.00")`), `termMonths` (Integer, `@NotNull @Min(1)`), `interestRate` (BigDecimal, `@NotNull @DecimalMin("0.00") @DecimalMax("100.00")`)

- [x] T009 Create `PropertyBondResponse` in `src/main/java/com/psybergate/financialcalculator/dto/PropertyBondResponse.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long), `userEmail` (String), `title` (String), `description` (String), `initialAmount` (BigDecimal), `monthlyContribution` (BigDecimal), `termMonths` (Integer), `interestRate` (BigDecimal), `forecastResults` (BondForecastResultDto), `monthlyProjection` (`List<BondMonthlyProjectionDto>`)

- [x] T010 Create `PropertyBondRepository` in `src/main/java/com/psybergate/financialcalculator/repository/PropertyBondRepository.java` — `public interface PropertyBondRepository extends JpaRepository<PropertyBond, Long>` with one derived query: `List<PropertyBond> findByUser(User user);`

- [x] T011 Create `PropertyBondCalculator` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondCalculator.java` — `@Service`; define public inner record `BondCalculationResult(BigDecimal totalLoanAmount, BigDecimal totalRepayments, BigDecimal totalInterestPaid, BigDecimal remainingBalance, Integer estimatedPayoffMonth, Boolean fullyPaid, List<BondMonthlyProjectionDto> entries)`; implement single public method `BondCalculationResult calculate(BigDecimal initialAmount, BigDecimal monthlyContribution, Integer termMonths, BigDecimal interestRate)` using:
  - `monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 10, HALF_UP).divide(BigDecimal.valueOf(100), 10, HALF_UP)`
  - `BigDecimal totalRepaymentsAccum = ZERO`, `BigDecimal totalInterestAccum = ZERO`
  - `Integer payoffMonth = null`
  - `BigDecimal balance = initialAmount`
  - Loop m = 1 → termMonths:
    - If `balance.compareTo(ZERO) <= 0`: add zeroed entry `{m, ZERO, ZERO, ZERO, ZERO, ZERO}`, continue
    - `interestCharged = balance.multiply(monthlyRate).setScale(2, HALF_UP)`
    - `principalPaid = monthlyContribution.subtract(interestCharged)`
    - If `principalPaid.compareTo(balance) >= 0` (over-payment in final month): `principalPaid = balance`, `actualPayment = balance.add(interestCharged).setScale(2, HALF_UP)`, `endingBalance = ZERO`, if `payoffMonth == null` set `payoffMonth = m`
    - Else: `actualPayment = monthlyContribution`, `endingBalance = balance.subtract(principalPaid).setScale(2, HALF_UP)`, if `endingBalance.compareTo(ZERO) <= 0` and `payoffMonth == null` set `payoffMonth = m`, then set `endingBalance = ZERO`
    - Accumulate `totalRepaymentsAccum += actualPayment`, `totalInterestAccum += interestCharged`
    - Add `BondMonthlyProjectionDto{m, balance, actualPayment, interestCharged, principalPaid, endingBalance}` to entries list
    - `balance = endingBalance`
  - After loop: `remainingBalance = balance.max(ZERO).setScale(2, HALF_UP)`, `payoffMonth = payoffMonth != null ? payoffMonth : termMonths`
  - Return `BondCalculationResult(initialAmount, totalRepaymentsAccum.setScale(2, HALF_UP), totalInterestAccum.setScale(2, HALF_UP), remainingBalance, payoffMonth, remainingBalance.compareTo(ZERO) == 0, entries)`

**Checkpoint**: All shared infrastructure ready — Phases 3–7 may begin.

---

## Phase 3: User Story 1 — Create and Save a Bond Plan (Priority: P1) 🎯 MVP

**Goal**: `POST /api/bonds` accepts valid inputs, persists the bond plan, and returns `201 Created` with a full response including the correct `monthlyProjection` array and verified per-month calculation values.

**Independent Test**: POST with `initialAmount=1200000`, `monthlyContribution=12000`, `termMonths=2`, `interestRate=11.00`, valid `userEmail` → 201; verify `monthlyProjection[0].interestCharged = 11000.00`, `monthlyProjection[0].principalPaid = 1000.00`, `monthlyProjection[0].endingBalance = 1199000.00`, `monthlyProjection[1].interestCharged = 10990.00`.

- [x] T012 [US1] Create `PropertyBondService` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondService.java` — `@Service @RequiredArgsConstructor`; inject `UserRepository`, `PropertyBondRepository`, `PropertyBondCalculator`; implement `PropertyBondResponse create(PropertyBondRequest request)`: look up user via `userRepository.findByEmailIgnoreCase(request.getUserEmail())`, throw `UserNotFoundException("User not found")` if absent; call `calculator.calculate(request.getInitialAmount(), request.getMonthlyContribution(), request.getTermMonths(), request.getInterestRate())` to get `BondCalculationResult`; build `PropertyBond` entity via builder with all input fields and all 6 summary fields from result; map `result.entries()` to `List<BondMonthlyProjection>` setting `bond` reference on each entry; set `entity.setMonthlyProjection(entries)`; save via `bondRepository.save(entity)`; return `toResponse(saved)`; also implement private `PropertyBondResponse toResponse(PropertyBond b)` that builds `PropertyBondResponse` using builder — maps `b.getMonthlyProjection()` to `List<BondMonthlyProjectionDto>` and wraps summary fields in `BondForecastResultDto`; `userEmail` is sourced from `b.getUser().getEmail()`

- [x] T013 [US1] Create `PropertyBondController` in `src/main/java/com/psybergate/financialcalculator/controller/PropertyBondController.java` — `@RestController @RequestMapping("/api/bonds") @RequiredArgsConstructor`; inject `PropertyBondService`; add `@PostMapping public ResponseEntity<PropertyBondResponse> create(@Valid @RequestBody PropertyBondRequest request)` that returns `ResponseEntity.status(HttpStatus.CREATED).body(bondService.create(request))`

- [x] T014 [US1] Create `PropertyBondSpec` test class in `src/test/java/com/psybergate/financialcalculator/bond/PropertyBondSpec.java` — `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`; inject `MockMvc`, `ObjectMapper`, `PropertyBondRepository`, `UserRepository`; `@BeforeEach` clears both repos (bond repo first due to FK); add helper `registerAndGetEmail(String first, String last, String email)` that POSTs to `/api/auth/register` and returns the email; add helper `buildRequest(String userEmail, String title, BigDecimal initial, BigDecimal monthly, int termMonths, BigDecimal rate)` that returns `PropertyBondRequest`; write US1 scenarios: (1) valid inputs → 201, `jsonPath("$.monthlyProjection.length()")=termMonths`, `jsonPath("$.monthlyProjection[0].interestCharged")=11000.00`, `jsonPath("$.monthlyProjection[0].principalPaid")=1000.00`, `jsonPath("$.monthlyProjection[0].endingBalance")=1199000.00`; (2) blank title → 400; (3) `termMonths=0` → 400; (4) `interestRate=101` → 400; (5) `initialAmount=-1` → 400; (6) unregistered `userEmail` → 404, `message="User not found"`; (7) `interestRate=0` → 201, `jsonPath("$.monthlyProjection[0].interestCharged")=0.00`, `jsonPath("$.forecastResults.fullyPaid")=true`

**Checkpoint**: Create endpoint verified end-to-end. US1 independently validated.

---

## Phase 4: User Story 2 — View All Bond Plans for a User (Priority: P2)

**Goal**: `GET /api/bonds?userEmail={email}` returns `200 OK` with all bond plans for the user (empty array if none), or `404` if the email does not match a registered user.

**Independent Test**: Create two bonds for userA and one for userB. `GET /api/bonds?userEmail=userA` → 200, array length 2, none belonging to userB. `GET /api/bonds?userEmail=unknown@example.com` → 404.

- [x] T015 [US2] Add `findAllByUser(String userEmail)` to `PropertyBondService` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondService.java` — look up user via `userRepository.findByEmailIgnoreCase(userEmail)`, throw `UserNotFoundException("User not found")` if absent; call `bondRepository.findByUser(user)`, map each entity to response via `toResponse()`, return `List<PropertyBondResponse>`

- [x] T016 [US2] Add `GET /api/bonds` endpoint to `PropertyBondController` in `src/main/java/com/psybergate/financialcalculator/controller/PropertyBondController.java` — `@GetMapping public ResponseEntity<List<PropertyBondResponse>> getAllByUser(@RequestParam String userEmail)` delegating to `bondService.findAllByUser(userEmail)`, returning `ResponseEntity.ok(...)`

- [x] T017 [US2] Add US2 test scenarios to `PropertyBondSpec` in `src/test/java/com/psybergate/financialcalculator/bond/PropertyBondSpec.java` — add helper `createBond(String userEmail, String title, ...)` that POSTs to `/api/bonds`; write: (1) two bonds for userA → `GET /api/bonds?userEmail=userA` → 200, `length()=2`; (2) no bonds → 200, `length()=0`; (3) one bond each for userA/userB → userA list has `length()=1` and `$[0].userEmail=userA`; (4) unregistered email → 404, `message="User not found"`

**Checkpoint**: List endpoint verified. US2 independently validated.

---

## Phase 5: User Story 3 — View a Single Bond Plan (Priority: P3)

**Goal**: `GET /api/bonds/{id}` returns `200 OK` with the full record (inputs + summary + monthly projection), or `404` if the ID does not exist.

**Independent Test**: Create a bond, capture its `id`, `GET /api/bonds/{id}` → 200, verify `title`, `forecastResults.totalLoanAmount`, and `monthlyProjection` length match the creation request. `GET /api/bonds/999999` → 404, `message="Property bond not found"`.

- [x] T018 [US3] Add `findById(Long id)` to `PropertyBondService` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondService.java` — call `bondRepository.findById(id)`, throw `PropertyBondNotFoundException("Property bond not found")` if absent, return `toResponse(entity)`

- [x] T019 [US3] Add `GET /api/bonds/{id}` endpoint to `PropertyBondController` in `src/main/java/com/psybergate/financialcalculator/controller/PropertyBondController.java` — `@GetMapping("/{id}") public ResponseEntity<PropertyBondResponse> getById(@PathVariable Long id)` delegating to `bondService.findById(id)`, returning `ResponseEntity.ok(...)`

- [x] T020 [US3] Add US3 test scenarios to `PropertyBondSpec` in `src/test/java/com/psybergate/financialcalculator/bond/PropertyBondSpec.java` — add helper `createBondAndGetId(String userEmail, ...)` that POSTs and extracts the `id`; write: (1) create bond, `GET /api/bonds/{id}` → 200, `$.id` not null, `$.title` matches, `$.forecastResults.totalLoanAmount` non-null, `$.monthlyProjection.length()` = termMonths; (2) `GET /api/bonds/999999` → 404, `status=404`, `error="Not Found"`, `message="Property bond not found"`

**Checkpoint**: Single-record read verified. US3 independently validated.

---

## Phase 6: User Story 4 — Update a Bond Plan (Priority: P4)

**Goal**: `PUT /api/bonds/{id}` replaces inputs, triggers full recalculation, updates the stored record, and returns `200 OK` with recalculated results. The original `id` is preserved. Invalid inputs return `400`, missing id returns `404`.

**Independent Test**: Create bond with `termMonths=12`. `PUT /api/bonds/{id}` with `termMonths=24` → 200; `$.monthlyProjection.length()=24`; `$.id` unchanged; `$.forecastResults.totalLoanAmount` reflects `initialAmount`.

- [x] T021 [US4] Add `update(Long id, PropertyBondRequest request)` to `PropertyBondService` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondService.java` — find existing entity by id via `bondRepository.findById(id)`, throw `PropertyBondNotFoundException("Property bond not found")` if absent; call `calculator.calculate(...)` with new inputs from request; set all input fields on the existing entity (`title`, `description`, `initialAmount`, `monthlyContribution`, `termMonths`, `interestRate`) and all 6 summary fields from `BondCalculationResult`; clear `existing.getMonthlyProjection()` and repopulate from `result.entries()` (setting `bond` reference on each new `BondMonthlyProjection`); save and return `toResponse(bondRepository.save(existing))`

- [x] T022 [US4] Add `PUT /api/bonds/{id}` endpoint to `PropertyBondController` in `src/main/java/com/psybergate/financialcalculator/controller/PropertyBondController.java` — `@PutMapping("/{id}") public ResponseEntity<PropertyBondResponse> update(@PathVariable Long id, @Valid @RequestBody PropertyBondRequest request)` delegating to `bondService.update(id, request)`, returning `ResponseEntity.ok(...)`

- [x] T023 [US4] Add US4 test scenarios to `PropertyBondSpec` in `src/test/java/com/psybergate/financialcalculator/bond/PropertyBondSpec.java` — write: (1) create with `termMonths=12`, PUT with `termMonths=24` → 200, `$.monthlyProjection.length()=24`, same `id`; (2) PUT with invalid field (`termMonths=0`) → 400; (3) PUT to non-existent id 999999 → 404, `message="Property bond not found"`; (4) PUT with changed `monthlyContribution` → 200, `$.forecastResults.totalRepayments` differs from original

**Checkpoint**: Update with full recalculation verified. US4 independently validated.

---

## Phase 7: User Story 5 — Delete a Bond Plan (Priority: P5)

**Goal**: `DELETE /api/bonds/{id}` returns `204 No Content` and permanently removes the record and all its monthly projection entries. Subsequent `GET /api/bonds/{id}` returns `404`.

**Independent Test**: Create a bond, `DELETE /api/bonds/{id}` → 204. Then `GET /api/bonds/{id}` → 404. `DELETE /api/bonds/999999` → 404.

- [x] T024 [US5] Add `delete(Long id)` to `PropertyBondService` in `src/main/java/com/psybergate/financialcalculator/service/PropertyBondService.java` — verify existence via `bondRepository.existsById(id)`, throw `PropertyBondNotFoundException("Property bond not found")` if absent; call `bondRepository.deleteById(id)` (cascade removes all `BondMonthlyProjection` rows automatically via `CascadeType.ALL` + `orphanRemoval=true`)

- [x] T025 [US5] Add `DELETE /api/bonds/{id}` endpoint to `PropertyBondController` in `src/main/java/com/psybergate/financialcalculator/controller/PropertyBondController.java` — `@DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id)` delegating to `bondService.delete(id)`, returning `ResponseEntity.noContent().build()`

- [x] T026 [US5] Add US5 test scenarios to `PropertyBondSpec` in `src/test/java/com/psybergate/financialcalculator/bond/PropertyBondSpec.java` — write: (1) create bond, `DELETE /api/bonds/{id}` → 204; (2) after delete, `GET /api/bonds/{id}` → 404; (3) `DELETE /api/bonds/999999` → 404, `message="Property bond not found"`

**Checkpoint**: Full CRUD lifecycle complete. All 5 user stories verified.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [x] T027 Run `./mvnw test` from the project root — confirm all tests (Features 1–6) pass with zero failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — run immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS all user story phases
- **Phase 3 (US1)**: Depends on Phase 2 — creates service and controller (new files)
- **Phase 4 (US2)**: Depends on Phase 3 — extends service + controller written in US1
- **Phase 5 (US3)**: Depends on Phase 4 — extends same service + controller
- **Phase 6 (US4)**: Depends on Phase 5 — extends same service + controller
- **Phase 7 (US5)**: Depends on Phase 6 — extends same service + controller
- **Phase 8 (Polish)**: Depends on all user story phases complete

### Within Phase 2

T002, T003, T004, T005, T006, T007, T008 are fully parallel (different files, no cross-dependencies).
T009 depends on T006 + T007 (references `BondForecastResultDto` + `BondMonthlyProjectionDto`).
T010 depends on T004 (references `PropertyBond` entity).
T011 depends on T007 (returns `BondMonthlyProjectionDto` entries).

### Within Each User Story Phase

Service method → Controller endpoint → Tests (service must exist before controller, both must exist before tests can run).

### Parallel Opportunities

```
Phase 2:
  T002 — PropertyBondNotFoundException        [P]
  T003 — GlobalExceptionHandler extension     [P]
  T004 — PropertyBond entity                  [P]
  T005 — BondMonthlyProjection entity         [P]
  T006 — BondForecastResultDto                [P]
  T007 — BondMonthlyProjectionDto             [P]
  T008 — PropertyBondRequest                  [P]
  → then T009 (needs T006+T007) + T010 (needs T004) + T011 (needs T007) can run in parallel
```

---

## Implementation Strategy

### MVP (US1 only)

1. Phase 1 (baseline) → Phase 2 (foundation) → Phase 3 (US1)
2. **STOP and VALIDATE**: POST a bond with 2-month term, inspect 201 response: verify `monthlyProjection[0].interestCharged=11000.00` and `monthlyProjection[1].interestCharged=10990.00` match the PRD example

### Full Feature Delivery

Setup → Foundational → US1 → US2 → US3 → US4 → US5 → Polish (sequential: US2–US5 all extend the same service and controller files created in US1)

---

## Notes

- US2–US5 phases all extend the same `PropertyBondService.java` and `PropertyBondController.java` created in US1 — must run sequentially
- `PropertyBondSpec.@BeforeEach` must clear `PropertyBondRepository` before `UserRepository` (FK constraint: bonds reference users)
- `@OrderBy("month_number ASC")` on `monthlyProjection` ensures consistent ordering without sorting in Java; note the column alias is `month_number` matching `@Column(name="month_number")` on the entity field
- `orphanRemoval=true` on the `@OneToMany` means clearing the list in `update()` automatically deletes old projection rows — no manual delete needed
- `PropertyBondCalculator` uses `HALF_UP` rounding throughout; all monetary intermediate values are scaled to 2 decimal places at the end of each month iteration to prevent accumulated drift
- `userEmail` in the response comes from `bond.getUser().getEmail()` in `toResponse()` — Hibernate lazy-loads the user within the same transaction; no `@Transactional` needed since `save()` triggers it within the session
- Zero-balance handling: once a month's `startingBalance == 0`, add a fully-zeroed `BondMonthlyProjectionDto` entry for that month and continue the loop — ensures `monthlyProjection.length() == termMonths` always
- The `@Email` annotation on `userEmail` in `PropertyBondRequest` requires a `jakarta.validation.constraints.Email` import; it validates basic email format client-side before the user lookup
