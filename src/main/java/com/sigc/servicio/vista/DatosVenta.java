package com.sigc.servicio.vista;

import com.sigc.dominio.ModalidadPago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Datos del formulario de venta. La fecha la registra el servidor con el reloj de negocio. */
public class DatosVenta {
    @NotNull(message = "Elija el vehículo.")
    private Long vehiculoId;

    @NotNull(message = "Elija el cliente.")
    private Long clienteId;

    @NotNull(message = "Elija el vendedor.")
    private Long vendedorId;

    @NotNull(message = "El precio de venta es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero.")
    private BigDecimal precioVenta;

    @NotNull(message = "Elija la modalidad de pago.")
    private ModalidadPago modalidadPago;

    /** UUID generado al abrir el formulario; evita ventas duplicadas por reenvio. */
    @NotNull(message = "Falta la clave de operación; abra nuevamente el formulario.")
    private String clave;

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    public ModalidadPago getModalidadPago() { return modalidadPago; }
    public void setModalidadPago(ModalidadPago modalidadPago) { this.modalidadPago = modalidadPago; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
