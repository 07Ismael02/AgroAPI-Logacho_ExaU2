package pe.upeu.agrosierra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import pe.upeu.agrosierra.repositorio.LaborRepositorio
import pe.upeu.agrosierra.repositorio.LoteRepositorio
import pe.upeu.agrosierra.repositorio.UsuarioRepositorio

@SpringBootTest
@TestPropertySource(properties = [
    "agro.datasource.url=jdbc:h2:mem:agro_semilla_test;DB_CLOSE_DELAY=-1",
    "agro.jwt.secret=clave-exclusiva-de-pruebas-AgroSierra-2026"
])
class PersistenciaSemillaTest {
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var lotes: LoteRepositorio
    @Autowired lateinit var labores: LaborRepositorio
    @Autowired lateinit var usuarios: UsuarioRepositorio

    private fun contar(sql: String): Long = jdbc.queryForObject(sql, Long::class.java)!!

    @Test fun semillaCumpleLasCantidadesDelAnexo() {
        assertEquals(8, contar("SELECT COUNT(*) FROM lote"))
        assertEquals(3, contar("SELECT COUNT(DISTINCT valle) FROM lote"))
        assertTrue(contar("SELECT COUNT(*) FROM lote WHERE campania_cerrada = TRUE") >= 2)
        assertEquals(20, contar("SELECT COUNT(*) FROM labor"))
        assertEquals(3, contar("SELECT COUNT(*) FROM usuario"))
        assertEquals(0, contar("""
            SELECT COUNT(*) FROM lote l WHERE l.campania_cerrada = FALSE
            AND (SELECT COUNT(*) FROM labor x WHERE x.lote_id = l.id) < 2
        """.trimIndent()))
        assertTrue(labores.tieneCosecha(1))
        assertFalse(labores.tieneCosecha(2))
    }

    @Test fun repositoriosLeenDominioYPermisosExactos() {
        assertEquals("ALT-001", lotes.buscar(1)!!.codigo)
        assertEquals(3, labores.contarDeLote(1))
        assertEquals("TECNICO", usuarios.buscarPorUsername("tecnico1")!!.rol)
        assertEquals(setOf("LOTE_LEER", "LABOR_LEER", "LABOR_CREAR", "LABOR_EDITAR"),
            usuarios.buscarPorUsername("tecnico1")!!.privilegios)
        assertEquals(setOf("LOTE_LEER", "LABOR_LEER", "LABOR_CREAR", "LABOR_EDITAR",
            "LABOR_ELIMINAR", "CAMPANIA_CERRAR"), usuarios.buscarPorUsername("jefe1")!!.privilegios)
        assertEquals(setOf("LOTE_LEER", "LABOR_LEER", "USUARIO_GESTIONAR"),
            usuarios.buscarPorUsername("admin")!!.privilegios)
    }

    @Test fun contrasenasSemillaEstanCifradasYVerificables() {
        val encoder = BCryptPasswordEncoder()
        listOf("tecnico1", "jefe1", "admin").forEach { nombre ->
            val hash = usuarios.buscarPorUsername(nombre)!!.passwordHash
            assertTrue(hash.startsWith("\$2a\$"))
            assertTrue(encoder.matches("Agro2026*", hash))
            assertFalse(hash == "Agro2026*")
        }
    }
}
