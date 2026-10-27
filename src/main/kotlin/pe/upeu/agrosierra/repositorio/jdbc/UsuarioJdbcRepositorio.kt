package pe.upeu.agrosierra.repositorio.jdbc

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import pe.upeu.agrosierra.repositorio.UsuarioRepositorio
import pe.upeu.agrosierra.seguridad.Usuario

@Repository
class UsuarioJdbcRepositorio(private val jdbc: JdbcTemplate) : UsuarioRepositorio {
    private val usuarioSql = """
        SELECT u.id, u.username, u.password_hash, u.estado, r.nombre AS rol
        FROM usuario u JOIN rol r ON r.id = u.rol_id
    """.trimIndent()

    override fun buscarPorId(id: Long): Usuario? = buscar("u.id = ?", id)
    override fun buscarPorUsername(username: String): Usuario? = buscar("u.username = ?", username)

    private fun buscar(condicion: String, valor: Any): Usuario? = jdbc.query(
        "$usuarioSql WHERE $condicion", { rs, _ ->
            val id = rs.getLong("id")
            val permisos = jdbc.queryForList(
                """
                SELECT p.codigo FROM usuario u
                JOIN rol_privilegio rp ON rp.rol_id = u.rol_id
                JOIN privilegio p ON p.id = rp.privilegio_id
                WHERE u.id = ?
                """.trimIndent(), String::class.java, id
            ).toSet()
            Usuario(id, rs.getString("username"), rs.getString("password_hash"),
                rs.getBoolean("estado"), rs.getString("rol"), permisos)
        }, valor
    ).firstOrNull()
}
