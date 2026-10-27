package pe.upeu.agrosierra.seguridad

data class Usuario(
    val id: Long,
    val username: String,
    val passwordHash: String,
    val estado: Boolean,
    val rol: String,
    val privilegios: Set<String>,
)

data class Rol(val id: Long, val nombre: String)
data class Privilegio(val id: Long, val codigo: String)
