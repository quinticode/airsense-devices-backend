package br.pucpr.authserver.sensors

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SensorTresholdRepository : JpaRepository<SensorTreshold, Long> {
    fun findByName(name: String): SensorTreshold?
}