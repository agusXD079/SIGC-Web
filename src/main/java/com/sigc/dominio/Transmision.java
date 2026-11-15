package com.sigc.dominio;

/** Tipo de transmision de un vehiculo. */
public enum Transmision {
    MANUAL("Manual"),
    AUTOMATICA("Automática");

    private final String etiqueta;

    Transmision(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
