package pe.upeu.agrosierra.repositorio

import pe.upeu.agrosierra.seguridad.Usuario

interface UsuarioRepositorio {
    fun buscarPorId(id: Long): Usuario?
    fun buscarPorUsername(username: String): Usuario?
}
