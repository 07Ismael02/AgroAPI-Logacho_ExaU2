package pe.upeu.agrosierra.dominio

enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }

data class Labor(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val cantidad: Double,
    val observacion: String,
    val pendiente: Boolean,
)
