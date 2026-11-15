package com.sigc.repositorio;

import com.sigc.dominio.Vehiculo;
import com.sigc.servicio.vista.FiltroVehiculos;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Busquedas de vehiculos; el texto externo viaja siempre como parametro, nunca en el JDOQL. */
@Repository
public class VehiculoRepositorio extends RepositorioJdo<Vehiculo> {
    /** Configura el tipo Vehiculo. */
    public VehiculoRepositorio() {
        super(Vehiculo.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param f criterios de busqueda
     * @return vehiculos gestionados por pm, ya ordenados
     */
    public List<Vehiculo> buscar(PersistenceManager pm, FiltroVehiculos f) {
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> p = new HashMap<>();
        if (!f.incluirBajas()) condiciones.add("activo == true");
        if (f.texto() != null && !f.texto().isBlank()) {
            // Cada palabra debe aparecer en la marca o en el modelo ("toyota corolla" encuentra ambos).
            int i = 0;
            for (String palabra : f.texto().strip().toLowerCase(Locale.ROOT).split("\\s+")) {
                String param = "texto" + i++;
                condiciones.add(
                        "(marca.toLowerCase().indexOf(:" + param + ") >= 0"
                                + " || modelo.toLowerCase().indexOf(:" + param + ") >= 0)");
                p.put(param, palabra);
            }
        }
        if (f.estado() != null) {
            condiciones.add("estado == :estado");
            p.put("estado", f.estado());
        }
        if (f.anioDesde() != null) {
            condiciones.add("anio >= :anioDesde");
            p.put("anioDesde", f.anioDesde());
        }
        if (f.anioHasta() != null) {
            condiciones.add("anio <= :anioHasta");
            p.put("anioHasta", f.anioHasta());
        }
        if (f.precioMin() != null) {
            condiciones.add("precio >= :precioMin");
            p.put("precioMin", f.precioMin());
        }
        if (f.precioMax() != null) {
            condiciones.add("precio <= :precioMax");
            p.put("precioMax", f.precioMax());
        }
        Class<? extends Vehiculo> candidata =
                f.tipo() == null ? Vehiculo.class : f.tipo().getClase();
        return ejecutar(pm, candidata, String.join(" && ", condiciones), p, orden(f.orden())).stream()
                .map(v -> (Vehiculo) v)
                .toList();
    }

    private static String orden(String codigo) {
        if ("PRECIO_ASC".equals(codigo)) return "precio ascending, id ascending";
        if ("PRECIO_DESC".equals(codigo)) return "precio descending, id ascending";
        return "id descending";
    }
}
