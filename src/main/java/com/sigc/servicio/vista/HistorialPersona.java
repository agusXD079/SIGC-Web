package com.sigc.servicio.vista;

import com.sigc.dominio.Persona;
import com.sigc.dominio.TestDrive;
import com.sigc.dominio.Venta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ficha de un cliente o vendedor con su historial.
 *
 * @param persona cliente o vendedor detached
 * @param ventas compras (cliente) o ventas realizadas (vendedor), de la mas reciente a la mas antigua
 * @param testDrives pruebas de manejo asociadas
 * @param montoTotal suma de los precios de venta de {@code ventas}
 */
public record HistorialPersona(
        Persona persona, List<Venta> ventas, List<TestDrive> testDrives, BigDecimal montoTotal) {}
