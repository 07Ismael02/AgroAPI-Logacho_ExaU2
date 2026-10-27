package pe.upeu.agrosierra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor
import pe.upeu.agrosierra.repositorio.LaborGuardada
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import pe.upeu.agrosierra.repositorio.LoteRepositorio
import pe.upeu.agrosierra.servicio.CampaniaServicio
import pe.upeu.agrosierra.servicio.ErrorRegla
import pe.upeu.agrosierra.servicio.LaborServicio
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ReglasNegocioTest {
    private val lotes = mock(LoteRepositorio::class.java)
    private val labores = mock(LaborRepositorio::class.java)
    private val ahora = Instant.parse("2026-10-27T12:00:00Z")
    private val servicio = LaborServicio(lotes, labores, Clock.fixed(ahora, ZoneOffset.UTC))
    private val lote = Lote(1, "L1", "Lote", 2.0, "Maíz", "Valle", false, 3.0)
    private val labor = Labor(10, 1, TipoLabor.RIEGO, "2026-10-27", "tecnico", 1.0, "", false)

    private fun error(estado: HttpStatus, campo: String? = null, accion: () -> Unit) {
        val e = assertThrows(ErrorRegla::class.java, accion)
        assertEquals(estado, e.estado)
        assertEquals(campo, e.campo)
    }

    @Test fun rn01ImpideRegistroYEdicionEnCampaniaCerrada() {
        `when`(lotes.buscar(1)).thenReturn(lote.copy(campaniaCerrada = true))
        error(HttpStatus.CONFLICT) { servicio.registrar(labor, 1) }
        `when`(labores.buscar(10)).thenReturn(LaborGuardada(labor, 1, ahora))
        error(HttpStatus.CONFLICT) { servicio.editar(labor, 1, "TECNICO") }
    }

    @Test fun rn02TecnicoSoloEditaLaborPropiaDentroDe24Horas() {
        `when`(lotes.buscar(1)).thenReturn(lote)
        `when`(labores.buscar(10)).thenReturn(LaborGuardada(labor, 2, ahora.minusSeconds(3600)))
        error(HttpStatus.FORBIDDEN) { servicio.editar(labor, 1, "TECNICO") }
        `when`(labores.buscar(10)).thenReturn(LaborGuardada(labor, 1, ahora.minusSeconds(24 * 3600 + 1)))
        error(HttpStatus.FORBIDDEN) { servicio.editar(labor, 1, "TECNICO") }
        `when`(labores.buscar(10)).thenReturn(LaborGuardada(labor, 1, ahora.minusSeconds(24 * 3600)))
        `when`(labores.actualizar(labor)).thenReturn(LaborGuardada(labor, 1, ahora))
        assertEquals(labor, servicio.editar(labor, 1, "TECNICO").labor)
    }

    @Test fun rn03RequiereCosechaParaCerrarCampania() {
        `when`(lotes.buscar(1)).thenReturn(lote, lote, lote.copy(campaniaCerrada = true))
        error(HttpStatus.CONFLICT) { CampaniaServicio(lotes, labores).cerrar(1) }
        `when`(labores.tieneCosecha(1)).thenReturn(true)
        `when`(lotes.cerrarCampania(1)).thenReturn(true)
        assertEquals(true, CampaniaServicio(lotes, labores).cerrar(1).campaniaCerrada)
        verify(lotes).cerrarCampania(1)
    }

    @Test fun rn04RechazaFumigacionSobreDosisMaximaEnCampoCantidad() {
        `when`(lotes.buscar(1)).thenReturn(lote)
        error(HttpStatus.BAD_REQUEST, "cantidad") {
            servicio.registrar(labor.copy(tipo = TipoLabor.FUMIGACION, cantidad = 6.01), 1)
        }
        `when`(labores.crear(labor.copy(tipo = TipoLabor.FUMIGACION, cantidad = 6.0), 1))
            .thenReturn(LaborGuardada(labor, 1, ahora))
        servicio.registrar(labor.copy(tipo = TipoLabor.FUMIGACION, cantidad = 6.0), 1)
    }
}
