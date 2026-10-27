package pe.upeu.agrosierra.servicio

import org.springframework.http.HttpStatus

class ErrorRegla(
    val estado: HttpStatus,
    override val message: String,
    val campo: String? = null,
) : RuntimeException(message)
