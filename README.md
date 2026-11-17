# SIGC · Sistema Integral de Gestión de Concesionaria

Trabajo Práctico de Programación 2 · IFES · Análisis de Sistemas

**Integrantes:** Thiago Vecchi, Franco Araneda, Tiago Silenzi y Luz Araneda.

## Descripción

SIGC resuelve la gestión diaria de una concesionaria de vehículos: qué hay en el inventario, a quién se vendió cada unidad, quién la vendió y quién la probó. Evoluciona el sistema de consola de Programación 1 hacia una aplicación web con Maven, Spring Boot, Spring MVC, Thymeleaf, HTML, CSS y Bootstrap 5, persistencia con DataNucleus JDO y MySQL.

**Entidades persistentes:** `Vehiculo` (con `Auto`, `Moto` y `Camion`), `Persona` (con `Cliente` y `Vendedor`), `Venta` y `TestDrive`.

**Relaciones:** un cliente tiene muchas ventas y muchos test drives (1:N); un vendedor tiene muchas ventas (1:N); un vehículo tiene muchos test drives (1:N) y a lo sumo una venta (1:0..1). La relación uno a muchos `Cliente`–`Venta` está declarada en JDO de los dos lados (`Cliente.compras` con `mappedBy` y `Venta.cliente`). Detalle en [docs/MODELO_DATOS.md](docs/MODELO_DATOS.md).

**Operaciones propias del negocio:** vender un vehículo (solo si está disponible, una única vez y de forma atómica) y registrar/finalizar un test drive (el vehículo pasa a `EN_TEST_DRIVE` y vuelve a `DISPONIBLE`).

Se conserva de la versión de consola la jerarquía `Vehiculo → Auto, Moto, Camion`, los estados `DISPONIBLE`, `VENDIDO` y `EN_TEST_DRIVE`, la excepción `VehiculoNoDisponibleException` y la interfaz `ITransaccionable`.

> **Estado:** vehículos, clientes, vendedores, ventas, test drives, dashboard y reportes. Ver [docs/ESTADO_Y_PENDIENTES.md](docs/ESTADO_Y_PENDIENTES.md). Compilado y verificado en Windows 11 con JDK 17 y MySQL 8.4: `mvn clean verify` (44 pruebas unitarias) y `mvn -Pmysql-it verify` (7 pruebas de integración con MySQL y JDO) pasan, y la aplicación se recorrió completa en ejecución. Qué se comprobó y qué no: [docs/VERIFICACION.md](docs/VERIFICACION.md) e [docs/INFORME_CORRECCIONES.md](docs/INFORME_CORRECCIONES.md).

## Funcionalidades

- Registrar, editar y dar de baja (baja lógica y reactivación) autos, motos y camiones, con fotografía.
- Listar en tarjetas o tabla; buscar por marca o modelo; filtrar por tipo, estado, año y precio; ordenar por precio.
- Ver la ficha completa de cada vehículo.
- Consultar el inventario con estadísticas (total, disponibles, vendidos, en test drive, precio promedio).
- Dashboard con totales reales, últimas operaciones y ventas por vendedor y por mes.
- Registrar y administrar clientes y vendedores (DNI único, búsqueda, baja lógica, historial).
- Vender un vehículo (una sola vez, de forma atómica) e imprimir el comprobante; filtrar ventas por cliente, vendedor y fechas.
- Iniciar y finalizar test drives (el vehículo pasa a `EN_TEST_DRIVE` y vuelve a `DISPONIBLE`).
- Reportes de ventas por vendedor, mes y tipo de vehículo, con rango de fechas.

## Requisitos

| Componente | Versión |
|---|---|
| JDK | 17 |
| Maven | 3.6.3 o posterior |
| MySQL Server | 8.4 LTS |

Se necesita conexión a Internet la primera vez que se compila, porque Maven descarga las dependencias.

## Ejecutar desde cero (Windows / PowerShell)

Todos los comandos se escriben en **PowerShell**: tecla Windows, escribir `powershell` y presionar Enter.

### Paso 0 · Instalar las herramientas (una sola vez por computadora)

