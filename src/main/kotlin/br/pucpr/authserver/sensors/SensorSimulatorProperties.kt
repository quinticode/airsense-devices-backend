package br.pucpr.authserver.sensors

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("airsense.sensor-simulator")
data class SensorSimulatorProperties(
    val enabled: Boolean = true,
    val interval: Long = 5000,
    val variationPercent: Double = 0.02,
    val historySize: Int = 1000
) {
    init {
        require(interval > 0) { "Sensor simulator interval must be greater than zero" }
        require(variationPercent in 0.0..1.0) { "Sensor simulator variation-percent must be between 0 and 1" }
        require(historySize > 0) { "Sensor simulator history-size must be greater than zero" }
    }
}
