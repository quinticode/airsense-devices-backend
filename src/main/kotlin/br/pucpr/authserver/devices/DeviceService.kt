package br.pucpr.authserver.devices

import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.devices.requests.UpdateDeviceRequest
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val repository: DeviceRepository,
    private val userRepository: UserRepository
) {
    fun findAll(userId: Long): List<Device> = repository.findAllByUserId(userId)

    fun findById(id: Long, userId: Long): Device =
        repository.findByIdAndUserId(id, userId) ?: throw NotFoundException(id)

    fun insert(device: Device, userId: Long): Device {
        val owner = userRepository.findByIdOrNull(userId) ?: throw NotFoundException(userId)
        device.user = owner
        owner.devices.add(device)
        return repository.save(device).also { log.info("Device ${it.id} created by user $userId") }
    }

    fun update(id: Long, userId: Long, request: UpdateDeviceRequest): Device {
        val device = findById(id, userId)
        device.nickname = request.nickname
        device.status = request.status
        return repository.save(device).also { log.info("Device $id updated by user $userId") }
    }

    @Transactional
    fun delete(id: Long, userId: Long) {
        val device = findById(id, userId)
        device.user.devices.remove(device)
        device.environments.toList().forEach { environment ->
            environment.devices.remove(device)
        }
        repository.delete(device)
        log.info("Device $id deleted by user $userId")
    }

    companion object {
        private val log = LoggerFactory.getLogger(DeviceService::class.java)
    }
}