1. **JDK 17.** Descargar el instalador `.msi` de Eclipse Temurin 17 (LTS) para Windows x64 desde <https://adoptium.net>. En la pantalla de componentes, activar **Add to PATH** y **Set JAVA_HOME variable**.
2. **Maven.** Descargar el *Binary zip archive* desde <https://maven.apache.org/download.cgi> y descomprimirlo, por ejemplo en `C:\Program Files\Apache\maven`. Agregar su carpeta `bin` (`C:\Program Files\Apache\maven\bin`) a la variable `Path`: menú Inicio → "Editar las variables de entorno del sistema" → **Variables de entorno** → en *Variables del sistema* elegir `Path` → **Editar** → **Nuevo**.
3. **MySQL Server 8.4 LTS.** Descargar el instalador *Windows (x86, 64-bit), MSI Installer* de la versión 8.4 LTS desde <https://dev.mysql.com/downloads/mysql/>. Al terminar se abre **MySQL Configurator**. Dejar el puerto **3306**, definir una contraseña para `root` y dejar MySQL configurado como **servicio de Windows** que arranca con el sistema. El nombre por defecto del servicio es `MySQL84`. Esa contraseña de `root` hay que recordarla; no se escribe en ningún archivo del proyecto.

Después de instalar, **cerrar y volver a abrir PowerShell**, para que tome el `Path` nuevo, y comprobar:

```powershell
java -version
mvn -version
& "$env:ProgramFiles\MySQL\MySQL Server 8.4\bin\mysql.exe" --version
Get-Service MySQL84
```

Resultado esperado:

- `java -version` muestra la versión 17.
- `mvn -version` muestra Maven 3.6.3 o posterior. En la línea `Java version` también debe decir 17; si dice otra, `JAVA_HOME` apunta a otro JDK.
- `mysql.exe --version` muestra la versión 8.4.
- `Get-Service MySQL84` muestra `Running`.

Si MySQL se instaló en otra carpeta, cambiar la ruta de `mysql.exe` en este comando y en los siguientes.

### Paso 1 · Obtener el proyecto y abrir su carpeta

Clonar el repositorio con `git clone https://github.com/agusXD079/SIGC-Web.git`, o descargarlo como ZIP desde GitHub y descomprimirlo. Después, ubicarse en esa carpeta:

```powershell
cd C:\ruta\a\SIGC-Web
```

Cambiar la ruta por la real. Todos los pasos siguientes se ejecutan desde esa carpeta.

### Paso 2 · Crear las bases `sigc` y `sigc_test` (una sola vez)

`root` es el administrador de MySQL. **Solo se usa en los pasos 2 y 3, para configurar MySQL.** La aplicación nunca se conecta como `root`: usa el usuario `sigc` del paso 3.

```powershell
& "$env:ProgramFiles\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p
```

Pide la contraseña de `root`; mientras se escribe no se ve nada. Después, dentro de `mysql>`:

```sql
SOURCE db/crear-bases.sql;
SHOW DATABASES LIKE 'sigc%';
```

Qué hace [`db/crear-bases.sql`](db/crear-bases.sql):

- Crea dos bases vacías con `utf8mb4`, para que se guarden bien los acentos y la ñ:
  - **`sigc`** guarda los datos de la aplicación.
  - **`sigc_test`** es exclusiva de las pruebas de integración. Sus tablas **se vacían en cada corrida**, así que no hay que guardar datos reales en ella.
- No crea tablas: DataNucleus las genera en el primer arranque de la aplicación.
- Usa `CREATE DATABASE IF NOT EXISTS` y no tiene ningún `DROP`. Si las bases ya existen, no las modifica ni borra datos, así que se puede ejecutar más de una vez.
- Sus últimas líneas, comentadas, son los mismos comandos del paso 3. No se ejecutan con `SOURCE`.

`SOURCE` busca el archivo a partir de la carpeta desde la que se abrió `mysql.exe`. Si da `Failed to open file`, usar la ruta completa con barras `/`, por ejemplo `SOURCE C:/ruta/a/SIGC-Web/db/crear-bases.sql;`.

`SHOW DATABASES LIKE 'sigc%';` debe listar `sigc` y `sigc_test`.

### Paso 3 · Crear el usuario `sigc` y darle permisos (una sola vez)

Seguir en la misma ventana de `mysql>`. Primero ver si el usuario ya existe:

```sql
SELECT user, host FROM mysql.user WHERE user = 'sigc';
```

- **Si no devuelve filas**, crearlo. Reemplazar `TU_CLAVE_LOCAL` por una contraseña elegida por quien instala; no guardarla en ningún archivo del repositorio.

  ```sql
  CREATE USER 'sigc'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
  ```

