package pe.upeu.agrosierra.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pe.upeu.agrosierra.repositorio.UsuarioRepositorio

data class PrincipalAgro(val id: Long, val username: String, val rol: String)

@Component
class JwtAuthenticationFilter(
    private val jwt: JwtService,
    private val usuarios: UsuarioRepositorio,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val cabecera = request.getHeader("Authorization")
        if (cabecera?.startsWith("Bearer ") == true) {
            val identidad = jwt.verificar(cabecera.removePrefix("Bearer "))
            val usuario = identidad?.let { usuarios.buscarPorId(it.usuarioId) }
            if (usuario != null && usuario.estado) {
                // Los privilegios efectivos se consultan en la base: una revocación tiene efecto inmediato.
                val autenticacion = UsernamePasswordAuthenticationToken(
                    PrincipalAgro(usuario.id, usuario.username, usuario.rol), null,
                    usuario.privilegios.map(::SimpleGrantedAuthority)
                )
                SecurityContextHolder.getContext().authentication = autenticacion
            }
        }
        chain.doFilter(request, response)
    }
}
