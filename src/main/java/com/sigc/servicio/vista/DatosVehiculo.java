package com.sigc.servicio.vista;

import com.sigc.dominio.*;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Datos del formulario de alta y edicion de vehiculos. Las anotaciones validan el formato en el
 * controlador; las reglas de negocio se vuelven a comprobar en el dominio.
 */
public class DatosVehiculo {
    @NotNull(message = "Elija el tipo de vehículo.")
    private TipoVehiculo tipo = TipoVehiculo.AUTO;

    @NotBlank(message = "La marca es obligatoria.")
    @Size(max = 60, message = "La marca admite hasta 60 caracteres.")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio.")
    @Size(max = 80, message = "El modelo admite hasta 80 caracteres.")
    private String modelo;

    @Size(max = 80, message = "La versión admite hasta 80 caracteres.")
    private String versionModelo;

    @NotNull(message = "El año es obligatorio.")
    private Integer anio;

    @NotNull(message = "El kilometraje es obligatorio.")
    @Min(value = 0, message = "El kilometraje no puede ser negativo.")
    private Integer kilometraje;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero.")
    private BigDecimal precio;

    @Size(max = 40, message = "El color admite hasta 40 caracteres.")
    private String color;

    @NotNull(message = "Elija el combustible.")
    private Combustible combustible;

    @NotNull(message = "Elija la transmisión.")
    private Transmision transmision;

    @Size(max = 1000, message = "La descripción admite hasta 1000 caracteres.")
    private String descripcion;

    // Datos propios de cada subclase; se exigen segun el tipo en VehiculoService.
    private Integer puertas;
    private TipoMoto tipoMoto;
    private Integer cilindrada;
    private BigDecimal capacidadCargaTn;

    /**
     * @param v vehiculo detached a editar
     * @return formulario precargado con sus datos
     */
    public static DatosVehiculo desde(Vehiculo v) {
        DatosVehiculo d = new DatosVehiculo();
        d.tipo = TipoVehiculo.de(v);
        d.marca = v.getMarca();
        d.modelo = v.getModelo();
        d.versionModelo = v.getVersionModelo();
        d.anio = v.getAnio();
        d.kilometraje = v.getKilometraje();
        d.precio = v.getPrecio();
        d.color = v.getColor();
        d.combustible = v.getCombustible();
        d.transmision = v.getTransmision();
        d.descripcion = v.getDescripcion();
        if (v instanceof Auto a) d.puertas = a.getPuertas();
        if (v instanceof Moto m) {
            d.tipoMoto = m.getTipoMoto();
            d.cilindrada = m.getCilindrada();
        }
        if (v instanceof Camion c) d.capacidadCargaTn = c.getCapacidadCargaTn();
        return d;
    }

    public TipoVehiculo getTipo() { return tipo; }
    public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getVersionModelo() { return versionModelo; }
    public void setVersionModelo(String versionModelo) { this.versionModelo = versionModelo; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Integer getKilometraje() { return kilometraje; }
    public void setKilometraje(Integer kilometraje) { this.kilometraje = kilometraje; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Combustible getCombustible() { return combustible; }
    public void setCombustible(Combustible combustible) { this.combustible = combustible; }
    public Transmision getTransmision() { return transmision; }
    public void setTransmision(Transmision transmision) { this.transmision = transmision; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Integer getPuertas() { return puertas; }
    public void setPuertas(Integer puertas) { this.puertas = puertas; }
    public TipoMoto getTipoMoto() { return tipoMoto; }
    public void setTipoMoto(TipoMoto tipoMoto) { this.tipoMoto = tipoMoto; }
    public Integer getCilindrada() { return cilindrada; }
    public void setCilindrada(Integer cilindrada) { this.cilindrada = cilindrada; }
    public BigDecimal getCapacidadCargaTn() { return capacidadCargaTn; }
    public void setCapacidadCargaTn(BigDecimal capacidadCargaTn) { this.capacidadCargaTn = capacidadCargaTn; }
}
