package pe.upeu.agrosierra.web

import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.config.PrincipalAgro
import pe.upeu.agrosierra.repositorio.UsuarioRepositorio
import pe.upeu.agrosierra.servicio.ErrorRegla

data class UsuarioActualRespuesta(val id: Long, val usuario: String, val rol: String,
                                  val privilegios: Set<String>)

@RestController
@RequestMapping("/api/v1/usuarios")
class UsuarioController(private val usuarios: UsuarioRepositorio) {
    @GetMapping("/me")
    fun actual(@AuthenticationPrincipal principal: PrincipalAgro): UsuarioActualRespuesta {
        val usuario = usuarios.buscarPorId(principal.id)
            ?: throw ErrorRegla(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión")
        return UsuarioActualRespuesta(usuario.id, usuario.username, usuario.rol, usuario.privilegios)
    }
}
