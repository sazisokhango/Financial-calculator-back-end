# Contract: DELETE /api/tax/{id}

**Feature**: 004-saved-calculations
**Method**: DELETE
**Path**: `/api/tax/{id}`

---

## Request

### Path Parameters

| Parameter | Type | Required |
|-----------|------|----------|
| id        | Long | Yes      |

No request body.

---

## Responses

### 204 No Content — Deleted successfully

No response body.

### 404 Not Found — Calculation does not exist

```json
{ "status": 404, "error": "Not Found", "message": "Calculation not found" }
```

---

## Scenarios

| Scenario              | Status | Response body         |
|-----------------------|--------|-----------------------|
| Valid id, exists      | 204    | Empty                 |
| Valid id, not found   | 404    | Standard error shape  |
