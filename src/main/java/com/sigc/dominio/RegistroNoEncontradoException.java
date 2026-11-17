package com.sigc.dominio;

/**
 * El registro pedido no existe. Es una {@link ReglaNegocioException} para que los formularios la
 * sigan mostrando como mensaje; cuando nadie la captura, la pagina de error responde HTTP 404.
 */
public class RegistroNoEncontradoException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje explicacion clara y apta para el usuario
     */
    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
