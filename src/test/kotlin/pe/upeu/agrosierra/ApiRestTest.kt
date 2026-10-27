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
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.seguridad.Usuario

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "agro.datasource.url=jdbc:h2:mem:agro_api_test;DB_CLOSE_DELAY=-1",
    "agro.jwt.secret=clave-exclusiva-de-pruebas-AgroSierra-2026"
])
class ApiRestTest {
    @Value("\${local.server.port}") private var puerto: Int = 0
    private val json = ObjectMapper()
    private data class Respuesta(val codigo: Int, val cuerpo: String)

    private fun pedir(metodo: String, ruta: String, token: String? = null, cuerpo: String? = null): Respuesta {
        val conexion = URI("http://127.0.0.1:$puerto$ruta").toURL().openConnection() as HttpURLConnection
        conexion.requestMethod = metodo
        conexion.connectTimeout = 3000
        conexion.readTimeout = 3000
        if (token != null) conexion.setRequestProperty("Authorization", "Bearer $token")
        if (cuerpo != null) {
            conexion.doOutput = true
            conexion.setRequestProperty("Content-Type", "application/json")
            conexion.outputStream.use { it.write(cuerpo.toByteArray()) }
        }
        val codigo = conexion.responseCode
        val texto = (if (codigo >= 400) conexion.errorStream else conexion.inputStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        conexion.disconnect()
        return Respuesta(codigo, texto)
    }

    private fun token(usuario: String): String {
        val r = pedir("POST", "/api/v1/auth/login", cuerpo =
            """{"usuario":"$usuario","contrasena":"Agro2026*"}""")
        assertEquals(200, r.codigo)
        return json.readTree(r.cuerpo).path("token").asText()
    }

    @Test fun paginacionFiltrosYDetalle() {
        val tecnico = token("tecnico1")
        val r = pedir("GET", "/api/v1/lotes?pagina=0&tamanio=2&valle=Valle%20Alto&estado=abierta", tecnico)
        assertEquals(200, r.codigo)
        val pagina = json.readTree(r.cuerpo)
        assertEquals(2, pagina.path("contenido").size())
        assertEquals(3, pagina.path("totalElementos").asInt())
        assertEquals(2, pagina.path("totalPaginas").asInt())
        assertFalse(pagina.path("ultima").asBoolean())
        assertEquals(200, pedir("GET", "/api/v1/lotes/1", tecnico).codigo)
        assertEquals(404, pedir("GET", "/api/v1/lotes/99999", tecnico).codigo)
        val labores = pedir("GET", "/api/v1/lotes/1/labores?pagina=0&tamanio=2", tecnico)
        assertEquals(200, labores.codigo)
        assertEquals(2, json.readTree(labores.cuerpo).path("contenido").size())
        assertEquals(400, pedir("GET", "/api/v1/lotes?estado=otro", tecnico).codigo)
    }

    @Test fun privilegiosDeTresRolesYUsuarioActual() {
        val tecnico = token("tecnico1")
        val jefe = token("jefe1")
        val admin = token("admin")
        assertEquals(200, pedir("GET", "/api/v1/usuarios/me", tecnico).codigo)
        assertEquals(200, pedir("GET", "/api/v1/lotes", admin).codigo)
        assertEquals(403, pedir("DELETE", "/api/v1/labores/99999", tecnico).codigo)
        assertEquals(403, pedir("POST", "/api/v1/lotes/2/cerrar-campania", tecnico).codigo)
        val cuerpo = """{"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","cantidad":1.0,"observacion":"Prueba"}"""
        assertEquals(403, pedir("POST", "/api/v1/labores", admin, cuerpo).codigo)
        assertEquals(404, pedir("DELETE", "/api/v1/labores/99999", jefe).codigo)
        assertEquals(401, pedir("GET", "/api/v1/usuarios/me").codigo)
        val tokenVencido = JwtService("clave-exclusiva-de-pruebas-AgroSierra-2026", json,
            Clock.fixed(Instant.now().minusSeconds(7200), ZoneOffset.UTC)).emitir(
                Usuario(1, "tecnico1", "", true, "TECNICO", setOf("LOTE_LEER")))
        assertEquals(401, pedir("GET", "/api/v1/lotes", tokenVencido).codigo)
    }

    @Test fun operacionesRestYReglasDeNegocio() {
        val tecnico = token("tecnico1")
        val jefe = token("jefe1")
        val base = """{"loteId":1,"tipo":"RIEGO","fecha":"2026-10-27","cantidad":1.0,"observacion":"API test"}"""
        val creado = pedir("POST", "/api/v1/labores", tecnico, base)
        assertEquals(201, creado.codigo)
        val id = json.readTree(creado.cuerpo).path("id").asLong()
        assertTrue(id > 0)
        assertEquals(200, pedir("PUT", "/api/v1/labores/$id", tecnico, base).codigo)
        assertEquals(409, pedir("POST", "/api/v1/labores", tecnico,
            base.replace("\"loteId\":1", "\"loteId\":4")).codigo)
        val dosis = pedir("POST", "/api/v1/labores", tecnico,
            base.replace("RIEGO", "FUMIGACION").replace("\"cantidad\":1.0", "\"cantidad\":99.0"))
        assertEquals(400, dosis.codigo)
        assertTrue(json.readTree(dosis.cuerpo).path("validationErrors").has("cantidad"))
        assertEquals(409, pedir("POST", "/api/v1/lotes/2/cerrar-campania", jefe).codigo)
        assertEquals(204, pedir("DELETE", "/api/v1/labores/$id", jefe).codigo)
    }
}
