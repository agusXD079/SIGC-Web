package com.sigc.dominio;

import java.util.Objects;

import javax.jdo.annotations.*;

/**
 * Raiz de la jerarquia Persona, Cliente y Vendedor (conservada del sistema original). Ordena por
 * apellido y nombre; la igualdad se basa en el DNI, unico para toda la concesionaria.
 */
@PersistenceCapable(table = "PERSONAS", detachable = "true")
@Inheritance(strategy = InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy = DiscriminatorStrategy.CLASS_NAME, column = "CLASE")
public abstract class Persona implements Comparable<Persona> {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    @Column(name = "ID")
    private long id;

    @Column(allowsNull = "false", length = 80)
    private String nombre;

    @Column(allowsNull = "false", length = 80)
    private String apellido;

    @Unique(name = "UQ_PERSONA_DNI")
    @Column(allowsNull = "false", length = 8)
    private String dni;

    @Column(length = 32)
    private String telefono;

    @Column(length = 120)
    private String email;

    /** Baja logica: se conserva el historial de ventas y pruebas de manejo. */
    @Column(allowsNull = "false")
    private boolean activo;

    /** Constructor reservado a JDO. */
    protected Persona() {}

    /**
     * Crea una persona valida y activa.
     *
     * @param nombre nombre de pila
     * @param apellido apellido
     * @param dni documento con separadores opcionales
     * @param telefono contacto opcional
     * @param email correo opcional
     */
    protected Persona(String nombre, String apellido, String dni, String telefono, String email) {
        actualizarDatos(nombre, apellido, dni, telefono, email);
        this.activo = true;
    }

    /**
     * Modifica los datos personales sin cambiar identidad ni historial.
     *
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param dni nuevo documento; el servicio verifica la unicidad global
     * @param telefono contacto opcional
     * @param email correo opcional
     */
    public void actualizarDatos(
            String nombre, String apellido, String dni, String telefono, String email) {
        this.nombre = Validaciones.texto(nombre, "El nombre", 80);
        this.apellido = Validaciones.texto(apellido, "El apellido", 80);
        this.dni = Validaciones.dni(dni);
        this.telefono = Validaciones.textoOpcional(telefono, "El teléfono", 32);
        this.email = Validaciones.email(email);
    }

    /** Baja logica. */
    public void darDeBaja() {
        activo = false;
    }

    /** Vuelve a habilitar a la persona. */
    public void reactivar() {
        activo = true;
    }

    /**
     * @return nombre en formato "Apellido, Nombre" para listados y comprobantes
     */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    @Override
    public int compareTo(Persona otra) {
        int porApellido = apellido.compareToIgnoreCase(otra.apellido);
        return porApellido != 0 ? porApellido : nombre.compareToIgnoreCase(otra.nombre);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Persona)) return false;
        return Objects.equals(dni, ((Persona) obj).dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

    /**
     * @return identidad generada por MySQL; cero mientras el objeto es transitorio
     */
    public long getId() {
        return id;
    }

    /**
     * @return nombre de pila
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * @return DNI normalizado
     */
    public String getDni() {
        return dni;
    }

    /**
     * @return telefono, posiblemente vacio
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @return correo, posiblemente vacio
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return false si fue dado de baja
     */
    public boolean isActivo() {
        return activo;
    }
}
