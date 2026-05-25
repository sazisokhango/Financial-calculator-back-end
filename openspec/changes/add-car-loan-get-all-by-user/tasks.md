## 1. Repository

- [x] 1.1 Add `findAllByUser_Id(Long userId)` derived query to `CarLoanRepository` (`src/main/java/com/psybergate/financialcalculator/repository/CarLoanRepository.java`)

## 2. Service

- [x] 2.1 Replace `findAll()` with `findAllByUser(Long userId)` in `CarLoanService` (`src/main/java/com/psybergate/financialcalculator/service/CarLoanService.java`) — inject `UserRepository`, look up user by id (throw `UserNotFoundException` if not found), delegate to `carLoanRepository.findAllByUser_Id(userId)`, map results to `CarLoanResponse`

## 3. Controller

- [x] 3.1 Update `GET /api/loans` in `CarLoanController` (`src/main/java/com/psybergate/financialcalculator/controller/CarLoanController.java`) — change `findAll()` to accept `@RequestParam Long userId` and call `carLoanService.findAllByUser(userId)`

## 4. Integration Tests

- [x] 4.1 Update `CarLoanControllerIT` (`src/test/java/com/psybergate/financialcalculator/carloan/CarLoanControllerIT.java`) — register a test user in `setUp()`, supply `?userId=` on all GET-list calls; add tests: (a) list returns only that user's loans, (b) unknown userId → 404, (c) missing userId param → 400, (d) user with no loans → 200 empty array, (e) two users each have loans — list for user A returns only user A's loans
