package br.pucpr.authserver.devices.requests

import br.pucpr.authserver.devices.DeviceStatus
import jakarta.validation.constraints.NotBlank

data class UpdateDeviceRequest(
    @field:NotBlank
    val nickname: String,
    val status: DeviceStatus
)
