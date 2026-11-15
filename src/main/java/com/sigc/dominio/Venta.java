package com.sigc.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.jdo.annotations.*;

/**
 * Operacion de venta. Un vehiculo solo puede tener una venta (restriccion UNIQUE en la base) y el
 * precio pactado queda grabado aunque luego cambie el precio de lista. Las ventas no se modifican
 * ni se borran: son historial.
 */
@PersistenceCapable(table = "VENTAS", detachable = "true")
public class Venta {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @Column(name = "ID")
    private long id;

    @Unique(name = "UQ_VENTA_VEHICULO")
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "VEHICULO_ID", allowsNull = "false")
    private Vehiculo vehiculo;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "CLIENTE_ID", allowsNull = "false")
    private Cliente cliente;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "VENDEDOR_ID", allowsNull = "false")
    private Vendedor vendedor;

    @Column(name = "FECHA_HORA", allowsNull = "false")
    private LocalDateTime fechaHora;

    @Column(name = "PRECIO_VENTA", allowsNull = "false", length = 14, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "MODALIDAD_PAGO", allowsNull = "false", length = 20)
    private ModalidadPago modalidadPago;

    /** UUID de la solicitud: un reenvio del mismo formulario no genera otra venta. */
    @Unique(name = "UQ_VENTA_CLAVE")
    @Column(name = "CLAVE_OPERACION", allowsNull = "false", length = 36)
    private String claveOperacion;

    /** Constructor reservado a JDO. */
    protected Venta() {}

    /**
     * Crea la venta de un vehiculo que ya fue marcado como VENDIDO en la misma transaccion.
     *
     * @param vehiculo vehiculo vendido
     * @param cliente comprador activo
     * @param vendedor vendedor activo
     * @param fechaHora momento de la operacion
     * @param precioVenta precio pactado, mayor que cero
     * @param modalidadPago forma de pago
     * @param claveOperacion UUID de la solicitud
     * @throws ReglaNegocioException si algun dato falta o el vehiculo no esta vendido
     */
    public Venta(
            Vehiculo vehiculo,
            Cliente cliente,
            Vendedor vendedor,
            LocalDateTime fechaHora,
            BigDecimal precioVenta,
            ModalidadPago modalidadPago,
            String claveOperacion) {
        this.vehiculo = Validaciones.requerido(vehiculo, "El vehículo");
        this.cliente = Validaciones.requerido(cliente, "El cliente");
        this.vendedor = Validaciones.requerido(vendedor, "El vendedor");
        this.fechaHora = Validaciones.requerido(fechaHora, "La fecha");
        this.modalidadPago = Validaciones.requerido(modalidadPago, "La modalidad de pago");
        this.precioVenta = Validaciones.importe(precioVenta, "El precio de venta", Vehiculo.PRECIO_MAXIMO);
        if (vehiculo.getEstado() != EstadoVehiculo.VENDIDO)
            throw new ReglaNegocioException("El vehículo debe quedar VENDIDO al registrar la venta.");
        try {
            this.claveOperacion = UUID.fromString(claveOperacion).toString();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ReglaNegocioException(
                    "La clave de operación no es válida. Abra nuevamente el formulario.");
        }
    }

    /**
     * @return numero de comprobante interno, por ejemplo "V-000012"
     */
    public String getNumeroComprobante() {
        return String.format("V-%06d", id);
    }

    /**
     * @return identidad generada por MySQL
     */
    public long getId() {
        return id;
    }

    /**
     * @return vehiculo vendido
     */
    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    /**
     * @return comprador
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * @return vendedor que realizo la operacion
     */
    public Vendedor getVendedor() {
        return vendedor;
    }

    /**
     * @return momento de la operacion
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * @return precio pactado
     */
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    /**
     * @return forma de pago
     */
    public ModalidadPago getModalidadPago() {
        return modalidadPago;
    }

    /**
     * @return UUID de la solicitud que la origino
     */
    public String getClaveOperacion() {
        return claveOperacion;
    }
}
