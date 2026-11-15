package com.sigc.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sigc.dominio.ReglaNegocioException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import org.junit.jupiter.api.Test;

/**
 * Comportamiento transaccional de {@link UnidadTrabajo} con una fabrica JDO simulada (sin base):
 * confirmacion, reversion y traduccion de errores de la base a mensajes de negocio.
 */
class UnidadTrabajoTest {
    private final List<String> eventos = new ArrayList<>();
    private boolean activa;
    private Throwable falloAlConfirmar;

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> tipo, InvocationHandler h) {
        return (T) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[] {tipo}, h);
    }

    private UnidadTrabajo unidad() {
        FetchPlan plan = proxy(FetchPlan.class, (p, m, a) -> m.getName().equals("setMaxFetchDepth") ? p : null);
        Transaction tx =
                proxy(
                        Transaction.class,
                        (p, m, a) -> {
                            switch (m.getName()) {
                                case "begin" -> { activa = true; eventos.add("begin"); }
                                case "commit" -> {
                                    if (falloAlConfirmar != null) {
                                        activa = false;
                                        sneaky(falloAlConfirmar);
                                    }
                                    activa = false;
                                    eventos.add("commit");
                                }
                                case "rollback" -> { activa = false; eventos.add("rollback"); }
                                case "isActive" -> { return activa; }
                                default -> { }
                            }
                            return null;
                        });
        PersistenceManager pm =
                proxy(
                        PersistenceManager.class,
                        (p, m, a) -> {
                            switch (m.getName()) {
                                case "currentTransaction": return tx;
                                case "getFetchPlan": return plan;
                                case "close": eventos.add("close"); return null;
                                default: return null;
                            }
                        });
        PersistenceManagerFactory fabrica =
                proxy(PersistenceManagerFactory.class, (p, m, a) -> m.getName().equals("getPersistenceManager") ? pm : null);
        return new UnidadTrabajo(fabrica);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    @Test
    void confirmaYCierraCuandoTodoSaleBien() {
        String r = unidad().escribir(pm -> "listo");
        assertThat(r).isEqualTo("listo");
        assertThat(eventos).containsExactly("begin", "commit", "close");
    }

    @Test
    void revierteYPropagaUnaReglaDeNegocio() {
        assertThatThrownBy(() -> unidad().escribir(pm -> { throw new ReglaNegocioException("no"); }))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("no");
        assertThat(eventos).containsExactly("begin", "rollback", "close");
    }

    @Test
    void unaViolacionUniqueSeTraduceAUnMensajeDeNegocio() {
        falloAlConfirmar =
                new JDODataStoreException("duplicado", new SQLIntegrityConstraintViolationException("Duplicate entry"));
        assertThatThrownBy(() -> unidad().escribir(pm -> 1))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("dato ya registrado");
    }

    @Test
    void otroErrorDeBaseSeEnvuelveSinExponerDetalles() {
        falloAlConfirmar = new JDODataStoreException("conexion perdida", new java.sql.SQLException("down", "08S01"));
        assertThatThrownBy(() -> unidad().escribir(pm -> 1)).isInstanceOf(PersistenciaException.class);
    }

    @Test
    void laEdicionSimultaneaSeInformaComoReglaDeNegocio() {
        falloAlConfirmar = new JDOOptimisticVerificationException("version");
        assertThatThrownBy(() -> unidad().escribir(pm -> 1))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Otro usuario");
    }
}
