package com.sigc.dominio;

import java.util.Locale;

import javax.jdo.annotations.*;

/**
 * Empleado que realiza ventas y pruebas de manejo. La cantidad de ventas realizadas ya no se
 * guarda como contador (podia quedar desfasada): se calcula siempre desde las ventas.
 */
@PersistenceCapable(table = "VENDEDORES", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Vendedor extends Persona {
    @Unique(name = "UQ_VENDEDOR_LEGAJO")
    @Column(name = "LEGAJO", allowsNull = "false", length = 20)
    private String legajo;

    /** Constructor reservado a JDO. */
    protected Vendedor() {}

    /**
     * @param legajo identificador interno unico (letras, numeros y guiones); los demas parametros
     *     se describen en {@link Persona}
     */
    public Vendedor(
            String nombre,
            String apellido,
            String dni,
            String telefono,
            String email,
            String legajo) {
        super(nombre, apellido, dni, telefono, email);
        this.legajo = normalizarLegajo(legajo);
    }

    /**
     * @param legajo nuevo legajo; el servicio verifica su unicidad
     */
    public void actualizarLegajo(String legajo) {
        this.legajo = normalizarLegajo(legajo);
    }

    private static String normalizarLegajo(String valor) {
        String limpio = Validaciones.texto(valor, "El legajo", 20).toUpperCase(Locale.ROOT);
        if (!limpio.matches("[A-Z0-9-]+"))
            throw new ReglaNegocioException("El legajo solo admite letras, números y guiones.");
        return limpio;
    }

    /**
     * @return legajo en mayusculas
     */
    public String getLegajo() {
        return legajo;
    }

    @Override
    public String toString() {
        return "Vendedor: " + getNombreCompleto() + " | Legajo: " + legajo;
    }
}
