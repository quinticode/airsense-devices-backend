package br.pucpr.authserver.users


fun oneUser(
    id: Long? = 1L,
    name: String = "User",
    email: String = "${name.lowercase().replace(" ", ".")}@email.com",
    password: String = "password"
): User = User(
    id = id,
    email = email,
    password = password,
    name = name
)

fun manyUsers(
    vararg names: String
): List<User> = names.mapIndexed { index, name ->
    oneUser(
        id = index + 1L,
        name = name
    )
}

fun manyUsers(
    names: List<String>
): List<User> = manyUsers(*names.toTypedArray())
