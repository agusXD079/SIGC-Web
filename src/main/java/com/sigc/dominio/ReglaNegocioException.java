package com.sigc.dominio;

/** Violacion de una regla de negocio o de validacion; el mensaje se muestra al usuario. */
public class ReglaNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje explicacion clara y apta para el usuario
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
