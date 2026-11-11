package br.pucpr.authserver.environments

import br.pucpr.authserver.devices.DeviceRepository
import br.pucpr.authserver.devices.DeviceStatus
import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.environments.responses.EnvironmentDeviceResponse
import br.pucpr.authserver.environments.responses.EnvironmentResponse
import br.pucpr.authserver.sensors.SensorSimulator
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class EnvironmentService(
    private val environmentRepository: EnvironmentRepository,
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
    private val simulator: SensorSimulator
) {
    @Transactional
    fun create(name: String, userId: Long): EnvironmentResponse {
        val owner = userRepository.findByIdOrNull(userId) ?: throw NotFoundException(userId)
        val environment = environmentRepository.save(Environment(name = name.trim(), owner = owner))
        log.info("Environment ${environment.id} created by user $userId")
        return toResponse(environment)
    }

    @Transactional(readOnly = true)
    fun findAll(
        userId: Long,
        quality: AirQuality?,
        sortBy: String,
        direction: String
    ): List<EnvironmentResponse> {
        val environments = environmentRepository.findAllByOwnerId(userId)
            .map(::toResponse)
            .let { responses ->
                if (quality == null) responses else responses.filter { it.airQuality == quality }
            }

        val comparator = when (sortBy.lowercase()) {
            "name" -> compareBy<EnvironmentResponse> { it.name.lowercase() }
            "quality" -> compareBy<EnvironmentResponse> { it.airQuality?.ordinal ?: -1 }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "sortBy must be name or quality")
        }

        return when (direction.lowercase()) {
            "asc" -> environments.sortedWith(comparator)
            "desc" -> environments.sortedWith(comparator.reversed())
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "direction must be asc or desc")
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long, userId: Long): EnvironmentResponse =
        toResponse(findOwnedEnvironment(id, userId))

    @Transactional
    fun update(id: Long, name: String, userId: Long): EnvironmentResponse {
        val environment = findOwnedEnvironment(id, userId)
        environment.name = name.trim()
        val saved = environmentRepository.save(environment)
        log.info("Environment $id updated by user $userId")
        return toResponse(saved)
    }

    @Transactional
    fun delete(id: Long, userId: Long) {
        val environment = findOwnedEnvironment(id, userId)
        environment.devices.forEach { it.environments.remove(environment) }
        environmentRepository.delete(environment)
        log.info("Environment $id deleted by user $userId")
    }

    @Transactional
    fun addDevice(environmentId: Long, deviceId: Long, userId: Long): EnvironmentResponse {
        val environment = findOwnedEnvironment(environmentId, userId)
        val device = deviceRepository.findByIdAndUserId(deviceId, userId)
            ?: throw NotFoundException(deviceId)
        if (environment.devices.add(device)) {
            device.environments.add(environment)
            log.info("Device $deviceId associated with environment $environmentId by user $userId")
        }
        return toResponse(environment)
    }

    @Transactional
    fun removeDevice(environmentId: Long, deviceId: Long, userId: Long): EnvironmentResponse {
        val environment = findOwnedEnvironment(environmentId, userId)
        val device = deviceRepository.findByIdAndUserId(deviceId, userId)
            ?: throw NotFoundException(deviceId)
        if (environment.devices.remove(device)) {
            device.environments.remove(environment)
            log.info("Device $deviceId removed from environment $environmentId by user $userId")
        } else {
            throw NotFoundException("Device $deviceId is not associated with environment $environmentId")
        }
        return toResponse(environment)
    }

    private fun findOwnedEnvironment(id: Long, userId: Long): Environment =
        environmentRepository.findByIdAndOwnerId(id, userId) ?: throw NotFoundException(id)

    private fun toResponse(environment: Environment): EnvironmentResponse {
        val activeDevices = environment.devices.filter { it.status == DeviceStatus.ACTIVE }
        val levelsByDevice = activeDevices.map { device ->
            device.id?.let(simulator::findLatestReadingsByDeviceId)
                .orEmpty()
                .map { it.level }
        }
        val quality = if (levelsByDevice.isEmpty() || levelsByDevice.any { it.isEmpty() }) {
            null
        } else {
            levelsByDevice.flatten()
                .maxByOrNull { it.ordinal }
                ?.let { AirQuality.valueOf(it.name) }
        }

        return EnvironmentResponse(
            id = environment.id!!,
            name = environment.name,
            airQuality = quality,
            devices = environment.devices
                .sortedBy { it.id }
                .map(::EnvironmentDeviceResponse)
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(EnvironmentService::class.java)
    }
}
