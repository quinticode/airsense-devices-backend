package br.pucpr.authserver.environments

import br.pucpr.authserver.environments.requests.EnvironmentRequest
import br.pucpr.authserver.environments.responses.EnvironmentResponse
import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.security.UserToken
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/environments")
@SecurityRequirement(name = "jwt-auth")
class EnvironmentController(private val service: EnvironmentService) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: EnvironmentRequest,
        auth: Authentication
    ): ResponseEntity<EnvironmentResponse> =
        service.create(request.name, userId(auth))
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun list(
        @RequestParam(required = false) quality: AirQuality?,
        @RequestParam(defaultValue = "name") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String,
        auth: Authentication
    ): ResponseEntity<List<EnvironmentResponse>> =
        ResponseEntity.ok(service.findAll(userId(auth), quality, sortBy, direction))

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<EnvironmentResponse> =
        ResponseEntity.ok(service.findById(id, userId(auth)))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: EnvironmentRequest,
        auth: Authentication
    ): ResponseEntity<EnvironmentResponse> =
        ResponseEntity.ok(service.update(id, request.name, userId(auth)))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, auth: Authentication): ResponseEntity<Void> {
        service.delete(id, userId(auth))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{environmentId}/devices/{deviceId}")
    fun addDevice(
        @PathVariable environmentId: Long,
        @PathVariable deviceId: Long,
        auth: Authentication
    ): ResponseEntity<EnvironmentResponse> =
        ResponseEntity.ok(service.addDevice(environmentId, deviceId, userId(auth)))

    @DeleteMapping("/{environmentId}/devices/{deviceId}")
    fun removeDevice(
        @PathVariable environmentId: Long,
        @PathVariable deviceId: Long,
        auth: Authentication
    ): ResponseEntity<EnvironmentResponse> =
        ResponseEntity.ok(service.removeDevice(environmentId, deviceId, userId(auth)))

    private fun userId(auth: Authentication): Long =
        (auth.principal as? UserToken)?.id ?: throw ForbiddenException()
}
