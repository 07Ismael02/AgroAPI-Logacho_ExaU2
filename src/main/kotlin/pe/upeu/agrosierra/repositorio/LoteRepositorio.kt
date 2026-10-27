package pe.upeu.agrosierra.repositorio

import pe.upeu.agrosierra.dominio.Lote

interface LoteRepositorio {
    fun buscar(id: Long): Lote?
    fun listar(pagina: Int, tamanio: Int, valle: String? = null, campaniaCerrada: Boolean? = null): List<Lote>
    fun contar(valle: String? = null, campaniaCerrada: Boolean? = null): Long
    fun cerrarCampania(id: Long): Boolean
}
