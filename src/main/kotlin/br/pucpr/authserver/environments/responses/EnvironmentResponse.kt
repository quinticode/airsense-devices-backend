package br.pucpr.authserver.environments.responses

import br.pucpr.authserver.environments.AirQuality

data class EnvironmentResponse(
    val id: Long,
    val name: String,
    val airQuality: AirQuality?,
    val devices: List<EnvironmentDeviceResponse>
)
