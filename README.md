# AirSense Backend

Spring Boot API for managing air-quality sensor devices. The API currently
supports device CRUD; sensor readings and app integration are not implemented.

## Device API

The server uses the `/api` context path. Device endpoints are:

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/devices` | List devices |
| `GET` | `/api/devices/{id}` | Get a device |
| `POST` | `/api/devices` | Create a device |
| `PUT` | `/api/devices/{id}` | Replace a device's nickname and status |
| `DELETE` | `/api/devices/{id}` | Delete a device |

All device endpoints require a JWT obtained from `/api/users/login`. Each device
belongs to the authenticated user who created it. Listing and accessing devices
only returns devices owned by that user; using another user's device ID returns
`404 Not Found`. The owner is taken from the JWT, not from the request body.

Example create/update body:

```json
{
  "nickname": "Living room",
  "status": "ACTIVE"
}
```

`status` is `ACTIVE` or `INACTIVE`. When omitted during creation, it defaults
to `ACTIVE`. A successful create returns `201 Created`, reads and updates return
`200 OK`, and deletion returns `204 No Content`. Requests for unknown IDs return
`404 Not Found`.

Swagger UI is available at
`http://localhost:8080/api/swagger-ui/index.html` while the server is running.
