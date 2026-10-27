package pe.upeu.agrosierra.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor

data class LoteRespuesta(
    val id: Long, val codigo: String, val nombre: String, val hectareas: Double,
    val cultivo: String, val valle: String, val campaniaCerrada: Boolean,
    val dosisMaximaPorHectarea: Double,
)

fun Lote.respuesta() = LoteRespuesta(id, codigo, nombre, hectareas, cultivo, valle,
    campaniaCerrada, dosisMaximaPorHectarea)

data class LaborPeticion(
    @field:NotNull val loteId: Long? = null,
    @field:NotNull val tipo: TipoLabor? = null,
    @field:NotBlank val fecha: String = "",
    @field:NotNull @field:Positive val cantidad: Double? = null,
    val observacion: String = "",
)

fun LaborPeticion.dominio(id: Long = 0, responsable: String) = Labor(
    id, requireNotNull(loteId), requireNotNull(tipo), fecha, responsable,
    requireNotNull(cantidad), observacion, false)

data class LaborRespuesta(
    val id: Long, val loteId: Long, val tipo: TipoLabor, val fecha: String,
    val responsable: String, val cantidad: Double, val observacion: String,
    val pendiente: Boolean,
)

fun Labor.respuesta() = LaborRespuesta(id, loteId, tipo, fecha, responsable,
    cantidad, observacion, pendiente)
