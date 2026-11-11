package br.pucpr.authserver.environments

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EnvironmentRepository : JpaRepository<Environment, Long> {
    @Query("select environment from Environment environment where environment.owner.id = :userId")
    fun findAllByOwnerId(@Param("userId") userId: Long): List<Environment>

    @Query(
        "select environment from Environment environment " +
            "where environment.id = :id and environment.owner.id = :userId"
    )
    fun findByIdAndOwnerId(@Param("id") id: Long, @Param("userId") userId: Long): Environment?
}
