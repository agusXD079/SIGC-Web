# Cambios de la revisión 3 (16/11/2026)

| Archivo | Corrección |
|---|---|
| `resources/messages.properties` (nuevo) | Cuando llegaba un valor no numérico a un campo numérico (año, kilometraje, precio, puertas, precio de venta) o una opción inexistente, el formulario mostraba el texto técnico de Spring en inglés ("Failed to convert property value of type java.lang.String..."). Ahora muestra "Ingrese un número entero válido.", "Ingrese un importe numérico válido." o "El valor ingresado no es válido.". |
| `dominio/Cliente.java` | La relación uno a muchos Cliente–Venta solo estaba declarada del lado "muchos" (`Venta.cliente`). Se agregó el lado "uno": `@Persistent(mappedBy = "cliente") Set<Venta> compras` y `getCompras()`. No cambia el esquema: usa la clave foránea `VENTAS.CLIENTE_ID` que ya existía. |
| `servicio/ClienteService.java` | `historial` obtiene las compras recorriendo `cliente.getCompras()` en lugar de consultar el repositorio de ventas; se quitó esa dependencia, que quedó sin uso. |
| `test/.../AplicacionMySqlIT.java` | Prueba nueva `relacionUnoAMuchosUnClienteReuneTodasSusCompras`. |
| `README.md` | Frase duplicada en la descripción; estado y enlaces actualizados. |
| `docs/` | `VERIFICACION.md` con la verificación en Windows + MySQL 8.4; `CASOS_DE_PRUEBA.md` ya no dice que las pruebas de integración están pendientes; `MODELO_DATOS.md` y `GUIA_DEFENSA.md` explican la relación 1:N; nuevo `INFORME_CORRECCIONES.md`. |

# Cambios de la revisión 2

| Archivo | Corrección |
|---|---|
| `servicio/UnidadTrabajo.java` | Las violaciones de restricciones de la base (UNIQUE, clave foránea; SQLSTATE 23xxx) ya no terminan en la página genérica "Error de base de datos": se traducen a un mensaje de negocio. Cubre dos solicitudes simultáneas con el mismo DNI/legajo o la venta simultánea del mismo vehículo. Un primer borrador usaba `ArrayDeque.add(null)` y lanzaba `NullPointerException`; lo detectó `UnidadTrabajoTest` y quedó corregido. |
| `repositorio/BusquedaPersonas.java` | Se quitaban `.` y `-` de **todas** las palabras buscadas, de modo que un apellido como "Pérez-Gómez" nunca se encontraba. Ahora nombre y apellido se comparan tal cual y el DNI solo por sus dígitos. |
| `repositorio/VehiculoRepositorio.java` | La búsqueda por texto exigía que la frase completa estuviera en la marca o en el modelo ("toyota corolla" no devolvía nada). Ahora cada palabra debe coincidir con la marca o el modelo. |
| `servicio/ReporteService.java` | Se quitó un import sin uso. |
| `test/.../AplicacionMySqlIT.java` | La limpieza usaba `deletePersistentAll()` masivo, riesgoso con tablas de subclases y claves foráneas; ahora borra objeto por objeto en el orden correcto. |
| Pruebas nuevas | `UnidadTrabajoTest`, `ReporteServiceTest`, `BusquedaPersonasTest`. |
| `docs/VERIFICACION.md` | Resultados reales de esta revisión y sus límites. |
