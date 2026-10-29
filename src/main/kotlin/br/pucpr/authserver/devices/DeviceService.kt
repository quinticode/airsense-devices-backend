package br.pucpr.authserver.devices

import br.pucpr.authserver.exception.NotFoundException
import br.pucpr.authserver.devices.requests.UpdateDeviceRequest
import br.pucpr.authserver.users.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val repository: DeviceRepository,
    private val userRepository: UserRepository
) {
    fun findAll(userId: Long): List<Device> = repository.findAllByUserId(userId)

    fun findById(id: Long, userId: Long): Device =
        repository.findByIdAndUserId(id, userId) ?: throw NotFoundException(id)

    fun insert(device: Device, userId: Long): Device {
        device.user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException(userId)
        return repository.save(device)
    }

    fun update(id: Long, userId: Long, request: UpdateDeviceRequest): Device {
        val device = findById(id, userId)
        device.nickname = request.nickname
        device.status = request.status
        return repository.save(device)
    }

    fun delete(id: Long, userId: Long) {
        repository.delete(findById(id, userId))
    }
}
