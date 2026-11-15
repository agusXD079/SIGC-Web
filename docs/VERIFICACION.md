# Verificación (Fases 1 a 4)

Este documento registra qué se comprobó realmente y qué no. No se informa ningún resultado que no se haya obtenido.

## Entorno donde se escribió el código

Java 21 y Maven 3.9.11 instalados, **sin acceso a Maven Central** (la descarga de dependencias fue rechazada con HTTP 403) y sin MySQL. Por eso `mvn clean verify` **no pudo ejecutarse**.

## Comprobado

| Comprobación | Resultado |
|---|---|
| Compilación de todas las clases del paquete `dominio` (Fases 2 y 3: Persona, Cliente, Vendedor, Venta, TestDrive, enums) con `javac` (JDK 21), usando stubs mínimos de las anotaciones `javax.jdo.annotations` | Compiló sin errores |
| Ejecución de 19 verificaciones adicionales de la Fase 3 con un programa ad hoc (DNI y correo, `equals/hashCode` por DNI, orden por apellido, legajo, venta solo con vehículo vendido, clave UUID, doble venta, test drive que bloquea la venta y devuelve el vehículo a DISPONIBLE, bajas lógicas) | 19 correctas, 0 fallidas |
| Ejecución de 31 verificaciones de las reglas del dominio con un programa ad hoc (estados, doble venta, test drive, bajas, validaciones, polimorfismo, redondeo de importes) | 31 correctas, 0 fallidas |

Alcance de esas comprobaciones: **solo lógica del dominio**. Servicios, repositorios, controladores y plantillas de las Fases 3 y 4 **no se compilaron ni ejecutaron** (faltan Spring y JDO en este entorno). No ejecuta JUnit, ni Spring, ni JDO, ni MySQL. Los 31 chequeos replican los escenarios de `VehiculoTest` y `ValidacionesTest`, pero no son esos tests.

## Revisión 2 (ejecución real de pruebas con un ejecutor propio)

Maven Central sigue bloqueado en el entorno de desarrollo, así que `mvn clean verify` **no se ejecutó**. En su lugar se compiló **todo el código de `src/main/java` y `src/test/java`** con `javac` contra *stubs* de las APIs externas (JDO, Spring, Jakarta Validation, slf4j) escritos a mano y se ejecutaron las clases de prueba reales con un ejecutor mínimo propio que imita `@Test`, `@TempDir` y las aserciones de AssertJ usadas.

| Qué | Resultado |
|---|---|
| Compilación de todo `src/main/java` y `AplicacionMySqlIT` contra los stubs | Sin errores de tipos |
| 41 pruebas: `VehiculoTest` (10), `ValidacionesTest` (4), `VentasYPersonasTest` (10), `FotoAlmacenTest` (4), `UnidadTrabajoTest` (5), `ReporteServiceTest` (4), `BusquedaPersonasTest` (4), ejecutadas con el ejecutor propio | 41 correctas, 0 fallidas |

Limitaciones: los *stubs* reflejan lo que se recuerda de las APIs, no los jars reales; el ejecutor no es JUnit ni AssertJ; ningún test tocó Spring, DataNucleus ni MySQL. Es evidencia de lógica, no de integración. Esta revisión encontró y corrigió errores reales (ver `docs/CAMBIOS.md`).

## NO comprobado (pendiente de ejecutar en una máquina con Internet y MySQL)

