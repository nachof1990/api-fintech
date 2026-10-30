# API Fintech

Trabajo práctico de la Diplomatura en Desarrollo de Software FinTech:
IA y Microservicios.

API REST desarrollada con Java y Spring Boot para gestionar usuarios
y cuentas bancarias, persistir información en MySQL y consultar la
cotización del dólar oficial mediante una API externa.

## Tecnologías

- Java 21.
- Spring Boot 4.1.1.
- Spring Web MVC.
- Spring Data JPA e Hibernate.
- MySQL.
- Maven Wrapper.
- JUnit Jupiter y Mockito.
- MockRestServiceServer y MockMvc.
- springdoc-openapi 3.2.0 y Swagger UI.

## Requisitos

- JDK 21.
- MySQL iniciado y accesible en `127.0.0.1:3306`.
- Base de datos `fintech`.
- Usuario MySQL `fintech_app` con acceso a esa base.
- Variable de entorno `DB_PASSWORD` con la contraseña local del usuario.
- Conexión a Internet para descargar dependencias y consultar DolarAPI.

La base puede crearse desde una conexión administradora de MySQL:

```sql
CREATE DATABASE IF NOT EXISTS fintech
CHARACTER SET utf8mb4;
```

El usuario `fintech_app` debe prepararse localmente con estos permisos
sobre `fintech.*`:

```text
SELECT, INSERT, UPDATE, DELETE, CREATE, REFERENCES, INDEX, ALTER
```

La contraseña real no se incluye en el repositorio.

## Descargar el proyecto

```powershell
git clone https://github.com/nachof1990/api-fintech.git
cd api-fintech
```

## Configuración

El archivo de configuración es:

```text
src/main/resources/application.properties
```

La conexión utiliza:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/fintech
spring.datasource.username=fintech_app
spring.datasource.password=${DB_PASSWORD}
```

Otras decisiones de configuración:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
cotizacion.api.base-url=https://dolarapi.com
```

Hibernate actualiza el esquema durante el desarrollo. La conversión
de cuentas a DTO se realiza dentro del servicio transaccional.

## Ejecutar desde IntelliJ IDEA

1. Abrir la carpeta que contiene `pom.xml`.
2. Seleccionar JDK 21 y cargar las dependencias Maven.
3. Abrir la configuración de ejecución de `ApiFintechApplication`.
4. Agregar la variable de entorno `DB_PASSWORD` con la contraseña local
   de `fintech_app`.
5. Mantener desmarcada la opción `Store as project file` para esa
   configuración que contiene la contraseña.
6. Asegurarse de que MySQL esté iniciado.
7. Ejecutar `ApiFintechApplication`.

La aplicación utiliza el puerto `8080`.

## Ejecutar desde PowerShell

Abrir una terminal en la raíz del proyecto.

Cargar la contraseña mediante una entrada oculta:

```powershell
$claveLocal = Read-Host "Contraseña de fintech_app" -AsSecureString
$env:DB_PASSWORD = ([System.Net.NetworkCredential]::new("", $claveLocal)).Password
```

Iniciar la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

La variable configurada en IntelliJ no se transfiere automáticamente
a una terminal independiente. Debe estar disponible en el proceso
que ejecute la aplicación o los tests.

## Estructura del proyecto

```text
api-fintech/
├── .mvn/
├── postman/
│   └── fintech.postman_collection.json
├── src/
│   ├── main/
│   │   ├── java/com/diplomatura/apifintech/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── ApiFintechApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/diplomatura/apifintech/
│           ├── controller/
│           ├── model/
│           ├── service/
│           └── ApiFintechApplicationTests.java
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

| Paquete | Responsabilidad |
|---|---|
| `config` | Configuración del cliente HTTP de cotizaciones |
| `controller` | Recepción de solicitudes y respuestas HTTP |
| `dto` | Datos de entrada y salida de la API |
| `exception` | Excepciones propias y manejo global de errores |
| `model` | Entidades, herencia y reglas del dominio |
| `repository` | Acceso a la base de datos mediante JPA |
| `service` | Coordinación de operaciones y validaciones |

## Modelo de dominio

Un usuario puede tener varias cuentas. Cada cuenta pertenece a un
único titular:

```text
Usuario (1) ───── (N) Cuenta
                       ├── CajaDeAhorro
                       └── CuentaCorriente
