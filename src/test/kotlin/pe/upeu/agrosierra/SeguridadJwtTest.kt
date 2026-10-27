package pe.upeu.agrosierra

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.seguridad.Usuario
import java.net.HttpURLConnection
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "agro.datasource.url=jdbc:h2:mem:agro_seguridad_test;DB_CLOSE_DELAY=-1",
    "agro.jwt.secret=clave-exclusiva-de-pruebas-AgroSierra-2026"
])
class SeguridadJwtTest {
    @Value("\${local.server.port}") private var puerto: Int = 0
    private val mapper = ObjectMapper()

    private fun peticion(ruta: String, metodo: String = "GET", cuerpo: String? = null,
                         autorizacion: String? = null): Pair<Int, String> {
        val conexion = URI("http://127.0.0.1:$puerto$ruta").toURL().openConnection() as HttpURLConnection
        conexion.requestMethod = metodo
        conexion.connectTimeout = 3000
        conexion.readTimeout = 3000
        if (autorizacion != null) conexion.setRequestProperty("Authorization", autorizacion)
        if (cuerpo != null) {
            conexion.doOutput = true
            conexion.setRequestProperty("Content-Type", "application/json")
            conexion.outputStream.use { it.write(cuerpo.toByteArray()) }
        }
        val codigo = conexion.responseCode
        val texto = (if (codigo >= 400) conexion.errorStream else conexion.inputStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        conexion.disconnect()
        return codigo to texto
    }

    @Test fun loginValidoEIncorrecto() {
        val correcto = peticion("/api/v1/auth/login", "POST",
            """{"usuario":"tecnico1","contrasena":"Agro2026*"}""")
        assertEquals(200, correcto.first)
        val json = mapper.readTree(correcto.second)
        assertEquals("tecnico1", json.path("usuario").asText())
        assertNotNull(json.path("token").asText())
        assertEquals(401, peticion("/api/v1/auth/login", "POST",
            """{"usuario":"tecnico1","contrasena":"incorrecta"}""").first)
    }

    @Test fun rutaProtegidaSinTokenYTokenAlteradoResponde401() {
        assertEquals(401, peticion("/api/v1/lotes").first)
        assertEquals(401, peticion("/api/v1/lotes", autorizacion = "Bearer alterado").first)
    }

    @Test fun firmaYExpiracionExactaDeUnaHora() {
        val instante = Instant.parse("2026-10-27T12:00:00Z")
        val usuario = Usuario(1, "tecnico1", "hash", true, "TECNICO", setOf("LOTE_LEER"))
        fun servicio(segundos: Long) = JwtService("clave-exclusiva-de-pruebas-AgroSierra-2026",
            mapper, Clock.fixed(instante.plusSeconds(segundos), ZoneOffset.UTC))
        val token = servicio(0).emitir(usuario)
        assertEquals(1, servicio(3599).verificar(token)?.usuarioId)
        assertNull(servicio(3600).verificar(token))
        assertNull(servicio(0).verificar(token.dropLast(1) + if (token.last() == 'A') 'B' else 'A'))
    }
}
