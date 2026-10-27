package pe.upeu.agrosierra.web

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.servicio.CampaniaServicio
import pe.upeu.agrosierra.servicio.ConsultaServicio
import pe.upeu.agrosierra.servicio.ErrorRegla
import pe.upeu.agrosierra.web.dto.PaginaRespuesta
import pe.upeu.agrosierra.web.dto.LaborRespuesta
import pe.upeu.agrosierra.web.dto.LoteRespuesta
import pe.upeu.agrosierra.web.dto.respuesta

@RestController
@RequestMapping("/api/v1/lotes")
class LoteController(private val consultas: ConsultaServicio, private val campanias: CampaniaServicio) {
    @GetMapping
    fun listar(@RequestParam(defaultValue = "0") pagina: Int,
               @RequestParam(defaultValue = "20") tamanio: Int,
               @RequestParam(required = false) valle: String?,
               @RequestParam(required = false) estado: String?): PaginaRespuesta<LoteRespuesta> {
        val cerrada = when (estado?.lowercase()) {
            null -> null
            "abierta" -> false
            "cerrada" -> true
            else -> throw ErrorRegla(HttpStatus.BAD_REQUEST, "Estado inválido", "estado")
        }
        validarPagina(pagina, tamanio)
        val resultado = consultas.listarLotes(pagina, tamanio, valle, cerrada)
        return PaginaRespuesta(resultado.contenido.map { it.respuesta() }, pagina, tamanio,
            resultado.totalElementos, resultado.totalPaginas, resultado.ultima)
    }

    @GetMapping("/{id}")
    fun detalle(@PathVariable id: Long): LoteRespuesta = consultas.lote(id).respuesta()

    @GetMapping("/{id}/labores")
    fun labores(@PathVariable id: Long, @RequestParam(defaultValue = "0") pagina: Int,
                @RequestParam(defaultValue = "20") tamanio: Int): PaginaRespuesta<LaborRespuesta> {
        validarPagina(pagina, tamanio)
        val resultado = consultas.labores(id, pagina, tamanio)
        return PaginaRespuesta(resultado.contenido.map { it.respuesta() }, pagina, tamanio,
            resultado.totalElementos, resultado.totalPaginas, resultado.ultima)
    }

    @PostMapping("/{id}/cerrar-campania")
    fun cerrar(@PathVariable id: Long): LoteRespuesta = campanias.cerrar(id).respuesta()

    private fun validarPagina(pagina: Int, tamanio: Int) {
        if (pagina < 0 || tamanio !in 1..100) {
            throw ErrorRegla(HttpStatus.BAD_REQUEST, "Paginación inválida", "pagina")
        }
    }
}
