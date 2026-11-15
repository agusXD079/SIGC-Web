package com.sigc.dominio;

/**
 * Excepcion verificada del sistema original: se lanza al vender o hacer test drive de un vehiculo
 * que no esta DISPONIBLE. Los servicios la traducen a {@link ReglaNegocioException} para la vista.
 */
public class VehiculoNoDisponibleException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje detalle del estado que impide la operacion
     */
    public VehiculoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
