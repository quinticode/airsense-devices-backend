package br.pucpr.authserver.devices

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DeviceRepository : JpaRepository<Device, Long> {
    @Query("select device from Device device where device.user.id = :userId")
    fun findAllByUserId(@Param("userId") userId: Long): List<Device>

    @Query("select device from Device device where device.id = :id and device.user.id = :userId")
    fun findByIdAndUserId(@Param("id") id: Long, @Param("userId") userId: Long): Device?
}
