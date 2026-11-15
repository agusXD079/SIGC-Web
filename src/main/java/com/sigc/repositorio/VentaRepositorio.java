package com.sigc.repositorio;

import com.sigc.dominio.Venta;
import com.sigc.servicio.vista.FiltroVentas;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

/** Consultas de ventas: historial filtrable y busqueda por clave de operacion. */
@Repository
public class VentaRepositorio extends RepositorioJdo<Venta> {
    /** Configura el tipo Venta. */
    public VentaRepositorio() {
        super(Venta.class);
    }

    /**
     * @param pm administrador de la unidad
     * @param clave UUID de la solicitud
     * @return la venta ya registrada con esa clave, o null
     */
    public Venta porClave(PersistenceManager pm, String clave) {
        List<Venta> r = listar(pm, "claveOperacion == :clave", Map.of("clave", clave), null);
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * @param pm administrador de la unidad
     * @param vehiculoId identidad del vehiculo
     * @return la venta de ese vehiculo, o null si no se vendio
     */
    public Venta porVehiculo(PersistenceManager pm, long vehiculoId) {
        List<Venta> r = listar(pm, "vehiculo.id == :id", Map.of("id", vehiculoId), null);
        return r.isEmpty() ? null : r.get(0);
    }

    /**
     * @param pm administrador de la unidad
     * @param f criterios de busqueda
     * @return ventas de la mas reciente a la mas antigua
     */
    public List<Venta> buscar(PersistenceManager pm, FiltroVentas f) {
        List<String> condiciones = new ArrayList<>();
        Map<String, Object> p = new HashMap<>();
        if (f.clienteId() != null) {
            condiciones.add("cliente.id == :clienteId");
            p.put("clienteId", f.clienteId());
        }
        if (f.vendedorId() != null) {
            condiciones.add("vendedor.id == :vendedorId");
            p.put("vendedorId", f.vendedorId());
        }
        if (f.desde() != null) {
            condiciones.add("fechaHora >= :desde");
            p.put("desde", f.desde().atStartOfDay());
        }
        if (f.hasta() != null) {
            LocalDateTime finDelDia = f.hasta().plusDays(1).atStartOfDay();
            condiciones.add("fechaHora < :hasta");
            p.put("hasta", finDelDia);
        }
        return listar(pm, String.join(" && ", condiciones), p, "fechaHora descending, id descending");
    }
}
