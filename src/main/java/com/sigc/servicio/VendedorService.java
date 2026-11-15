package com.sigc.servicio;

import com.sigc.dominio.*;
import com.sigc.repositorio.PersonaRepositorio;
import com.sigc.repositorio.TestDriveRepositorio;
import com.sigc.repositorio.VendedorRepositorio;
import com.sigc.repositorio.VentaRepositorio;
import com.sigc.servicio.vista.DatosPersona;
import com.sigc.servicio.vista.FiltroVentas;
import com.sigc.servicio.vista.HistorialPersona;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;

/** Casos de uso de vendedores: alta, edicion, baja logica, consulta e historial de operaciones. */
@Service
public class VendedorService {
    private final UnidadTrabajo unidad;
    private final VendedorRepositorio vendedores;
    private final PersonaRepositorio personas;
    private final VentaRepositorio ventas;
    private final TestDriveRepositorio testDrives;

    /**
     * @param unidad limite transaccional
     * @param vendedores acceso a vendedores
     * @param personas verificacion de DNI unico
     * @param ventas ventas realizadas
     * @param testDrives pruebas acompañadas
     */
    public VendedorService(
            UnidadTrabajo unidad,
            VendedorRepositorio vendedores,
            PersonaRepositorio personas,
            VentaRepositorio ventas,
            TestDriveRepositorio testDrives) {
        this.unidad = unidad;
        this.vendedores = vendedores;
        this.personas = personas;
        this.ventas = ventas;
        this.testDrives = testDrives;
    }

    /**
     * @param texto nombre, apellido o DNI; vacio lista todos
     * @param incluirBajas true para incluir vendedores dados de baja
     * @return vendedores detached ordenados por apellido
     */
    public List<Vendedor> listar(String texto, boolean incluirBajas) {
        return unidad.leer(
                pm -> new ArrayList<>(pm.detachCopyAll(vendedores.buscar(pm, texto, incluirBajas))));
    }

    /**
     * @param id identidad del vendedor
     * @return vendedor detached
     */
    public Vendedor obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(vendedores.porId(pm, id)));
    }

    /**
     * @param id identidad del vendedor
     * @return vendedor con sus ventas, pruebas acompañadas y monto total vendido
     */
    public HistorialPersona historial(long id) {
        return unidad.leer(
                pm -> {
                    Vendedor v = vendedores.porId(pm, id);
                    List<Venta> realizadas = ventas.buscar(pm, new FiltroVentas(null, id, null, null));
                    BigDecimal total =
                            realizadas.stream()
                                    .map(Venta::getPrecioVenta)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new HistorialPersona(
                            pm.detachCopy(v),
                            new ArrayList<>(pm.detachCopyAll(realizadas)),
                            new ArrayList<>(
                                    pm.detachCopyAll(testDrives.buscar(pm, null, null, null, id))),
                            total);
                });
    }

    /**
     * @param datos formulario validado (incluye legajo)
     * @return identidad del vendedor creado
     * @throws ReglaNegocioException si el DNI o el legajo ya existen
     */
    public long crear(DatosPersona datos) {
        return unidad.escribir(
                pm -> {
                    exigirDniLibre(pm, Validaciones.dni(datos.getDni()), 0);
                    Vendedor nuevo =
                            new Vendedor(
                                    datos.getNombre(), datos.getApellido(), datos.getDni(),
                                    datos.getTelefono(), datos.getEmail(), datos.getLegajo());
                    exigirLegajoLibre(pm, nuevo.getLegajo(), 0);
                    Vendedor guardado = vendedores.guardar(pm, nuevo);
                    pm.flush();
                    return guardado.getId();
                });
    }

    /**
     * @param id identidad del vendedor
     * @param datos formulario validado
     * @throws ReglaNegocioException si el DNI o el legajo pertenecen a otro
     */
    public void editar(long id, DatosPersona datos) {
        unidad.escribir(
                pm -> {
                    Vendedor v = vendedores.porId(pm, id);
                    exigirDniLibre(pm, Validaciones.dni(datos.getDni()), id);
                    v.actualizarDatos(
                            datos.getNombre(), datos.getApellido(), datos.getDni(),
                            datos.getTelefono(), datos.getEmail());
                    v.actualizarLegajo(datos.getLegajo());
                    exigirLegajoLibre(pm, v.getLegajo(), id);
                    return null;
                });
    }

    /**
     * Baja logica; se conservan sus ventas.
     *
     * @param id identidad del vendedor
     */
    public void darDeBaja(long id) {
        unidad.escribir(
                pm -> {
                    vendedores.porId(pm, id).darDeBaja();
                    return null;
                });
    }

    /**
     * @param id identidad del vendedor dado de baja
     */
    public void reactivar(long id) {
        unidad.escribir(
                pm -> {
                    vendedores.porId(pm, id).reactivar();
                    return null;
                });
    }

    private void exigirDniLibre(PersistenceManager pm, String dni, long idPropio) {
        Persona existente = personas.porDni(pm, dni);
        if (existente != null && existente.getId() != idPropio)
            throw new ReglaNegocioException(
                    "Ya existe una persona registrada con el DNI " + dni + " ("
                            + existente.getNombreCompleto() + ").");
    }

    private void exigirLegajoLibre(PersistenceManager pm, String legajo, long idPropio) {
        Vendedor existente = vendedores.porLegajo(pm, legajo);
        if (existente != null && existente.getId() != idPropio)
            throw new ReglaNegocioException("Ya existe un vendedor con el legajo " + legajo + ".");
    }
}
