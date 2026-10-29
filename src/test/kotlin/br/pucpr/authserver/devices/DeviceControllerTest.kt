package br.pucpr.authserver.devices

import br.pucpr.authserver.devices.requests.CreateDeviceRequest
import br.pucpr.authserver.devices.requests.UpdateDeviceRequest
import br.pucpr.authserver.devices.responses.DeviceResponse
import br.pucpr.authserver.security.UserToken
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class DeviceControllerTest {
    private val service = mockk<DeviceService>()
    private val controller = DeviceController(service)
    private val auth = UsernamePasswordAuthenticationToken(UserToken(10L, "Owner"), null, emptyList())

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should return device responses`() {
        val device = device(1L)
        every { service.findAll(10L) } returns listOf(device)

        val response = controller.list(auth)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(DeviceResponse(device))
    }

    @Test
    fun `insert should return created device`() {
        val request = CreateDeviceRequest("Living room", DeviceStatus.ACTIVE)
        val device = device(1L)
        every {
            service.insert(match { it.nickname == request.nickname && it.status == request.status }, 10L)
        } returns device

        val response = controller.insert(request, auth)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe DeviceResponse(device)
    }

    @Test
    fun `getById should return requested device`() {
        val device = device(1L)
        every { service.findById(1L, 10L) } returns device

        val response = controller.getById(1L, auth)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe DeviceResponse(device)
    }

    @Test
    fun `update should return updated device`() {
        val request = UpdateDeviceRequest("Bedroom", DeviceStatus.INACTIVE)
        val device = device(1L).apply {
            nickname = request.nickname
            status = request.status
        }
        every { service.update(1L, 10L, request) } returns device

        val response = controller.update(1L, request, auth)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe DeviceResponse(device)
    }

    @Test
    fun `delete should return no content`() {
        every { service.delete(1L, 10L) } returns Unit

        val response = controller.delete(1L, auth)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        verify(exactly = 1) { service.delete(1L, 10L) }
    }

    private fun device(id: Long) = Device(
        id = id,
        nickname = "Living room",
        status = DeviceStatus.ACTIVE
    ).apply {
        user = br.pucpr.authserver.users.User(id = 10L, email = "owner@example.com", password = "password")
    }
}
