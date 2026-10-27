package pe.upeu.agrosierra.seguridad

import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.repositorio.UsuarioRepositorio
import pe.upeu.agrosierra.servicio.ErrorRegla

data class LoginPeticion(val usuario: String = "", val contrasena: String = "")
data class LoginRespuesta(val token: String, val usuario: String, val rol: String, val privilegios: Set<String>)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val usuarios: UsuarioRepositorio,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService,
) {
    @PostMapping("/login")
    fun login(@RequestBody peticion: LoginPeticion): LoginRespuesta {
        val usuario = usuarios.buscarPorUsername(peticion.usuario)
        if (usuario == null || !usuario.estado || !encoder.matches(peticion.contrasena, usuario.passwordHash)) {
            throw ErrorRegla(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas")
        }
        return LoginRespuesta(jwt.emitir(usuario), usuario.username, usuario.rol, usuario.privilegios)
    }
}
