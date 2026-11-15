package com.sigc.dominio;

/**
 * Categoria de una moto. Reemplaza al texto libre del sistema original ("deportiva/scooter/cross")
 * para evitar valores inconsistentes en la base.
 */
public enum TipoMoto {
    DEPORTIVA("Deportiva"),
    SCOOTER("Scooter"),
    CROSS("Cross"),
    CUSTOM("Custom"),
    NAKED("Naked");

    private final String etiqueta;

    TipoMoto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
