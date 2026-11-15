package com.sigc.servicio.vista;

import java.math.BigDecimal;

/**
 * Una fila de un reporte de ventas.
 *
 * @param etiqueta vendedor, periodo o tipo de vehiculo
 * @param cantidad ventas del grupo
 * @param monto suma de precios de venta del grupo
 * @param ancho largo relativo (0 a 100) de la barra, respecto del grupo con mas ventas
 */
public record FilaReporte(String etiqueta, long cantidad, BigDecimal monto, int ancho) {}
