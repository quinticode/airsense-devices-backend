package br.pucpr.authserver.devices.responses

import br.pucpr.authserver.devices.Device
import br.pucpr.authserver.devices.DeviceStatus

data class DeviceResponse(
    val id: Long,
    val nickname: String,
    val status: DeviceStatus,
    val userId: Long
) {
    constructor(device: Device) : this(device.id!!, device.nickname, device.status, device.user.id!!)
}
