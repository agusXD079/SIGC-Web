# Guía de defensa y cumplimiento de la consigna

Documento para preparar la presentación del Proyecto integrador grupal. Cada punto de la consigna está asociado a dónde se cumple en el código. Cada integrante debe poder recorrerlo.

## 1. Requisitos técnicos

| Requisito de la consigna | Dónde se cumple |
|---|---|
| Maven, con dependencias y plugins en el `pom.xml` | `pom.xml`: Spring Boot 3.5, Thymeleaf, Validation, Security (solo CSRF), DataNucleus (core, api-jdo, rdbms), MySQL Connector/J, Bootstrap (WebJar); plugin `datanucleus-maven-plugin` (enhancer en `process-classes`) y `spring-boot-maven-plugin` |
| Al menos tres entidades persistentes relacionadas | `Vehiculo`, `Persona` (`Cliente`, `Vendedor`), `Venta` y `TestDrive` en `src/main/java/com/sigc/dominio/` |
| Al menos una relación uno a muchos | `Cliente` 1:N `Venta`, declarada de los dos lados: `Cliente.compras` (`@Persistent(mappedBy = "cliente")`, un `Set<Venta>`) y `Venta.cliente`; usa la clave foránea `VENTAS.CLIENTE_ID`. Además `Vendedor` 1:N `Venta`, `Cliente` 1:N `TestDrive` y `Vehiculo` 1:N `TestDrive`, declaradas del lado "muchos" |
| Encapsulamiento y tipos adecuados | Atributos privados con getters; `BigDecimal` para dinero, `LocalDate`/`LocalDateTime` para fechas, enums para estados |
| Persistencia con DataNucleus JDO | Anotaciones JDO en el dominio; `META-INF/persistence.xml`; `configuracion/Configuracion` crea la `PersistenceManagerFactory` |
| Los datos se mantienen al reiniciar | Base MySQL; DataNucleus crea las tablas en el primer arranque |
| Transacciones y recursos de persistencia | `servicio/UnidadTrabajo`: cada caso de uso abre un `PersistenceManager`, inicia la transacción, confirma o revierte y siempre cierra |
| Capas dominio, repositorio, servicio, controlador | Paquetes `dominio`, `repositorio`, `servicio`, `controlador` |
| Acceso a datos en repositorios y reglas en servicios | `repositorio/*Repositorio` solo consulta y guarda; las reglas están en `servicio/*Service` y en las entidades |
| Spring administra componentes e inyecta dependencias | `@Service`, `@Repository`, `@Controller`, `@Component` con inyección por constructor |
| Sin acceso a repositorios desde las vistas | Las plantillas solo reciben datos del `Model`; los controladores dependen únicamente de servicios |
| Spring Boot y MVC, controladores, `Model` | `SigcApplication`; clases del paquete `controlador` |
| Thymeleaf, HTML, CSS y Bootstrap | `src/main/resources/templates/`, `static/css/sigc.css`, WebJar de Bootstrap 5 |

## 2. Funcionalidades mínimas

| N.º | Requisito | Dónde |
|---|---|---|
| 1 | Registrar, listar, modificar y eliminar al menos dos entidades | Vehículos (`/vehiculos`), clientes (`/clientes`) y vendedores (`/vendedores`). "Eliminar" es una **baja lógica** con reactivación, permitida por el punto 4 de la consigna: así no se pierden ventas ni historial |
| 2 | Consultar la tercera entidad y usarla en una funcionalidad | Ventas (`/ventas`, comprobante) y test drives (`/testdrives`); las ventas alimentan el dashboard y los reportes |
| 3 | Al menos una búsqueda o filtro | Búsqueda de vehículos por marca/modelo con filtros de tipo, estado, año y precio; búsqueda de clientes y vendedores por nombre, apellido o DNI; filtro de ventas por cliente, vendedor y fechas |
| 4 | Crear y consultar relaciones desde la web | La venta une vehículo, cliente y vendedor; la ficha del cliente muestra sus compras (se obtienen recorriendo `cliente.getCompras()` en `ClienteService.historial`) y pruebas; la del vehículo, su venta y test drives |
| 5 | Una operación propia del negocio | **Vender un vehículo** (`VentaService.registrar`) y **test drive** (`TestDriveService.iniciar` / `finalizar`) |

Validaciones propias del tema: solo se vende un vehículo `DISPONIBLE`, nunca dos veces; un vehículo en test drive no se vende; el cliente y el vendedor deben estar activos; DNI de 7 u 8 dígitos único; legajo único. Detalle en [REGLAS_NEGOCIO.md](REGLAS_NEGOCIO.md).

## 3. Validaciones e interfaz

