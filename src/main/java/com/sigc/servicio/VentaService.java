package com.sigc.servicio;

import com.sigc.dominio.*;
import com.sigc.repositorio.ClienteRepositorio;
import com.sigc.repositorio.VehiculoRepositorio;
import com.sigc.repositorio.VendedorRepositorio;
import com.sigc.repositorio.VentaRepositorio;
import com.sigc.servicio.vista.DatosVenta;
import com.sigc.servicio.vista.FiltroVentas;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registro de ventas. La venta, el cambio de estado del vehiculo y la clave de operacion se
 * confirman o se revierten juntos. El vehiculo se lee primero con bloqueo de fila: dos ventas
 * simultaneas del mismo vehiculo se serializan y la segunda ve el estado VENDIDO. Como ultima
 * defensa, VENTAS.VEHICULO_ID es UNIQUE en la base.
 */
@Service
public class VentaService {
    private final UnidadTrabajo unidad;
    private final VentaRepositorio ventas;
    private final VehiculoRepositorio vehiculos;
    private final ClienteRepositorio clientes;
    private final VendedorRepositorio vendedores;
    private final Clock reloj;

    /**
     * @param unidad limite transaccional
     * @param ventas acceso a ventas
     * @param vehiculos acceso a vehiculos
     * @param clientes acceso a clientes
     * @param vendedores acceso a vendedores
     * @param reloj reloj de negocio
     */
    public VentaService(
            UnidadTrabajo unidad,
            VentaRepositorio ventas,
            VehiculoRepositorio vehiculos,
            ClienteRepositorio clientes,
            VendedorRepositorio vendedores,
            Clock reloj) {
        this.unidad = unidad;
        this.ventas = ventas;
        this.vehiculos = vehiculos;
        this.clientes = clientes;
        this.vendedores = vendedores;
        this.reloj = reloj;
    }

    /**
     * @return nueva clave de operacion para un formulario recien abierto
     */
    public String nuevaClave() {
        return UUID.randomUUID().toString();
    }

    /**
     * Registra una venta de forma atomica. Si la solicitud ya fue procesada (mismo UUID) devuelve
     * la venta existente sin vender otra vez.
     *
     * @param datos formulario validado
     * @return identidad de la venta registrada o ya confirmada
     * @throws ReglaNegocioException si el vehiculo no esta disponible, el cliente o vendedor estan
     *     de baja, o la clave se uso con otros datos
     */
    public long registrar(DatosVenta datos) {
        String clave;
        try {
            clave = UUID.fromString(datos.getClave()).toString();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ReglaNegocioException(
                    "La clave de operación no es válida. Abra nuevamente el formulario.");
        }
        return unidad.escribir(
                pm -> {
                    Vehiculo vehiculo = vehiculos.porId(pm, datos.getVehiculoId());
                    pm.retrieve(vehiculo);
                    Venta previa = ventas.porClave(pm, clave);
                    if (previa != null) {
                        if (previa.getVehiculo().getId() != datos.getVehiculoId()
                                || previa.getCliente().getId() != datos.getClienteId()
                                || previa.getVendedor().getId() != datos.getVendedorId()
                                || previa.getModalidadPago() != datos.getModalidadPago())
                            throw new ReglaNegocioException(
                                    "La clave ya fue utilizada con otros datos; no se registró otra venta.");
                        return previa.getId();
                    }
                    Cliente cliente = clientes.porId(pm, datos.getClienteId());
                    Vendedor vendedor = vendedores.porId(pm, datos.getVendedorId());
                    if (!cliente.isActivo())
                        throw new ReglaNegocioException("El cliente está dado de baja.");
                    if (!vendedor.isActivo())
                        throw new ReglaNegocioException("El vendedor está dado de baja.");
                    try {
                        vehiculo.vender();
                    } catch (VehiculoNoDisponibleException ex) {
                        throw new ReglaNegocioException(ex.getMessage());
                    }
                    Venta venta =
                            ventas.guardar(
                                    pm,
                                    new Venta(
                                            vehiculo, cliente, vendedor, LocalDateTime.now(reloj),
                                            datos.getPrecioVenta(), datos.getModalidadPago(), clave));
                    pm.flush();
                    return venta.getId();
                });
    }

    /**
     * @param id identidad de la venta
     * @return venta detached con vehiculo, cliente y vendedor, para el comprobante
     */
    public Venta obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(ventas.porId(pm, id)));
    }

    /**
     * @param filtro criterios de busqueda
     * @return historial de ventas detached, de la mas reciente a la mas antigua
     */
    public List<Venta> listar(FiltroVentas filtro) {
        return unidad.leer(pm -> new ArrayList<>(pm.detachCopyAll(ventas.buscar(pm, filtro))));
    }

    /**
     * @param vehiculoId identidad del vehiculo
     * @return su venta detached, o null si no se vendio
     */
    public Venta deVehiculo(long vehiculoId) {
        return unidad.leer(
                pm -> {
                    Venta v = ventas.porVehiculo(pm, vehiculoId);
                    return v == null ? null : pm.detachCopy(v);
                });
    }
}
