package com.sigc.controlador;

import com.sigc.dominio.*;
import com.sigc.servicio.TestDriveService;
import com.sigc.servicio.VehiculoService;
import com.sigc.servicio.VentaService;
import com.sigc.servicio.vista.DatosVehiculo;
import com.sigc.servicio.vista.FiltroVehiculos;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Rutas HTTP del modulo de vehiculos: listado, alta, edicion, ficha, baja y reactivacion. */
@Controller
@RequestMapping("/vehiculos")
public class VehiculoController {
    private final VehiculoService servicio;
    private final VentaService ventas;
    private final TestDriveService testDrives;

    /**
     * @param servicio casos de uso de vehiculos
     * @param ventas consulta de la venta del vehiculo
     * @param testDrives consulta del historial de pruebas del vehiculo
     */
    public VehiculoController(
            VehiculoService servicio, VentaService ventas, TestDriveService testDrives) {
        this.servicio = servicio;
        this.ventas = ventas;
        this.testDrives = testDrives;
    }

    /** Listado con busqueda, filtros y orden; muestra tarjetas o tabla segun "vista". */
    @GetMapping
    public String lista(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) TipoVehiculo tipo,
            @RequestParam(required = false) EstadoVehiculo estado,
            @RequestParam(required = false) Integer anioDesde,
            @RequestParam(required = false) Integer anioHasta,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "tarjetas") String vista,
            @RequestParam(defaultValue = "false") boolean bajas,
            Model model) {
        // Un rango invertido se informa y se ignora, igual que las fechas en ventas y reportes.
        List<String> avisos = new ArrayList<>();
        if (anioDesde != null && anioHasta != null && anioHasta < anioDesde) {
            avisos.add("El año \"hasta\" no puede ser anterior al año \"desde\".");
            anioDesde = null;
            anioHasta = null;
        }
        if (precioMin != null && precioMax != null && precioMax.compareTo(precioMin) < 0) {
            avisos.add("El precio máximo no puede ser menor que el precio mínimo.");
            precioMin = null;
            precioMax = null;
        }
        if (!avisos.isEmpty()) model.addAttribute("error", String.join(" ", avisos));
        FiltroVehiculos filtro =
                new FiltroVehiculos(
                        texto, tipo, estado, anioDesde, anioHasta, precioMin, precioMax, orden, bajas);
        model.addAttribute("vehiculos", servicio.listar(filtro));
        model.addAttribute("filtro", filtro);
        model.addAttribute("vista", "tabla".equals(vista) ? "tabla" : "tarjetas");
        catalogos(model);
        return "vehiculos/lista";
    }

    /** Formulario de alta; "tipo" preselecciona la categoria. */
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) TipoVehiculo tipo, Model model) {
        DatosVehiculo datos = new DatosVehiculo();
        if (tipo != null) datos.setTipo(tipo);
        return formulario(model, datos, null);
    }

    /** Registra el vehiculo; ante errores vuelve al formulario con los datos ingresados. */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("datos") DatosVehiculo datos,
            BindingResult errores,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos, null);
        try {
            long id = servicio.crear(datos, foto);
            redireccion.addFlashAttribute("mensaje", "Vehículo registrado correctamente.");
            return "redirect:/vehiculos/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, null);
        }
    }

    /** Ficha completa del vehiculo. */
    @GetMapping("/{id}")
    public String ficha(@PathVariable long id, Model model) {
        model.addAttribute("v", servicio.obtener(id));
        model.addAttribute("venta", ventas.deVehiculo(id));
        model.addAttribute("pruebas", testDrives.deVehiculo(id));
        return "vehiculos/detalle";
    }

    /** Formulario de edicion precargado. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        Vehiculo v = servicio.obtener(id);
        return formulario(model, DatosVehiculo.desde(v), v);
    }

    /** Guarda los cambios; el tipo no se puede modificar. */
    @PostMapping("/{id}")
    public String guardar(
            @PathVariable long id,
            @Valid @ModelAttribute("datos") DatosVehiculo datos,
            BindingResult errores,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(defaultValue = "false") boolean quitarFoto,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos, servicio.obtener(id));
        try {
            servicio.editar(id, datos, foto, quitarFoto);
            redireccion.addFlashAttribute("mensaje", "Cambios guardados.");
            return "redirect:/vehiculos/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, servicio.obtener(id));
        }
    }

    /** Baja logica. */
    @PostMapping("/{id}/baja")
    public String baja(@PathVariable long id, RedirectAttributes redireccion) {
        try {
            servicio.darDeBaja(id);
            redireccion.addFlashAttribute("mensaje", "Vehículo dado de baja.");
        } catch (ReglaNegocioException ex) {
            redireccion.addFlashAttribute("error", ex.getMessage());
            return "redirect:/vehiculos/" + id;
        }
        return "redirect:/vehiculos";
    }

    /** Reactiva un vehiculo dado de baja. */
    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable long id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Vehículo reactivado.");
        return "redirect:/vehiculos/" + id;
    }

    private String formulario(Model model, DatosVehiculo datos, Vehiculo existente) {
        model.addAttribute("datos", datos);
        model.addAttribute("existente", existente);
        catalogos(model);
        return "vehiculos/form";
    }

    private static void catalogos(Model model) {
        model.addAttribute("tipos", TipoVehiculo.values());
        model.addAttribute("estados", EstadoVehiculo.values());
        model.addAttribute("combustibles", Combustible.values());
        model.addAttribute("transmisiones", Transmision.values());
        model.addAttribute("tiposMoto", TipoMoto.values());
    }
}
