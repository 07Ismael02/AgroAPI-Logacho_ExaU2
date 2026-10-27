package pe.upeu.agrosierra.repositorio.jdbc

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.repositorio.LoteRepositorio

@Repository
class LoteJdbcRepositorio(private val jdbc: JdbcTemplate) : LoteRepositorio {
    private val mapper = RowMapper<Lote> { rs, _ ->
        Lote(
            rs.getLong("id"), rs.getString("codigo"), rs.getString("nombre"),
            rs.getDouble("hectareas"), rs.getString("cultivo"), rs.getString("valle"),
            rs.getBoolean("campania_cerrada"), rs.getDouble("dosis_maxima_por_hectarea")
        )
    }

    override fun buscar(id: Long): Lote? = jdbc.query(
        "SELECT * FROM lote WHERE id = ?", mapper, id
    ).firstOrNull()

    private fun filtro(valle: String?, campaniaCerrada: Boolean?): Pair<String, List<Any>> {
        val clausulas = mutableListOf<String>()
        val valores = mutableListOf<Any>()
        if (valle != null) { clausulas += "valle = ?"; valores += valle }
        if (campaniaCerrada != null) { clausulas += "campania_cerrada = ?"; valores += campaniaCerrada }
        return (if (clausulas.isEmpty()) "" else " WHERE ${clausulas.joinToString(" AND ")}") to valores
    }

    override fun listar(pagina: Int, tamanio: Int, valle: String?, campaniaCerrada: Boolean?): List<Lote> {
        require(pagina >= 0 && tamanio in 1..100)
        val (where, valores) = filtro(valle, campaniaCerrada)
        return jdbc.query(
            "SELECT * FROM lote$where ORDER BY id LIMIT ? OFFSET ?", mapper,
            *(valores + listOf(tamanio, pagina.toLong() * tamanio)).toTypedArray()
        )
    }

    override fun contar(valle: String?, campaniaCerrada: Boolean?): Long {
        val (where, valores) = filtro(valle, campaniaCerrada)
        return jdbc.queryForObject("SELECT COUNT(*) FROM lote$where", Long::class.java, *valores.toTypedArray()) ?: 0
    }

    override fun cerrarCampania(id: Long): Boolean =
        jdbc.update("UPDATE lote SET campania_cerrada = TRUE WHERE id = ?", id) == 1
}
