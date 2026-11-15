package com.sigc.dominio;

/** Forma en que el cliente abona un vehiculo. */
public enum ModalidadPago {
    CONTADO("Contado"),
    TRANSFERENCIA("Transferencia"),
    TARJETA("Tarjeta"),
    FINANCIADO("Financiado");

    private final String etiqueta;

    ModalidadPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * @return texto para mostrar en las vistas
     */
    public String getEtiqueta() {
        return etiqueta;
    }
}
