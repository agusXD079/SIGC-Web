package com.sigc.repositorio;

import com.sigc.dominio.EstadoTestDrive;
import com.sigc.dominio.TestDrive;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Consultas de pruebas de manejo. */
@Repository
public class TestDriveRepositorio extends RepositorioJdo<TestDrive> {
    /** Configura el tipo TestDrive. */
    public TestDriveRepositorio() {
        super(TestDrive.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param estado estado a exigir, o null para todos
     * @param clienteId cliente a exigir, o null
     * @param vehiculoId vehiculo a exigir, o null
     * @param vendedorId vendedor a exigir, o null
     * @return pruebas de la mas reciente a la mas antigua
     */
    public List<TestDrive> buscar(
            PersistenceManager pm,
            EstadoTestDrive estado,
            Long clienteId,
            Long vehiculoId,
            Long vendedorId) {
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> p = new HashMap<>();
        if (estado != null) {
            condiciones.add("estado == :estado");
            p.put("estado", estado);
        }
        if (clienteId != null) {
            condiciones.add("cliente.id == :clienteId");
            p.put("clienteId", clienteId);
        }
        if (vehiculoId != null) {
            condiciones.add("vehiculo.id == :vehiculoId");
            p.put("vehiculoId", vehiculoId);
        }
        if (vendedorId != null) {
            condiciones.add("vendedor.id == :vendedorId");
            p.put("vendedorId", vendedorId);
        }
        return listar(pm, String.join(" && ", condiciones), p, "fechaInicio descending, id descending");
    }
}
