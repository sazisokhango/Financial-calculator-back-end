# Financial Calculator Back-End

Spring Boot REST API for the Financial Calculator application.

## Tech Stack

- Java 17
- Spring Boot 3.5.0
- PostgreSQL
- Maven
- Lombok
- H2 (test)

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL running locally

## Database Setup

Create the database and user:

```sql
CREATE USER tax_calc WITH PASSWORD 'password';
CREATE DATABASE financial_calculator OWNER tax_calc;
GRANT ALL PRIVILEGES ON DATABASE financial_calculator TO tax_calc;
```

## Running the Application

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8080`.

## Running Tests

```bash
./mvnw test
```

Tests use H2 in-memory database — no PostgreSQL required.

## Project Structure

```
src/main/java/com/psybergate/financialcalculator/
├── config/         # Spring configuration (Security, CORS, etc.)
├── controller/     # REST controllers (HTTP boundary only)
├── dto/            # Request/response DTOs
├── entity/         # JPA entities
├── exception/      # Exception handlers and custom exceptions
├── repository/     # Spring Data JPA repositories
└── service/        # Business logic
```

## Development Workflow

This project uses **Spec-Driven Development (SDD)** via SpecKit.

SDD cycle per feature:
1. `/speckit-specify` — write the feature spec
2. `/speckit-plan` — generate the implementation plan
3. `/speckit-tasks` — generate actionable tasks
4. `/speckit-implement` — execute the implementation
