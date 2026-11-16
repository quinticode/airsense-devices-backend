package br.pucpr.authserver.sensors

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/sensor-thresholds")
@Validated
@SecurityRequirement(name = "jwt-auth")
class SensorTresholdController(
    val service: SensorTresholdService,
    private val simulator: SensorSimulator
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
        @RequestParam(required = false) value: Double?
    ): ResponseEntity<Map<String, Any>> {
        val evaluatedValue = value ?: simulator.findReadingsByName(name).lastOrNull()?.value
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No simulated reading available for sensor $name"
            )
        val level = service.evaluate(name, evaluatedValue)
        val response = mapOf(
            "name" to name.uppercase(),
            "value" to evaluatedValue,
            "level" to level
        )
        return ResponseEntity.ok(response)
    }
}