- **Si ya existe y se conoce su contraseña**, no hace falta crearlo: pasar directamente a los permisos. Si igual se ejecuta `CREATE USER`, MySQL responde `ERROR 1396 (HY000): Operation CREATE USER failed`. Ese error no cambia nada.
- **Si ya existe y no se conoce su contraseña**, asignarle una nueva. Cualquier otro programa que use ese usuario también va a necesitar la contraseña nueva.

  ```sql
  ALTER USER 'sigc'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
  ```

  `CREATE USER IF NOT EXISTS` **no** sirve para esto: si el usuario existe, no le cambia la contraseña.

Después, en todos los casos, dar los permisos. Repetirlos no produce error.

```sql
GRANT ALL PRIVILEGES ON sigc.* TO 'sigc'@'localhost';
GRANT ALL PRIVILEGES ON sigc_test.* TO 'sigc'@'localhost';
SHOW GRANTS FOR 'sigc'@'localhost';
exit
```

`SHOW GRANTS` debe mostrar `GRANT ALL PRIVILEGES ON sigc.*` y `ON sigc_test.*`. Los permisos son solo sobre esas dos bases, no sobre el resto del servidor. No hace falta `FLUSH PRIVILEGES`, porque `CREATE USER`, `ALTER USER` y `GRANT` aplican los cambios al instante.

Para comprobar que la contraseña de `sigc` funciona antes de arrancar la aplicación:

```powershell
& "$env:ProgramFiles\MySQL\MySQL Server 8.4\bin\mysql.exe" -u sigc -p sigc
```

Pide la contraseña de `sigc`. Si aparece `mysql>`, la contraseña es correcta; salir con `exit`.

### Paso 4 · Compilar y correr las pruebas unitarias

```powershell
mvn clean verify
```

Este comando compila, aplica el *enhancer* de JDO, corre las 44 pruebas unitarias y genera `target\sigc-1.0.0-SNAPSHOT.jar`. Debe terminar en `BUILD SUCCESS`. No necesita MySQL ni contraseña: las pruebas de integración se omiten si no está definida `SIGC_TEST_DB_URL` (ver paso 7).

### Paso 5 · Configurar las variables de entorno y arrancar

La aplicación toma la configuración solo de variables de entorno ([`application.properties`](src/main/resources/application.properties)). La contraseña se pide por consola y no se guarda en ningún archivo.

```powershell
$env:SIGC_DB_USER = 'sigc'
$claveSigc = Read-Host 'Contraseña de sigc' -AsSecureString
$env:SIGC_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveSigc).Password
java -jar .\target\sigc-1.0.0-SNAPSHOT.jar
```

- `$env:...` define la variable **solo en esa ventana de PowerShell**. Al cerrarla se pierde, así que en cada ventana nueva hay que repetir estas líneas antes de `java -jar`.
- `SIGC_DB_URL` no hace falta definirla si MySQL está en `localhost:3306`. Si usa otro puerto, definirla antes de `java -jar`, por ejemplo con 3307: `$env:SIGC_DB_URL = 'jdbc:mysql://localhost:3307/sigc?connectionTimeZone=America/Argentina/Buenos_Aires'`.
- La aplicación arrancó cuando el log muestra `Started SigcApplication`.

### Paso 6 · Usar y detener la aplicación

Abrir <http://127.0.0.1:8080> en el navegador de la misma computadora. En el primer arranque DataNucleus crea las tablas en la base `sigc`.

Para detenerla, presionar **Ctrl+C** en la ventana de PowerShell donde corre.

Para volver a usarla otro día, alcanza con los pasos 1 y 5: abrir PowerShell en la carpeta, definir las variables y ejecutar `java -jar`. Solo hay que recompilar (paso 4) si cambió el código. Antes de recompilar hay que detener la aplicación, porque Windows bloquea el `.jar` mientras está en uso.

Durante el desarrollo también se puede arrancar con `mvn process-classes spring-boot:run`, con las mismas variables definidas. Este comando ejecuta antes el *enhancer* de JDO. En Linux/macOS se definen las mismas variables con `export`.

### Paso 7 · Pruebas de integración con MySQL (opcional)

Corren contra la base `sigc_test`, nunca contra `sigc`. Solo se ejecutan si la URL termina en `_test`. **Vacían las tablas de `sigc_test` en cada corrida.** Primero detener la aplicación. Después, en la misma ventana donde se definió `SIGC_DB_PASSWORD` en el paso 5:

```powershell
$env:SIGC_TEST_DB_URL = 'jdbc:mysql://localhost:3306/sigc_test'
$env:SIGC_TEST_DB_USER = 'sigc'
$env:SIGC_TEST_DB_PASSWORD = $env:SIGC_DB_PASSWORD
mvn -Pmysql-it clean verify
```

