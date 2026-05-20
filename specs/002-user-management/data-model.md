# Data Model: User Management

**Feature**: 002-user-management
**Date**: 2026-05-20

---

## Entities

No new entities are introduced by this feature.

The `User` entity created in Feature 1 (`entity/User.java`) is reused as-is:

| Field     | Type   | Constraints                          |
|-----------|--------|--------------------------------------|
| id        | Long   | Primary key, auto-generated          |
| firstName | String | NOT NULL                             |
| lastName  | String | NOT NULL                             |
| email     | String | NOT NULL, UNIQUE, stored lowercase   |

**Table**: `users`

---

## DTOs

No new DTOs are introduced. The `UserResponse` DTO from Feature 1 is reused for both endpoints:

| Field     | Type   |
|-----------|--------|
| id        | Long   |
| firstName | String |
| lastName  | String |
| email     | String |

---

## New Exception

**UserNotFoundException** — thrown by `UserService.findById()` when no user exists for the
requested id. Handled by `GlobalExceptionHandler` → `404 Not Found`.

---

## No Schema Changes

This feature is read-only. No new tables, columns, or migrations are required.
