package br.pucpr.authserver.sensors

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/sensor-thresholds")
@Validated
class SensorTresholdController(
    val service: SensorTresholdService
) {
    @GetMapping
    fun list(): ResponseEntity<List<SensorTresholdResponse>> {
        return service.findAll()
            .map { SensorTresholdResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun insert(
        @Valid @RequestBody request: SensorTresholdRequest
    ): ResponseEntity<SensorTresholdResponse> {
        return service.insert(request.toSensorTreshold())
            .let { SensorTresholdResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
    }

    @GetMapping("/{name}")
    fun getByName(@PathVariable name: String): ResponseEntity<SensorTresholdResponse> {
        return service.findByName(name)
            .let { SensorTresholdResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{name}/evaluate")
    fun evaluate(
        @PathVariable name: String,
        @RequestParam value: Double
    ): ResponseEntity<Map<String, Any>> {
        val level = service.evaluate(name, value)
        val response = mapOf(
            "name" to name.uppercase(),
            "value" to value,
            "level" to level
        )
        return ResponseEntity.ok(response)
    }
}