package com.sigc.servicio;

/** Fallo de infraestructura de persistencia; la vista muestra un mensaje generico. */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param causa excepcion JDO original, conservada para el registro
     */
    public PersistenciaException(Throwable causa) {
        super("No se pudo completar la operación en la base de datos.", causa);
    }
}
