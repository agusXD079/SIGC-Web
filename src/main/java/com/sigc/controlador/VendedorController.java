package com.sigc.controlador;

import com.sigc.dominio.Vendedor;
import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.VendedorService;
import com.sigc.servicio.vista.DatosPersona;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Rutas HTTP del modulo de vendedores. Las plantillas se comparten con vendedores (carpeta personas). */
@Controller
@RequestMapping("/vendedores")
public class VendedorController {
    private final VendedorService servicio;

    /**
     * @param servicio casos de uso de vendedores
     */
    public VendedorController(VendedorService servicio) {
        this.servicio = servicio;
    }

    /** Listado con busqueda por nombre, apellido o DNI. */
    @GetMapping
    public String lista(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "false") boolean bajas,
            Model model) {
        model.addAttribute("personas", servicio.listar(texto, bajas));
        model.addAttribute("texto", texto);
        model.addAttribute("bajas", bajas);
        modulo(model);
        return "personas/lista";
    }

    /** Formulario de alta. */
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        return formulario(model, new DatosPersona(), null);
    }

    /** Registra el vendedor. */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("datos") DatosPersona datos,
            BindingResult errores,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos, null);
        try {
            long id = servicio.crear(datos);
            redireccion.addFlashAttribute("mensaje", "Vendedor registrado correctamente.");
            return "redirect:/vendedores/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, null);
        }
    }

    /** Ficha con ventas realizadas y pruebas de manejo. */
    @GetMapping("/{id}")
    public String ficha(@PathVariable long id, Model model) {
        model.addAttribute("historial", servicio.historial(id));
        modulo(model);
        return "personas/ficha";
    }

    /** Formulario de edicion precargado. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        Vendedor c = servicio.obtener(id);
        return formulario(model, DatosPersona.desde(c), c);
    }

    /** Guarda los cambios. */
    @PostMapping("/{id}")
    public String guardar(
            @PathVariable long id,
            @Valid @ModelAttribute("datos") DatosPersona datos,
            BindingResult errores,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos, servicio.obtener(id));
        try {
            servicio.editar(id, datos);
            redireccion.addFlashAttribute("mensaje", "Cambios guardados.");
            return "redirect:/vendedores/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, servicio.obtener(id));
        }
    }

    /** Baja logica: conserva ventas y pruebas asociadas. */
    @PostMapping("/{id}/baja")
    public String baja(@PathVariable long id, RedirectAttributes redireccion) {
        try {
            servicio.darDeBaja(id);
            redireccion.addFlashAttribute("mensaje", "Vendedor dado de baja. Su historial se conserva.");
        } catch (ReglaNegocioException ex) {
            redireccion.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/vendedores/" + id;
    }

    /** Reactiva un vendedor dado de baja. */
    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable long id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Vendedor reactivado.");
        return "redirect:/vendedores/" + id;
    }

    private String formulario(Model model, DatosPersona datos, Vendedor existente) {
        model.addAttribute("datos", datos);
        model.addAttribute("existente", existente);
        modulo(model);
        return "personas/form";
    }

    private static void modulo(Model model) {
        model.addAttribute("modulo", "vendedores");
        model.addAttribute("singular", "vendedor");
        model.addAttribute("plural", "Vendedores");
        model.addAttribute("esVendedor", true);
    }
}
