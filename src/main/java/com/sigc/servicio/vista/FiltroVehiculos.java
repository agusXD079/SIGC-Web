package com.sigc.servicio.vista;

import com.sigc.dominio.EstadoVehiculo;
import com.sigc.dominio.TipoVehiculo;

import java.math.BigDecimal;

/**
 * Criterios de busqueda de vehiculos. Todos son opcionales.
 *
 * @param texto fragmento de marca o modelo
 * @param tipo AUTO, MOTO o CAMION
 * @param estado estado comercial
 * @param anioDesde anio minimo
 * @param anioHasta anio maximo
 * @param precioMin precio minimo
 * @param precioMax precio maximo
 * @param orden PRECIO_ASC, PRECIO_DESC o RECIENTES (por defecto)
 * @param incluirBajas true para listar tambien los dados de baja
 */
public record FiltroVehiculos(
        String texto,
        TipoVehiculo tipo,
        EstadoVehiculo estado,
        Integer anioDesde,
        Integer anioHasta,
        BigDecimal precioMin,
        BigDecimal precioMax,
        String orden,
        boolean incluirBajas) {

    /**
     * @return filtro sin criterios, solo vehiculos activos
     */
    public static FiltroVehiculos todos() {
        return new FiltroVehiculos(null, null, null, null, null, null, null, null, false);
    }

    /**
     * @param nuevo estado a exigir
     * @return copia del filtro con ese estado
     */
    public FiltroVehiculos conEstado(EstadoVehiculo nuevo) {
        return new FiltroVehiculos(
                texto, tipo, nuevo, anioDesde, anioHasta, precioMin, precioMax, orden, incluirBajas);
    }
}
