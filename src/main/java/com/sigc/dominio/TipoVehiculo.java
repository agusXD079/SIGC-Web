package com.sigc.dominio;

/** Tipo de vehiculo elegible en formularios y filtros; se corresponde con las subclases. */
public enum TipoVehiculo {
    AUTO("Auto", Auto.class),
    MOTO("Moto", Moto.class),
    CAMION("Camión", Camion.class);

    private final String etiqueta;
    private final Class<? extends Vehiculo> clase;

    TipoVehiculo(String etiqueta, Class<? extends Vehiculo> clase) {
        this.etiqueta = etiqueta;
        this.clase = clase;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * @return clase persistente correspondiente
     */
    public Class<? extends Vehiculo> getClase() {
        return clase;
    }

    /**
     * @param vehiculo vehiculo concreto
     * @return tipo que le corresponde
     */
    public static TipoVehiculo de(Vehiculo vehiculo) {
        if (vehiculo instanceof Moto) return MOTO;
        if (vehiculo instanceof Camion) return CAMION;
        return AUTO;
    }
}
