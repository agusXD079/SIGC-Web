package com.sigc.servicio;

import com.sigc.dominio.*;
import com.sigc.repositorio.ClienteRepositorio;
import com.sigc.repositorio.TestDriveRepositorio;
import com.sigc.repositorio.VehiculoRepositorio;
import com.sigc.repositorio.VendedorRepositorio;
import com.sigc.servicio.vista.DatosTestDrive;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas de manejo. Iniciar una prueba pasa el vehiculo a EN_TEST_DRIVE y finalizarla lo devuelve
 * a DISPONIBLE, siempre dentro de una misma transaccion con el vehiculo bloqueado.
 */
@Service
public class TestDriveService {
    private final UnidadTrabajo unidad;
    private final TestDriveRepositorio testDrives;
    private final VehiculoRepositorio vehiculos;
    private final ClienteRepositorio clientes;
    private final VendedorRepositorio vendedores;
    private final Clock reloj;

    /**
     * @param unidad limite transaccional
     * @param testDrives acceso a pruebas de manejo
     * @param vehiculos acceso a vehiculos
     * @param clientes acceso a clientes
     * @param vendedores acceso a vendedores
     * @param reloj reloj de negocio
     */
    public TestDriveService(
            UnidadTrabajo unidad,
            TestDriveRepositorio testDrives,
            VehiculoRepositorio vehiculos,
            ClienteRepositorio clientes,
            VendedorRepositorio vendedores,
            Clock reloj) {
        this.unidad = unidad;
        this.testDrives = testDrives;
        this.vehiculos = vehiculos;
        this.clientes = clientes;
        this.vendedores = vendedores;
        this.reloj = reloj;
    }

    /**
     * Abre una prueba de manejo y deja el vehiculo EN_TEST_DRIVE.
     *
     * @param datos formulario validado
     * @return identidad de la prueba
     * @throws ReglaNegocioException si el vehiculo no esta disponible o cliente/vendedor estan de baja
     */
    public long iniciar(DatosTestDrive datos) {
        return unidad.escribir(
                pm -> {
                    Vehiculo vehiculo = vehiculos.porId(pm, datos.getVehiculoId());
                    pm.retrieve(vehiculo);
                    Cliente cliente = clientes.porId(pm, datos.getClienteId());
                    if (!cliente.isActivo())
                        throw new ReglaNegocioException("El cliente está dado de baja.");
                    Vendedor vendedor = null;
                    if (datos.getVendedorId() != null) {
                        vendedor = vendedores.porId(pm, datos.getVendedorId());
                        if (!vendedor.isActivo())
                            throw new ReglaNegocioException("El vendedor está dado de baja.");
                    }
                    try {
                        vehiculo.hacerTestDrive();
                    } catch (VehiculoNoDisponibleException ex) {
                        throw new ReglaNegocioException(ex.getMessage());
                    }
                    TestDrive prueba =
                            testDrives.guardar(
                                    pm,
                                    new TestDrive(
                                            vehiculo, cliente, vendedor, LocalDateTime.now(reloj),
                                            datos.getObservaciones()));
                    pm.flush();
                    return prueba.getId();
                });
    }

    /**
     * Cierra la prueba y devuelve el vehiculo a DISPONIBLE.
     *
     * @param id identidad de la prueba
     * @param observaciones notas finales opcionales
     * @throws ReglaNegocioException si ya estaba finalizada
     */
    public void finalizar(long id, String observaciones) {
        unidad.escribir(
                pm -> {
                    TestDrive prueba = testDrives.porId(pm, id);
                    pm.retrieve(prueba);
                    Vehiculo vehiculo = prueba.getVehiculo();
                    pm.retrieve(vehiculo);
                    prueba.finalizar(LocalDateTime.now(reloj), observaciones);
                    vehiculo.finalizarTestDrive();
                    return null;
                });
    }

    /**
     * @param estado estado a exigir, o null para todas
     * @return pruebas detached, de la mas reciente a la mas antigua
     */
    public List<TestDrive> listar(EstadoTestDrive estado) {
        return unidad.leer(
                pm ->
                        new ArrayList<>(
                                pm.detachCopyAll(testDrives.buscar(pm, estado, null, null, null))));
    }

    /**
     * @param vehiculoId identidad del vehiculo
     * @return historial de pruebas de ese vehiculo
     */
    public List<TestDrive> deVehiculo(long vehiculoId) {
        return unidad.leer(
                pm ->
                        new ArrayList<>(
                                pm.detachCopyAll(
                                        testDrives.buscar(pm, null, null, vehiculoId, null))));
    }
}
