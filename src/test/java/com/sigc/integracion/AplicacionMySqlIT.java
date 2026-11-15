package com.sigc.integracion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sigc.dominio.*;
import com.sigc.servicio.*;
import com.sigc.servicio.vista.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Pruebas de integracion contra MySQL real con DataNucleus. Solo corren si existe la variable
 * SIGC_TEST_DB_URL (base cuyo nombre termine en "_test"); asi nunca tocan los datos reales.
 * Ejecucion: {@code mvn -Pmysql-it verify}. Ver docs/VERIFICACION.md.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SIGC_TEST_DB_URL", matches = ".+_test(\\?.*)?")
class AplicacionMySqlIT {

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry r) {
        r.add("sigc.db.url", () -> System.getenv("SIGC_TEST_DB_URL"));
        r.add("sigc.db.user", () -> System.getenv("SIGC_TEST_DB_USER"));
        r.add("sigc.db.password", () -> System.getenv("SIGC_TEST_DB_PASSWORD"));
    }

    @Autowired PersistenceManagerFactory pmf;
    @Autowired VehiculoService vehiculos;
    @Autowired ClienteService clientes;
    @Autowired VendedorService vendedores;
    @Autowired VentaService ventas;
    @Autowired TestDriveService testDrives;
    @Autowired DashboardService dashboard;
    @Autowired ReporteService reportes;

    @BeforeEach
    @AfterEach
    void limpiar() {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.currentTransaction().begin();
            // Borrado objeto por objeto: respeta las tablas de las subclases y el orden de las claves foraneas.
            for (Class<?> clase : List.of(TestDrive.class, Venta.class, Vehiculo.class, Persona.class)) {
                javax.jdo.Query<?> q = pm.newQuery(clase);
                try {
                    pm.deletePersistentAll(new ArrayList<Object>((java.util.Collection<?>) q.execute()));
                } finally {
                    q.closeAll();
                }
                pm.flush();
            }
            pm.currentTransaction().commit();
        } finally {
            if (pm.currentTransaction().isActive()) pm.currentTransaction().rollback();
            pm.close();
        }
    }

    private long auto(String modelo) {
        DatosVehiculo d = new DatosVehiculo();
        d.setTipo(TipoVehiculo.AUTO);
        d.setMarca("Toyota");
        d.setModelo(modelo);
        d.setAnio(2021);
        d.setKilometraje(1000);
        d.setPrecio(new BigDecimal("10000000"));
        d.setCombustible(Combustible.NAFTA);
        d.setTransmision(Transmision.MANUAL);
        d.setPuertas(4);
        return vehiculos.crear(d, null);
    }

    private DatosPersona persona(String apellido, String dni, String legajo) {
        DatosPersona p = new DatosPersona();
        p.setNombre("Test");
        p.setApellido(apellido);
        p.setDni(dni);
        p.setLegajo(legajo);
        return p;
    }

    private DatosVenta datosVenta(long veh, long cli, long ven) {
        DatosVenta d = new DatosVenta();
        d.setVehiculoId(veh);
        d.setClienteId(cli);
        d.setVendedorId(ven);
        d.setPrecioVenta(new BigDecimal("9500000"));
        d.setModalidadPago(ModalidadPago.CONTADO);
        d.setClave(ventas.nuevaClave());
        return d;
    }

    @Test
    void dniUnicoEntreClientes() {
        clientes.crear(persona("Gomez", "20111222", null));
        assertThatThrownBy(() -> clientes.crear(persona("Otro", "20.111.222", null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void ventaCompletaYNoSePuedeRepetir() {
        long veh = auto("Corolla");
        long cli = clientes.crear(persona("Gomez", "20111222", null));
        long ven = vendedores.crear(persona("Paz", "30111222", "V1"));
        long id = ventas.registrar(datosVenta(veh, cli, ven));

        assertThat(vehiculos.obtener(veh).getEstado()).isEqualTo(EstadoVehiculo.VENDIDO);
        assertThat(ventas.obtener(id).getNumeroComprobante()).startsWith("V-");
        assertThatThrownBy(() -> ventas.registrar(datosVenta(veh, cli, ven)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(ventas.listar(FiltroVentas.todas())).hasSize(1);
    }

    @Test
    void reenvioConLaMismaClaveNoDuplicaLaVenta() {
        long veh = auto("Etios");
        long cli = clientes.crear(persona("Gomez", "20111222", null));
        long ven = vendedores.crear(persona("Paz", "30111222", "V1"));
        DatosVenta d = datosVenta(veh, cli, ven);
        long primera = ventas.registrar(d);
        long segunda = ventas.registrar(d);
        assertThat(segunda).isEqualTo(primera);
        assertThat(ventas.listar(FiltroVentas.todas())).hasSize(1);
    }

    @Test
    void dosVentasSimultaneasDelMismoVehiculoSoloUnaTieneExito() throws Exception {
        long veh = auto("Hilux");
        long cli = clientes.crear(persona("Gomez", "20111222", null));
        long ven = vendedores.crear(persona("Paz", "30111222", "V1"));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DatosVenta d = datosVenta(veh, cli, ven);
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    ventas.registrar(d);
                    return true;
                } catch (ReglaNegocioException ex) {
                    return false;
                }
            }));
        }
        salida.countDown();
        int exitos = 0;
        for (Future<Boolean> f : resultados) if (f.get(30, TimeUnit.SECONDS)) exitos++;
        pool.shutdown();
        assertThat(exitos).isEqualTo(1);
        assertThat(ventas.listar(FiltroVentas.todas())).hasSize(1);
    }

    @Test
    void testDriveBloqueaLaVentaYAlFinalizarVuelveADisponible() {
        long veh = auto("Yaris");
        long cli = clientes.crear(persona("Gomez", "20111222", null));
        long ven = vendedores.crear(persona("Paz", "30111222", "V1"));
        DatosTestDrive t = new DatosTestDrive();
        t.setVehiculoId(veh);
        t.setClienteId(cli);
        long td = testDrives.iniciar(t);

        assertThat(vehiculos.obtener(veh).getEstado()).isEqualTo(EstadoVehiculo.EN_TEST_DRIVE);
        assertThatThrownBy(() -> ventas.registrar(datosVenta(veh, cli, ven)))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> testDrives.iniciar(t)).isInstanceOf(ReglaNegocioException.class);

        testDrives.finalizar(td, "Todo bien");
        assertThat(vehiculos.obtener(veh).getEstado()).isEqualTo(EstadoVehiculo.DISPONIBLE);
        assertThat(testDrives.listar(EstadoTestDrive.FINALIZADO)).hasSize(1);
    }

    @Test
    void bajaDeClienteConservaSuHistorialYElPanelCuentaDatosReales() {
        long veh = auto("Corolla");
        long cli = clientes.crear(persona("Gomez", "20111222", null));
        long ven = vendedores.crear(persona("Paz", "30111222", "V1"));
        ventas.registrar(datosVenta(veh, cli, ven));
        clientes.darDeBaja(cli);

        assertThat(clientes.historial(cli).ventas()).hasSize(1);
        PanelControl p = dashboard.panel();
        assertThat(p.ventas()).isEqualTo(1);
        assertThat(p.inventario().vendidos()).isEqualTo(1);
        assertThat(p.clientes()).isZero();
        assertThat(p.vendedores()).isEqualTo(1);
        assertThat(reportes.generar(null, null).porVendedor()).hasSize(1);
    }
}
