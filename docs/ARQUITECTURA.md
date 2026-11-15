# Arquitectura

SIGC es un proyecto Maven de un solo módulo, organizado en capas con la misma convención de paquetes que Gestor_GYM (`dominio`, `repositorio`, `servicio`, `controlador`, `configuracion`) y un subpaquete `servicio/vista` para DTO y modelos de vista.

| Capa | Responsabilidad | Clases (Fase 2) |
|---|---|---|
| Dominio | Estado, identidad y reglas de las entidades; transiciones de estado | `Vehiculo`, `Auto`, `Moto`, `Camion`, `EstadoVehiculo`, `Combustible`, `Transmision`, `TipoMoto`, `TipoVehiculo`, `Validaciones`, `ReglaNegocioException`, `VehiculoNoDisponibleException`, `ITransaccionable` |
| Repositorio | Consultas y escrituras JDO; nunca confirma transacciones | `IRepositorio<T>`, `RepositorioJdo<T>`, `VehiculoRepositorio` |
| Servicio | Casos de uso, transacciones, fotos | `VehiculoService`, `UnidadTrabajo`, `FotoAlmacen`, `PersistenciaException` |
| Servicio / vista | DTO de entrada y salida | `DatosVehiculo`, `FiltroVehiculos`, `ResumenInventario` |
| Controlador | Rutas HTTP, validación de formularios, elección de vista | `InicioController`, `VehiculoController`, `InventarioController`, `FotoController`, `ErroresController` |
| Configuración | Fábrica JDO, reloj, CSRF | `Configuracion` |
| Vista | Thymeleaf + Bootstrap 5 + CSS propio | `templates/`, `static/css/sigc.css` |

Los controladores no contienen reglas de negocio: validan el formato del formulario (`jakarta.validation`) y delegan en el servicio. Las reglas viven en el dominio y se vuelven a comprobar ahí, de modo que ninguna otra capa puede saltearlas.

## Persistencia y transacciones

- **JDO/DataNucleus 6** con MySQL 8.4. `Configuracion` arma la `PersistenceManagerFactory` desde la unidad `sigc` de `META-INF/persistence.xml` y aplica las credenciales del entorno.
- **Unidad de trabajo.** Cada caso de uso abre un `PersistenceManager`, ejecuta y cierra (`UnidadTrabajo.leer` / `escribir`). Las escrituras corren en una transacción con lecturas bloqueantes (`setSerializeRead(true)`) y READ COMMITTED.
- **Datos hacia la vista.** Los servicios devuelven copias *detached* (`detachCopy`); ninguna plantilla ejecuta consultas.
- **Concurrencia.** `Vehiculo` declara `@Version` (columna `VERSION_FILA`): si dos usuarios editan la misma fila, el segundo recibe "Otro usuario modificó este registro" en lugar de pisar los cambios. En la Fase 3 la venta usará además el bloqueo de la fila del vehículo y una restricción `UNIQUE(vehiculo_id)` en `VENTAS`.
- **Herencia.** Cada clase de la jerarquía tiene su tabla unida por identidad; `VEHICULOS.CLASE` es el discriminador.
- **Búsquedas.** El texto externo viaja siempre como parámetro de JDOQL, nunca concatenado.

## Recorrido: abrir el listado de vehículos

1. El navegador envía `GET /vehiculos?texto=toyota&orden=PRECIO_ASC`. `VehiculoController.lista` arma un `FiltroVehiculos`.
2. `VehiculoService.listar` abre una unidad de lectura y llama a `VehiculoRepositorio.buscar`, que construye el JDOQL y consulta MySQL.
3. El servicio devuelve copias detached; el controlador las pone en el `Model` y Thymeleaf genera `vehiculos/lista.html`.

## Recorrido: registrar un vehículo con foto

1. `POST /vehiculos` (multipart, con token CSRF). `@Valid` revisa el formato; si hay errores se vuelve al formulario con los datos cargados.
2. `VehiculoService.crear` guarda la foto (`FotoAlmacen`: tamaño, firma real, nombre UUID) y abre una transacción que construye `Auto`, `Moto` o `Camion` (el dominio valida) y lo persiste.
3. Si la transacción falla, la foto recién guardada se elimina. El controlador redirige a la ficha con un mensaje.

## Decisiones técnicas

| Decisión | Motivo |
|---|---|
| JDO/DataNucleus en lugar de JPA | Coherencia con Gestor_GYM y con la materia. **Pendiente de confirmar con el docente.** |
| Spring Security solo para CSRF | Sin login ni roles en esta versión (consigna). |
| `TipoMoto` como enum | El original usaba texto libre ("deportiva/scooter/cross"); el enum evita valores inconsistentes en la base. |
| `BigDecimal` para importes | El original usaba `double`. |
| Fotos en carpeta configurable, solo el nombre en la base | No inflar la base ni el repositorio. |
| Gráficos con CSS (sin librerías) | Sin dependencias externas ni CDN; los indicadores salen de datos reales. |
| `mostrarInfo()` devuelve `String` | Evita imprimir por consola desde el dominio; la vista decide cómo mostrarlo. |
