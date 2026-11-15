package com.sigc.servicio;

import com.sigc.dominio.TipoVehiculo;
import com.sigc.dominio.Venta;
import com.sigc.servicio.vista.FiltroVentas;
import com.sigc.servicio.vista.FilaReporte;
import com.sigc.servicio.vista.ReporteVentas;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Reportes de ventas por vendedor, por periodo y por tipo de vehiculo, desde los datos reales. */
@Service
public class ReporteService {
    private final VentaService ventas;

    /**
     * @param ventas consulta de ventas
     */
    public ReporteService(VentaService ventas) {
        this.ventas = ventas;
    }

    /**
     * @param desde primer dia incluido, o null
     * @param hasta ultimo dia incluido, o null
     * @return reporte del rango; si ambas fechas existen, los meses sin ventas aparecen en cero
     */
    public ReporteVentas generar(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde))
            throw new com.sigc.dominio.ReglaNegocioException(
                    "La fecha final no puede ser anterior a la inicial.");
        List<Venta> lista = ventas.listar(new FiltroVentas(null, null, desde, hasta));

        Map<String, List<Venta>> porVendedor = new LinkedHashMap<>();
        for (Venta v : lista)
            porVendedor.computeIfAbsent(v.getVendedor().getNombreCompleto(), k -> new ArrayList<>()).add(v);

        TreeMap<YearMonth, List<Venta>> porMes = new TreeMap<>();
        if (desde != null && hasta != null)
            for (YearMonth m = YearMonth.from(desde); !m.isAfter(YearMonth.from(hasta)); m = m.plusMonths(1))
                porMes.put(m, new ArrayList<>());
        for (Venta v : lista)
            porMes.computeIfAbsent(YearMonth.from(v.getFechaHora()), k -> new ArrayList<>()).add(v);
        Map<String, List<Venta>> periodos = new LinkedHashMap<>();
        porMes.forEach((mes, vs) -> periodos.put(String.format("%02d/%d", mes.getMonthValue(), mes.getYear()), vs));

        Map<String, List<Venta>> porTipo = new LinkedHashMap<>();
        for (TipoVehiculo t : TipoVehiculo.values()) porTipo.put(t.getEtiqueta(), new ArrayList<>());
        for (Venta v : lista)
            porTipo.get(TipoVehiculo.de(v.getVehiculo()).getEtiqueta()).add(v);

        return new ReporteVentas(
                desde,
                hasta,
                lista.size(),
                suma(lista),
                filas(porVendedor, true),
                filas(periodos, false),
                filas(porTipo, false));
    }

    private static List<FilaReporte> filas(Map<String, List<Venta>> grupos, boolean ordenarPorCantidad) {
        long maximo = grupos.values().stream().mapToLong(List::size).max().orElse(0);
        List<FilaReporte> filas = new ArrayList<>();
        grupos.forEach(
                (etiqueta, vs) ->
                        filas.add(
                                new FilaReporte(
                                        etiqueta,
                                        vs.size(),
                                        suma(vs),
                                        maximo == 0 ? 0 : (int) Math.round(vs.size() * 100.0 / maximo))));
        if (ordenarPorCantidad)
            filas.sort(Comparator.comparingLong(FilaReporte::cantidad).reversed()
                    .thenComparing(FilaReporte::etiqueta));
        return filas;
    }

    private static BigDecimal suma(List<Venta> lista) {
        return lista.stream().map(Venta::getPrecioVenta).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
