package com.sigc.dominio;

import java.math.BigDecimal;

import javax.jdo.annotations.*;

/** Automovil: agrega la cantidad de puertas. */
@PersistenceCapable(table = "AUTOS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Auto extends Vehiculo {
    @Column(allowsNull = "false")
    private int puertas;

    /** Constructor reservado a JDO. */
    protected Auto() {}

    /**
     * @param puertas cantidad de puertas (2 a 6); los demas parametros se describen en {@link Vehiculo}
     */
    public Auto(
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
            int puertas) {
        super(marca, modelo, versionModelo, anio, kilometraje, precio, color, combustible,
                transmision, descripcion);
        this.puertas = Validaciones.entero(puertas, "Las puertas", 2, 6);
    }

    /**
     * @param puertas nueva cantidad de puertas (2 a 6)
     */
    public void actualizarEspecifico(int puertas) {
        this.puertas = Validaciones.entero(puertas, "Las puertas", 2, 6);
    }

    @Override
    public String getTipo() {
        return "Auto";
    }

    @Override
    public String getDetalleEspecifico() {
        return puertas + " puertas";
    }

    /**
     * @return cantidad de puertas
     */
    public int getPuertas() {
        return puertas;
    }
}