```

La relación es unidireccional desde `Cuenta` hacia `Usuario`.

`Cuenta` es una clase abstracta. Sus hijas implementan `retirar()`
y `getTipo()`, aplicando polimorfismo.

### Caja de ahorro

- No permite retirar más que el saldo disponible.
- No tiene límite de descubierto.
- En la respuesta, `limiteDescubierto` es `null`.

### Cuenta corriente

- Permite retirar utilizando el saldo y el límite de descubierto.
- Puede quedar con saldo negativo después de un retiro, hasta el
  límite permitido.
- El saldo inicial debe ser no negativo.

### Reglas comunes

- Importes representados con `BigDecimal`.
- Cuenta nueva en moneda `ARS` y estado `ACTIVA`.
- Fecha de alta asignada por la aplicación.
- Titular existente obligatorio.
- Retiros permitidos únicamente en cuentas activas.
- Saldo privado, sin setter público.
- CBU, alias y número de cuenta únicos.
- Actualización del alias sin modificar los demás datos.
- Eliminación física permitida únicamente con saldo cero.

Los estados definidos son `ACTIVA`, `BLOQUEADA` y `CERRADA`.
No se implementaron operaciones para cambiar entre esos estados.

## Persistencia

La herencia utiliza la estrategia JPA `JOINED`.

Tablas:

- `usuarios`
- `cuentas`
- `cajas_de_ahorro`
- `cuentas_corrientes`

Los importes se almacenan en columnas `DECIMAL(19,2)`.

La tabla `cuentas` tiene restricciones únicas para CBU, alias y número.
Estas restricciones complementan las comprobaciones del servicio y
protegen frente a solicitudes simultáneas.

No hay carga automática de datos iniciales. Los usuarios y cuentas
se crean mediante la API.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/usuarios` | Lista usuarios |
| POST | `/usuarios` | Crea un usuario |
| GET | `/usuarios/{id}` | Busca un usuario |
| GET | `/cuentas` | Lista cuentas |
| POST | `/cuentas` | Crea una cuenta |
| GET | `/cuentas/{id}` | Busca una cuenta |
| PUT | `/cuentas/{id}` | Actualiza el alias |
| DELETE | `/cuentas/{id}` | Elimina una cuenta con saldo cero |
| GET | `/cotizaciones/dolar/oficial` | Consulta el dólar oficial |

## Ejemplos de uso

Dirección base:

```text
http://localhost:8080
```

### Crear un usuario

`POST /usuarios`

```json
{
  "nombre": "Ana",
  "apellido": "Perez",
  "email": "ana@example.com",
  "dni": "12345678",
  "fechaNacimiento": "1995-05-20"
}
```

Respuesta esperada: `201 Created`, con el usuario creado y la cabecera
`Location`.

El id recibido se utiliza como `usuarioId` al crear cuentas.
Los ejemplos siguientes suponen que el usuario tiene id `1`.

### Crear una caja de ahorro

`POST /cuentas`

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

### Crear una cuenta corriente

`POST /cuentas`

```json
{
  "tipo": "CUENTA_CORRIENTE",
  "numeroCuenta": "000002",
  "alias": "ana.corriente.tp",
  "cbu": "0000000000000000000002",
  "saldoInicial": 2000.00,
  "usuarioId": 1,
  "limiteDescubierto": 500.00
}
```

El límite no se suma al saldo: ambos valores se devuelven por separado.

Los datos de estos ejemplos son ficticios. El id, la fecha de alta,
la moneda y el estado no se envían al crear una cuenta.

Ambas creaciones exitosas devuelven `201 Created`, los datos de la
cuenta y una cabecera `Location` con su ruta de consulta.

### Actualizar una cuenta

`PUT /cuentas/{id}`

Reemplazar `{id}` por el identificador de la cuenta.

```json
{
  "alias": "ana.alias.actualizado"
}
```

Respuesta esperada: `200 OK`, con los datos de la cuenta actualizada.

La representación editable contiene únicamente el alias:

- Puede conservarse el alias actual.
- El alias no puede ser nulo ni estar en blanco.
- No puede pertenecer a otra cuenta.
- No se modifican saldo, CBU, número ni titular.

### Eliminar una cuenta

`DELETE /cuentas/{id}`

La solicitud no lleva cuerpo.

- Saldo cero: `204 No Content`, sin cuerpo.
- Saldo distinto de cero: `409 Conflict`.
- Cuenta inexistente: `404 Not Found`.

El borrado es físico. Elimina la cuenta, pero conserva al usuario y sus
otras cuentas. Repetir el DELETE de una cuenta eliminada devuelve 404.

## Validaciones y errores

Las respuestas de errores gestionados utilizan `ProblemDetail`.

| Estado | Situaciones |
|---|---|
| 400 Bad Request | Id no positivo, datos obligatorios ausentes, CBU con formato inválido, alias en blanco, saldo inicial o descubierto negativos |
| 404 Not Found | Cuenta o usuario inexistente |
| 409 Conflict | Identificadores duplicados, eliminación con saldo distinto de cero o conflicto de integridad de datos |
| 503 Service Unavailable | Fallo al obtener la cotización o respuesta incompleta del proveedor |

El CBU se valida por formato de 22 dígitos. No se valida su dígito
verificador bancario.

Los identificadores bancarios se reciben como texto para conservar
sus ceros iniciales.

## Cotización externa

Se consulta el dólar oficial mediante DolarAPI:

https://dolarapi.com/v1/dolares/oficial

La respuesta incluye:

- Moneda y mercado.
- Nombre de la cotización.
- Precio de compra.
- Precio de venta.
- Fecha de actualización del proveedor.

La integración utiliza `RestClient` con:

- Tiempo de conexión de 5 segundos.
- Tiempo de espera de respuesta de 10 segundos.

