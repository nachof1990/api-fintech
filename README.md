# API Fintech

Trabajo práctico de la Diplomatura en Desarrollo de Software FinTech:
IA y Microservicios.

API REST para gestionar usuarios y cuentas bancarias con persistencia
en MySQL. Incluye consulta del dólar oficial mediante DolarAPI.

## Tecnologías

- Java 21.
- Spring Boot 4.1.1.
- Spring Web MVC.
- Spring Data JPA e Hibernate.
- MySQL.
- JUnit Jupiter, Mockito y herramientas de pruebas de Spring.
- springdoc-openapi 3.2.0 y Swagger UI.
- Maven Wrapper.

## Requisitos de ejecución

- JDK 21.
- MySQL iniciado y accesible en 127.0.0.1:3306.
- Base de datos `fintech`.
- Usuario MySQL `fintech_app` con acceso a esa base.
- Conexión a Internet para descargar dependencias y consultar DolarAPI.

La configuración utiliza estos permisos sobre `fintech.*`:
SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES, INDEX y ALTER.

La base puede crearse desde una conexión administradora de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS fintech
CHARACTER SET utf8mb4;
```

El usuario `fintech_app` y sus permisos deben prepararse localmente.
La contraseña no se incluye en el repositorio.

## Configuración

La aplicación utiliza `src/main/resources/application.properties`.

La conexión predeterminada es:

- URL: `jdbc:mysql://127.0.0.1:3306/fintech`
- Usuario: `fintech_app`
- Contraseña: variable de entorno `DB_PASSWORD`

La variable debe estar disponible en el proceso que ejecute la
aplicación o los tests.

Se utiliza `spring.jpa.hibernate.ddl-auto=update` para actualizar el
esquema durante el desarrollo y `spring.jpa.open-in-view=false`.

Proveedor de cotizaciones:

```properties
cotizacion.api.base-url=https://dolarapi.com
```

## Ejecutar desde IntelliJ

1. Clonar o descargar el repositorio y abrir la carpeta que contiene
   `pom.xml`.
2. Configurar el proyecto con JDK 21 y cargar las dependencias Maven.
3. En la configuración de ejecución de `ApiFintechApplication`,
   agregar la variable de entorno `DB_PASSWORD` con la contraseña local
   de `fintech_app`.
4. Mantener esa configuración local, sin guardarla como archivo del
   proyecto.
5. Ejecutar `ApiFintechApplication`.

La aplicación utiliza el puerto 8080.

## Ejecutar desde PowerShell

Abrir una terminal en la carpeta que contiene `pom.xml`.

Cargar la contraseña local sin escribirla como texto visible:

```powershell
$claveLocal = Read-Host "Contraseña de fintech_app" -AsSecureString
$env:DB_PASSWORD = ([System.Net.NetworkCredential]::new("", $claveLocal)).Password
```

Ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

La variable configurada en IntelliJ no se transfiere automáticamente
a una terminal independiente.

## Modelo de dominio

Un usuario puede tener varias cuentas. Cada cuenta tiene un único
usuario titular:

```text
Usuario (1) ───── (N) Cuenta
                       ├── CajaDeAhorro
                       └── CuentaCorriente
```

- `Cuenta` es abstracta.
- Las hijas implementan `retirar()` y `getTipo()`.
- La caja de ahorro no permite retirar más que el saldo disponible.
- La cuenta corriente permite utilizar su límite de descubierto.
- Ambas requieren saldo inicial no negativo.
- Las cuentas se crean en moneda ARS y estado ACTIVA.
- Los retiros requieren una cuenta activa.
- El saldo se modifica mediante operaciones del dominio, sin setter
  público.

La herencia se persiste con estrategia JOINED:

- `usuarios`
- `cuentas`
- `cajas_de_ahorro`
- `cuentas_corrientes`

