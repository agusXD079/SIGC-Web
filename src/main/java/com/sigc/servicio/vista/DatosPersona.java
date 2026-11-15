package com.sigc.servicio.vista;

import com.sigc.dominio.Persona;
import com.sigc.dominio.Vendedor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos del formulario de clientes y vendedores ({@code legajo} solo se usa en vendedores). */
public class DatosPersona {
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres.")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 80, message = "El apellido admite hasta 80 caracteres.")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio.")
    @Size(max = 12, message = "El DNI es demasiado largo.")
    private String dni;

    @Size(max = 32, message = "El teléfono admite hasta 32 caracteres.")
    private String telefono;

    @Size(max = 120, message = "El correo admite hasta 120 caracteres.")
    private String email;

    @Size(max = 20, message = "El legajo admite hasta 20 caracteres.")
    private String legajo;

    /**
     * @param p cliente o vendedor detached
     * @return formulario precargado
     */
    public static DatosPersona desde(Persona p) {
        DatosPersona d = new DatosPersona();
        d.nombre = p.getNombre();
        d.apellido = p.getApellido();
        d.dni = p.getDni();
        d.telefono = p.getTelefono();
        d.email = p.getEmail();
        if (p instanceof Vendedor v) d.legajo = v.getLegajo();
        return d;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLegajo() { return legajo; }
    public void setLegajo(String legajo) { this.legajo = legajo; }
}
