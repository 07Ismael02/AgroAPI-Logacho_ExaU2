package pe.upeu.agrosierra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean fun passwordEncoder() = BCryptPasswordEncoder()

    // Evita la cuenta y contraseña autogeneradas de Spring: solo existe el login propio sobre H2.
    @Bean fun withoutDefaultUser(): UserDetailsService = UserDetailsService {
        throw UsernameNotFoundException("Cuenta gestionada por AgroAPI")
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtFilter: JwtAuthenticationFilter,
                            errores: RespuestaError): SecurityFilterChain =
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/login").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { request, response, _ ->
                    errores.escribir(request, response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión")
                }
                it.accessDeniedHandler { request, response, _ ->
                    errores.escribir(request, response, HttpStatus.FORBIDDEN, "Privilegio insuficiente")
                }
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
}
