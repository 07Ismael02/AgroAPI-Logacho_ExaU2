package pe.upeu.agrosierra.servicio

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import pe.upeu.agrosierra.repositorio.LoteRepositorio

@Service
class CampaniaServicio(
    private val lotes: LoteRepositorio,
    private val labores: LaborRepositorio,
) {
    @Transactional
    @PreAuthorize("hasAuthority('CAMPANIA_CERRAR')")
    fun cerrar(loteId: Long): Lote {
        val lote = lotes.buscar(loteId)
            ?: throw ErrorRegla(HttpStatus.NOT_FOUND, "Lote no encontrado")
        if (lote.campaniaCerrada) {
            throw ErrorRegla(HttpStatus.CONFLICT, "La campaña ya está cerrada")
        }
        if (!labores.tieneCosecha(loteId)) {
            throw ErrorRegla(HttpStatus.CONFLICT, "No se puede cerrar una campaña sin cosecha")
        }
        if (!lotes.cerrarCampania(loteId)) {
            throw ErrorRegla(HttpStatus.CONFLICT, "La campaña ya fue cerrada")
        }
        return requireNotNull(lotes.buscar(loteId))
    }
}
