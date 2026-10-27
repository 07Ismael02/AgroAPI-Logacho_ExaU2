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

La API y la seguridad se verifican con Maven y HTTP local.

## Ejecución

Requisitos: Java 21. En PowerShell, desde la carpeta del proyecto:

```powershell
$b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
$env:AGRO_JWT_SECRET = [Convert]::ToBase64String($b)
.\mvnw.cmd spring-boot:run      # http://localhost:8080 (emulador Android: http://10.0.2.2:8080)
.\mvnw.cmd test                 # pruebas
```

Usuarios de prueba (anexo 10 del examen): `tecnico1`, `jefe1` y `admin`.

## Estructura de paquetes (`pe.upeu.agrosierra`)

```
config            Configuración de seguridad, datasource H2 y JWT
dominio           Entidades y reglas del dominio
repositorio/jdbc  Acceso a H2 con JDBC
seguridad         Login, emisión y validación de JWT, carga de privilegios
servicio          Casos de uso con @PreAuthorize y reglas RN-01 a RN-04
web, web/dto      Controladores REST, DTO y manejo uniforme de errores
resources/db/migration   Flyway: esquema (V1) y datos semilla (V2)
```

## Arquitectura

Controladores REST delgados → servicios con autorización por privilegio y reglas de negocio → repositorios JDBC sobre H2 persistente. Los privilegios se asignan por rol en la base (`rol_privilegio`), por lo que ningún controlador compara nombres de rol.

## Roles y privilegios

| Rol | Privilegios |
| --- | --- |
| TECNICO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR |
| JEFE_CAMPO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR, LABOR_ELIMINAR, CAMPANIA_CERRAR |
| ADMINISTRADOR | LOTE_LEER, LABOR_LEER, USUARIO_GESTIONAR |

| Código | Caso |
| --- | --- |
| 400 | Validación (`validationErrors` por campo) |
| 401 | Token ausente, alterado o vencido |
| 403 | Autenticado sin el privilegio |
| 404 | Recurso inexistente |
| 409 | Regla de negocio incumplida |

## Colección API

`docs/AgroAPI_Logacho.postman_collection.json` (Postman v2.1) contiene las nueve rutas y ejemplos de 400, 401 y 403. `docs/api-coleccion-ejecucion.txt` registra una ejecución real de esas peticiones contra esta versión.
