package com.sigc.dominio;

import java.time.LocalDate;

import javax.jdo.annotations.*;

/** Comprador de vehiculos. Su historial de compras se consulta a traves de las ventas. */
@PersistenceCapable(table = "CLIENTES", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Cliente extends Persona {
    @Column(name = "FECHA_ALTA", allowsNull = "false")
    private LocalDate fechaAlta;

    /** Constructor reservado a JDO. */
    protected Cliente() {}

    /**
     * @param fechaAlta dia de registro; los demas parametros se describen en {@link Persona}
     */
    public Cliente(
            String nombre,
            String apellido,
            String dni,
            String telefono,
            String email,
            LocalDate fechaAlta) {
        super(nombre, apellido, dni, telefono, email);
        this.fechaAlta = Validaciones.requerido(fechaAlta, "La fecha de alta");
    }

    /**
     * @return dia en que se registro el cliente
     */
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    @Override
    public String toString() {
        return "Cliente: " + getNombreCompleto() + " (DNI: " + getDni() + ")";
    }
}
