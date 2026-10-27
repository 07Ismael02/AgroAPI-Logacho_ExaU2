package pe.upeu.agrosierra.repositorio

import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.TipoLabor
import java.time.Instant

data class LaborGuardada(
    val labor: Labor,
    val autorId: Long,
    val creadoEn: Instant,
)

interface LaborRepositorio {
    fun buscar(id: Long): LaborGuardada?
    fun listarDeLote(loteId: Long, pagina: Int, tamanio: Int): List<Labor>
    fun contarDeLote(loteId: Long): Long
    fun tieneCosecha(loteId: Long): Boolean
    fun crear(labor: Labor, autorId: Long): LaborGuardada
    fun actualizar(labor: Labor): LaborGuardada?
    fun eliminar(id: Long): Boolean
}
