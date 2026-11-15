package com.sigc.controlador;

import com.sigc.dominio.Cliente;
import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.ClienteService;
import com.sigc.servicio.vista.DatosPersona;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Rutas HTTP del modulo de clientes. Las plantillas se comparten con vendedores (carpeta personas). */
@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService servicio;

    /**
     * @param servicio casos de uso de clientes
     */
    public ClienteController(ClienteService servicio) {
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

    /** Registra el cliente. */
    @PostMapping
    public String crear(
            @Valid @ModelAttribute("datos") DatosPersona datos,
            BindingResult errores,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos, null);
        try {
            long id = servicio.crear(datos);
            redireccion.addFlashAttribute("mensaje", "Cliente registrado correctamente.");
            return "redirect:/clientes/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, null);
        }
    }

    /** Ficha con historial de compras y pruebas de manejo. */
    @GetMapping("/{id}")
    public String ficha(@PathVariable long id, Model model) {
        model.addAttribute("historial", servicio.historial(id));
        modulo(model);
        return "personas/ficha";
    }

    /** Formulario de edicion precargado. */
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        Cliente c = servicio.obtener(id);
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
            return "redirect:/clientes/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos, servicio.obtener(id));
        }
    }

    /** Baja logica: conserva compras y pruebas asociadas. */
    @PostMapping("/{id}/baja")
    public String baja(@PathVariable long id, RedirectAttributes redireccion) {
        try {
            servicio.darDeBaja(id);
            redireccion.addFlashAttribute("mensaje", "Cliente dado de baja. Su historial se conserva.");
        } catch (ReglaNegocioException ex) {
            redireccion.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/clientes/" + id;
    }

    /** Reactiva un cliente dado de baja. */
    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable long id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Cliente reactivado.");
        return "redirect:/clientes/" + id;
    }

    private String formulario(Model model, DatosPersona datos, Cliente existente) {
        model.addAttribute("datos", datos);
        model.addAttribute("existente", existente);
        modulo(model);
        return "personas/form";
    }

    private static void modulo(Model model) {
        model.addAttribute("modulo", "clientes");
        model.addAttribute("singular", "cliente");
        model.addAttribute("plural", "Clientes");
        model.addAttribute("esVendedor", false);
    }
}
