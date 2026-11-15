package com.sigc.repositorio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Arma el filtro JDOQL de busqueda por texto (nombre, apellido o DNI). Cada palabra debe coincidir
 * con alguno de los tres campos; el texto viaja siempre como parametro, nunca en la expresion.
 */
final class BusquedaPersonas {
    /** Expresion JDOQL resultante, o cadena vacia si no hay criterios. */
    final String filtro;

    /** Valores de los parametros implicitos de {@link #filtro}. */
    final Map<String, Object> parametros;

    private BusquedaPersonas(String filtro, Map<String, Object> parametros) {
        this.filtro = filtro;
        this.parametros = parametros;
    }

    /**
     * @param texto lo que escribio el usuario, posiblemente vacio
     * @param incluirBajas true para incluir personas dadas de baja
     * @return filtro listo para ejecutar
     */
    static BusquedaPersonas de(String texto, boolean incluirBajas) {
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> p = new HashMap<>();
        if (!incluirBajas) condiciones.add("activo == true");
        String limpio = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
        int i = 0;
        for (String palabra : limpio.split("\\s+")) {
            if (palabra.isEmpty()) continue;
            String param = "t" + i;
            String solo = "d" + i;
            i++;
            // El nombre y el apellido se comparan tal como se escribieron (p. ej. "perez-gomez").
            StringBuilder condicion =
                    new StringBuilder("(nombre.toLowerCase().indexOf(:" + param + ") >= 0")
                            .append(" || apellido.toLowerCase().indexOf(:" + param + ") >= 0");
            p.put(param, palabra);
            // El DNI se compara solo por sus digitos; sin digitos no se agrega la condicion
            // (indexOf("") coincidiria con todos los registros).
            String digitos = palabra.replaceAll("[^0-9]", "");
            if (!digitos.isEmpty()) {
                condicion.append(" || dni.indexOf(:" + solo + ") >= 0");
                p.put(solo, digitos);
            }
            condiciones.add(condicion.append(")").toString());
        }
        return new BusquedaPersonas(String.join(" && ", condiciones), p);
    }
}
