package br.pucpr.authserver.environments

import br.pucpr.authserver.devices.Device
import br.pucpr.authserver.users.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "environments")
class Environment(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var owner: User
) {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "environment_devices",
        joinColumns = [JoinColumn(name = "environment_id")],
        inverseJoinColumns = [JoinColumn(name = "device_id")]
    )
    var devices: MutableSet<Device> = mutableSetOf()
}
