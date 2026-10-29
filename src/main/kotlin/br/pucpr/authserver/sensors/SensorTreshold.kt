package br.pucpr.authserver.sensors

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class SensorTreshold(
    @Id @GeneratedValue // acho que aqui é o id do negocio lari nao sei
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var unit: String = "PPM", // talvez a gente nao queira isso, se for medir tudo em PPM

    @Column(nullable = false)
    var lowThreshold: Double = 800.0,

    @Column(nullable = false)
    var highThreshold: Double = 1200.0
)  {
fun evaluate(value: Double): AlertLevel {
    return when {
                value <= lowThreshold -> AlertLevel.LOW
                value <= highThreshold -> AlertLevel.MEDIUM
                else -> AlertLevel.HIGH
            }
        }
    }