package pe.upeu.agrosierra.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import javax.sql.DataSource

/** Aislada de SPRING_DATASOURCE_* heredadas de otros proyectos del equipo. */
@Configuration
class AgroDataSourceConfig {
    @Bean
    fun jwtObjectMapper(): ObjectMapper = ObjectMapper()

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun dataSource(
        @Value("\${agro.datasource.url}") url: String,
        @Value("\${agro.datasource.username}") usuario: String,
        @Value("\${agro.datasource.password:}") clave: String,
    ): DataSource = HikariDataSource().apply {
        jdbcUrl = url
        username = usuario
        password = clave
        driverClassName = "org.h2.Driver"
    }
}
