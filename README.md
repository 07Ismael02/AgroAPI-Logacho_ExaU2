# AgroAPI-Logacho

Backend independiente del examen parcial II, Unidad 2, caso AgroSierra (versión A).

**Autor:** Richard Ismael Logacho Rivera.

Paquete raíz: `pe.upeu.agrosierra`. Kotlin, Java 21, Spring Boot 4.0.7, Maven Wrapper, H2 persistente en `data/` y migraciones Flyway. `data/` no se versiona. No usa la base Oracle ni archivos de PharmaMobil.

Desde la carpeta del proyecto en Windows: `./mvnw.cmd spring-boot:run`. Flyway prepara el esquema y los datos de demostración en la primera ejecución. Para compilar y probar: `./mvnw.cmd test`.

**Alcance de la fase 1:** estructura, persistencia, datos semilla y reglas RN-01–RN-04 con pruebas. La API HTTP completa, JWT y autorización del PDF se incorporarán en la siguiente fase; no declarar este backend como examen terminado hasta hacerlo.
