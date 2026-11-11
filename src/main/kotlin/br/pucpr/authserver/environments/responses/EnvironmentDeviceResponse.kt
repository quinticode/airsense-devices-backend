package br.pucpr.authserver.environments.responses

import br.pucpr.authserver.devices.Device
import br.pucpr.authserver.devices.DeviceStatus

data class EnvironmentDeviceResponse(
    val id: Long,
    val nickname: String,
    val status: DeviceStatus
) {
    constructor(device: Device) : this(device.id!!, device.nickname, device.status)
}