Resultado esperado: 44 pruebas unitarias y 7 de integración correctas, y `BUILD SUCCESS`.

### Problemas frecuentes al arrancar

| Mensaje en el log | Causa | Solución |
|---|---|---|
| `Could not resolve placeholder 'SIGC_DB_PASSWORD'` | No se definió la contraseña en esta ventana de PowerShell | Repetir el paso 5 en la misma ventana |
| `Access denied for user 'sigc'@'localhost' (using password: YES)` | Contraseña de `sigc` incorrecta | Probarla con `mysql.exe -u sigc -p sigc` (paso 3) o cambiarla con `ALTER USER` |
| `Access denied for user 'sigc'@'localhost' to database 'sigc'` o `Unknown database 'sigc'` | Falta la base o faltan los permisos | Repetir los pasos 2 y 3 |
| `Communications link failure` | MySQL no está en marcha o usa otro puerto | `Get-Service MySQL84`; si está detenido, `Start-Service MySQL84` en PowerShell como administrador. Si usa otro puerto, definir `SIGC_DB_URL` |
| `Port 8080 was already in use` | Otra aplicación, u otra copia de SIGC, usa el puerto | Cerrarla, o definir otro puerto: `$env:SIGC_PORT = '8081'` |

### Variables de entorno

Todas están en [`src/main/resources/application.properties`](src/main/resources/application.properties). Las de prueba están en `AplicacionMySqlIT`.

| Variable | Por defecto | Uso |
|---|---|---|
| `SIGC_DB_PASSWORD` | *(obligatoria)* | Contraseña del usuario MySQL |
| `SIGC_DB_USER` | `sigc` | Usuario MySQL |
| `SIGC_DB_URL` | `jdbc:mysql://localhost:3306/sigc?connectionTimeZone=America/Argentina/Buenos_Aires` | URL JDBC |
| `SIGC_PORT` / `SIGC_ADDRESS` | `8080` / `127.0.0.1` | Puerto y dirección de escucha |
| `SIGC_FOTOS_DIR` | `./fotos` | Carpeta donde se guardan las fotos (fuera del repositorio) |
| `SIGC_TEST_DB_URL` | *(sin valor: se omiten las pruebas de integración)* | URL de la base de pruebas; debe terminar en `_test` |
| `SIGC_TEST_DB_USER` / `SIGC_TEST_DB_PASSWORD` | *(sin valor)* | Usuario y contraseña para las pruebas de integración |

## Estructura

```text
SIGC/
├── pom.xml
├── README.md
├── db/                 crear-bases.sql, esquema-referencia.sql
├── docs/               arquitectura, modelo de datos, reglas, pruebas, guía de defensa, estado
├── .github/workflows/  verificar.yml
└── src/
    ├── main/java/com/sigc/
    │   ├── SigcApplication.java
    │   ├── configuracion/   fábrica JDO, reloj, CSRF
    │   ├── dominio/         entidades (Vehiculo, Auto, Moto, Camion, Persona, Cliente, Vendedor, Venta, TestDrive), enums, validaciones, excepciones
    │   ├── repositorio/     acceso a datos con JDO (un repositorio por entidad)
    │   ├── servicio/        reglas de negocio y transacciones (Vehiculo, Cliente, Vendedor, Venta, TestDrive, Dashboard, Reporte)
    │   │   └── vista/       DTO y modelos de vista
    │   └── controlador/     Inicio, Vehiculo, Inventario, Cliente, Vendedor, Venta, TestDrive, Reporte, Foto, Errores
    ├── main/resources/      application.properties, META-INF/persistence.xml, templates/, static/css/sigc.css
    └── test/java/com/sigc/  pruebas unitarias y de integración
```

## Documentación

[Arquitectura](docs/ARQUITECTURA.md) · [Modelo de datos](docs/MODELO_DATOS.md) · [Reglas de negocio](docs/REGLAS_NEGOCIO.md) · [Casos de prueba](docs/CASOS_DE_PRUEBA.md) · [Verificación](docs/VERIFICACION.md) · [Estado y pendientes](docs/ESTADO_Y_PENDIENTES.md) · [Guía de defensa](docs/GUIA_DEFENSA.md) · [Informe de correcciones](docs/INFORME_CORRECCIONES.md)

## Seguridad

CSRF activo en todos los formularios (Spring Security, sin login ni roles en esta versión). Las contraseñas solo se leen del entorno. Las fotos se validan por firma real del archivo, se renombran con UUID y se sirven solo si el nombre fue generado por el sistema. No versionar `fotos/`, `target/` ni archivos con credenciales.
