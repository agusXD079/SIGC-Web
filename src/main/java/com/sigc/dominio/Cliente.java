package com.sigc.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.*;

/**
 * Comprador de vehiculos. Es el lado "uno" de la relacion uno a muchos Cliente - Venta: un cliente
 * tiene muchas compras y cada venta pertenece a un unico cliente.
 */
@PersistenceCapable(table = "CLIENTES", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
public class Cliente extends Persona {
    @Column(name = "FECHA_ALTA", allowsNull = "false")
    private LocalDate fechaAlta;

    /**
     * Relacion 1:N bidireccional. {@code mappedBy} indica que la clave foranea es la columna
     * VENTAS.CLIENTE_ID del atributo {@link Venta#getCliente()}: no se crea ninguna tabla ni
     * columna adicional. Las ventas nunca se quitan de aqui (son historial).
     */
    @Persistent(mappedBy = "cliente")
    private Set<Venta> compras = new HashSet<>();

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

    /**
     * Recorre la relacion 1:N. Debe llamarse con el cliente gestionado por JDO (dentro de la unidad
     * de trabajo del servicio): la coleccion se carga recien al pedirla.
     *
     * @return copia de las compras del cliente, de la mas reciente a la mas antigua
     */
    public List<Venta> getCompras() {
        List<Venta> ordenadas = new ArrayList<>(compras);
        ordenadas.sort(
                Comparator.comparing(Venta::getFechaHora)
                        .thenComparing(Venta::getId)
                        .reversed());
        return ordenadas;
    }

    @Override
    public String toString() {
        return "Cliente: " + getNombreCompleto() + " (DNI: " + getDni() + ")";
    }
}
