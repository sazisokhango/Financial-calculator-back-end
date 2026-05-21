# Data Model: Investment Forecast

**Feature**: 006-investment-forecast
**Date**: 2026-05-21

---

## New Entities

### InvestmentForecast

JPA entity persisted to `investment_forecasts`.

| Field                | Java Type  | Column                    | Constraints                    |
|----------------------|------------|---------------------------|-------------------------------|
| id                   | Long       | `id` BIGSERIAL PK         | Auto-generated                |
| user                 | User       | `user_id` FK → users      | NOT NULL, lazy fetch           |
| title                | String     | `title` VARCHAR(255)      | NOT NULL                       |
| description          | String     | `description` TEXT        | nullable                       |
| initialAmount        | BigDecimal | `initial_amount` NUMERIC(15,2)       | NOT NULL |
| monthlyContribution  | BigDecimal | `monthly_contribution` NUMERIC(15,2) | NOT NULL |
| termMonths           | Integer    | `term_months` INT         | NOT NULL                       |
| annualInterestRate   | BigDecimal | `annual_interest_rate` NUMERIC(7,4)  | NOT NULL |
| projectedValue       | BigDecimal | `projected_value` NUMERIC(15,2)      | NOT NULL |
| totalContributions   | BigDecimal | `total_contributions` NUMERIC(15,2)  | NOT NULL |
| totalInterestEarned  | BigDecimal | `total_interest_earned` NUMERIC(15,2)| NOT NULL |
| roiPercentage        | BigDecimal | `roi_percentage` NUMERIC(10,4)       | NOT NULL |
| averageMonthlyGrowth | BigDecimal | `average_monthly_growth` NUMERIC(15,2)| NOT NULL |
| monthlyProjection    | List\<MonthlyProjectionEntry\> | (mapped by `forecast`) | `@OneToMany(cascade=ALL, orphanRemoval=true)` ordered by `month ASC` |

Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

### MonthlyProjectionEntry

JPA entity persisted to `investment_forecast_monthly_projections`.

| Field               | Java Type  | Column                       | Constraints           |
|---------------------|------------|------------------------------|----------------------|
| id                  | Long       | `id` BIGSERIAL PK            | Auto-generated        |
| forecast            | InvestmentForecast | `forecast_id` FK → investment_forecasts | NOT NULL, lazy fetch |
| month               | Integer    | `month` INT                  | NOT NULL              |
| startingBalance     | BigDecimal | `starting_balance` NUMERIC(15,2)    | NOT NULL       |
| monthlyContribution | BigDecimal | `monthly_contribution` NUMERIC(15,2)| NOT NULL       |
| interestEarned      | BigDecimal | `interest_earned` NUMERIC(15,2)     | NOT NULL       |
| endingBalance       | BigDecimal | `ending_balance` NUMERIC(15,2)      | NOT NULL       |

Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

---

## New DTOs

### InvestmentForecastRequest

| Field               | Type       | Validation                                         |
|---------------------|------------|----------------------------------------------------|
| userId              | Long       | `@NotNull`                                         |
| title               | String     | `@NotBlank`                                        |
| description         | String     | none (optional)                                    |
| initialAmount       | BigDecimal | `@NotNull` `@DecimalMin("0.00")`                   |
| monthlyContribution | BigDecimal | `@NotNull` `@DecimalMin("0.00")`                   |
| termMonths          | Integer    | `@NotNull` `@Min(1)`                               |
| annualInterestRate  | BigDecimal | `@NotNull` `@DecimalMin("0.00")` `@DecimalMax("100.00")` |

### InvestmentForecastResponse

Returned by all five endpoints (201, 200 on GET/PUT).

| Field               | Type                          |
|---------------------|-------------------------------|
| id                  | Long                          |
| userId              | Long                          |
| title               | String                        |
| description         | String                        |
| initialAmount       | BigDecimal                    |
| monthlyContribution | BigDecimal                    |
| termMonths          | Integer                       |
| annualInterestRate  | BigDecimal                    |
| forecastResults     | ForecastResultDto             |
| monthlyProjection   | List\<MonthlyProjectionEntryDto\> |

### ForecastResultDto

Nested inside `InvestmentForecastResponse.forecastResults`.

| Field               | Type       |
|---------------------|------------|
| projectedValue      | BigDecimal |
| totalContributions  | BigDecimal |
| totalInterestEarned | BigDecimal |
| roiPercentage       | BigDecimal |
| averageMonthlyGrowth| BigDecimal |

### MonthlyProjectionEntryDto

Each item in `InvestmentForecastResponse.monthlyProjection`.

| Field               | Type       |
|---------------------|------------|
| month               | Integer    |
| startingBalance     | BigDecimal |
| monthlyContribution | BigDecimal |
| interestEarned      | BigDecimal |
| endingBalance       | BigDecimal |

---

## New Exception

**`InvestmentForecastNotFoundException`** — thrown by `findById()`, `update()`, and `delete()` when no `InvestmentForecast` exists for the given id.

Registered in `GlobalExceptionHandler` → `404 Not Found`, message `"Investment forecast not found"`.

---

## New Repository

**`InvestmentForecastRepository`** — extends `JpaRepository<InvestmentForecast, Long>`.

Custom query method:

```java
List<InvestmentForecast> findByUser(User user);
```

---

## New Calculator Service

**`InvestmentForecastCalculator`** (`@Service`) — injectable calculation engine.

Input: `initialAmount`, `monthlyContribution`, `termMonths`, `annualInterestRate` (all `BigDecimal`/`Integer`).

Output: `ForecastCalculationResult` (record/value holder) containing:
- `projectedValue`, `totalContributions`, `totalInterestEarned`, `roiPercentage`, `averageMonthlyGrowth`
- `List<MonthlyProjectionEntryData>` (one per month — plain data holder, not entity)

The service layer maps `ForecastCalculationResult` → `InvestmentForecast` entity before persisting.

---

## Database Schema (auto-generated by Hibernate `ddl-auto=update`)

```sql
CREATE TABLE investment_forecasts (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL REFERENCES users(id),
    title                 VARCHAR(255) NOT NULL,
    description           TEXT,
    initial_amount        NUMERIC(15,2) NOT NULL,
    monthly_contribution  NUMERIC(15,2) NOT NULL,
    term_months           INT NOT NULL,
    annual_interest_rate  NUMERIC(7,4) NOT NULL,
    projected_value       NUMERIC(15,2) NOT NULL,
    total_contributions   NUMERIC(15,2) NOT NULL,
    total_interest_earned NUMERIC(15,2) NOT NULL,
    roi_percentage        NUMERIC(10,4) NOT NULL,
    average_monthly_growth NUMERIC(15,2) NOT NULL
);

CREATE TABLE investment_forecast_monthly_projections (
    id                   BIGSERIAL PRIMARY KEY,
    forecast_id          BIGINT NOT NULL REFERENCES investment_forecasts(id),
    month                INT NOT NULL,
    starting_balance     NUMERIC(15,2) NOT NULL,
    monthly_contribution NUMERIC(15,2) NOT NULL,
    interest_earned      NUMERIC(15,2) NOT NULL,
    ending_balance       NUMERIC(15,2) NOT NULL
);
```

Hibernate creates both tables automatically on startup. No migration script is required for MVP.

---

## Relationship to Existing Model

- `InvestmentForecast` → `User`: `@ManyToOne` (same pattern as `TaxCalculation` → `User`).
- No changes to `User`, `TaxCalculation`, `TaxCalculationRequest`, `TaxCalculationResponse`, or any existing repository.
- `GlobalExceptionHandler` receives one new `@ExceptionHandler` method — no existing handlers modified.
