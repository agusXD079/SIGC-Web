package com.sigc.servicio.vista;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Estadisticas del inventario activo.
 *
 * @param total vehiculos activos
 * @param disponibles vehiculos DISPONIBLES
 * @param vendidos vehiculos VENDIDOS
 * @param enTestDrive vehiculos EN_TEST_DRIVE
 * @param precioPromedioDisponibles promedio de precio de los disponibles (cero si no hay)
 * @param valorDisponibles suma de precios de los disponibles
 * @param porTipo cantidad por etiqueta de tipo (Auto, Moto, Camión)
 */
public record ResumenInventario(
        long total,
        long disponibles,
        long vendidos,
        long enTestDrive,
        BigDecimal precioPromedioDisponibles,
        BigDecimal valorDisponibles,
        Map<String, Long> porTipo) {

    /**
     * @param cantidad valor a expresar
     * @return porcentaje entero respecto del total (0 si no hay vehiculos)
     */
    public int porcentaje(long cantidad) {
        return total == 0 ? 0 : (int) Math.round(cantidad * 100.0 / total);
    }
}
