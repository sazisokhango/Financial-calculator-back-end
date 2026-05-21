# Tasks: Investment Forecast

**Input**: Design documents from `specs/006-investment-forecast/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

**Organization**: Tasks grouped by user story. All new source files follow the identical layering as the existing tax feature. `InvestmentForecastSpec.java` is a new test class mirroring `SavedCalculationsSpec.java`.

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

- [x] T002 [P] Create `InvestmentForecastNotFoundException` in `src/main/java/com/psybergate/financialcalculator/exception/InvestmentForecastNotFoundException.java` — extends `RuntimeException`, single constructor `(String message)` that calls `super(message)`

- [x] T003 [P] Extend `GlobalExceptionHandler` in `src/main/java/com/psybergate/financialcalculator/exception/GlobalExceptionHandler.java` — add two new `@ExceptionHandler` methods: (1) `InvestmentForecastNotFoundException` → `ResponseEntity` with `404 Not Found` and body `error(404, "Not Found", ex.getMessage())`; (2) `IllegalArgumentException` → `ResponseEntity` with `400 Bad Request` and body `error(400, "Bad Request", ex.getMessage())`

- [x] T004 [P] Create `InvestmentForecast` entity in `src/main/java/com/psybergate/financialcalculator/entity/InvestmentForecast.java` — `@Entity @Table(name="investment_forecasts")`, `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long, `@Id @GeneratedValue(strategy=IDENTITY)`), `user` (User, `@ManyToOne(fetch=LAZY) @JoinColumn(name="user_id", nullable=false)`), `title` (String, `@Column(nullable=false)`), `description` (String, `@Column(columnDefinition="TEXT")`), `initialAmount` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `monthlyContribution` (BigDecimal, same), `termMonths` (Integer, `@Column(nullable=false)`), `annualInterestRate` (BigDecimal, `@Column(nullable=false, precision=7, scale=4)`), `projectedValue` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `totalContributions` (same), `totalInterestEarned` (same), `roiPercentage` (BigDecimal, `@Column(nullable=false, precision=10, scale=4)`), `averageMonthlyGrowth` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `monthlyProjection` (`List<MonthlyProjectionEntry>`, `@OneToMany(mappedBy="forecast", cascade=CascadeType.ALL, orphanRemoval=true)`, `@OrderBy("month ASC")`, initialised to `new ArrayList<>()`)

- [x] T005 [P] Create `MonthlyProjectionEntry` entity in `src/main/java/com/psybergate/financialcalculator/entity/MonthlyProjectionEntry.java` — `@Entity @Table(name="investment_forecast_monthly_projections")`, `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long, `@Id @GeneratedValue(strategy=IDENTITY)`), `forecast` (InvestmentForecast, `@ManyToOne(fetch=LAZY) @JoinColumn(name="forecast_id", nullable=false)`), `month` (Integer, `@Column(nullable=false)`), `startingBalance` (BigDecimal, `@Column(nullable=false, precision=15, scale=2)`), `monthlyContribution` (BigDecimal, same), `interestEarned` (BigDecimal, same), `endingBalance` (BigDecimal, same)

- [x] T006 [P] Create `ForecastResultDto` in `src/main/java/com/psybergate/financialcalculator/dto/ForecastResultDto.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `projectedValue` (BigDecimal), `totalContributions` (BigDecimal), `totalInterestEarned` (BigDecimal), `roiPercentage` (BigDecimal), `averageMonthlyGrowth` (BigDecimal)

