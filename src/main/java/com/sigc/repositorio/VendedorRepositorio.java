package com.sigc.repositorio;

import com.sigc.dominio.Vendedor;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Busquedas de vendedores por nombre, apellido, DNI o legajo. */
@Repository
public class VendedorRepositorio extends RepositorioJdo<Vendedor> {
    /** Configura el tipo Vendedor. */
    public VendedorRepositorio() {
        super(Vendedor.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param texto palabras a buscar; vacio lista todos
     * @param incluirBajas true para incluir vendedores dados de baja
     * @return vendedores ordenados por apellido y nombre
     */
    public List<Vendedor> buscar(PersistenceManager pm, String texto, boolean incluirBajas) {
        BusquedaPersonas b = BusquedaPersonas.de(texto, incluirBajas);
        return listar(pm, b.filtro, b.parametros, "apellido ascending, nombre ascending");
    }

    /**
     * @param pm administrador de la unidad
     * @param legajo legajo normalizado en mayusculas
     * @return el vendedor con ese legajo, o null
     */
    public Vendedor porLegajo(PersistenceManager pm, String legajo) {
        List<Vendedor> r = listar(pm, "legajo == :legajo", Map.of("legajo", legajo), null);
        return r.isEmpty() ? null : r.get(0);
    }
}
