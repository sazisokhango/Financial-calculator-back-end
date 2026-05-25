## MODIFIED Requirements

### Requirement: Persist and retrieve car loan calculations
The system SHALL save each submitted car loan calculation and return all persisted fields. Users SHALL be able to retrieve a list of their own saved loans by providing their `userId`, and retrieve a single loan by id including the full monthly projection. The list endpoint SHALL require a `userId` query parameter of type Long and SHALL return only loans belonging to that user. If the `userId` does not correspond to an existing user the system SHALL return 404 Not Found.

#### Scenario: Create returns 201 with full response
- **WHEN** POST /api/loans is called with valid inputs
- **THEN** the system SHALL respond with HTTP 201 Created and a body containing id, all input fields, forecastResults, and the complete monthlyProjection array

#### Scenario: List loans for a user
- **WHEN** GET /api/loans?userId={userId} is called with a valid userId
- **THEN** the system SHALL return HTTP 200 with a JSON array containing only the loans belonging to that user

#### Scenario: List loans — userId missing
- **WHEN** GET /api/loans is called without a userId parameter
- **THEN** the system SHALL return HTTP 400 Bad Request

#### Scenario: List loans — unknown userId
- **WHEN** GET /api/loans?userId={userId} is called with a userId that does not exist
- **THEN** the system SHALL return HTTP 404 Not Found with an error body containing message and timestamp

#### Scenario: List loans — user has no loans
- **WHEN** GET /api/loans?userId={userId} is called for a user with no saved loans
- **THEN** the system SHALL return HTTP 200 with an empty JSON array

#### Scenario: List loans — isolation between users
- **WHEN** user A and user B each have saved loans and GET /api/loans?userId={userAId} is called
- **THEN** only user A's loans SHALL be returned

#### Scenario: Get by id
- **WHEN** GET /api/loans/{id} is called with an existing id
- **THEN** the system SHALL return HTTP 200 with the full loan detail including all projection entries

#### Scenario: Get by id not found
- **WHEN** GET /api/loans/{id} is called with a non-existent id
- **THEN** the system SHALL return HTTP 404 Not Found

#### Scenario: Update loan
- **WHEN** PUT /api/loans/{id} is called with updated inputs
- **THEN** the system SHALL recalculate and return HTTP 200 with the updated loan and fresh projection

#### Scenario: Delete loan
- **WHEN** DELETE /api/loans/{id} is called
- **THEN** the system SHALL return HTTP 204 No Content and the loan SHALL no longer be retrievable
