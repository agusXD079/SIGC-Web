# SIGC · Sistema Integral de Gestión de Concesionaria

Trabajo Práctico de Programación 2 · IFES · Análisis de Sistemas

**Integrantes:** Thiago Vecchi, Franco Araneda y Ángel Venditti.

SIGC evoluciona el sistema de consola de Programación 1 (`legacy/consola/`) hacia una aplicación web de gestión de concesionaria: Spring Boot, Spring MVC, Thymeleaf, Bootstrap 5, JDO/DataNucleus y MySQL. Se conserva la jerarquía original `Vehiculo → Auto, Moto, Camion`, los estados `DISPONIBLE`, `VENDIDO` y `EN_TEST_DRIVE`, la excepción `VehiculoNoDisponibleException` y la interfaz `ITransaccionable`.

> **Estado: Fases 1 a 4 implementadas** (vehículos, clientes, vendedores, ventas, test drives, dashboard y reportes). Ver [docs/ESTADO_Y_PENDIENTES.md](docs/ESTADO_Y_PENDIENTES.md). Compilado y verificado: `mvn clean verify` (41 pruebas unitarias) y `mvn -Pmysql-it verify` contra MySQL 8 (6 pruebas de integración) pasan, y la aplicación se recorrió completa en el navegador: ver [docs/VERIFICACION.md](docs/VERIFICACION.md).

## Qué se puede hacer hoy

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

## Ejecutar desde cero (Windows / PowerShell)

1. **Base de datos** (una sola vez). Desde la carpeta del proyecto:

   ```powershell
   & "$env:ProgramFiles\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p
   ```

   ```sql
   SOURCE db/crear-bases.sql;
   CREATE USER 'sigc'@'localhost' IDENTIFIED BY 'TU_CLAVE_LOCAL';
   GRANT ALL PRIVILEGES ON sigc.* TO 'sigc'@'localhost';
   GRANT ALL PRIVILEGES ON sigc_test.* TO 'sigc'@'localhost';
   exit
   ```

2. **Compilar:** `mvn clean verify` (la primera vez descarga dependencias; requiere Internet).

3. **Arrancar** (la contraseña se pide por consola y no se guarda en ningún archivo):

   ```powershell
   $env:SIGC_DB_USER = 'sigc'
   $claveSigc = Read-Host 'Contraseña de sigc' -AsSecureString
   $env:SIGC_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveSigc).Password
   java -jar .\target\sigc-1.0.0-SNAPSHOT.jar
   ```

4. Abrir <http://127.0.0.1:8080>. En el primer arranque DataNucleus crea las tablas.

En Linux/macOS se definen las mismas variables con `export`. Durante el desarrollo: `mvn process-classes spring-boot:run` (ejecuta antes el *enhancer* de JDO).

### Variables de entorno

| Variable | Por defecto | Uso |
|---|---|---|
| `SIGC_DB_PASSWORD` | *(obligatoria)* | Contraseña del usuario MySQL |
| `SIGC_DB_USER` | `sigc` | Usuario MySQL |
| `SIGC_DB_URL` | `jdbc:mysql://localhost:3306/sigc?...` | URL JDBC |
| `SIGC_PORT` / `SIGC_ADDRESS` | `8080` / `127.0.0.1` | Puerto y dirección de escucha |
| `SIGC_FOTOS_DIR` | `./fotos` | Carpeta donde se guardan las fotos (fuera del repositorio) |

## Estructura

```text
SIGC/
├── pom.xml
├── README.md
├── db/                 crear-bases.sql, esquema-referencia.sql
├── docs/               arquitectura, modelo de datos, reglas, pruebas, defensa, estado
├── legacy/consola/     proyecto original de consola (ver docs/INCORPORAR_AL_REPO.md)
├── .github/workflows/  verificar.yml
└── src/
    ├── main/java/com/sigc/
    │   ├── SigcApplication.java
    │   ├── configuracion/   fábrica JDO, reloj, CSRF
    │   ├── dominio/         Vehiculo, Auto, Moto, Camion, enums, validaciones, excepciones
    │   ├── repositorio/     IRepositorio, RepositorioJdo, VehiculoRepositorio
    │   ├── servicio/        VehiculoService, UnidadTrabajo, FotoAlmacen
    │   │   └── vista/       DTO y modelos de vista (DatosVehiculo, FiltroVehiculos, ResumenInventario)
    │   └── controlador/     Inicio, Vehiculo, Inventario, Foto, Errores
    ├── main/resources/      application.properties, META-INF/persistence.xml, templates/, static/css/sigc.css
    └── test/java/com/sigc/  pruebas unitarias
```

## Documentación

[Arquitectura](docs/ARQUITECTURA.md) · [Modelo de datos](docs/MODELO_DATOS.md) · [Reglas de negocio](docs/REGLAS_NEGOCIO.md) · [Casos de prueba](docs/CASOS_DE_PRUEBA.md) · [Verificación](docs/VERIFICACION.md) · [Estado y pendientes](docs/ESTADO_Y_PENDIENTES.md) · [Incorporar al repositorio](docs/INCORPORAR_AL_REPO.md)

## Seguridad

CSRF activo en todos los formularios (Spring Security, sin login ni roles en esta versión). Las contraseñas solo se leen del entorno. Las fotos se validan por firma real del archivo, se renombran con UUID y se sirven solo si el nombre fue generado por el sistema. No versionar `fotos/`, `target/` ni archivos con credenciales.
