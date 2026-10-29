package br.pucpr.authserver.sensors

data class SensorTresholdResponse(
    val id: Long?,
    val name: String,
    val unit: String,
    val lowThreshold: Double,
    val highThreshold: Double
) {
    constructor(sensorTreshold: SensorTreshold) : this(
        id = sensorTreshold.id,
        name = sensorTreshold.name,
        unit = sensorTreshold.unit,
        lowThreshold = sensorTreshold.lowThreshold,
        highThreshold = sensorTreshold.highThreshold
    )
}