package com.sigc.servicio.vista;

import java.time.LocalDate;

/**
 * Criterios de busqueda de ventas. Todos son opcionales; el rango de fechas es inclusivo.
 *
 * @param clienteId solo ventas de este cliente
 * @param vendedorId solo ventas de este vendedor
 * @param desde primer dia incluido
 * @param hasta ultimo dia incluido
 */
public record FiltroVentas(Long clienteId, Long vendedorId, LocalDate desde, LocalDate hasta) {
    /**
     * @return filtro sin criterios
     */
    public static FiltroVentas todas() {
        return new FiltroVentas(null, null, null, null);
    }
}
