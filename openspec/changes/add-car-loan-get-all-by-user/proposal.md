## Why

The current `GET /api/loans` endpoint returns every loan in the system regardless of who created it. Users need to retrieve only their own saved loan calculations, scoped by their `userId`.

## What Changes

- **BREAKING** `GET /api/loans` now requires `@RequestParam Long userId` — callers that omit the parameter will receive 400 Bad Request
- `CarLoanRepository` gains `findAllByUser_Id(Long userId)` derived query
- `CarLoanService.findAll()` replaced by `findAllByUser(Long userId)` — looks up the user, returns 404 if not found, returns their loans
- `CarLoanController.findAll()` updated to accept `@RequestParam Long userId` and delegate to the new service method
- Integration test updated to supply `userId` when calling the list endpoint

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `car-loan-calculator`: "List all loans" requirement changes — `GET /api/loans` now filters by `userId` query parameter instead of returning all loans globally; returns 404 if the user does not exist

## Impact

- **Modified code**: `CarLoanRepository`, `CarLoanService`, `CarLoanController`
- **API (breaking)**: `GET /api/loans` requires `?userId=<Long>` — any client calling without it will get 400
- **Tests**: `CarLoanControllerIT` GET-list tests must supply `userId`; add test for unknown userId → 404
- **No new entities or DTOs**
