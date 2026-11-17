package com.sigc.servicio;

import com.sigc.dominio.*;
import com.sigc.repositorio.ClienteRepositorio;
import com.sigc.repositorio.PersonaRepositorio;
import com.sigc.repositorio.TestDriveRepositorio;
import com.sigc.servicio.vista.DatosPersona;
import com.sigc.servicio.vista.HistorialPersona;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Casos de uso de clientes: alta, edicion, baja logica, busqueda e historial. */
@Service
public class ClienteService {
    private final UnidadTrabajo unidad;
    private final ClienteRepositorio clientes;
    private final PersonaRepositorio personas;
    private final TestDriveRepositorio testDrives;
    private final Clock reloj;

    /**
     * @param unidad limite transaccional
     * @param clientes acceso a clientes
     * @param personas verificacion de DNI unico
     * @param testDrives historial de pruebas
     * @param reloj reloj de negocio
     */
    public ClienteService(
            UnidadTrabajo unidad,
            ClienteRepositorio clientes,
            PersonaRepositorio personas,
            TestDriveRepositorio testDrives,
            Clock reloj) {
        this.unidad = unidad;
        this.clientes = clientes;
        this.personas = personas;
        this.testDrives = testDrives;
        this.reloj = reloj;
    }

    /**
     * @param texto nombre, apellido o DNI; vacio lista todos
     * @param incluirBajas true para incluir clientes dados de baja
     * @return clientes detached ordenados por apellido
     */
    public List<Cliente> listar(String texto, boolean incluirBajas) {
        return unidad.leer(
                pm -> new ArrayList<>(pm.detachCopyAll(clientes.buscar(pm, texto, incluirBajas))));
    }

    /**
     * @param id identidad del cliente
     * @return cliente detached
     */
    public Cliente obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(clientes.porId(pm, id)));
    }

    /**
     * @param id identidad del cliente
     * @return cliente con sus compras, pruebas de manejo y monto total comprado
     */
    public HistorialPersona historial(long id) {
        return unidad.leer(
                pm -> {
                    Cliente c = clientes.porId(pm, id);
                    // Relacion uno a muchos: las compras se obtienen navegando Cliente -> ventas.
                    List<Venta> compras = c.getCompras();
                    BigDecimal total =
                            compras.stream()
                                    .map(Venta::getPrecioVenta)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new HistorialPersona(
                            pm.detachCopy(c),
                            new ArrayList<>(pm.detachCopyAll(compras)),
                            new ArrayList<>(
                                    pm.detachCopyAll(testDrives.buscar(pm, null, id, null, null))),
                            total);
                });
    }

    /**
     * @param datos formulario validado
     * @return identidad del cliente creado
     * @throws ReglaNegocioException si el DNI ya esta registrado
     */
    public long crear(DatosPersona datos) {
        return unidad.escribir(
                pm -> {
                    exigirDniLibre(pm, Validaciones.dni(datos.getDni()), 0);
                    Cliente nuevo =
                            new Cliente(
                                    datos.getNombre(), datos.getApellido(), datos.getDni(),
                                    datos.getTelefono(), datos.getEmail(), LocalDate.now(reloj));
                    Cliente guardado = clientes.guardar(pm, nuevo);
                    pm.flush();
                    return guardado.getId();
                });
    }

    /**
     * @param id identidad del cliente
     * @param datos formulario validado
     * @throws ReglaNegocioException si el DNI pertenece a otra persona
     */
    public void editar(long id, DatosPersona datos) {
        unidad.escribir(
                pm -> {
                    Cliente c = clientes.porId(pm, id);
                    exigirDniLibre(pm, Validaciones.dni(datos.getDni()), id);
                    c.actualizarDatos(
                            datos.getNombre(), datos.getApellido(), datos.getDni(),
                            datos.getTelefono(), datos.getEmail());
                    return null;
                });
    }

    /**
     * Baja logica; se conservan sus compras y pruebas. No se permite con una prueba en curso.
     *
     * @param id identidad del cliente
     */
    public void darDeBaja(long id) {
        unidad.escribir(
                pm -> {
                    Cliente c = clientes.porId(pm, id);
                    boolean pruebaAbierta =
                            !testDrives.buscar(pm, EstadoTestDrive.EN_CURSO, id, null, null).isEmpty();
                    if (pruebaAbierta)
                        throw new ReglaNegocioException(
                                "El cliente tiene una prueba de manejo en curso; finalícela antes de la baja.");
                    c.darDeBaja();
                    return null;
                });
    }

    /**
     * @param id identidad del cliente dado de baja
     */
    public void reactivar(long id) {
        unidad.escribir(
                pm -> {
                    clientes.porId(pm, id).reactivar();
                    return null;
                });
    }

    private void exigirDniLibre(javax.jdo.PersistenceManager pm, String dni, long idPropio) {
        Persona existente = personas.porDni(pm, dni);
        if (existente != null && existente.getId() != idPropio)
            throw new ReglaNegocioException(
                    "Ya existe una persona registrada con el DNI " + dni + " ("
                            + existente.getNombreCompleto() + ").");
    }
}
