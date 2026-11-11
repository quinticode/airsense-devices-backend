package br.pucpr.authserver.users

import br.pucpr.authserver.devices.Device
import jakarta.persistence.FetchType
import jakarta.persistence.*

@Entity
@Table(name = "UserTable")
class User (
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var email: String,

    var password: String,
    var name: String = ""
) {
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    var devices: MutableSet<Device> = mutableSetOf()
}