- [x] T007 [P] Create `MonthlyProjectionEntryDto` in `src/main/java/com/psybergate/financialcalculator/dto/MonthlyProjectionEntryDto.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `month` (Integer), `startingBalance` (BigDecimal), `monthlyContribution` (BigDecimal), `interestEarned` (BigDecimal), `endingBalance` (BigDecimal)

- [x] T008 [P] Create `InvestmentForecastRequest` in `src/main/java/com/psybergate/financialcalculator/dto/InvestmentForecastRequest.java` — `@Data @NoArgsConstructor @AllArgsConstructor`; fields: `userId` (Long, `@NotNull`), `title` (String, `@NotBlank`), `description` (String, no annotation), `initialAmount` (BigDecimal, `@NotNull @DecimalMin("0.00")`), `monthlyContribution` (BigDecimal, `@NotNull @DecimalMin("0.00")`), `termMonths` (Integer, `@NotNull @Min(1)`), `annualInterestRate` (BigDecimal, `@NotNull @DecimalMin("0.00") @DecimalMax("100.00")`)

- [x] T009 Create `InvestmentForecastResponse` in `src/main/java/com/psybergate/financialcalculator/dto/InvestmentForecastResponse.java` — `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; fields: `id` (Long), `userId` (Long), `title` (String), `description` (String), `initialAmount` (BigDecimal), `monthlyContribution` (BigDecimal), `termMonths` (Integer), `annualInterestRate` (BigDecimal), `forecastResults` (ForecastResultDto), `monthlyProjection` (`List<MonthlyProjectionEntryDto>`)

- [x] T010 Create `InvestmentForecastRepository` in `src/main/java/com/psybergate/financialcalculator/repository/InvestmentForecastRepository.java` — `public interface InvestmentForecastRepository extends JpaRepository<InvestmentForecast, Long>` with one derived query method: `List<InvestmentForecast> findByUser(User user);`

- [x] T011 Create `InvestmentForecastCalculator` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastCalculator.java` — `@Service`; define a public inner record `ForecastCalculationResult(BigDecimal projectedValue, BigDecimal totalContributions, BigDecimal totalInterestEarned, BigDecimal roiPercentage, BigDecimal averageMonthlyGrowth, List<MonthlyProjectionEntryDto> entries)`; implement single public method `ForecastCalculationResult calculate(BigDecimal initialAmount, BigDecimal monthlyContribution, Integer termMonths, BigDecimal annualInterestRate)` using: `monthlyRate = annualInterestRate.divide(BigDecimal.valueOf(12), 10, HALF_UP).divide(BigDecimal.valueOf(100), 10, HALF_UP)`; iterate months 1→termMonths: `interestEarned = startingBalance.multiply(monthlyRate).setScale(2, HALF_UP)`, `endingBalance = startingBalance.add(monthlyContribution).add(interestEarned).setScale(2, HALF_UP)`, add `MonthlyProjectionEntryDto` to list, advance balance; after loop: `totalContributions = initialAmount.add(monthlyContribution.multiply(BigDecimal.valueOf(termMonths))).setScale(2, HALF_UP)`, `totalInterestEarned = projectedValue.subtract(totalContributions).setScale(2, HALF_UP)`, `roiPercentage = totalContributions.compareTo(ZERO)==0 ? ZERO : totalInterestEarned.divide(totalContributions, 4, HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, HALF_UP)`, `averageMonthlyGrowth = totalInterestEarned.divide(BigDecimal.valueOf(termMonths), 2, HALF_UP)`

**Checkpoint**: All shared infrastructure ready — Phases 3–7 may begin.

---

## Phase 3: User Story 1 — Create an Investment Forecast (Priority: P1) 🎯 MVP

**Goal**: `POST /api/investments/forecast` accepts valid inputs, persists the forecast, and returns `201 Created` with a full response including the correct `monthlyProjection` array length and verified per-month values.

**Independent Test**: POST with `initialAmount=10000`, `monthlyContribution=2000`, `termMonths=2`, `annualInterestRate=12` → 201; verify `monthlyProjection[0].interestEarned = 100.00`, `monthlyProjection[0].endingBalance = 12100.00`, `monthlyProjection[1].interestEarned = 121.00`.

- [x] T012 [US1] Create `InvestmentForecastService` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastService.java` — `@Service @RequiredArgsConstructor`; inject `UserRepository`, `InvestmentForecastRepository`, `InvestmentForecastCalculator`; implement `InvestmentForecastResponse create(InvestmentForecastRequest request)`: look up user by `request.getUserId()` via `userRepository.findById()`, throw `UserNotFoundException("User not found")` if absent; call `calculator.calculate(...)` to get `ForecastCalculationResult`; build `InvestmentForecast` entity via builder with all input fields and all 5 summary fields from result; map `result.entries()` to `List<MonthlyProjectionEntry>` setting `forecast` reference on each entry; set `entity.setMonthlyProjection(entries)`; save via `forecastRepository.save(entity)`; return `toResponse(saved)`; also implement private `toResponse(InvestmentForecast f)` helper that builds `InvestmentForecastResponse` using builder — maps `f.getMonthlyProjection()` to `List<MonthlyProjectionEntryDto>` and wraps summary fields in `ForecastResultDto`

