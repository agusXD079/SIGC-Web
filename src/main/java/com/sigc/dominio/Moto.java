package com.sigc.dominio;

import java.math.BigDecimal;

import javax.jdo.annotations.*;

/** Motocicleta: agrega categoria y cilindrada. */
@PersistenceCapable(table = "MOTOS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Moto extends Vehiculo {
    @Column(name = "TIPO_MOTO", allowsNull = "false", length = 20)
    private TipoMoto tipoMoto;

    @Column(allowsNull = "false")
    private int cilindrada;

    /** Constructor reservado a JDO. */
    protected Moto() {}

    /**
     * @param tipoMoto categoria
     * @param cilindrada cilindrada en cc (50 a 3000); los demas parametros se describen en {@link Vehiculo}
     */
    public Moto(
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
            TipoMoto tipoMoto,
            int cilindrada) {
        super(marca, modelo, versionModelo, anio, kilometraje, precio, color, combustible,
                transmision, descripcion);
        actualizarEspecifico(tipoMoto, cilindrada);
    }

    /**
     * @param tipoMoto nueva categoria
     * @param cilindrada nueva cilindrada en cc (50 a 3000)
     */
    public void actualizarEspecifico(TipoMoto tipoMoto, int cilindrada) {
        this.tipoMoto = Validaciones.requerido(tipoMoto, "El tipo de moto");
        this.cilindrada = Validaciones.entero(cilindrada, "La cilindrada", 50, 3000);
    }

    @Override
    public String getTipo() {
        return "Moto";
    }

    @Override
    public String getDetalleEspecifico() {
        return tipoMoto.getEtiqueta() + " · " + cilindrada + " cc";
    }

    /**
     * @return categoria
     */
    public TipoMoto getTipoMoto() {
        return tipoMoto;
    }

    /**
     * @return cilindrada en cc
     */
    public int getCilindrada() {
        return cilindrada;
    }
}