- Campos obligatorios y valores: `jakarta.validation` en los DTO (`servicio/vista`) y reglas en el dominio (`Validaciones`).
- Mensajes de confirmación y error: banner superior en cada pantalla (`fragmentos.html`) y página `error.html`. Los errores de conversión (por ejemplo un texto en un campo numérico) se muestran en castellano gracias a `src/main/resources/messages.properties`.
- Navegación: menú lateral con todas las secciones.
- Eliminación de registros relacionados: baja lógica; un cliente con test drive abierto no se puede dar de baja y el historial de ventas se conserva.

## 4. Entrega

| Elemento | Dónde |
|---|---|
| Código fuente completo y `pom.xml` | Raíz del repositorio |
| Configuración sin contraseñas | `application.properties` toma usuario y clave de variables de entorno (`SIGC_DB_USER`, `SIGC_DB_PASSWORD`) |
| Scripts de base de datos | `db/crear-bases.sql`, `db/esquema-referencia.sql` |
| README con nombre, integrantes, descripción, funcionalidades e instrucciones | `README.md` |

## 5. Para la defensa

### Recorrido de una operación, de la vista a la base de datos (vender)

1. `ventas/form.html` (Thymeleaf + Bootstrap) envía `POST /ventas` con token CSRF.
2. `VentaController.registrar` valida `DatosVenta` y llama a `VentaService`.
3. `VentaService.registrar` abre la transacción con `UnidadTrabajo.escribir`, busca vehículo, cliente y vendedor a través de sus repositorios y llama a `Vehiculo.vender()`.
4. `Vehiculo.vender()` cambia el estado a `VENDIDO` o lanza `VehiculoNoDisponibleException`.
5. `VentaRepositorio` persiste la `Venta` con JDO; `UnidadTrabajo` confirma (o revierte si hubo error) y cierra el `PersistenceManager`.
6. El controlador redirige al comprobante, que muestra los datos con Thymeleaf.

### Responsabilidad de cada capa

- **Dominio:** estado y reglas de las entidades.
- **Repositorio:** consultas y escrituras JDO, sin reglas de negocio.
- **Servicio:** casos de uso y transacciones.
- **Controlador:** recibir la solicitud, validar el formulario, elegir la vista.
- **Vista:** mostrar datos; no accede a datos.

### Uso de cada tecnología

- **Maven:** compila, ejecuta el *enhancer* de JDO (`process-classes`), corre las pruebas y empaqueta el JAR ejecutable (`mvn clean verify`).
- **Spring / Spring Boot:** crea y conecta los componentes; arranca el servidor web embebido.
- **JDO / DataNucleus:** mapea las entidades a tablas MySQL; el *enhancer* modifica las clases compiladas para que JDO detecte los cambios.
- **MVC:** el controlador recibe la solicitud, el servicio trabaja y la vista muestra el resultado.
- **Thymeleaf:** genera el HTML con los datos del `Model` (`th:text`, `th:each`, `th:field`, fragmentos).

### Preguntas probables

- ¿Por qué baja lógica y no borrado físico? Para no dejar ventas sin cliente ni vehículo.
- ¿Qué pasa si dos usuarios venden el mismo auto a la vez? El bloqueo de la fila y la restricción `UNIQUE(vehiculo_id)` hacen que solo una venta tenga éxito.
- ¿Por qué `BigDecimal` para el precio? Evita errores de redondeo de `double`.
- ¿Dónde se cierra el `PersistenceManager`? En `UnidadTrabajo`, al terminar cada caso de uso.
- ¿Dónde está la relación uno a muchos? En `Cliente.compras` (`mappedBy = "cliente"`): un cliente tiene muchas ventas y cada `Venta` guarda su `cliente`. En la base es la clave foránea `VENTAS.CLIENTE_ID`. `mappedBy` le dice a JDO que no cree otra tabla, porque la relación ya está guardada en esa columna.

### Participación de cada integrante

Completar antes de la defensa, con lo que realmente hizo cada uno.

| Integrante | Parte del proyecto que desarrolló y puede explicar en profundidad |
|---|---|
| Thiago Vecchi | |
| Franco Araneda | |
| Tiago Silenzi | |
| Luz Araneda | |

## 6. Propuesta (para el documento de la planilla de grupos)

- **Proyecto:** SIGC · Sistema Integral de Gestión de Concesionaria.
- **Problema:** una concesionaria necesita controlar su inventario, sus clientes y vendedores, las ventas y las pruebas de manejo sin planillas sueltas.
- **Entidades:** Vehículo (Auto, Moto, Camión), Persona (Cliente, Vendedor), Venta y TestDrive.
- **Relaciones:** Cliente 1:N Venta; Vendedor 1:N Venta; Cliente 1:N TestDrive; Vehículo 1:N TestDrive; Vehículo 1:0..1 Venta.
- **Funcionalidades:** ABM de vehículos, clientes y vendedores; búsqueda y filtros; venta y comprobante; test drive; dashboard y reportes.
