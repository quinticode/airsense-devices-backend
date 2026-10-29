package br.pucpr.authserver.sensors

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SensorTresholdRequest(
    @field:NotBlank
    val name: String?,

    val unit: String? = "PPM",

    @field:NotNull
    val lowThreshold: Double? = 800.0,

    val highThreshold: Double? = 1200.0
){
    fun toSensorTreshold() = SensorTreshold(
        id = null,
        name = name!!,
        unit = unit ?: "PPM",
        lowThreshold = lowThreshold ?: 800.0,
        highThreshold = highThreshold ?: 1200.0
    )
}