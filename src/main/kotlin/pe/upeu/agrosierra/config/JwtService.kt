package pe.upeu.agrosierra.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import pe.upeu.agrosierra.seguridad.Usuario
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class IdentidadToken(val usuarioId: Long, val privilegios: Set<String>)

@Service
class JwtService(
    @Value("\${agro.jwt.secret:}") secreto: String,
    private val mapper: ObjectMapper,
    private val reloj: Clock,
) {
    private val clave = secreto.toByteArray(UTF_8).also {
        require(it.size >= 32) { "AGRO_JWT_SECRET debe tener al menos 32 bytes" }
    }
    private val b64 = Base64.getUrlEncoder().withoutPadding()
    private val dec = Base64.getUrlDecoder()

    fun emitir(usuario: Usuario): String {
        val ahora = Instant.now(reloj).epochSecond
        val header = codificar(mapOf("alg" to "HS256", "typ" to "JWT"))
        val payload = codificar(mapOf(
            "sub" to usuario.id.toString(), "iat" to ahora, "exp" to ahora + 3600,
            "privilegios" to usuario.privilegios.sorted()
        ))
        val contenido = "$header.$payload"
        return "$contenido.${b64.encodeToString(firmar(contenido))}"
    }

    fun verificar(token: String): IdentidadToken? {
        return try {
        val partes = token.split('.')
        if (partes.size != 3) return null
        val contenido = "${partes[0]}.${partes[1]}"
        if (!MessageDigest.isEqual(firmar(contenido), dec.decode(partes[2]))) return null
        val header = mapper.readTree(dec.decode(partes[0]))
        val payload = mapper.readTree(dec.decode(partes[1]))
        if (header.path("alg").asText() != "HS256" || header.path("typ").asText() != "JWT") return null
        if (payload.path("exp").asLong() <= Instant.now(reloj).epochSecond) return null
        if (payload.path("iat").asLong() > Instant.now(reloj).epochSecond) return null
        val id = payload.path("sub").asText().toLong()
        val permisos = payload.path("privilegios").map { it.asText() }.toSet()
        IdentidadToken(id, permisos)
        } catch (_: Exception) { null }
    }

    private fun codificar(valor: Any) = b64.encodeToString(mapper.writeValueAsBytes(valor))
    private fun firmar(contenido: String): ByteArray = Mac.getInstance("HmacSHA256").run {
        init(SecretKeySpec(clave, "HmacSHA256"))
        doFinal(contenido.toByteArray(UTF_8))
    }
}
