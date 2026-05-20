# Data Model: User Registration

**Feature**: 001-user-registration
**Date**: 2026-05-20

---

## Entity: User

Represents a registered identity in the system. The single entity introduced by this feature.

| Field       | Type    | Constraints                              | Notes                         |
|-------------|---------|------------------------------------------|-------------------------------|
| id          | Long    | Primary key, auto-generated              | Not supplied in request       |
| firstName   | String  | NOT NULL, NOT BLANK                      | Max 100 chars                 |
| lastName    | String  | NOT NULL, NOT BLANK                      | Max 100 chars                 |
| email       | String  | NOT NULL, UNIQUE, valid email format     | Stored lowercase              |

**Table name**: `users`

**Unique constraint**: `email` column — enforced at both application and database level.

---

## DTOs

### RegisterRequest (inbound)

| Field      | Type   | Validation            |
|------------|--------|-----------------------|
| firstName  | String | `@NotBlank`           |
| lastName   | String | `@NotBlank`           |
| email      | String | `@NotBlank`, `@Email` |

### UserResponse (outbound)

| Field      | Type   | Notes                      |
|------------|--------|----------------------------|
| id         | Long   | System-generated           |
| firstName  | String |                            |
| lastName   | String |                            |
| email      | String | Lowercased                 |

---

## Relationships

The `User` entity has no relationships in this feature.
Future features (Tax Calculations) will associate `TaxCalculation → User` (many-to-one).

---

## Database Schema

```sql
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE
);
```
