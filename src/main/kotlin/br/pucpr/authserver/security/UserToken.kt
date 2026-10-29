package br.pucpr.authserver.security

import br.pucpr.authserver.users.User

data class UserToken(
    val id: Long,
    val name: String
) {
    constructor() : this(0, "")
    constructor(user: User) : this(
        id = user.id ?: -1L,
        name = user.name
    )
}