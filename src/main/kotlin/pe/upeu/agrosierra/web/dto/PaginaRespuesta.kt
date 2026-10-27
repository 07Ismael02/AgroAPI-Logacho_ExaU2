package pe.upeu.agrosierra.web.dto

data class PaginaRespuesta<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean,
)
