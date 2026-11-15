package com.sigc.dominio;

/** Operaciones comerciales que puede atravesar un vehiculo (interfaz del sistema original). */
public interface ITransaccionable {
    /**
     * Marca el vehiculo como VENDIDO.
     *
     * @throws VehiculoNoDisponibleException si no esta DISPONIBLE
     */
    void vender() throws VehiculoNoDisponibleException;

    /**
     * Marca el vehiculo como EN_TEST_DRIVE.
     *
     * @throws VehiculoNoDisponibleException si no esta DISPONIBLE
     */
    void hacerTestDrive() throws VehiculoNoDisponibleException;

    /**
     * Resumen de una linea; reemplaza al mostrarInfo() por consola del original.
     *
     * @return descripcion legible
     */
    String mostrarInfo();
}
