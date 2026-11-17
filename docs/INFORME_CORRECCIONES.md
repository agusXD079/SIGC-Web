# Informe de correcciones · rama `correcciones-aprobacion`

Fecha: 16/11/2026. Revisión del proyecto contra la consigna "Proyecto integrador grupal: aplicación web de gestión" (Programación II).

## Resumen

El proyecto **ya compilaba y pasaba sus pruebas** antes de esta revisión: no se encontraron errores de compilación ni fallos en las pruebas existentes. Se encontraron y corrigieron dos puntos que afectaban el cumplimiento de la consigna y varios errores de documentación. Después de los cambios:

| Comprobación | Resultado |
|---|---|
| `mvn clean compile` | Correcto |
| `mvn clean verify` | Correcto · 41 pruebas unitarias |
| `mvn -Pmysql-it clean verify` (MySQL 8.4.13 + DataNucleus JDO) | Correcto · 7 pruebas de integración |
| Arranque (`java -jar` y `mvn process-classes spring-boot:run`) | Correcto |
| Recorrido HTTP de las funcionalidades mínimas con la aplicación en marcha | 104 comprobaciones, 0 fallidas |
| Persistencia tras reiniciar la aplicación | Correcto |

**Límite importante de la verificación:** todo lo anterior se ejecutó contra una instancia de MySQL 8.4.13 creada solo para la prueba (puerto 3307, carpeta temporal, mismos binarios que el servidor instalado). No se probó contra la base `sigc` del servicio `MySQL84` del equipo (puerto 3306) porque su contraseña no está disponible en ningún archivo ni variable de entorno. Esa base no se modificó.

## Errores encontrados y modificaciones

### 1. Mensajes técnicos en inglés en los formularios (consigna, punto 4: "mostrar mensajes claros")

- **Problema.** Si a un campo numérico llegaba un texto (año, kilometraje, precio, puertas, precio de venta) o a un desplegable una opción inexistente, debajo del campo aparecía el mensaje interno de Spring: `Failed to convert property value of type java.lang.String to required type java.lang.Integer for property anio; For input string: "dosmil"`.
- **Corrección.** Nuevo archivo `src/main/resources/messages.properties` con los mensajes `typeMismatch` en castellano. Spring Boot lo carga solo; no hubo que tocar controladores ni plantillas.
- **Comprobado.** El formulario muestra "Ingrese un número entero válido.", "Ingrese un importe numérico válido." o "El valor ingresado no es válido." y ya no aparece "Failed to convert".

### 2. Relación uno a muchos declarada de un solo lado (consigna, punto 2: "implementar, como mínimo, una relación uno a muchos")

- **Problema.** Las relaciones 1:N existían en la base (claves foráneas) pero en el código solo estaban del lado "muchos" (`Venta.cliente`, `Venta.vendedor`, `TestDrive.cliente`...). Ninguna entidad tenía la colección del lado "uno", que es la forma en que JDO declara una relación uno a muchos y lo primero que se busca al corregir ese requisito.
- **Corrección.** `Cliente` ahora declara `@Persistent(mappedBy = "cliente") private Set<Venta> compras` y el método `getCompras()`. `ClienteService.historial` arma la ficha del cliente recorriendo esa colección (antes consultaba el repositorio de ventas, dependencia que se quitó por quedar sin uso).
- **Sin cambios en la base.** `mappedBy` reutiliza la columna `VENTAS.CLIENTE_ID`. Se comprobó que el esquema generado es idéntico (mismas 9 tablas y columnas) y que la versión nueva arranca sobre tablas creadas por la versión anterior.
- **Comprobado.** Prueba de integración nueva `relacionUnoAMuchosUnClienteReuneTodasSusCompras` y, en ejecución, la ficha de un cliente con dos compras muestra ambas y la suma.
- **Para la defensa.** Este es el único cambio en el modelo de dominio. Está explicado en `docs/GUIA_DEFENSA.md` (tabla de requisitos y "Preguntas probables") y en `docs/MODELO_DATOS.md`.

### 3. Documentación

