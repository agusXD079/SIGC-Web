package com.sigc.servicio.vista;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de ventas de un rango de fechas, calculado desde la base de datos.
 *
 * @param desde primer dia incluido, o null
 * @param hasta ultimo dia incluido, o null
 * @param cantidad total de ventas del rango
 * @param monto suma de precios de venta
 * @param porVendedor ventas por vendedor, de mayor a menor cantidad
 * @param porPeriodo ventas por mes, en orden cronologico
 * @param porTipo ventas por tipo de vehiculo (Auto, Moto, Camion)
 */
public record ReporteVentas(
        LocalDate desde,
        LocalDate hasta,
        long cantidad,
        BigDecimal monto,
        List<FilaReporte> porVendedor,
        List<FilaReporte> porPeriodo,
        List<FilaReporte> porTipo) {}
