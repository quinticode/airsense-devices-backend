package br.pucpr.authserver.sensors

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class SensorSimulator(
    private val thresholdRepository: SensorTresholdRepository,
    private val properties: SensorSimulatorProperties
) {
    private val lock = Any()
    private val history = ArrayDeque<SensorReading>()
    private val previousValues = mutableMapOf<String, Double>()

    @Scheduled(fixedDelayString = "\${airsense.sensor-simulator.interval:5000}")
    fun generateReadings() {
        if (!properties.enabled) return

        val thresholds = thresholdRepository.findAll()
        val timestamp = java.time.Instant.now()

        synchronized(lock) {
            thresholds.forEach { threshold ->
                require(threshold.lowThreshold <= threshold.highThreshold) {
                    "Sensor ${threshold.name} has a low threshold greater than its high threshold"
                }

                val range = threshold.highThreshold - threshold.lowThreshold
                val previous = previousValues[threshold.name]
                    ?.coerceIn(threshold.lowThreshold, threshold.highThreshold)
                val value = if (previous == null || range == 0.0) {
                    threshold.lowThreshold + range / 2.0
                } else {
                    (previous + Random.nextDouble(-1.0, 1.0) * range * properties.variationPercent)
                        .coerceIn(threshold.lowThreshold, threshold.highThreshold)
                }

                previousValues[threshold.name] = value
                history.addLast(
                    SensorReading(
                        name = threshold.name,
                        unit = threshold.unit,
                        value = value,
                        level = threshold.evaluate(value),
                        timestamp = timestamp
                    )
                )
                while (history.size > properties.historySize) {
                    history.removeFirst()
                }
            }
        }
    }

    fun findAllReadings(): List<SensorReading> = synchronized(lock) { history.toList() }

    fun findReadingsByName(name: String): List<SensorReading> = synchronized(lock) {
        history.filter { it.name.equals(name, ignoreCase = true) }
    }
}
