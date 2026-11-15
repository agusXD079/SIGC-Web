package com.sigc.dominio;

import java.math.BigDecimal;

import javax.jdo.annotations.*;

/** Camion: agrega la capacidad de carga en toneladas. */
@PersistenceCapable(table = "CAMIONES", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Camion extends Vehiculo {
    private static final BigDecimal CARGA_MAXIMA = new BigDecimal("9999.99");

    @Column(name = "CAPACIDAD_CARGA_TN", allowsNull = "false", length = 6, scale = 2)
    private BigDecimal capacidadCargaTn;

    /** Constructor reservado a JDO. */
    protected Camion() {}

    /**
     * @param capacidadCargaTn toneladas (mayor que cero); los demas parametros se describen en {@link Vehiculo}
     */
    public Camion(
            String marca,
            String modelo,
            String versionModelo,
            int anio,
            int kilometraje,
            BigDecimal precio,
            String color,
            Combustible combustible,
            Transmision transmision,
            String descripcion,
            BigDecimal capacidadCargaTn) {
        super(marca, modelo, versionModelo, anio, kilometraje, precio, color, combustible,
                transmision, descripcion);
        actualizarEspecifico(capacidadCargaTn);
    }

    /**
     * @param capacidadCargaTn nueva capacidad en toneladas
     */
    public void actualizarEspecifico(BigDecimal capacidadCargaTn) {
        this.capacidadCargaTn =
                Validaciones.importe(capacidadCargaTn, "La capacidad de carga", CARGA_MAXIMA);
    }

    @Override
    public String getTipo() {
        return "Camion";
    }

    @Override
    public String getDetalleEspecifico() {
        return "Carga " + capacidadCargaTn.stripTrailingZeros().toPlainString() + " tn";
    }

    /**
     * @return capacidad de carga en toneladas
     */
    public BigDecimal getCapacidadCargaTn() {
        return capacidadCargaTn;
    }
}
