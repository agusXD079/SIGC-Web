package com.sigc.servicio;

import com.sigc.dominio.ReglaNegocioException;

import org.springframework.stereotype.Component;

import java.util.function.Function;

import javax.jdo.JDOException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

/**
 * Un administrador y una transaccion por caso de uso. Las escrituras usan lecturas bloqueantes en
 * MySQL y READ COMMITTED; ademas Vehiculo lleva un numero de version para detectar ediciones
 * simultaneas de la misma fila.
 */
@Component
public class UnidadTrabajo {
    private final PersistenceManagerFactory fabrica;

    /**
     * @param fabrica fabrica unica administrada por Spring
     */
    public UnidadTrabajo(PersistenceManagerFactory fabrica) {
        this.fabrica = fabrica;
    }

    /**
     * Ejecuta una consulta y cierra todo el contexto antes de retornar.
     *
     * @param operacion consulta que prepara objetos detached
     * @param <T> tipo del resultado
     * @return resultado independiente del administrador cerrado
     */
    public <T> T leer(Function<PersistenceManager, T> operacion) {
        return operar(operacion, false);
    }

    /**
     * Confirma todas las escrituras o revierte la unidad ante cualquier excepcion.
     *
     * @param operacion caso de uso con repositorios que comparten pm
     * @param <T> tipo del resultado
     * @return resultado de la operacion confirmada
     */
    public <T> T escribir(Function<PersistenceManager, T> operacion) {
        return operar(operacion, true);
    }

    private <T> T operar(Function<PersistenceManager, T> operacion, boolean bloquear) {
        try (PersistenceManager pm = fabrica.getPersistenceManager()) {
            pm.getFetchPlan().setMaxFetchDepth(3);
            Transaction tx = pm.currentTransaction();
            tx.setSerializeRead(bloquear);
            try {
                tx.begin();
                T resultado = operacion.apply(pm);
                tx.commit();
                return resultado;
            } finally {
                if (tx.isActive()) tx.rollback();
            }
        } catch (JDOOptimisticVerificationException ex) {
            throw new ReglaNegocioException(
                    "Otro usuario modificó este registro mientras lo editaba. "
                            + "Vuelva a abrirlo e intente nuevamente.");
        } catch (JDOException ex) {
            if (violaRestriccion(ex))
                throw new ReglaNegocioException(
                        "La operación choca con un dato ya registrado (por ejemplo DNI, legajo o "
                                + "un vehículo que otra persona acaba de vender). Actualice la pantalla "
                                + "e intente nuevamente.");
            throw new PersistenciaException(ex);
        }
    }

    /**
     * Detecta una violacion de restriccion UNIQUE o de clave foranea (SQLSTATE 23xxx), que ocurre
     * cuando dos solicitudes simultaneas llegan a la base antes que cualquier validacion de negocio.
     */
    private static boolean violaRestriccion(Throwable raiz) {
        java.util.Deque<Throwable> pendientes = new java.util.ArrayDeque<>();
        java.util.Set<Throwable> vistos = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        if (raiz != null) pendientes.add(raiz);
        while (!pendientes.isEmpty()) {
            Throwable t = pendientes.poll();
            if (!vistos.add(t)) continue;
            if (t instanceof java.sql.SQLIntegrityConstraintViolationException) return true;
            if (t instanceof java.sql.SQLException sql
                    && sql.getSQLState() != null
                    && sql.getSQLState().startsWith("23")) return true;
            if (t.getCause() != null) pendientes.add(t.getCause());
            if (t instanceof JDOException jdo && jdo.getNestedExceptions() != null)
                for (Throwable n : jdo.getNestedExceptions()) if (n != null) pendientes.add(n);
            if (t instanceof java.sql.SQLException sql && sql.getNextException() != null)
                pendientes.add(sql.getNextException());
        }
        return false;
    }
}
