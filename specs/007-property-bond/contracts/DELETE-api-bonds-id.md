# DELETE /api/bonds/{id}

**Feature**: 007-property-bond

## Summary

Permanently delete a property bond plan by its unique ID.

## Request

**Method**: `DELETE`
**Path**: `/api/bonds/{id}`

### Path Parameters

| Parameter | Type | Required | Description        |
|-----------|------|----------|--------------------|
| id        | Long | Yes      | Bond record ID     |

**Example**: `DELETE /api/bonds/1`

## Responses

### 204 No Content

Bond deleted successfully. No response body.

### 404 Not Found

Bond with the given ID does not exist.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Property bond not found"
}
```
