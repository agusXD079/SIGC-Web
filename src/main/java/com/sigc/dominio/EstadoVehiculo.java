package com.sigc.dominio;

/** Estado comercial de un vehiculo (conservado del sistema de consola original). */
public enum EstadoVehiculo {
    DISPONIBLE("Disponible"),
    VENDIDO("Vendido"),
    EN_TEST_DRIVE("En test drive");

    private final String etiqueta;

    EstadoVehiculo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