| Archivo | Corrección |
|---|---|
| `README.md` | Frase duplicada ("Se conserva la jerarquía original Se conserva de la versión de consola..."); estado actualizado (41 + 7 pruebas, Windows + MySQL 8.4); enlace a este informe |
| `docs/CASOS_DE_PRUEBA.md` | Decía que las pruebas de integración con MySQL estaban "pendientes (Fase 4)" cuando ya existían; se agregaron los casos B16 y B17 |
| `docs/ESTADO_Y_PENDIENTES.md` | Decía "falta repetirlo en Windows con MySQL 8.4"; ya está hecho |
| `docs/VERIFICACION.md` | Registro de lo comprobado el 16/11/2026 y de lo que no se pudo comprobar |
| `docs/MODELO_DATOS.md`, `docs/GUIA_DEFENSA.md`, `docs/CAMBIOS.md` | Relación 1:N, mensajes y lista de cambios |

Además, la copia de trabajo local tenía documentación más nueva que `main` (README reorganizado según la consigna, `docs/GUIA_DEFENSA.md` nuevo, `docs/INCORPORAR_AL_REPO.md` eliminado por referirse a un repositorio anterior). Esos cambios se incluyeron en esta rama tal como estaban.

## Revisado sin encontrar errores

Se inspeccionaron las capas y se probaron en ejecución, sin necesidad de cambios:

- **Maven y JDO:** `pom.xml` con el *enhancer* de DataNucleus en `process-classes`; `persistence.xml` con las 9 clases; creación automática de tablas en MySQL 8.4.
- **Transacciones y recursos:** `UnidadTrabajo` abre un `PersistenceManager` por caso de uso, confirma o revierte y siempre cierra.
- **Capas:** los controladores solo dependen de servicios; los repositorios no confirman transacciones; las vistas solo usan el `Model`.
- **Funcionalidades mínimas:** ABM con baja lógica de vehículos, clientes y vendedores; consulta de ventas y test drives; búsquedas y filtros; venta y test drive con sus validaciones (vehículo no disponible, venta duplicada, cliente o vendedor dados de baja, DNI y legajo únicos).
- **Interfaz:** navegación por menú, mensajes de confirmación y error, formularios con validación, diseño adaptable a pantalla chica.
- **Texto con acentos y ñ**, fotos (tipo real y tamaño), token CSRF y páginas de error.

## Pruebas ejecutadas

Entorno: Windows 11, JDK 17.0.21 (Temurin), Maven 3.9.11, MySQL 8.4.13. Detalle completo en [VERIFICACION.md](VERIFICACION.md).

1. `mvn clean compile` y `mvn clean verify` antes de tocar el código (línea de base: 41 pruebas correctas).
2. `mvn -Pmysql-it clean verify` antes y después de los cambios, sobre base vacía y sobre base con tablas preexistentes (6 pruebas antes, 7 después; todas correctas).
3. Aplicación en marcha: recorrido HTTP automatizado de 104 comprobaciones (pantallas, ABM, validaciones, búsquedas, venta, test drive, bajas, relaciones, reportes, errores, CSRF), más pruebas puntuales de acentos, fotos (válida, falsa y de más de 3 MB), identificadores cruzados entre clientes y vendedores, rangos de fechas invertidos y parámetros inválidos. Ninguna respuesta 5xx ni excepción en el log.
4. Navegador: dashboard, listados, venta registrada desde el formulario, reportes y vista móvil.
5. Reinicio de la aplicación: los datos y las fotos se conservan.

## Pendiente

| Tema | Detalle |
|---|---|
| Base `sigc` definitiva | Arrancar una vez la aplicación contra el MySQL del equipo de la defensa siguiendo el README (crear bases y usuario `sigc`, definir `SIGC_DB_PASSWORD`). No se pudo hacer sin la contraseña |
| GitHub Actions | `.github/workflows/verificar.yml` solo se dispara con *push* a `main` o `avance-programacion-2` y con *pull requests*. No se modificó; se ejecutará al abrir el *pull request* de esta rama hacia `main` |
| Fusión a `main` | Esta rama no se fusionó. Revisar y abrir el *pull request* |
| Guía de defensa | Completar la tabla "Participación de cada integrante" en `docs/GUIA_DEFENSA.md` |
| Pruebas manuales | Impresión del comprobante y venta del mismo vehículo desde dos pestañas (este último caso sí está cubierto por una prueba automática) |
| Observaciones menores, sin corregir | Un registro inexistente (por ejemplo `/vehiculos/999999`) responde 400 con página de error clara, no 404; un rango de años invertido en el filtro de vehículos devuelve la lista vacía sin mensaje. No incumplen la consigna |
