package br.pucpr.authserver.devices.requests

import br.pucpr.authserver.devices.Device
import br.pucpr.authserver.devices.DeviceStatus
import jakarta.validation.constraints.NotBlank

data class CreateDeviceRequest(
    @field:NotBlank
    val nickname: String,
    val status: DeviceStatus = DeviceStatus.ACTIVE
) {
    fun toDevice() = Device(nickname = nickname, status = status)
}
