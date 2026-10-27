# AgroAPI-Logacho

Backend independiente del examen parcial II, Unidad 2, caso AgroSierra (versión A).

**Autor:** Richard Ismael Logacho Rivera.

Paquete raíz: `pe.upeu.agrosierra`. Kotlin, Java 21, Spring Boot 4.0.7, Maven Wrapper, H2 persistente en `data/` y migraciones Flyway. `data/` no se versiona. No usa la base Oracle ni archivos de PharmaMobil. La configuración `agro.datasource.*` impide que variables genéricas `SPRING_DATASOURCE_*` de otros proyectos cambien la conexión de esta aplicación.

Defina `AGRO_JWT_SECRET` en el entorno de ejecución con una clave privada aleatoria de al menos 32 bytes antes de arrancar. No la registre en Git ni en el README. Desde la carpeta del proyecto en Windows: `./mvnw.cmd spring-boot:run`. Flyway prepara el esquema y los datos de demostración en la primera ejecución. Para compilar y probar: `./mvnw.cmd test`.

**Alcance actual:** estructura, persistencia, datos semilla, reglas RN-01–RN-04, inicio de sesión y filtro JWT. La API HTTP de lotes y labores se incorpora en la siguiente rama; no declarar este backend como examen terminado hasta hacerlo.