- [x] T013 [US1] Create `InvestmentForecastController` in `src/main/java/com/psybergate/financialcalculator/controller/InvestmentForecastController.java` — `@RestController @RequestMapping("/api/investments") @RequiredArgsConstructor`; inject `InvestmentForecastService`; add `@PostMapping("/forecast") public ResponseEntity<InvestmentForecastResponse> create(@Valid @RequestBody InvestmentForecastRequest request)` that returns `ResponseEntity.status(HttpStatus.CREATED).body(forecastService.create(request))`

- [x] T014 [US1] Create `InvestmentForecastSpec` test class in `src/test/java/com/psybergate/financialcalculator/investment/InvestmentForecastSpec.java` — `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`; inject `MockMvc`, `ObjectMapper`, `InvestmentForecastRepository`, `UserRepository`; `@BeforeEach` clears both repos (forecast repo first due to FK); add helper methods `registerAndGetId(...)` and `buildRequest(Long userId, String title, BigDecimal initial, BigDecimal monthly, int termMonths, BigDecimal rate)`; write US1 scenarios: (1) valid inputs → 201, `jsonPath("$.monthlyProjection.length()")=termMonths`, `jsonPath("$.monthlyProjection[0].interestEarned")=100.00`, `jsonPath("$.monthlyProjection[0].endingBalance")=12100.00`; (2) blank title → 400; (3) `termMonths=0` → 400; (4) `annualInterestRate=101` → 400; (5) non-existent userId → 404, `message: "User not found"`; (6) `annualInterestRate=0` → 201, `jsonPath("$.monthlyProjection[0].interestEarned")=0.00`

**Checkpoint**: Create endpoint verified end-to-end. US1 independently validated.

---

## Phase 4: User Story 2 — View All Saved Forecasts (Priority: P2)

**Goal**: `GET /api/investments?userId={userId}` returns `200 OK` with all forecasts for the user, an empty list if none, and `404` if the user does not exist.

**Independent Test**: Create two forecasts for user A and one for user B. `GET /api/investments?userId=A` → 200, array length 2, no records belonging to user B. `GET /api/investments?userId=999` → 404.

- [x] T015 [US2] Add `findAllByUser(Long userId)` to `InvestmentForecastService` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastService.java` — look up user by id via `userRepository.findById()`, throw `UserNotFoundException("User not found")` if absent; call `forecastRepository.findByUser(user)`, map each entity to response via `toResponse()`, return `List<InvestmentForecastResponse>`

- [x] T016 [US2] Add `GET /api/investments` endpoint to `InvestmentForecastController` in `src/main/java/com/psybergate/financialcalculator/controller/InvestmentForecastController.java` — `@GetMapping public ResponseEntity<List<InvestmentForecastResponse>> getAllByUser(@RequestParam Long userId)` delegating to `forecastService.findAllByUser(userId)`, returning `ResponseEntity.ok(...)`

- [x] T017 [US2] Add US2 test scenarios to `InvestmentForecastSpec` in `src/test/java/com/psybergate/financialcalculator/investment/InvestmentForecastSpec.java` — add helper `createForecast(Long userId, ...)` that POSTs to `/api/investments/forecast`; write: (1) two forecasts for userA → `GET /api/investments?userId=userA` → 200, `length()=2`; (2) no forecasts → 200, `length()=0`; (3) one forecast each for userA/userB → userA list has `length()=1` and `$[0].userId = userA`; (4) non-existent userId → 404, `message: "User not found"`

**Checkpoint**: List endpoint verified. US2 independently validated.

---

## Phase 5: User Story 3 — View a Single Saved Forecast (Priority: P3)

**Goal**: `GET /api/investments/{id}` returns `200 OK` with the full record (inputs + summary + monthly projection), or `404` if the id does not exist.

**Independent Test**: Create a forecast, capture its `id`, `GET /api/investments/{id}` → 200, verify `title`, `forecastResults.projectedValue`, and `monthlyProjection` length match the creation request. `GET /api/investments/999999` → 404, `message: "Investment forecast not found"`.

- [x] T018 [US3] Add `findById(Long id)` to `InvestmentForecastService` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastService.java` — call `forecastRepository.findById(id)`, throw `InvestmentForecastNotFoundException("Investment forecast not found")` if absent, return `toResponse(entity)`

