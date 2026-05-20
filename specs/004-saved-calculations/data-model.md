# Data Model: Saved Calculations CRUD

**Feature**: 004-saved-calculations
**Date**: 2026-05-20

---

## Entities

No new entities. `TaxCalculation` from Feature 3 is used as-is.

---

## DTOs

No new DTOs. `TaxCalculationRequest` and `TaxCalculationResponse` from Feature 3 are reused.

**PUT behaviour note**: `TaxCalculationRequest.userId` is validated (user must exist) but
does not change the calculation's owner — ownership is fixed at creation.

---

## New Exception

**TaxCalculationNotFoundException** — thrown by `findById()`, `update()`, and `delete()`
when no `TaxCalculation` exists for the given id.
Mapped by `GlobalExceptionHandler` → `404 Not Found`, message `"Calculation not found"`.

---

## Repository Addition

`TaxCalculationRepository` gains one derived query method:

```java
List<TaxCalculation> findByUser(User user);
```

---

## No Schema Changes

All data already persists in `tax_calculations` (created in Feature 3).
This feature only adds read, update, and delete paths — no new columns or tables.
