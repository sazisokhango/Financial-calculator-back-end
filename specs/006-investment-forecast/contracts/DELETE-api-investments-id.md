# Contract: DELETE /api/investments/{id}

**Feature**: 006-investment-forecast
**Method**: DELETE
**Path**: `/api/investments/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required | Description                    |
|-----------|------|----------|--------------------------------|
| id        | Long | Yes      | ID of the forecast to delete   |

---

## Responses

### 204 No Content — Forecast deleted

No response body.

### 404 Not Found — Forecast does not exist

```json
{ "status": 404, "error": "Not Found", "message": "Investment forecast not found" }
```

---

## Scenarios

| Scenario                          | Status | Response              |
|-----------------------------------|--------|-----------------------|
| Forecast with given id exists     | 204    | Empty body            |
| No forecast with given id         | 404    | Standard error shape  |

---

## Notes

- Deletion is permanent (hard delete). The `investment_forecast_monthly_projections` rows are removed automatically via `cascade = CascadeType.ALL` on the `@OneToMany` relationship.
- After a successful DELETE, `GET /api/investments/{id}` returns `404 Not Found`.
