package br.pucpr.authserver.sensors

import br.pucpr.authserver.devices.DeviceRepository
import br.pucpr.authserver.devices.DeviceStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class SensorSimulator(
    private val thresholdRepository: SensorTresholdRepository,
    private val deviceRepository: DeviceRepository,
    private val properties: SensorSimulatorProperties
) {
    private val lock = Any()
    private val history = ArrayDeque<SensorReading>()
    private val previousValues = mutableMapOf<String, Double>()
    private val latestByDeviceAndSensor = mutableMapOf<Pair<Long, String>, SensorReading>()

    @Scheduled(fixedDelayString = "\${airsense.sensor-simulator.interval:5000}")
    fun generateReadings() {
        if (!properties.enabled) return

        val thresholds = thresholdRepository.findAll()
        val activeDevices = deviceRepository.findAll()
            .filter { it.status == DeviceStatus.ACTIVE }
        val timestamp = java.time.Instant.now()

        synchronized(lock) {
            val activeDeviceIds = activeDevices.mapNotNull { it.id }.toSet()
            val configuredSensors = thresholds.map { it.name }.toSet()
            latestByDeviceAndSensor.keys.removeIf {
                it.first !in activeDeviceIds || it.second !in configuredSensors
            }
            val validPreviousValueKeys = if (activeDeviceIds.isEmpty()) {
                configuredSensors.map { "global:$it" }.toSet()
            } else {
                activeDeviceIds.flatMap { deviceId ->
                    configuredSensors.map { sensorName -> "$deviceId:$sensorName" }
                }.toSet()
            }
            previousValues.keys.removeIf { it !in validPreviousValueKeys }
            thresholds.forEach { threshold ->
                require(threshold.lowThreshold <= threshold.highThreshold) {
                    "Sensor ${threshold.name} has a low threshold greater than its high threshold"
                }

                val targets = activeDevices.map { it.id }.ifEmpty { listOf(null) }
                targets.forEach { deviceId ->
                    val key = "${deviceId ?: "global"}:${threshold.name}"
                    val range = threshold.highThreshold - threshold.lowThreshold
                    val previous = previousValues[key]
                        ?.coerceIn(threshold.lowThreshold, threshold.highThreshold)
                    val value = if (previous == null || range == 0.0) {
                        threshold.lowThreshold + range / 2.0
                    } else {
                        (previous + Random.nextDouble(-1.0, 1.0) * range * properties.variationPercent)
                            .coerceIn(threshold.lowThreshold, threshold.highThreshold)
                    }

                    previousValues[key] = value
                    val reading = SensorReading(
                        name = threshold.name,
                        unit = threshold.unit,
                        value = value,
                        level = threshold.evaluate(value),
                        timestamp = timestamp,
                        deviceId = deviceId
                    )
                    history.addLast(reading)
                    if (deviceId != null) {
                        latestByDeviceAndSensor[deviceId to threshold.name] = reading
                    }
                    while (history.size > properties.historySize) {
                        history.removeFirst()
                    }
                }
            }
        }
    }

    fun findAllReadings(): List<SensorReading> = synchronized(lock) { history.toList() }

    fun findReadingsByName(name: String): List<SensorReading> = synchronized(lock) {
        history.filter { it.name.equals(name, ignoreCase = true) }
    }

    fun findLatestReadingsByDeviceId(deviceId: Long): List<SensorReading> =
        synchronized(lock) {
            latestByDeviceAndSensor
                .filterKeys { it.first == deviceId }
                .values
                .toList()
        }
}