- [x] T019 [US3] Add `GET /api/investments/{id}` endpoint to `InvestmentForecastController` in `src/main/java/com/psybergate/financialcalculator/controller/InvestmentForecastController.java` — `@GetMapping("/{id}") public ResponseEntity<InvestmentForecastResponse> getById(@PathVariable Long id)` delegating to `forecastService.findById(id)`, returning `ResponseEntity.ok(...)`

- [x] T020 [US3] Add US3 test scenarios to `InvestmentForecastSpec` in `src/test/java/com/psybergate/financialcalculator/investment/InvestmentForecastSpec.java` — add helper `createForecastAndGetId(Long userId, ...)` that POSTs and extracts the `id`; write: (1) create forecast, `GET /api/investments/{id}` → 200, verify `$.id`, `$.title`, `$.forecastResults.projectedValue` non-null, `$.monthlyProjection.length()` = termMonths; (2) `GET /api/investments/999999` → 404, `status=404`, `error="Not Found"`, `message="Investment forecast not found"`

**Checkpoint**: Single-record read verified. US3 independently validated.

---

## Phase 6: User Story 4 — Update a Saved Forecast (Priority: P4)

**Goal**: `PUT /api/investments/{id}` replaces inputs, triggers full recalculation, updates the stored record, and returns `200 OK`. The original `id` is preserved. Invalid inputs return `400`, missing id returns `404`, ownership mismatch returns `400`.

**Independent Test**: Create forecast with `termMonths=12`. `PUT /api/investments/{id}` with `termMonths=24` → 200; `$.monthlyProjection.length()=24`; `$.id` unchanged.

