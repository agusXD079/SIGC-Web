package com.sigc.repositorio;

import com.sigc.dominio.ReglaNegocioException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Implementacion generica que comparte el PersistenceManager recibido por parametro.
 *
 * @param <T> entidad persistente
 */
public class RepositorioJdo<T> implements IRepositorio<T> {
    private final Class<T> clase;

    /**
     * @param clase clase persistente consultada por este repositorio
     */
    public RepositorioJdo(Class<T> clase) {
        this.clase = clase;
    }

    @Override
    public T guardar(PersistenceManager pm, T entidad) {
        return pm.makePersistent(entidad);
    }

    @Override
    public T porId(PersistenceManager pm, long id) {
        if (id <= 0) throw new ReglaNegocioException("La identidad solicitada no es válida.");
        try {
            return pm.getObjectById(clase, id);
        } catch (JDOObjectNotFoundException ex) {
            throw new ReglaNegocioException("No se encontró el registro solicitado.");
        }
    }

    @Override
    public List<T> listar(
            PersistenceManager pm, String filtro, Map<String, ?> parametros, String orden) {
        return ejecutar(pm, clase, filtro, parametros, orden);
    }

    /**
     * Ejecuta JDOQL sobre una clase candidata (por ejemplo una subclase) y libera el resultado.
     *
     * @param pm administrador del caso de uso
     * @param candidata clase sobre la que se consulta
     * @param filtro expresion con parametros implicitos, o vacio
     * @param parametros valores del filtro
     * @param orden orden JDOQL, o vacio
     * @param <C> tipo candidato
     * @return copia materializada; sus entidades siguen asociadas a pm
     */
    @SuppressWarnings("unchecked")
    protected <C> List<C> ejecutar(
            PersistenceManager pm,
            Class<C> candidata,
            String filtro,
            Map<String, ?> parametros,
            String orden) {
        Query<C> consulta = pm.newQuery(candidata);
        try {
            if (filtro != null && !filtro.isBlank()) consulta.setFilter(filtro);
            if (orden != null && !orden.isBlank()) consulta.setOrdering(orden);
            return new ArrayList<>((List<C>) consulta.executeWithMap(parametros));
        } finally {
            consulta.closeAll();
        }
    }
}
