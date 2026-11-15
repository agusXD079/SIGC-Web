package com.sigc.dominio;

/** Situacion de una prueba de manejo. */
public enum EstadoTestDrive {
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado");

    private final String etiqueta;

    EstadoTestDrive(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
