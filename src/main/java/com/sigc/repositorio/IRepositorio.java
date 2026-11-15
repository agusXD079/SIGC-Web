package com.sigc.repositorio;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * Contrato de acceso a datos (evolucion del IRepositorio del sistema de consola). Ninguna operacion
 * confirma transacciones: el servicio decide el limite transaccional y comparte el administrador.
 *
 * @param <T> entidad persistente
 */
public interface IRepositorio<T> {
    /**
     * @param pm administrador de la unidad de trabajo
     * @param entidad objeto a persistir
     * @return objeto gestionado por JDO
     */
    T guardar(PersistenceManager pm, T entidad);

    /**
     * @param pm administrador de la unidad de trabajo
     * @param id identidad positiva
     * @return entidad gestionada
     */
    T porId(PersistenceManager pm, long id);

    /**
     * @param pm administrador de la unidad de trabajo
     * @param filtro expresion JDOQL con parametros implicitos, o vacio
     * @param parametros valores del filtro
     * @param orden orden JDOQL, o vacio
     * @return entidades gestionadas por pm
     */
    List<T> listar(PersistenceManager pm, String filtro, Map<String, ?> parametros, String orden);
}
