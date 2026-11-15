package com.sigc.servicio.vista;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos del dashboard principal.
 *
 * @param inventario estadisticas de vehiculos (total, disponibles, vendidos, por tipo, precios)
 * @param clientes clientes activos
 * @param vendedores vendedores activos
 * @param ventas cantidad total de ventas
 * @param montoVentas suma de todas las ventas
 * @param testDrivesEnCurso pruebas de manejo sin finalizar
 * @param ultimas ultimas operaciones, de la mas reciente a la mas antigua
 * @param ultimosMeses reporte de los ultimos seis meses
 */
public record PanelControl(
        ResumenInventario inventario,
        long clientes,
        long vendedores,
        long ventas,
        BigDecimal montoVentas,
        long testDrivesEnCurso,
        List<Operacion> ultimas,
        ReporteVentas ultimosMeses) {}
