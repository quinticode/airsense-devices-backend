package br.pucpr.authserver.users

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exception.ForbiddenException
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.requests.CreateUserRequest
import br.pucpr.authserver.users.responses.UserResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.FieldError
import org.springframework.core.MethodParameter
import org.springframework.web.bind.MethodArgumentNotValidException

class UserControllerTest {

    private val service = mockk<UserService>()
    private val controller = UserController(service)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `insert should create and return user response with status 201`() {
        val request = CreateUserRequest(
            email = "user@email.com",
            password = "Password123!",
            name = "User"
        )
        val createdUser = oneUser(
            id = 1L,
            email = "user@email.com",
            password = "Password123!",
            name = "User"
        )

        every {
            service.insert(match {
                it.email == request.email &&
                        it.password == request.password &&
                        it.name == request.name
            })
        } returns createdUser

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe UserResponse(createdUser)
    }

    @Test
    fun `insert should propagate exception when service throws BadRequestException`() {
        val request = CreateUserRequest(
            email = "user@email.com",
            password = "Password123!",
            name = "User"
        )

        every {
            service.insert(match {
                it.email == request.email &&
                        it.password == request.password &&
                        it.name == request.name
            })
        } throws BadRequestException("User already exists")

        val exception = shouldThrow<BadRequestException> {
            controller.insert(request)
        }

        exception.message shouldBe "User already exists"
    }

    @Test
    fun `weak password validation should return a clear bad request message`() {
        val request = CreateUserRequest(
            email = "user@email.com",
            password = "weak",
            name = "User"
        )
        val violations = Validation.buildDefaultValidatorFactory().use { factory ->
            factory.validator.validate(request)
        }
        val passwordViolation = violations.single { it.propertyPath.toString() == "password" }

        passwordViolation.message shouldBe
                "Password is too weak. It must be at least 8 characters and include a letter, a number, and a special character."

        val bindingResult = BeanPropertyBindingResult(request, "request")
        bindingResult.addError(
            FieldError("request", "password", "weak", false, null, null, passwordViolation.message)
        )
        val insertMethod = UserController::class.java.getDeclaredMethod("insert", CreateUserRequest::class.java)
        val exception = MethodArgumentNotValidException(MethodParameter(insertMethod, 0), bindingResult)

        val response = controller.handleValidationException(exception)

        response.statusCode shouldBe HttpStatus.BAD_REQUEST
        response.body shouldBe mapOf("message" to passwordViolation.message)
    }

    @Test
    fun `getById should return the authenticated user's response`() {
        val auth = mockk<Authentication>()
        val user = oneUser(id = 1L)
        every { auth.principal } returns UserToken(id = 1L, name = user.name)
        every { service.findById(1L) } returns user

        val response = controller.getById(1L, auth)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe UserResponse(user)
    }

    @Test
    fun `getById should reject access to another user's profile`() {
        val auth = mockk<Authentication>()
        every { auth.principal } returns UserToken(id = 1L, name = "User")

        val exception = shouldThrow<ForbiddenException> {
            controller.getById(2L, auth)
        }

        exception.message shouldBe "Access to another user's profile is not allowed"
    }
}