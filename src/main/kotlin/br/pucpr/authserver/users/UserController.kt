package br.pucpr.authserver.users

import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.requests.CreateUserRequest
import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UpdateUserRequest
import br.pucpr.authserver.users.responses.UserResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(val service: UserService) {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, String>> {
        val message = exception.bindingResult.fieldErrors.firstOrNull { it.field == "password" }?.defaultMessage
            ?: exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
            ?: "Invalid request"
        return ResponseEntity.badRequest().body(mapOf("message" to message))
    }

    @PostMapping
    fun insert(
        @Valid @RequestBody user: CreateUserRequest
    ) = service.insert(user.toUser())
        .let { UserResponse(it) }
        .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody login: LoginRequest
    ) = service.login(login.email!!, login.password!!)

    @SecurityRequirement(name = "jwt-auth")
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        auth: Authentication
    ): ResponseEntity<UserResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        if (token.id != id) {
            throw ForbiddenException("Access to another user's profile is not allowed")
        }
        return ResponseEntity.ok(UserResponse(service.findById(id)))
    }

    @SecurityRequirement(name = "jwt-auth")
    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody user: UpdateUserRequest,
        auth: Authentication
    ): ResponseEntity<UserResponse> {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        if (token.id != id) {
            throw ForbiddenException("Update is not allowed")
        }
        return service.update(id, user.name!!)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()
    }

    @SecurityRequirement(name = "jwt-auth")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        auth: Authentication
    ) {
        val token = auth.principal as? UserToken ?: throw ForbiddenException()
        if (token.id != id) {
            throw ForbiddenException("Delete is not allowed")
        }
        service.delete(id)
    }
}