1. `mvn clean verify`: compilación completa con Spring Boot, enhancement de JDO y los tests JUnit (`VehiculoTest`, `ValidacionesTest`, `VentasYPersonasTest`, `FotoAlmacenTest`). **Ninguno se ejecutó con JUnit**; hay que correrlos.
1b. `mvn -Pmysql-it verify` con `AplicacionMySqlIT` (venta duplicada, ventas simultáneas, reenvío con la misma clave, test drive, DNI único, panel). **Escrita pero nunca ejecutada.** Requiere MySQL y las variables `SIGC_TEST_DB_URL` (la base debe terminar en `_test`), `SIGC_TEST_DB_USER` y `SIGC_TEST_DB_PASSWORD`; sin ellas la prueba se omite.
2. Arranque de la aplicación y creación automática de tablas con DataNucleus.
3. Consultas JDOQL de `VehiculoRepositorio.buscar` (parámetros implícitos, `indexOf`, ordenamiento, consulta sobre subclases).
4. Renderizado de las plantillas Thymeleaf (sintaxis revisada solo a mano).
5. Subida y visualización de fotos, CSRF y los casos A01 a A15 de [CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md).

## Riesgos conocidos a revisar primero si algo falla

- **Mapeo JDO de enums y `BigDecimal`**: se declaran `length`/`scale` en `@Column`; si DataNucleus las ignora o crea otro tipo, ajustar con `@Persistent`/`jdbcType`.
- **Subclases con `NEW_TABLE`**: se espera que DataNucleus una cada tabla por la clave primaria del padre; si no, agregar `@PrimaryKeyJoin`/`@Join`.
- **`@Version` con herencia**: declarada solo en la raíz.
- **Accesores de `record` en Thymeleaf**: las plantillas usan la sintaxis `filtro.texto()` / `resumen.total()` por prudencia.
- **Parámetros nulos en enlaces** (`@{/inventario(estado=${filtro.estado()})}`): Thymeleaf los renderiza sin valor (`?estado`); Spring los interpreta como ausentes.

- **Copias detached**: los servicios devuelven `detachCopy` y las plantillas navegan `venta.cliente`, `venta.vehiculo`, etc. Depende de que el plan de carga (`setMaxFetchDepth(3)`) incluya esas referencias; si aparece `JDODetachedFieldAccessException`, agregar los grupos de carga o las referencias al `FetchPlan`.
- **Orden de borrado en la limpieza de `AplicacionMySqlIT`** y colisión de nombres de tabla `VENTAS`/`TEST_DRIVES` según el nombrado de DataNucleus.
- **Ventas simultáneas**: el bloqueo de fila depende de `setSerializeRead(true)` en InnoDB; si la prueba concurrente falla, las restricciones `UNIQUE` siguen impidiendo la doble venta y la segunda transacción debe fallar.

Cuando se ejecute `mvn clean verify`, registrar aquí el resultado real (fecha, versión y cantidad de pruebas).


## Verificación del 15/11/2026 (JDK 21, Maven, MySQL 8.0)

- `mvn clean verify`: compila, el *enhancer* de DataNucleus procesa las 9 entidades y pasan las 41 pruebas unitarias.
- `mvn -Pmysql-it verify` con `SIGC_TEST_DB_URL=jdbc:mysql://localhost:3306/sigc_test`: pasan las 6 pruebas de integración.
- **Error corregido:** `JDODetachedFieldAccessException` al leer `venta.getVehiculo()` en el dashboard. En JDO las referencias a otras entidades no están en el *default fetch group*, así que `detachCopy` las dejaba sin cargar. Se agregó `@Persistent(defaultFetchGroup = "true")` a `vehiculo`, `cliente` y `vendedor` en `Venta` y `TestDrive`. Además del dashboard, el error afectaba el listado de ventas, el comprobante y el listado de test drives.
- Recorrido en el navegador con la aplicación real (`java -jar`): alta de auto, moto y camión; validaciones del servidor; DNI duplicado rechazado; alta de clientes y vendedor; venta con comprobante; el vehículo vendido deja de ofrecerse; test drive iniciado (el vehículo no se puede vender) y finalizado; búsqueda y filtros; edición; baja lógica de un cliente con venta (el historial se conserva); dashboard y reportes. Ninguna respuesta 5xx y ningún error en el log.
- Persistencia: tras detener y volver a arrancar la aplicación, siguen todos los datos.
