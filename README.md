# AgroAPI-Logacho

Backend independiente del examen parcial II, Unidad 2, caso AgroSierra (versión A).

**Autor:** Richard Ismael Logacho Rivera.

Paquete raíz: `pe.upeu.agrosierra`. Kotlin, Java 21, Spring Boot 4.0.7, Maven Wrapper, H2 persistente en `data/` y migraciones Flyway. `data/` no se versiona. No usa la base Oracle ni archivos de PharmaMobil. La configuración `agro.datasource.*` impide que variables genéricas `SPRING_DATASOURCE_*` de otros proyectos cambien la conexión de esta aplicación.

Defina `AGRO_JWT_SECRET` en el entorno de ejecución con una clave privada aleatoria de al menos 32 bytes antes de arrancar. No la registre en Git ni en el README. Desde la carpeta del proyecto en Windows: `./mvnw.cmd spring-boot:run`. Flyway prepara el esquema y los datos de demostración en la primera ejecución. Para compilar y probar: `./mvnw.cmd test`.

## Contrato REST

Todas las rutas están bajo `/api/v1`. `POST /auth/login` recibe `usuario` y `contrasena`; devuelve `token`, `usuario`, `rol` y `privilegios`. Las demás rutas exigen `Authorization: Bearer <token>`.

| Verbo | Ruta | Privilegio |
| --- | --- | --- |
| GET | `/lotes?pagina&tamanio&valle&estado` | `LOTE_LEER` |
| GET | `/lotes/{id}` | `LOTE_LEER` |
| GET | `/lotes/{id}/labores?pagina&tamanio` | `LABOR_LEER` |
| POST | `/labores` | `LABOR_CREAR` |
| PUT | `/labores/{id}` | `LABOR_EDITAR` |
| DELETE | `/labores/{id}` | `LABOR_ELIMINAR` |
| POST | `/lotes/{id}/cerrar-campania` | `CAMPANIA_CERRAR` |
| GET | `/usuarios/me` | Usuario autenticado |

`estado` es opcional y acepta `abierta` o `cerrada`. Resuelve el filtro por estado de RF-B02, aunque la tabla de rutas 4.3 solo enumera `pagina`, `tamanio` y `valle`. Los listados devuelven `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y `ultima`. Los errores devuelven `timestamp`, `status`, `error`, `message`, `path` y `validationErrors`.

Los privilegios se cargan de H2 en cada petición. `TECNICO` puede leer, crear y editar labores propias dentro de 24 horas; `JEFE_CAMPO` también elimina y cierra campañas; `ADMINISTRADOR` solo lee lotes y labores y gestiona usuarios (la ruta de gestión no forma parte de los nueve endpoints exigidos). La autorización se verifica en servicios; RN-01 a RN-04 también están en servicios.

La API y la seguridad se verifican con Maven y HTTP local. La aplicación AgroCampo y la entrega final siguen pendientes.
