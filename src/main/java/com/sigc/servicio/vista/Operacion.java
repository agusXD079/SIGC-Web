package com.sigc.servicio.vista;

import java.time.LocalDateTime;

/**
 * Una operacion reciente para el panel (venta o prueba de manejo).
 *
 * @param tipo "Venta" o "Test drive"
 * @param fecha momento de la operacion
 * @param descripcion vehiculo involucrado
 * @param detalle cliente y vendedor
 * @param enlace ruta interna al detalle
 */
public record Operacion(
        String tipo, LocalDateTime fecha, String descripcion, String detalle, String enlace) {}
