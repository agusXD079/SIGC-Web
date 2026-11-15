package com.sigc.repositorio;

import com.sigc.dominio.Cliente;

import org.springframework.stereotype.Repository;

import java.util.List;

import javax.jdo.PersistenceManager;

/** Busquedas de clientes por nombre, apellido o DNI. */
@Repository
public class ClienteRepositorio extends RepositorioJdo<Cliente> {
    /** Configura el tipo Cliente. */
    public ClienteRepositorio() {
        super(Cliente.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param texto palabras a buscar; vacio lista todos
     * @param incluirBajas true para incluir clientes dados de baja
     * @return clientes ordenados por apellido y nombre
     */
    public List<Cliente> buscar(PersistenceManager pm, String texto, boolean incluirBajas) {
        BusquedaPersonas b = BusquedaPersonas.de(texto, incluirBajas);
        return listar(pm, b.filtro, b.parametros, "apellido ascending, nombre ascending");
    }
}
