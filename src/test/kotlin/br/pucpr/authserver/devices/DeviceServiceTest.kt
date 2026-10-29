package br.pucpr.authserver.devices

import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.devices.requests.UpdateDeviceRequest
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeviceServiceTest {
    private val repository = mockk<DeviceRepository>()
    private val userRepository = mockk<UserRepository>()
    private val service = DeviceService(repository, userRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository, userRepository)
    }

    @Test
    fun `findAll should return all devices`() {
        val devices = listOf(device(1L), device(2L))
        every { repository.findAllByUserId(10L) } returns devices

        service.findAll(10L) shouldBe devices
    }

    @Test
    fun `findById should return device`() {
        val device = device(1L)
        every { repository.findByIdAndUserId(1L, 10L) } returns device

        service.findById(1L, 10L) shouldBe device
    }

    @Test
    fun `findById should throw when device does not exist`() {
        every { repository.findByIdAndUserId(1L, 10L) } returns null

        shouldThrow<NotFoundException> { service.findById(1L, 10L) }
    }

    @Test
    fun `insert should save device`() {
        val device = device()
        val owner = user()
        every { userRepository.findById(10L) } returns java.util.Optional.of(owner)
        every { repository.save(device) } returns device

        service.insert(device, 10L) shouldBe device
        device.user shouldBe owner
    }

    @Test
    fun `update should replace nickname and status`() {
        val device = device(1L)
        val request = UpdateDeviceRequest("Bedroom", DeviceStatus.INACTIVE)
        every { repository.findByIdAndUserId(1L, 10L) } returns device
        every { repository.save(device) } returns device

        val updated = service.update(1L, 10L, request)

        updated.nickname shouldBe "Bedroom"
        updated.status shouldBe DeviceStatus.INACTIVE
        verify(exactly = 1) { repository.save(device) }
    }

    @Test
    fun `delete should remove the requested device`() {
        val device = device(1L)
        every { repository.findByIdAndUserId(1L, 10L) } returns device
        every { repository.delete(device) } returns Unit

        service.delete(1L, 10L)

        verify(exactly = 1) { repository.delete(device) }
    }

    private fun device(id: Long? = null) = Device(
        id = id,
        nickname = "Living room",
        status = DeviceStatus.ACTIVE
    ).apply { user = user() }

    private fun user() = User(id = 10L, email = "owner@example.com", password = "password")
}
