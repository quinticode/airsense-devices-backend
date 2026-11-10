# AirSense Backend

Spring Boot API for managing air-quality sensor devices. It also provides a
configurable in-memory sensor simulator for testing dashboard integrations.

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

## Simulated sensor readings

The simulator generates one reading per configured sensor threshold at a regular
interval. Each value stays between that sensor's low and high thresholds and
changes gradually between readings. Readings are held in a bounded in-memory
history and are lost when the server restarts. Configure `airsense.sensor-simulator`
in `application.yaml` (or with matching environment variables):

| Setting | Default | Purpose |
|---|---:|---|
| `enabled` | `true` | Enable or disable generation |
| `interval` | `5000` | Delay between generations, in milliseconds |
| `variation-percent` | `0.02` | Maximum change per reading as a fraction of the threshold range |
| `history-size` | `1000` | Maximum number of readings retained |

Poll `GET /api/sensor-readings` for all retained readings or
`GET /api/sensor-readings/{name}` for readings from one sensor. Configure sensor
thresholds using the existing `/api/sensor-thresholds` endpoints before expecting
readings.
