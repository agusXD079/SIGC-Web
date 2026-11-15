package com.sigc.dominio;

/** Tipo de combustible de un vehiculo. */
public enum Combustible {
    NAFTA("Nafta"),
    DIESEL("Diésel"),
    GNC("GNC"),
    HIBRIDO("Híbrido"),
    ELECTRICO("Eléctrico");

    private final String etiqueta;

    Combustible(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
