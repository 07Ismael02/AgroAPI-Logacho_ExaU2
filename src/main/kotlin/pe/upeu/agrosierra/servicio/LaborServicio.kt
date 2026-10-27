package pe.upeu.agrosierra.servicio

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborGuardada
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import pe.upeu.agrosierra.repositorio.LoteRepositorio
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class LaborServicio(
    private val lotes: LoteRepositorio,
    private val labores: LaborRepositorio,
    private val reloj: Clock = Clock.systemUTC(),
) {
    @Transactional
    fun registrar(labor: Labor, usuarioId: Long): LaborGuardada {
        validarLoteYDosificacion(labor)
        return labores.crear(labor, usuarioId)
    }

    @Transactional
    fun editar(labor: Labor, usuarioId: Long, rol: String): LaborGuardada {
        val anterior = labores.buscar(labor.id)
            ?: throw ErrorRegla(HttpStatus.NOT_FOUND, "Labor no encontrada")
        if (rol != "JEFE_CAMPO") {
            if (rol != "TECNICO" || anterior.autorId != usuarioId ||
                Instant.now(reloj).isAfter(anterior.creadoEn.plus(Duration.ofHours(24)))) {
                throw ErrorRegla(HttpStatus.FORBIDDEN, "No puede editar esta labor")
            }
        }
        if (labor.loteId != anterior.labor.loteId) {
            throw ErrorRegla(HttpStatus.BAD_REQUEST, "No puede cambiar el lote de una labor", "loteId")
        }
        validarLoteYDosificacion(labor)
        return labores.actualizar(labor)
            ?: throw ErrorRegla(HttpStatus.NOT_FOUND, "Labor no encontrada")
    }

    private fun validarLoteYDosificacion(labor: Labor) {
        val lote = lotes.buscar(labor.loteId)
            ?: throw ErrorRegla(HttpStatus.NOT_FOUND, "Lote no encontrado")
        if (lote.campaniaCerrada) {
            throw ErrorRegla(HttpStatus.CONFLICT, "No se permiten labores en una campaña cerrada")
        }
        if (labor.tipo == TipoLabor.FUMIGACION &&
            labor.cantidad > lote.dosisMaximaPorHectarea * lote.hectareas) {
            throw ErrorRegla(HttpStatus.BAD_REQUEST, "La cantidad supera la dosis máxima del lote", "cantidad")
        }
    }
}
