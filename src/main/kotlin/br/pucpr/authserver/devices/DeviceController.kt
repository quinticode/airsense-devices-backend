package br.pucpr.authserver.devices

import br.pucpr.authserver.devices.requests.CreateDeviceRequest
import br.pucpr.authserver.devices.requests.UpdateDeviceRequest
import br.pucpr.authserver.devices.responses.DeviceResponse
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/devices")
@SecurityRequirement(name = "jwt-auth") // para testar no swagger
class DeviceController(private val service: DeviceService) {
    @GetMapping
    fun list(auth: Authentication): ResponseEntity<List<DeviceResponse>> =
        service.findAll(userId(auth))
            .map(::DeviceResponse)
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun insert(
        @Valid @RequestBody request: CreateDeviceRequest,
        auth: Authentication
    ): ResponseEntity<DeviceResponse> =
        service.insert(request.toDevice(), userId(auth))
            .let(::DeviceResponse)
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long, auth: Authentication): ResponseEntity<DeviceResponse> =
        service.findById(id, userId(auth))
            .let(::DeviceResponse)
            .let { ResponseEntity.ok(it) }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDeviceRequest,
        auth: Authentication
    ): ResponseEntity<DeviceResponse> =
        service.update(id, userId(auth), request)
            .let(::DeviceResponse)
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, auth: Authentication): ResponseEntity<Void> {
        service.delete(id, userId(auth))
        return ResponseEntity.noContent().build()
    }

    private fun userId(auth: Authentication): Long =
        (auth.principal as? UserToken)?.id ?: throw ForbiddenException()
}
