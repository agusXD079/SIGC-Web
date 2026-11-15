package com.sigc.servicio.vista;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos del formulario de inicio de una prueba de manejo. */
public class DatosTestDrive {
    @NotNull(message = "Elija el vehículo.")
    private Long vehiculoId;

    @NotNull(message = "Elija el cliente.")
    private Long clienteId;

    /** Vendedor que acompaña; opcional. */
    private Long vendedorId;

    @Size(max = 300, message = "Las observaciones admiten hasta 300 caracteres.")
    private String observaciones;

    public Long getVehiculoId() { return vehiculoId; }
    public void setVehiculoId(Long vehiculoId) { this.vehiculoId = vehiculoId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getVendedorId() { return vendedorId; }
    public void setVendedorId(Long vendedorId) { this.vendedorId = vendedorId; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
