# Reglas de negocio (vehículos)

| Id | Regla | Dónde se aplica |
|---|---|---|
| RV01 | Marca (≤60) y modelo (≤80) son obligatorios; versión, color y descripción son opcionales. | `Vehiculo.actualizarDatos`, `Validaciones` |
| RV02 | Año entre 1950 y el año actual + 1. | `Vehiculo.actualizarDatos` |
| RV03 | Kilometraje entre 0 y 3.000.000. | `Vehiculo.actualizarDatos` |
| RV04 | El precio es `BigDecimal` mayor que cero, con 2 decimales (redondeo HALF_UP) y tope de 999.999.999.999,99. | `Validaciones.importe` |
| RV05 | Combustible y transmisión son obligatorios (enums). | dominio |
| RV06 | Auto: 2 a 6 puertas. Moto: tipo obligatorio y cilindrada 50 a 3000 cc. Camión: capacidad de carga > 0 y ≤ 9999,99 tn. | subclases |
| RV07 | Un vehículo nuevo queda `DISPONIBLE` y activo. | constructor |
| RV08 | Solo un vehículo activo y `DISPONIBLE` puede venderse o pasar a `EN_TEST_DRIVE`; si no, se lanza `VehiculoNoDisponibleException`. | `Vehiculo.vender/hacerTestDrive` |
| RV09 | `finalizarTestDrive` solo es válido desde `EN_TEST_DRIVE` y devuelve el vehículo a `DISPONIBLE`. | `Vehiculo` |
| RV10 | Un vehículo `VENDIDO` no puede editarse ni darse de baja (forma parte del historial). | `actualizarDatos`, `darDeBaja` |
| RV11 | No se da de baja un vehículo `EN_TEST_DRIVE`. | `darDeBaja` |
| RV12 | La baja es lógica: el vehículo deja de listarse (salvo "Incluir bajas") y puede reactivarse. | `activo` |
| RV13 | El tipo de un vehículo existente no puede cambiarse. | `VehiculoService.editar` |
| RV14 | Foto: JPG, PNG o WebP, hasta 3 MB, validada por la firma real del archivo; nombre generado por el sistema. | `FotoAlmacen` |
| RV15 | Dos ediciones simultáneas de la misma fila no se pisan: la segunda se rechaza con un mensaje. | `@Version`, `UnidadTrabajo` |

| RP01 | DNI de 7 u 8 dígitos (se aceptan puntos, espacios y guiones al escribirlo); único entre clientes y vendedores. | `Validaciones.dni`, `UQ_PERSONA_DNI` |
| RP02 | Nombre y apellido obligatorios; teléfono y correo opcionales (el correo, si se carga, debe tener formato válido). | `Persona` |
| RP03 | El legajo del vendedor es obligatorio, en mayúsculas (letras, números y guiones) y único. | `Vendedor`, `UQ_VENDEDOR_LEGAJO` |
| RP04 | Personas: `equals/hashCode` por DNI; orden natural por apellido y luego nombre. | `Persona` |
| RP05 | La baja de clientes y vendedores es lógica y conserva ventas y pruebas. Un cliente con test drive en curso no puede darse de baja. | `ClienteService`, `VendedorService` |
| RV16 | Solo se vende un vehículo activo y `DISPONIBLE`; el vehículo `EN_TEST_DRIVE` no se vende hasta finalizar la prueba. | `Vehiculo.vender` |
| RV17 | Cliente y vendedor deben estar activos al vender o probar. | `VentaService`, `TestDriveService` |
| RV18 | Un vehículo se vende una sola vez: bloqueo de la fila del vehículo al vender + `UNIQUE (VEHICULO_ID)`. | `VentaService`, BD |
| RV19 | Cada formulario de venta lleva un UUID. Reenviar la misma solicitud devuelve la venta ya registrada; usar el UUID con otros datos se rechaza. | `VentaService`, `UQ_VENTA_CLAVE` |
| RV20 | El precio de venta se graba al vender (sugiere el precio de lista, pero es editable); la fecha la pone el servidor (zona Buenos Aires). | `Venta` |
| RV21 | La venta, el cambio de estado del vehículo y el alta del comprobante ocurren en una sola transacción: o se hace todo o nada. | `UnidadTrabajo` |
| RV22 | Un test drive pasa el vehículo a `EN_TEST_DRIVE`; al finalizar vuelve a `DISPONIBLE`. Solo puede haber una prueba abierta por vehículo (`UNIQUE VEHICULO_ABIERTO_ID`). | `TestDriveService` |
| RV23 | Una prueba finalizada no puede finalizarse otra vez ni terminar antes de su inicio. | `TestDrive.finalizar` |
| RV24 | Un vehículo con venta o prueba asociada no se borra: solo baja lógica (y `VENDIDO` no se da de baja). | `Vehiculo`, sin DELETE |
| RR01 | Reportes: rango de fechas inclusivo; la fecha final no puede ser anterior a la inicial; los meses sin ventas aparecen en cero cuando hay ambas fechas. | `ReporteService` |
