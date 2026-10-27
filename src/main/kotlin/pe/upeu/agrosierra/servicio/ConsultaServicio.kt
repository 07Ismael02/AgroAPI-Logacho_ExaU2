package pe.upeu.agrosierra.servicio

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import pe.upeu.agrosierra.repositorio.LoteRepositorio
import pe.upeu.agrosierra.web.dto.PaginaRespuesta

@Service
class ConsultaServicio(private val lotes: LoteRepositorio, private val labores: LaborRepositorio) {
    @PreAuthorize("hasAuthority('LOTE_LEER')")
    fun listarLotes(pagina: Int, tamanio: Int, valle: String?, cerrada: Boolean?): PaginaRespuesta<Lote> =
        paginar(lotes.listar(pagina, tamanio, valle, cerrada), pagina, tamanio,
            lotes.contar(valle, cerrada))

    @PreAuthorize("hasAuthority('LOTE_LEER')")
    fun lote(id: Long): Lote = lotes.buscar(id) ?: throw ErrorRegla(HttpStatus.NOT_FOUND, "Lote no encontrado")

    @PreAuthorize("hasAuthority('LABOR_LEER')")
    fun labores(id: Long, pagina: Int, tamanio: Int): PaginaRespuesta<Labor> {
        if (lotes.buscar(id) == null) throw ErrorRegla(HttpStatus.NOT_FOUND, "Lote no encontrado")
        return paginar(labores.listarDeLote(id, pagina, tamanio), pagina, tamanio, labores.contarDeLote(id))
    }

    private fun <T> paginar(contenido: List<T>, pagina: Int, tamanio: Int, total: Long): PaginaRespuesta<T> =
        PaginaRespuesta(contenido, pagina, tamanio, total,
            ((total + tamanio - 1) / tamanio).toInt(), (pagina.toLong() + 1) * tamanio >= total)
}
