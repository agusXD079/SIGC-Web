# Estado del proyecto y pendientes

## Hecho en este avance

**Fase 1 · Estructura y configuración:** proyecto Maven (Spring Boot 3.5.16, Java 17), JDO/DataNucleus + MySQL, capas, Thymeleaf, Bootstrap 5.3.8 por WebJar, CSS propio, menú lateral responsive, `.gitignore`, scripts SQL, GitHub Actions y documentación inicial.

**Fase 2 · Vehículos:** entidades `Vehiculo → Auto, Moto, Camion` con persistencia JDO, repositorio con filtros, servicio transaccional, controladores y vistas (listado en tarjetas y tabla, formulario de alta/edición, ficha), baja lógica y reactivación, fotos, control de concurrencia, inventario y dashboard con datos reales.

**Fase 3 · Clientes, vendedores, ventas y test drives:** entidades `Persona → Cliente / Vendedor`, `Venta` y `TestDrive` con restricciones `UNIQUE` (DNI, legajo, vehículo vendido, clave de operación, test drive abierto por vehículo); servicios transaccionales con bloqueo de la fila del vehículo e idempotencia por UUID; CRUD con baja lógica de clientes y vendedores (historial conservado); alta de venta con comprobante imprimible; inicio y finalización de test drive; historial por cliente, vendedor y vehículo.

**Fase 4 · Dashboard y reportes:** panel con totales reales (vehículos, clientes, vendedores, ventas, disponibles, vendidos, monto), últimas operaciones, ventas por vendedor y por mes; pantalla `/reportes` con rango de fechas (por vendedor, por mes, por tipo). Pruebas unitarias de dominio y prueba de integración con MySQL (`AplicacionMySqlIT`, perfil `mysql-it`).

## Pendiente

| Tema | Pendiente |
|---|---|
| Ejecución | **Hecho (15/11/2026).** `mvn clean verify` y `mvn -Pmysql-it verify` pasan (41 + 6 pruebas). Se corrigió un error de JDO (ver VERIFICACION.md). Falta repetirlo en Windows con MySQL 8.4 |
| Pruebas | No hay pruebas unitarias de los servicios con base simulada; la cobertura de servicios depende de la integración con MySQL |
| Reportes | Sin exportación a PDF/CSV: se imprime desde el navegador |
| Ventas | No se pueden anular ventas ni editar una venta registrada (decisión: evita inconsistencias; confirmar si la consigna lo pide) |
| Autenticación | Sin login ni roles, por indicación; CSRF activo |
| Documentación | Capturas de pantalla y guía de defensa oral con resultados reales tras la primera ejecución |

## Decisiones tomadas que conviene confirmar

0. DNI de 7 u 8 dígitos único entre todas las personas; un vehículo EN_TEST_DRIVE no se puede vender; el precio de venta es editable y queda congelado; el vendedor del test drive es opcional; clientes con test drive abierto no se pueden dar de baja.

1. **JDO** en lugar de JPA (coherencia con Gestor_GYM): confirmar que la consigna de Programación 2 lo permite o exige.
2. **`TipoMoto` como enum** en lugar de texto libre, y `BigDecimal` en lugar de `double`: mejoras justificadas, pero cambian el modelo original.
3. Se agregaron atributos nuevos a los vehículos (año, versión, km, color, combustible, transmisión, descripción, foto), como pidió la consigna.

## Funcionalidades del sistema original

| Original | Estado en SIGC |
|---|---|
| Agregar vehículo (Auto, Moto, Camión) | Hecho (web) |
| Mostrar disponibles y ordenar por precio | Hecho |
| Precio promedio del inventario disponible | Hecho (dashboard e inventario) |
| Registrar cliente y vender | Hecho (web), sin ejecutar aún |
| Test drive | Hecho (web), sin ejecutar aún |
| Registrar vendedor | Hecho (web), sin ejecutar aún |
| Persistencia en `vehiculos.dat` | Reemplazada por MySQL |

No se eliminó ninguna funcionalidad. "Hecho" significa implementado; no verificado en ejecución salvo lo indicado en [VERIFICACION.md](VERIFICACION.md).
