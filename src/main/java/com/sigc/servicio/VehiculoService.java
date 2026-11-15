package com.sigc.servicio;

import com.sigc.dominio.*;
import com.sigc.repositorio.VehiculoRepositorio;
import com.sigc.servicio.vista.DatosVehiculo;
import com.sigc.servicio.vista.FiltroVehiculos;
import com.sigc.servicio.vista.ResumenInventario;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Casos de uso de vehiculos: alta, edicion, baja logica, consultas y estadisticas. */
@Service
public class VehiculoService {
    private final UnidadTrabajo unidad;
    private final VehiculoRepositorio vehiculos;
    private final FotoAlmacen fotos;

    /**
     * @param unidad limite transaccional
     * @param vehiculos acceso a datos
     * @param fotos almacenamiento de imagenes
     */
    public VehiculoService(UnidadTrabajo unidad, VehiculoRepositorio vehiculos, FotoAlmacen fotos) {
        this.unidad = unidad;
        this.vehiculos = vehiculos;
        this.fotos = fotos;
    }

    /**
     * @param filtro criterios de busqueda
     * @return vehiculos detached, listos para la vista
     */
    public List<Vehiculo> listar(FiltroVehiculos filtro) {
        return unidad.leer(
                pm -> new ArrayList<>(pm.detachCopyAll(vehiculos.buscar(pm, filtro))));
    }

    /**
     * @param id identidad del vehiculo
     * @return vehiculo detached, activo o no
     */
    public Vehiculo obtener(long id) {
        return unidad.leer(pm -> pm.detachCopy(vehiculos.porId(pm, id)));
    }

    /**
     * Da de alta un vehiculo DISPONIBLE. Si la base rechaza la operacion, la foto ya guardada se
     * elimina para no dejar archivos huerfanos.
     *
     * @param datos formulario validado
     * @param foto imagen opcional
     * @return identidad del vehiculo creado
     */
    public long crear(DatosVehiculo datos, MultipartFile foto) {
        String nombreFoto = hayArchivo(foto) ? fotos.guardar(foto) : null;
        try {
            return unidad.escribir(
                    pm -> {
                        Vehiculo v = construir(datos);
                        v.asignarFoto(nombreFoto);
                        Vehiculo guardado = vehiculos.guardar(pm, v);
                        pm.flush();
                        return guardado.getId();
                    });
        } catch (RuntimeException ex) {
            fotos.eliminar(nombreFoto);
            throw ex;
        }
    }

    /**
     * Edita los datos de un vehiculo no vendido. El tipo no puede cambiarse.
     *
     * @param id identidad del vehiculo
     * @param datos formulario validado
     * @param foto nueva imagen opcional
     * @param quitarFoto true para eliminar la foto actual si no se envia otra
     */
    public void editar(long id, DatosVehiculo datos, MultipartFile foto, boolean quitarFoto) {
        String nuevaFoto = hayArchivo(foto) ? fotos.guardar(foto) : null;
        try {
            String anterior =
                    unidad.escribir(
                            pm -> {
                                Vehiculo v = vehiculos.porId(pm, id);
                                if (!v.isActivo())
                                    throw new ReglaNegocioException(
                                            "El vehículo está dado de baja; reactívelo para editarlo.");
                                if (TipoVehiculo.de(v) != datos.getTipo())
                                    throw new ReglaNegocioException(
                                            "No se puede cambiar el tipo de un vehículo existente.");
                                aplicar(v, datos);
                                String previa = v.getFoto();
                                if (nuevaFoto != null) {
                                    v.asignarFoto(nuevaFoto);
                                    return previa;
                                }
                                if (quitarFoto) {
                                    v.asignarFoto(null);
                                    return previa;
                                }
                                return null;
                            });
            fotos.eliminar(anterior);
        } catch (RuntimeException ex) {
            fotos.eliminar(nuevaFoto);
            throw ex;
        }
    }

