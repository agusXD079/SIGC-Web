package com.sigc.servicio;

import com.sigc.dominio.EstadoTestDrive;
import com.sigc.dominio.TestDrive;
import com.sigc.dominio.Venta;
import com.sigc.servicio.vista.FiltroVentas;
import com.sigc.servicio.vista.Operacion;
import com.sigc.servicio.vista.PanelControl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Arma el panel principal combinando inventario, clientes, vendedores, ventas y pruebas. */
@Service
public class DashboardService {
    private static final int MAX_OPERACIONES = 8;

    private final VehiculoService vehiculos;
    private final ClienteService clientes;
    private final VendedorService vendedores;
    private final VentaService ventas;
    private final TestDriveService testDrives;
    private final ReporteService reportes;
    private final Clock reloj;

    /**
     * @param vehiculos inventario
     * @param clientes clientes
     * @param vendedores vendedores
     * @param ventas ventas
     * @param testDrives pruebas de manejo
     * @param reportes reportes de ventas
     * @param reloj reloj de negocio
     */
    public DashboardService(
            VehiculoService vehiculos,
            ClienteService clientes,
            VendedorService vendedores,
            VentaService ventas,
            TestDriveService testDrives,
            ReporteService reportes,
            Clock reloj) {
        this.vehiculos = vehiculos;
        this.clientes = clientes;
        this.vendedores = vendedores;
        this.ventas = ventas;
        this.testDrives = testDrives;
        this.reportes = reportes;
        this.reloj = reloj;
    }

    /**
     * @return datos del dashboard, todos leidos de la base de datos
     */
    public PanelControl panel() {
        List<Venta> todas = ventas.listar(FiltroVentas.todas());
        List<TestDrive> pruebas = testDrives.listar(null);
        BigDecimal monto =
                todas.stream().map(Venta::getPrecioVenta).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Operacion> operaciones = new ArrayList<>();
        for (Venta v : todas)
            operaciones.add(
                    new Operacion(
                            "Venta",
                            v.getFechaHora(),
                            v.getVehiculo().getTitulo(),
                            v.getCliente().getNombreCompleto() + " · " + v.getVendedor().getNombreCompleto(),
                            "/ventas/" + v.getId()));
        for (TestDrive t : pruebas)
            operaciones.add(
                    new Operacion(
                            "Test drive",
                            t.getFechaInicio(),
                            t.getVehiculo().getTitulo(),
                            t.getCliente().getNombreCompleto() + " · " + t.getEstado().getEtiqueta(),
                            "/testdrives"));
        operaciones.sort(Comparator.comparing(Operacion::fecha).reversed());

        LocalDate hoy = LocalDate.now(reloj);
        return new PanelControl(
                vehiculos.resumen(),
                clientes.listar(null, false).size(),
                vendedores.listar(null, false).size(),
                todas.size(),
                monto,
                pruebas.stream().filter(t -> t.getEstado() == EstadoTestDrive.EN_CURSO).count(),
                operaciones.subList(0, Math.min(MAX_OPERACIONES, operaciones.size())),
                reportes.generar(hoy.minusMonths(5).withDayOfMonth(1), hoy));
    }
}
