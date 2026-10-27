package pe.upeu.agrosierra.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import pe.upeu.agrosierra.servicio.ErrorRegla

@RestControllerAdvice
class ManejadorDeErrores(private val errores: RespuestaError) {
    @ExceptionHandler(ErrorRegla::class)
    fun regla(e: ErrorRegla, request: HttpServletRequest): ResponseEntity<CuerpoError> =
        ResponseEntity.status(e.estado).body(errores.cuerpo(e.estado, e.message, request.requestURI,
            e.campo?.let { mapOf(it to e.message) }))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validacion(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<CuerpoError> {
        val campos = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Valor inválido") }
        return ResponseEntity.badRequest().body(
            errores.cuerpo(HttpStatus.BAD_REQUEST, "Datos inválidos", request.requestURI, campos))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun cuerpoInvalido(e: HttpMessageNotReadableException, request: HttpServletRequest): ResponseEntity<CuerpoError> =
        ResponseEntity.badRequest().body(errores.cuerpo(HttpStatus.BAD_REQUEST, "JSON inválido", request.requestURI))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun parametroInvalido(e: MethodArgumentTypeMismatchException, request: HttpServletRequest): ResponseEntity<CuerpoError> =
        ResponseEntity.badRequest().body(errores.cuerpo(HttpStatus.BAD_REQUEST, "Parámetro inválido",
            request.requestURI, mapOf(e.name to "Valor inválido")))

    @ExceptionHandler(Exception::class)
    fun inesperado(e: Exception, request: HttpServletRequest): ResponseEntity<CuerpoError> {
        if (e is AccessDeniedException || e is AuthenticationException) throw e
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            errores.cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", request.requestURI))
    }
}