    /**
     * Baja logica: el vehiculo deja de listarse pero conserva su historial.
     *
     * @param id identidad del vehiculo
     */
    public void darDeBaja(long id) {
        unidad.escribir(
                pm -> {
                    vehiculos.porId(pm, id).darDeBaja();
                    return null;
                });
    }

    /**
     * @param id identidad del vehiculo dado de baja
     */
    public void reactivar(long id) {
        unidad.escribir(
                pm -> {
                    vehiculos.porId(pm, id).reactivar();
                    return null;
                });
    }

    /**
     * @return estadisticas del inventario activo
     */
    public ResumenInventario resumen() {
        List<Vehiculo> activos = listar(FiltroVehiculos.todos());
        long disponibles = contar(activos, EstadoVehiculo.DISPONIBLE);
        List<BigDecimal> precios =
                activos.stream()
                        .filter(v -> v.getEstado() == EstadoVehiculo.DISPONIBLE)
                        .map(Vehiculo::getPrecio)
                        .toList();
        BigDecimal suma = precios.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio =
                precios.isEmpty()
                        ? BigDecimal.ZERO
                        : suma.divide(BigDecimal.valueOf(precios.size()), 2, RoundingMode.HALF_UP);
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoVehiculo t : TipoVehiculo.values())
            porTipo.put(
                    t.getEtiqueta(),
                    activos.stream().filter(v -> TipoVehiculo.de(v) == t).count());
        return new ResumenInventario(
                activos.size(),
                disponibles,
                contar(activos, EstadoVehiculo.VENDIDO),
                contar(activos, EstadoVehiculo.EN_TEST_DRIVE),
                promedio,
                suma,
                porTipo);
    }

    private static long contar(List<Vehiculo> lista, EstadoVehiculo estado) {
        return lista.stream().filter(v -> v.getEstado() == estado).count();
    }

    private static boolean hayArchivo(MultipartFile foto) {
        return foto != null && !foto.isEmpty();
    }

    private static Vehiculo construir(DatosVehiculo d) {
        return switch (d.getTipo()) {
            case AUTO ->
                    new Auto(
                            d.getMarca(), d.getModelo(), d.getVersionModelo(), anio(d), km(d),
                            d.getPrecio(), d.getColor(), d.getCombustible(), d.getTransmision(),
                            d.getDescripcion(), requerido(d.getPuertas(), "Las puertas"));
            case MOTO ->
                    new Moto(
                            d.getMarca(), d.getModelo(), d.getVersionModelo(), anio(d), km(d),
                            d.getPrecio(), d.getColor(), d.getCombustible(), d.getTransmision(),
                            d.getDescripcion(), d.getTipoMoto(),
                            requerido(d.getCilindrada(), "La cilindrada"));
            case CAMION ->
                    new Camion(
                            d.getMarca(), d.getModelo(), d.getVersionModelo(), anio(d), km(d),
                            d.getPrecio(), d.getColor(), d.getCombustible(), d.getTransmision(),
                            d.getDescripcion(), d.getCapacidadCargaTn());
        };
    }

    private static void aplicar(Vehiculo v, DatosVehiculo d) {
        v.actualizarDatos(
                d.getMarca(), d.getModelo(), d.getVersionModelo(), anio(d), km(d), d.getPrecio(),
                d.getColor(), d.getCombustible(), d.getTransmision(), d.getDescripcion());
        if (v instanceof Auto a) a.actualizarEspecifico(requerido(d.getPuertas(), "Las puertas"));
        if (v instanceof Moto m)
            m.actualizarEspecifico(d.getTipoMoto(), requerido(d.getCilindrada(), "La cilindrada"));
        if (v instanceof Camion c) c.actualizarEspecifico(d.getCapacidadCargaTn());
    }

    private static int anio(DatosVehiculo d) {
        return requerido(d.getAnio(), "El año");
    }

    private static int km(DatosVehiculo d) {
        return requerido(d.getKilometraje(), "El kilometraje");
    }

    private static int requerido(Integer valor, String campo) {
        if (valor == null) throw new ReglaNegocioException(campo + " es obligatorio.");
        return valor;
    }
}
