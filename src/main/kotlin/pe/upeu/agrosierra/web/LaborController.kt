package pe.upeu.agrosierra.web

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.config.PrincipalAgro
import pe.upeu.agrosierra.servicio.LaborServicio
import pe.upeu.agrosierra.web.dto.LaborPeticion
import pe.upeu.agrosierra.web.dto.LaborRespuesta
import pe.upeu.agrosierra.web.dto.dominio
import pe.upeu.agrosierra.web.dto.respuesta

@RestController
@RequestMapping("/api/v1/labores")
class LaborController(private val servicio: LaborServicio) {
    @PostMapping
    fun registrar(@Valid @RequestBody peticion: LaborPeticion,
                 @AuthenticationPrincipal principal: PrincipalAgro): ResponseEntity<LaborRespuesta> =
        ResponseEntity.status(HttpStatus.CREATED).body(
            servicio.registrar(peticion.dominio(responsable = principal.username), principal.id)
                .labor.respuesta())

    @PutMapping("/{id}")
    fun editar(@PathVariable id: Long, @Valid @RequestBody peticion: LaborPeticion,
               @AuthenticationPrincipal principal: PrincipalAgro): LaborRespuesta =
        servicio.editar(peticion.dominio(id, principal.username), principal.id, principal.rol)
            .labor.respuesta()

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Long): ResponseEntity<Void> {
        servicio.eliminar(id)
        return ResponseEntity.noContent().build()
    }
}