- [x] T021 [US4] Add `update(Long id, InvestmentForecastRequest request)` to `InvestmentForecastService` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastService.java` — find existing entity by id, throw `InvestmentForecastNotFoundException("Investment forecast not found")` if absent; verify `request.getUserId().equals(existing.getUser().getId())`, throw `IllegalArgumentException("User does not own this forecast")` if not matching; call `calculator.calculate(...)` with new inputs; set all input fields and all 5 summary fields on the existing entity; clear `existing.getMonthlyProjection()` and repopulate from `result.entries()` (setting `forecast` reference on each new entry); save and return `toResponse(forecastRepository.save(existing))`

- [x] T022 [US4] Add `PUT /api/investments/{id}` endpoint to `InvestmentForecastController` in `src/main/java/com/psybergate/financialcalculator/controller/InvestmentForecastController.java` — `@PutMapping("/{id}") public ResponseEntity<InvestmentForecastResponse> update(@PathVariable Long id, @Valid @RequestBody InvestmentForecastRequest request)` delegating to `forecastService.update(id, request)`, returning `ResponseEntity.ok(...)`

- [x] T023 [US4] Add US4 test scenarios to `InvestmentForecastSpec` in `src/test/java/com/psybergate/financialcalculator/investment/InvestmentForecastSpec.java` — write: (1) create with `termMonths=12`, PUT with `termMonths=24` → 200, `$.monthlyProjection.length()=24`, same `id`; (2) PUT with invalid field (e.g. `termMonths=0`) → 400; (3) PUT to non-existent id 999999 → 404, `message="Investment forecast not found"`; (4) PUT with a `userId` belonging to a different registered user (register a second user, use their id in the PUT body) → 400, `message="User does not own this forecast"`

**Checkpoint**: Update with recalculation and ownership guard verified. US4 independently validated.

---

## Phase 7: User Story 5 — Delete a Saved Forecast (Priority: P5)

**Goal**: `DELETE /api/investments/{id}` returns `204 No Content` and permanently removes the record and all its monthly projection entries. Subsequent `GET /api/investments/{id}` returns `404`.

**Independent Test**: Create a forecast, `DELETE /api/investments/{id}` → 204. Then `GET /api/investments/{id}` → 404. `DELETE /api/investments/999999` → 404.

- [x] T024 [US5] Add `delete(Long id)` to `InvestmentForecastService` in `src/main/java/com/psybergate/financialcalculator/service/InvestmentForecastService.java` — verify existence with `forecastRepository.existsById(id)`, throw `InvestmentForecastNotFoundException("Investment forecast not found")` if absent; call `forecastRepository.deleteById(id)` (cascade removes all `MonthlyProjectionEntry` rows automatically)

- [x] T025 [US5] Add `DELETE /api/investments/{id}` endpoint to `InvestmentForecastController` in `src/main/java/com/psybergate/financialcalculator/controller/InvestmentForecastController.java` — `@DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id)` delegating to `forecastService.delete(id)`, returning `ResponseEntity.noContent().build()`

- [x] T026 [US5] Add US5 test scenarios to `InvestmentForecastSpec` in `src/test/java/com/psybergate/financialcalculator/investment/InvestmentForecastSpec.java` — write: (1) create forecast, `DELETE /api/investments/{id}` → 204; (2) after delete, `GET /api/investments/{id}` → 404; (3) `DELETE /api/investments/999999` → 404, `message="Investment forecast not found"`

**Checkpoint**: Full CRUD lifecycle complete. All 5 user stories verified.

---

## Phase 8: Polish & Cross-Cutting Concerns

- [x] T027 Run `./mvnw test` from the project root — confirm all tests (Features 1–5) pass with zero failures

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

T002, T003, T004, T005, T006, T007, T008 are all fully parallel (different files, no cross-dependencies).
T009 depends on T006 + T007 (references ForecastResultDto + MonthlyProjectionEntryDto).
T010 depends on T004 + T005 (references both entity types).
T011 depends on T007 (returns MonthlyProjectionEntryDto entries) — can start as soon as T007 is done.

### Within Each User Story Phase

Service method → Controller endpoint → Tests (service must exist before controller, both must exist before tests).

### Parallel Opportunities

```
Phase 2:
  T002 — InvestmentForecastNotFoundException        [P]
  T003 — GlobalExceptionHandler extension           [P]
  T004 — InvestmentForecast entity                  [P]
  T005 — MonthlyProjectionEntry entity              [P]
  T006 — ForecastResultDto                          [P]
  T007 — MonthlyProjectionEntryDto                  [P]
  T008 — InvestmentForecastRequest                  [P]
  → then T009 (needs T006 + T007) + T010 (needs T004 + T005) can run in parallel
  → then T011 (needs T007)
```

---

## Implementation Strategy

### MVP (US1 only)

1. Phase 1 (baseline) → Phase 2 (foundation) → Phase 3 (US1)
2. **STOP and VALIDATE**: POST a forecast, inspect the 201 response and monthly projection values against the PRD example

### Full Feature Delivery

Setup → Foundational → US1 → US2 → US3 → US4 → US5 → Polish (sequential: US2–US5 all extend the same service and controller files created in US1)

---

## Notes

- US2–US5 phases all extend the same `InvestmentForecastService.java` and `InvestmentForecastController.java` created in US1 — must run sequentially
- `InvestmentForecastSpec.@BeforeEach` must clear `InvestmentForecastRepository` before `UserRepository` (FK constraint: forecasts reference users)
- `@OrderBy("month ASC")` on `monthlyProjection` ensures consistent ordering without sorting in Java
- `orphanRemoval=true` on the `@OneToMany` means clearing the list in `update()` automatically deletes the old monthly projection rows — no manual delete needed
- The `toResponse()` helper in the service accesses `entity.getMonthlyProjection()` — Hibernate lazy-loads this within the same transaction; no `@Transactional` annotation should be needed since `save()` triggers the load within the same session
- Use `@Builder(toBuilder=true)` on entities only if needed; standard `@Builder` is sufficient for this feature
