# Casos de prueba

## Automáticos (JUnit 5 + AssertJ, sin base de datos)

| Archivo | Cubre |
|---|---|
| `dominio/VehiculoTest` | RV02 a RV12: estados, doble venta, test drive, bajas, vehículo vendido inmodificable, validaciones, polimorfismo |
| `dominio/ValidacionesTest` | Texto, importe (redondeo y tope) y rangos |
| `dominio/VentasYPersonasTest` | DNI y correo, `equals/hashCode`, orden por apellido, legajo, venta, test drive, bajas |
| `integracion/AplicacionMySqlIT` (perfil `mysql-it`, requiere MySQL) | DNI único, venta completa y no repetible, reenvío idempotente, dos ventas simultáneas del mismo vehículo, test drive bloquea la venta, panel con datos reales |
| `servicio/FotoAlmacenTest` | RV14: firma real, tamaño, nombre generado, rutas inválidas |

Ejecutar: `mvn clean verify`. **Estado de ejecución: ver [VERIFICACION.md](VERIFICACION.md).**

## Aceptación manual (requieren MySQL y navegador)

| Id | Pasos | Resultado esperado |
|---|---|---|
| A01 | Arrancar con la base vacía y abrir `/` | Dashboard con todos los contadores en 0; sin errores en consola |
| A02 | Registrar un Auto con foto PNG | Redirige a la ficha con "Vehículo registrado"; la foto se ve |
| A03 | Registrar una Moto y un Camión | Cada formulario pide sus campos propios; ambos aparecen en el listado |
| A04 | Enviar el formulario con precio 0, año 2100 y marca vacía | Vuelve al formulario con mensajes en rojo y datos conservados |
| A05 | Subir un `.txt` renombrado a `.png` | Mensaje "La foto debe ser una imagen JPG, PNG o WebP" |
| A06 | Subir una imagen mayor a 3 MB | Mensaje de máximo 3 MB |
| A07 | Editar precio y color de un vehículo | Cambios visibles en la ficha |
| A08 | Filtrar por tipo, estado, rango de año y de precio; ordenar por precio ascendente y descendente | El listado respeta cada criterio y el orden |
| A09 | Dar de baja un vehículo disponible | Desaparece del listado; aparece con "Incluir bajas" |
| A10 | Reactivarlo desde su ficha | Vuelve al listado |
| A11 | Abrir `/inventario?estado=DISPONIBLE` | Solo disponibles; los contadores coinciden con el dashboard |
| A12 | Reiniciar la aplicación | Los datos y las fotos siguen presentes |
| A13 | Enviar un POST sin token CSRF (por ejemplo con `curl`) | Respuesta 403 |
| A14 | Abrir en un celular (o ventana angosta) | El menú pasa a la parte superior; tarjetas en una columna |
| A15 | Abrir `/vehiculos/999999` | Página de error con mensaje claro, sin traza interna |

## Pendiente (Fase 4)

Pruebas de integración contra MySQL real (`sigc_test`): persistencia de la jerarquía, bloqueo y `@Version`, ventas concurrentes del mismo vehículo.

## Manuales de las Fases 3 y 4 (con la aplicación en marcha)

| N.º | Acción | Resultado esperado |
|---|---|---|
| B01 | Registrar cliente con DNI `30.111.222` | Se guarda como `30111222` |
| B02 | Registrar otra persona con el mismo DNI | Mensaje de DNI ya registrado, sin crear duplicado |
| B03 | Registrar vendedor con legajo repetido | Mensaje claro; no se crea |
| B04 | Buscar cliente por parte del apellido o por DNI | Solo coincidencias; texto sin dígitos no devuelve todo por error |
| B05 | Vender un vehículo disponible | Comprobante `V-000001`; el vehículo queda `VENDIDO` |
| B06 | Reenviar el formulario con F5 o doble clic | No se crea otra venta |
| B07 | Abrir dos pestañas con el mismo vehículo y vender en ambas | La segunda recibe "no está disponible" |
| B08 | Iniciar test drive y luego intentar vender ese vehículo | Rechazado: está en test drive |
| B09 | Finalizar el test drive | El vehículo vuelve a `DISPONIBLE` |
| B10 | Dar de baja un cliente con compras | Se conserva el historial; no aparece en el selector de ventas |
| B11 | Dar de baja un cliente con test drive en curso | Rechazado con mensaje |
| B12 | Filtrar ventas por vendedor y fechas; fecha final anterior a la inicial | Lista filtrada; mensaje de error en el segundo caso |
| B13 | Imprimir comprobante | El menú y los botones no salen en la impresión |
| B14 | Dashboard y `/reportes` tras varias ventas | Totales coherentes con el listado de ventas |
| B15 | POST sin token CSRF a `/ventas` | 403 |
