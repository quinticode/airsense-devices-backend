package br.pucpr.authserver.sensors

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/sensor-readings")
class SensorReadingController(private val simulator: SensorSimulator) {
    @GetMapping
    fun list(): ResponseEntity<List<SensorReading>> =
        ResponseEntity.ok(simulator.findAllReadings())

    @GetMapping("/{name}")
    fun listByName(@PathVariable name: String): ResponseEntity<List<SensorReading>> =
        ResponseEntity.ok(simulator.findReadingsByName(name))
}
