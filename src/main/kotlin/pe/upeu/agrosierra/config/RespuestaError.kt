package pe.upeu.agrosierra.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset

data class CuerpoError(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val validationErrors: Map<String, String>?,
)

@Component
class RespuestaError(private val mapper: ObjectMapper) {
    fun cuerpo(estado: HttpStatus, mensaje: String, ruta: String, campos: Map<String, String>? = null) =
        CuerpoError(LocalDateTime.now(ZoneOffset.UTC).toString(), estado.value(),
            estado.reasonPhrase, mensaje, ruta, campos)

    fun escribir(request: HttpServletRequest, response: HttpServletResponse, estado: HttpStatus, mensaje: String) {
        response.status = estado.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        mapper.writeValue(response.writer, cuerpo(estado, mensaje, request.requestURI))
    }
}
