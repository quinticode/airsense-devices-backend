package br.pucpr.authserver.environments

import br.pucpr.authserver.devices.Device
import br.pucpr.authserver.devices.DeviceRepository
import br.pucpr.authserver.devices.DeviceStatus
import br.pucpr.authserver.sensors.AlertLevel
import br.pucpr.authserver.sensors.SensorReading
import br.pucpr.authserver.sensors.SensorSimulator
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class EnvironmentServiceTest {
    private val environmentRepository = mockk<EnvironmentRepository>()
    private val deviceRepository = mockk<DeviceRepository>()
    private val userRepository = mockk<UserRepository>()
    private val simulator = mockk<SensorSimulator>()
    private val service = EnvironmentService(
        environmentRepository,
        deviceRepository,
        userRepository,
        simulator
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(environmentRepository, deviceRepository, userRepository, simulator)
    }

    @Test
    fun `findAll should filter by worst device quality and sort by name`() {
        val environments = listOf(
            environment(1L, "Zeta", device(11L)),
            environment(2L, "Alpha", device(12L)),
            environment(3L, "Medium", device(13L))
        )
        every { environmentRepository.findAllByOwnerId(7L) } returns environments
        every { simulator.findLatestReadingsByDeviceId(11L) } returns listOf(reading(AlertLevel.HIGH))
        every { simulator.findLatestReadingsByDeviceId(12L) } returns listOf(reading(AlertLevel.HIGH))
        every { simulator.findLatestReadingsByDeviceId(13L) } returns listOf(reading(AlertLevel.MEDIUM))

        val result = service.findAll(7L, AirQuality.HIGH, "name", "desc")

        result.map { it.name } shouldContainExactly listOf("Zeta", "Alpha")
        result.map { it.airQuality } shouldContainExactly listOf(AirQuality.HIGH, AirQuality.HIGH)
    }

    private fun environment(id: Long, name: String, device: Device) =
        Environment(id = id, name = name, owner = owner()).apply {
            devices.add(device)
            device.environments.add(this)
        }

    private fun device(id: Long) = Device(
        id = id,
        nickname = "Device $id",
        status = DeviceStatus.ACTIVE
    ).apply {
        user = owner()
    }

    private fun owner() = User(id = 7L, email = "owner@example.com", password = "password")

    private fun reading(level: AlertLevel) = SensorReading(
        name = "CO2",
        unit = "ppm",
        value = 1000.0,
        level = level,
        timestamp = Instant.parse("2026-11-10T12:00:00Z"),
        deviceId = 11L
    )
}
