package pe.upeu.agrosierra.repositorio.jdbc

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborGuardada
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import java.sql.Date
import java.time.LocalDate
import java.time.OffsetDateTime

@Repository
class LaborJdbcRepositorio(private val jdbc: JdbcTemplate) : LaborRepositorio {
    private val baseSql = "SELECT l.*, u.username AS responsable FROM labor l JOIN usuario u ON u.id = l.responsable_usuario_id"
    private val mapper = RowMapper<LaborGuardada> { rs, _ ->
        LaborGuardada(
            labor = Labor(
                id = rs.getLong("id"), loteId = rs.getLong("lote_id"),
                tipo = TipoLabor.valueOf(rs.getString("tipo")),
                fecha = rs.getDate("fecha").toLocalDate().toString(),
                responsable = rs.getString("responsable"),
                cantidad = rs.getDouble("cantidad"), observacion = rs.getString("observacion"),
                pendiente = false
            ),
            autorId = rs.getLong("responsable_usuario_id"),
            creadoEn = rs.getObject("creado_en", OffsetDateTime::class.java).toInstant()
        )
    }

    override fun buscar(id: Long): LaborGuardada? =
        jdbc.query("$baseSql WHERE l.id = ?", mapper, id).firstOrNull()

    override fun listarDeLote(loteId: Long, pagina: Int, tamanio: Int): List<Labor> {
        require(pagina >= 0 && tamanio in 1..100)
        return jdbc.query(
            "$baseSql WHERE l.lote_id = ? ORDER BY l.fecha DESC, l.creado_en DESC, l.id DESC LIMIT ? OFFSET ?",
            mapper, loteId, tamanio, pagina.toLong() * tamanio
        ).map { it.labor }
    }

    override fun contarDeLote(loteId: Long): Long =
        jdbc.queryForObject("SELECT COUNT(*) FROM labor WHERE lote_id = ?", Long::class.java, loteId) ?: 0

    override fun tieneCosecha(loteId: Long): Boolean =
        (jdbc.queryForObject(
            "SELECT COUNT(*) FROM labor WHERE lote_id = ? AND tipo = 'COSECHA'",
            Long::class.java, loteId
        ) ?: 0) > 0

    override fun crear(labor: Labor, autorId: Long): LaborGuardada {
        val keys = GeneratedKeyHolder()
        jdbc.update({ connection ->
            connection.prepareStatement(
                "INSERT INTO labor (lote_id, tipo, fecha, responsable_usuario_id, cantidad, observacion) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf("id")
            ).apply {
                setLong(1, labor.loteId)
                setString(2, labor.tipo.name)
                setDate(3, Date.valueOf(LocalDate.parse(labor.fecha)))
                setLong(4, autorId)
                setDouble(5, labor.cantidad)
                setString(6, labor.observacion)
            }
        }, keys)
        val id = requireNotNull(keys.key).toLong()
        return requireNotNull(buscar(id))
    }

    override fun actualizar(labor: Labor): LaborGuardada? {
        val filas = jdbc.update(
            "UPDATE labor SET tipo = ?, fecha = ?, cantidad = ?, observacion = ? WHERE id = ?",
            labor.tipo.name, Date.valueOf(LocalDate.parse(labor.fecha)), labor.cantidad,
            labor.observacion, labor.id
        )
        return if (filas == 1) buscar(labor.id) else null
    }

    override fun eliminar(id: Long): Boolean = jdbc.update("DELETE FROM labor WHERE id = ?", id) == 1
}
