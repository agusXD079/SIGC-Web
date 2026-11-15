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

No se modificó `pom.xml` ni `persistence.xml`: se compararon con los del proyecto de referencia (mismas versiones de Spring Boot 3.5.16, DataNucleus 6.0.11/6.0.5/6.0.11, plugin de enhancement 6.0.1, jdo-api 3.2.1 y mismas propiedades de esquema), que sigue sin modificarse.