Un `try/catch` específico transforma los fallos del cliente HTTP en
`CotizacionNoDisponibleException`. El manejador global responde con 503.

Los precios y la fecha corresponden al proveedor y pueden cambiar
entre consultas. La cotización no se persiste en MySQL.

## Swagger y OpenAPI

Con la aplicación iniciada:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

La documentación organiza los nueve endpoints en:

- Usuarios.
- Cuentas.
- Cotizaciones.

Incluye descripciones, parámetros, cuerpos de entrada y respuestas
de éxito y error. Los POST documentan la cabecera `Location`.

## Tests

La suite contiene 26 tests:

| Grupo | Cantidad |
|---|---:|
| Lógica de caja de ahorro y cuenta corriente | 4 |
| Consulta y creación de cuentas | 8 |
| Actualización y eliminación de cuentas | 8 |
| Servicio de cotización | 4 |
| Controlador de cotización y respuesta 503 | 1 |
| Carga del contexto de Spring | 1 |
| Total | 26 |

Se utilizan entidades reales y dependencias simuladas según el
escenario de prueba.

Los tests comprueban, entre otros casos:

- Retiros con saldo y descubierto.
- Búsquedas inexistentes.
- Creación de ambos tipos de cuenta.
- Solicitudes rechazadas que no deben guardarse.
- Duplicados de CBU, alias y número.
- Actualización de alias y conservación del alias propio.
- Eliminación con saldo cero y rechazo de cuentas con saldo.
- Respuestas externas válidas, incompletas, vacías y fallidas.
- Conversión del error de cotización en HTTP 503.

### Ejecutar desde IntelliJ

Ejecutar todos los tests del paquete:

```text
com.diplomatura.apifintech
```

La configuración debe incluir los subpaquetes y la variable
`DB_PASSWORD`.

### Ejecutar desde PowerShell

Con MySQL iniciado y `DB_PASSWORD` configurada en esa terminal:

```powershell
.\mvnw.cmd test
```

Para ejecutar la verificación de construcción:

```powershell
.\mvnw.cmd verify
```

Los tests de dominio, servicios y controlador no necesitan MySQL.
El test de contexto sí utiliza la conexión configurada y puede
actualizar el esquema con `ddl-auto=update`; no tiene una base aislada.

Las pruebas de cotización utilizan respuestas simuladas y no necesitan
Internet ni precios reales.

## Colección Postman

Archivo de entrega:

```text
postman/fintech.postman_collection.json
```

Importar el archivo en Postman. La colección se organiza en las carpetas:

- Usuarios.
- Cuentas.
- Cotizaciones.

Variable de colección:

```text
baseUrl = http://localhost:8080
```

Los nombres de las solicitudes indican el código HTTP esperado.

### Preparación de los datos

En una base nueva, crear primero un usuario y utilizar el id recibido
en las solicitudes de cuentas.

Las consultas de recursos existentes requieren que el id solicitado
esté registrado.

Para repetir una creación exitosa deben usarse CBU, alias y número
disponibles. Reutilizar datos registrados produce 409 Conflict.

En las pruebas de alias o número duplicado, los otros identificadores
deben estar disponibles para que el rechazo corresponda al campo
que se quiere comprobar.

### Circuito de PUT y DELETE

1. Crear una cuenta de prueba con saldo cero.
2. Guardar el id recibido en la variable de colección `cuentaPruebaId`.
3. Actualizar su alias y consultarla.
4. Comprobar que conservar el alias propio devuelve 200.
5. Probar alias ocupado, alias vacío y cuenta inexistente.
6. Comprobar el rechazo al eliminar una cuenta con saldo.
7. Eliminar la cuenta de prueba: debe devolver 204.
8. Consultarla y repetir su eliminación: deben devolver 404.

Al terminar el circuito, `cuentaPruebaId` identifica una cuenta eliminada.
Para repetirlo, crear otra cuenta de prueba y actualizar la variable.

## Decisiones de diseño

- Separación de responsabilidades por capas.
- Inyección de dependencias por constructor.
- Clase abstracta y polimorfismo para los tipos de cuenta.
- Un titular por cuenta como simplificación del dominio.
- `BigDecimal` para importes.
- DTO de entrada y salida para cuentas.
- Conversión al DTO dentro de métodos transaccionales.
- Relación con el titular configurada como LAZY.
- `open-in-view=false`.
- Excepciones específicas y manejo global con `ProblemDetail`.
- Restricciones de unicidad en MySQL además de validaciones del servicio.
- Actualización limitada al alias para preservar los demás datos.
- Borrado físico permitido solamente con saldo cero.
- Consulta externa con tiempos de espera y tratamiento específico de fallos.

## Alcance

Se implementaron los endpoints obligatorios y los bonus PUT y DELETE
de cuentas.

PUT permite actualizar el alias. DELETE elimina físicamente una cuenta
únicamente cuando su saldo es cero.

Los depósitos y retiros están implementados como lógica del dominio
y cubiertos por pruebas; no se exponen mediante endpoints.