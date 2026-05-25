## 1. Data Model

- [x] 1.1 Create `CarLoan` entity (`src/main/java/com/psybergate/financialcalculator/entity/CarLoan.java`) — fields: id (@Id @GeneratedValue), user (@ManyToOne User), title, description, purchasePrice, initialDeposit, onceOffFee, adminFee, balloonPayment, termMonths, interestRate; computed summary fields: financedAmount, monthlyRepayment, totalRepayments, totalInterestPaid, totalFeesPaid, remainingBalance, estimatedPayoffMonth, fullyPaid; monthlyProjection (@OneToMany CarLoanMonthlyProjection, CascadeType.ALL, orphanRemoval=true, ordered by monthNumber ASC)
- [x] 1.2 Create `CarLoanMonthlyProjection` entity (`src/main/java/com/psybergate/financialcalculator/entity/CarLoanMonthlyProjection.java`) — fields: id, carLoan (@ManyToOne), monthNumber, startingBalance, monthlyRepayment, interestCharged, adminFee, principalPaid, endingBalance; all monetary fields BigDecimal(15,2)

## 2. Repository

- [x] 2.1 Create `CarLoanRepository` interface (`src/main/java/com/psybergate/financialcalculator/repository/CarLoanRepository.java`) — extends `JpaRepository<CarLoan, Long>`; add `findAllByUserEmail(String userEmail)` for future user-scoped queries

## 3. DTOs

- [x] 3.1 Create `CarLoanRequest` DTO (`src/main/java/com/psybergate/financialcalculator/dto/CarLoanRequest.java`) — fields with validation: title (@NotBlank), description, purchasePrice (@NotNull @DecimalMin("0.01")), initialDeposit (@NotNull @DecimalMin("0.00")), onceOffFee (@NotNull @DecimalMin("0.00")), adminFee (@NotNull @DecimalMin("0.00")), balloonPayment (@NotNull @DecimalMin("0.00")), termMonths (@NotNull @Min(1)), interestRate (@NotNull @DecimalMin("0.00") @DecimalMax("100.00"))
- [x] 3.2 Create `CarLoanMonthlyProjectionDto` (`src/main/java/com/psybergate/financialcalculator/dto/CarLoanMonthlyProjectionDto.java`) — fields: month, startingBalance, monthlyRepayment, interestCharged, adminFee, principalPaid, endingBalance
- [x] 3.3 Create `CarLoanForecastResultDto` (`src/main/java/com/psybergate/financialcalculator/dto/CarLoanForecastResultDto.java`) — fields: financedAmount, monthlyRepayment, totalRepayments, totalInterestPaid, totalFeesPaid, balloonPayment, remainingBalance, estimatedPayoffMonth, fullyPaid
- [x] 3.4 Create `CarLoanResponse` DTO (`src/main/java/com/psybergate/financialcalculator/dto/CarLoanResponse.java`) — fields: id, title, description, purchasePrice, initialDeposit, onceOffFee, adminFee, balloonPayment, termMonths, interestRate, forecastResults (CarLoanForecastResultDto), monthlyProjection (List<CarLoanMonthlyProjectionDto>)

## 4. Calculator (Pure Math Logic)

- [x] 4.1 Create `CarLoanCalculator` service (`src/main/java/com/psybergate/financialcalculator/service/CarLoanCalculator.java`) — implement: (a) financed amount = purchasePrice − initialDeposit + onceOffFee; (b) annuity PMT with balloon via BigDecimal.pow() (no Math.pow); (c) zero-rate edge case: PMT = (P − BV) / n; (d) zero-financed-amount guard (return empty schedule); (e) "too low" guard (throw IllegalArgumentException if PMT ≤ month-1 interest); (f) per-month loop building CarLoanMonthlyProjectionDto list with balloon deducted in final month; (g) return internal record `CarLoanResult(financedAmount, monthlyRepayment, totalRepayments, totalInterestPaid, totalFeesPaid, remainingBalance, estimatedPayoffMonth, fullyPaid, List<CarLoanMonthlyProjectionDto>)`

## 5. Service

- [x] 5.1 Create `CarLoanService` (`src/main/java/com/psybergate/financialcalculator/service/CarLoanService.java`) — methods: `create(CarLoanRequest)`, `findAll()`, `findById(Long id)`, `update(Long id, CarLoanRequest)`, `delete(Long id)`; validate initialDeposit ≤ purchasePrice and balloonPayment ≤ financedAmount (throw ResponseStatusException 400); delegate math to `CarLoanCalculator`; catch IllegalArgumentException from calculator and re-throw as 400; map result to `CarLoan` entity (replace child collection on update); map entity to `CarLoanResponse`

## 6. Controller

- [x] 6.1 Create `CarLoanController` (`src/main/java/com/psybergate/financialcalculator/controller/CarLoanController.java`) — `@RestController @RequestMapping("/api/loans")`; endpoints: `POST /` → 201, `GET /` → 200 list, `GET /{id}` → 200, `PUT /{id}` → 200, `DELETE /{id}` → 204; annotate request body with `@Valid`

## 7. Error Handling

- [x] 7.1 Ensure global exception handler (or add `@RestControllerAdvice` if not present) returns JSON error body with `httpStatus`, `message`, and `timestamp` (ISO-8601) for `MethodArgumentNotValidException` (400), `ResponseStatusException` (pass-through), and `EntityNotFoundException` (404)

## 8. Unit Tests — Calculator

- [x] 8.1 Create `CarLoanCalculatorTest` (`src/test/java/com/psybergate/financialcalculator/service/CarLoanCalculatorTest.java`) — test cases: (a) standard loan with balloon (verify financedAmount, monthlyRepayment, month-1 projection values, final-month balloon deduction, remainingBalance=0); (b) zero interest rate (instalment = (P−BV)/n + adminFee, totalInterestPaid=0); (c) zero financed amount returns empty projection; (d) repayment too low throws IllegalArgumentException with expected message

## 9. Integration Tests — Controller

- [x] 9.1 Create `CarLoanControllerIT` (`src/test/java/com/psybergate/financialcalculator/controller/CarLoanControllerIT.java`) — `@SpringBootTest` tests: (a) POST valid request → 201 with forecastResults and monthlyProjection; (b) GET list → 200 array; (c) GET by id → 200 full detail; (d) GET unknown id → 404 with error body; (e) PUT updates loan and returns recalculated response; (f) DELETE → 204, subsequent GET → 404; (g) POST with initialDeposit > purchasePrice → 400; (h) POST with balloonPayment > financedAmount → 400; (i) POST with blank title → 400; (j) POST with repayment too low → 400 with descriptive message
