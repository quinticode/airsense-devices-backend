package br.pucpr.authserver.sensors

import jdk.jfr.Threshold
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class SensorTresholdService(
    val repository: SensorTresholdRepository
) {
    fun insert(threshold: SensorTreshold): SensorTreshold {
         threshold.name = threshold.name.uppercase()

        if (repository.findByName(threshold.name) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Threshold ${threshold.name} ja existe")
        }
        return repository.save(threshold)
    }

    fun findAll(): List<SensorTreshold> = repository.findAll()

    fun findByName(name: String): SensorTreshold =
        repository.findByName(name.uppercase())
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Threshold $name nao encontrada")

    fun evaluate(name:String, value: Double): AlertLevel {
        val threshold = findByName(name)
        return threshold.evaluate(value)
    }
}