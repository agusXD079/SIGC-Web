# Verificación

Este documento registra qué se comprobó realmente. No se informa ningún resultado que no se haya obtenido.

## Verificación del 16/11/2026 (Windows 11, JDK 17.0.21, Maven 3.9.11, MySQL 8.4.13)

Entorno: el equipo de desarrollo con Windows 11. Para no depender de la contraseña del servidor MySQL instalado (que no está guardada en ningún archivo ni variable), se usó una **instancia aparte de MySQL 8.4.13** creada solo para la verificación con los mismos binarios (`mysqld --initialize-insecure`, puerto 3307, carpeta temporal). La base `sigc` del servidor instalado (puerto 3306) no se tocó.

- `mvn clean compile`: compila sin errores; el *enhancer* de DataNucleus procesa las 9 entidades.
- `mvn clean verify`: pasan las 41 pruebas unitarias y se genera `target/sigc-1.0.0-SNAPSHOT.jar`.
- `mvn -Pmysql-it clean verify`: pasan las 7 pruebas de integración (`AplicacionMySqlIT`) con MySQL 8.4 y JDO. Se corrió dos veces: sobre una base **vacía** (DataNucleus crea las 9 tablas) y sobre una base cuyas tablas había creado la versión anterior del código (arranca sin cambios de esquema).
- Esquema generado: 9 tablas (`vehiculos`, `autos`, `motos`, `camiones`, `personas`, `clientes`, `vendedores`, `ventas`, `test_drives`), claves foráneas y las restricciones `UQ_PERSONA_DNI`, `UQ_VENDEDOR_LEGAJO`, `UQ_VENTA_VEHICULO`, `UQ_VENTA_CLAVE` y `UQ_TD_VEHICULO_ABIERTO`.
- Arranque real: `java -jar target/sigc-1.0.0-SNAPSHOT.jar` y `mvn process-classes spring-boot:run`; ambos inician en unos 3 segundos.
- Recorrido HTTP automatizado con la aplicación en marcha (104 comprobaciones, todas correctas): las 14 pantallas, Bootstrap y CSS; alta, edición, baja lógica y reactivación de vehículos, clientes y vendedores; validaciones de campos; DNI y legajo duplicados; búsqueda por texto y filtros por tipo, estado, precio y fechas; test drive (inicio, bloqueo de venta y de bajas, finalización); venta (comprobante, reenvío idempotente, segunda venta rechazada, vehículo vendido inmodificable); fichas con las relaciones (compras del cliente, ventas del vendedor, test drives del vehículo); dashboard, inventario y reportes; rutas y parámetros inválidos sin error 500; POST sin token CSRF rechazado con 403.
- Navegador: dashboard, listado de vehículos y venta registrada desde el formulario (el precio de lista se completa solo); vista móvil; sin errores en la consola del navegador.
- Texto con acentos y ñ: se guarda en UTF-8 y se busca correctamente ("Muñoz Güemes, José").
- Fotos: se acepta un PNG real, se rechaza un archivo de texto renombrado a `.png`, una foto de más de 3 MB responde 413 con el mensaje "La foto supera el máximo de 3 MB." y una ruta con `..` responde 400.
- Persistencia: tras detener y volver a arrancar la aplicación siguen los vehículos, clientes, ventas, test drives y fotos.
- Log de la aplicación: ninguna excepción ni respuesta 5xx durante el recorrido.

### No verificado en esta fecha

- El arranque contra la base `sigc` del servidor MySQL instalado en el equipo (servicio `MySQL84`, puerto 3306): requiere la contraseña del usuario `sigc`, que solo conoce el grupo. Pasos: los del README (crear bases y usuario, definir `SIGC_DB_PASSWORD`, `java -jar`).
- La ejecución del flujo de GitHub Actions (`.github/workflows/verificar.yml`) sobre la rama nueva.
- Impresión del comprobante (caso B13) y dos pestañas vendiendo el mismo vehículo a mano (B07; el caso equivalente sí está cubierto por la prueba de integración de ventas simultáneas): quedan como pruebas manuales.

## Verificación del 15/11/2026 (JDK 21, Maven, MySQL 8.0)

- `mvn clean verify`: compila, el *enhancer* de DataNucleus procesa las 9 entidades y pasan las 41 pruebas unitarias.
- `mvn -Pmysql-it verify` con `SIGC_TEST_DB_URL=jdbc:mysql://localhost:3306/sigc_test`: pasan las 6 pruebas de integración.
- **Error corregido:** `JDODetachedFieldAccessException` al leer `venta.getVehiculo()` en el dashboard. En JDO las referencias a otras entidades no están en el *default fetch group*, así que `detachCopy` las dejaba sin cargar. Se agregó `@Persistent(defaultFetchGroup = "true")` a `vehiculo`, `cliente` y `vendedor` en `Venta` y `TestDrive`. Además del dashboard, el error afectaba el listado de ventas, el comprobante y el listado de test drives.
- Recorrido en el navegador con la aplicación real (`java -jar`): alta de auto, moto y camión; validaciones del servidor; DNI duplicado rechazado; alta de clientes y vendedor; venta con comprobante; el vehículo vendido deja de ofrecerse; test drive iniciado (el vehículo no se puede vender) y finalizado; búsqueda y filtros; edición; baja lógica de un cliente con venta (el historial se conserva); dashboard y reportes. Ninguna respuesta 5xx y ningún error en el log.
- Persistencia: tras detener y volver a arrancar la aplicación, siguen todos los datos.

## Pendiente de verificar

- Arrancar una vez contra la base `sigc` del equipo donde se hará la defensa (ver "No verificado en esta fecha").
- Recorrer los casos manuales de [CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md) en el equipo donde se hará la defensa.
- Las fases de desarrollo previas se verificaron solo con stubs y un ejecutor propio (ver historial del repositorio); esa evidencia quedó reemplazada por la ejecución real descrita arriba.
