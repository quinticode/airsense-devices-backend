package br.pucpr.authserver.sensors

import java.time.Instant

data class SensorReading(
    val name: String,
    val unit: String,
    val value: Double,
    val level: AlertLevel,
    val timestamp: Instant,
    val deviceId: Long? = null
)
