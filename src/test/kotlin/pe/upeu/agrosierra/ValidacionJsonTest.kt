package pe.upeu.agrosierra

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.net.HttpURLConnection
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "agro.datasource.url=jdbc:h2:mem:agro_validacion_test;DB_CLOSE_DELAY=-1",
    "agro.jwt.secret=clave-exclusiva-de-pruebas-AgroSierra-2026"
])
class ValidacionJsonTest {
    @Value("\${local.server.port}") private var puerto: Int = 0
    private val json = ObjectMapper()

    private fun peticion(ruta: String, cuerpo: String, token: String? = null): Pair<Int, String> {
        val conexion = URI("http://127.0.0.1:$puerto$ruta").toURL().openConnection() as HttpURLConnection
        conexion.requestMethod = "POST"
        conexion.connectTimeout = 3000
        conexion.readTimeout = 3000
        conexion.setRequestProperty("Content-Type", "application/json")
        if (token != null) conexion.setRequestProperty("Authorization", "Bearer $token")
        conexion.doOutput = true
        conexion.outputStream.use { it.write(cuerpo.toByteArray()) }
        val codigo = conexion.responseCode
        val respuesta = (if (codigo >= 400) conexion.errorStream else conexion.inputStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        conexion.disconnect()
        return codigo to respuesta
    }

    private fun registrar(cuerpo: String): Pair<Int, String> {
        val login = peticion("/api/v1/auth/login",
            """{"usuario":"tecnico1","contrasena":"Agro2026*"}""")
        assertEquals(200, login.first)
        val token = json.readTree(login.second).path("token").asText()
        return peticion("/api/v1/labores", cuerpo, token)
    }

    private fun errorDeCampo(cuerpo: String, campo: String) {
        val (codigo, texto) = registrar(cuerpo)
        assertEquals(400, codigo)
        val respuesta = json.readTree(texto)
        assertEquals(setOf("timestamp", "status", "error", "message", "path", "validationErrors"),
            respuesta.fieldNames().asSequence().toSet())
        assertTrue(respuesta.path("validationErrors").has(campo), texto)
        assertFalse(texto.contains("Exception"))
    }

    @Test fun omitirObservacionAplicaValorPredeterminado() {
        val (codigo, texto) = registrar("""{"loteId":2,"tipo":"RIEGO","fecha":"2026-10-27","cantidad":1.0}""")
        assertEquals(201, codigo, texto)
        assertEquals("", json.readTree(texto).path("observacion").asText())
    }

    @Test fun tipoDesconocidoIndicaCampo() = errorDeCampo(
        """{"loteId":2,"tipo":"OTRO","fecha":"2026-10-27","cantidad":1.0}""", "tipo")

    @Test fun loteIdOmitidoIndicaCampo() = errorDeCampo(
        """{"tipo":"RIEGO","fecha":"2026-10-27","cantidad":1.0}""", "loteId")

    @Test fun cantidadOmitidaIndicaCampo() = errorDeCampo(
        """{"loteId":2,"tipo":"RIEGO","fecha":"2026-10-27"}""", "cantidad")

    @Test fun fechaInvalidaIndicaCampo() = errorDeCampo(
        """{"loteId":2,"tipo":"RIEGO","fecha":"hoy","cantidad":1.0}""", "fecha")

    @Test fun fumigacionExcesivaIndicaCantidad() = errorDeCampo(
        """{"loteId":2,"tipo":"FUMIGACION","fecha":"2026-10-27","cantidad":99.0}""", "cantidad")

    @Test fun observacionNulaNoSeConvierteEnValorPredeterminado() = errorDeCampo(
        """{"loteId":2,"tipo":"RIEGO","fecha":"2026-10-27","cantidad":1.0,"observacion":null}""", "observacion")

    @Test fun cantidadNoNumericaIndicaCampo() = errorDeCampo(
        """{"loteId":2,"tipo":"RIEGO","fecha":"2026-10-27","cantidad":"abc"}""", "cantidad")
}
