package com.sigc.repositorio;

import com.sigc.dominio.Persona;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Consultas comunes a clientes y vendedores; el DNI es unico para toda la jerarquia. */
@Repository
public class PersonaRepositorio extends RepositorioJdo<Persona> {
    /** Configura el tipo Persona. */
    public PersonaRepositorio() {
        super(Persona.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param dni DNI normalizado
     * @return la persona con ese DNI, o null si no existe
     */
    public Persona porDni(PersistenceManager pm, String dni) {
        List<Persona> encontradas = listar(pm, "dni == :dni", Map.of("dni", dni), null);
        return encontradas.isEmpty() ? null : encontradas.get(0);
    }
}