La relación es unidireccional desde Cuenta hacia Usuario.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/usuarios` | Lista usuarios |
| POST | `/usuarios` | Crea un usuario |
| GET | `/usuarios/{id}` | Busca un usuario |
| GET | `/cuentas` | Lista ambos tipos de cuentas |
| POST | `/cuentas` | Crea una cuenta |
| GET | `/cuentas/{id}` | Busca una cuenta |
| GET | `/cotizaciones/dolar/oficial` | Consulta el dólar oficial |

## Crear usuarios y cuentas

Primero debe existir un usuario. El id obtenido al crearlo se utiliza
como `usuarioId` al crear una cuenta.

Ejemplo de usuario:

```json
{
  "nombre": "Ana",
  "apellido": "Perez",
  "email": "ana@example.com",
  "dni": "12345678",
  "fechaNacimiento": "1995-05-20"
}
```

Ejemplo de caja de ahorro, suponiendo que el usuario creado tiene id 1:

```json
{
  "tipo": "CAJA_DE_AHORRO",
  "numeroCuenta": "000001",
  "alias": "ana.ahorro.tp",
  "cbu": "0000000000000000000001",
  "saldoInicial": 1000.00,
  "usuarioId": 1
}
```

Para cuenta corriente, usar el tipo `CUENTA_CORRIENTE` y agregar
`limiteDescubierto` con un importe no negativo.

El id, la fecha de alta, la moneda y el estado no se envían al crear
una cuenta. Los asignan la base o la aplicación.

El POST exitoso devuelve 201 Created, el recurso creado y la cabecera
Location con su ruta de consulta.

## Validaciones y errores

Las respuestas de errores gestionados usan `ProblemDetail`.

- 400: datos inválidos, como id no positivo, CBU con formato incorrecto,
  saldo inicial negativo o límite de descubierto negativo.
- 404: cuenta o usuario inexistente.
- 409: CBU, alias o número de cuenta repetido, o conflicto de integridad
  detectado por la base.
- 503: no fue posible obtener una cotización completa.

El CBU se valida por formato de 22 dígitos. No se valida su dígito
verificador bancario.

CBU, alias y número tienen comprobaciones previas en el servicio y
restricciones de unicidad en MySQL.

## Cotización externa

Se consulta:

https://dolarapi.com/v1/dolares/oficial

La integración utiliza RestClient, un tiempo de conexión de 5 segundos
y un tiempo de espera de respuesta de 10 segundos.

Los fallos del cliente HTTP se tratan con un try/catch específico y una
excepción propia. Los precios y la fecha corresponden a lo informado
por el proveedor.

## Documentación interactiva

Con la aplicación iniciada:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Los endpoints se organizan en Usuarios, Cuentas y Cotizaciones.

## Tests

Ejecutar desde PowerShell con `DB_PASSWORD` configurada:

```powershell
.\mvnw.cmd test
```

También pueden ejecutarse desde IntelliJ.

La suite actual contiene 18 tests:

- 4 de lógica de cuentas.
- 8 de CuentaService.
- 4 de CotizacionService.
- 1 del controlador de cotización y su respuesta 503.
- 1 de carga del contexto de Spring.

Los tests de dominio, servicios y controlador no necesitan MySQL.
El test de contexto sí utiliza la configuración de conexión:
requiere MySQL iniciado y DB_PASSWORD disponible.

El test de contexto utiliza la base configurada y puede actualizar su
esquema; no configura una base aislada para pruebas.

Los tests de cotización utilizan respuestas simuladas y no dependen
de Internet ni de precios reales.

## Colección Postman

Archivo:

`postman/fintech.postman_collection.json`

Importarlo en Postman. La colección utiliza:

```text
baseUrl = http://localhost:8080
```

Las solicitudes se organizan en Usuarios, Cuentas y Cotizaciones.
Los nombres indican el código HTTP esperado.

Los ejemplos de cuentas requieren un usuario existente. Adaptar su
id cuando se utilice una base nueva.

Para repetir una creación exitosa de cuenta deben usarse CBU, alias
y número disponibles. Reutilizar identificadores registrados produce
409 Conflict.

## Decisiones de diseño

- Capas Controller, Service y Repository.
- Inyección de dependencias por constructor.
- BigDecimal para representar importes.
- Clase abstracta y polimorfismo para las reglas de cada tipo de cuenta.
- Un solo titular por cuenta como simplificación del dominio.
- DTO de entrada y salida para cuentas.
- Conversión a DTO dentro del servicio transaccional, conservando
  la relación LAZY y open-in-view=false.
- Manejo global de errores para mantener respuestas coherentes.

## Alcance y mejoras pendientes

Los depósitos y retiros están implementados como lógica del dominio;
no se exponen mediante endpoints.

No se implementaron los bonus PUT y DELETE de cuentas.

Como mejoras quedan la validación avanzada y unicidad de datos de
usuarios, el control completo de precisión y rango de importes y las
operaciones de transición entre estados de cuenta.