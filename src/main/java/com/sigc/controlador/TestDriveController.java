package com.sigc.controlador;

import com.sigc.dominio.EstadoTestDrive;
import com.sigc.dominio.EstadoVehiculo;
import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.ClienteService;
import com.sigc.servicio.TestDriveService;
import com.sigc.servicio.VehiculoService;
import com.sigc.servicio.VendedorService;
import com.sigc.servicio.vista.DatosTestDrive;
import com.sigc.servicio.vista.FiltroVehiculos;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Rutas HTTP de pruebas de manejo: historial, inicio y finalizacion. */
@Controller
@RequestMapping("/testdrives")
public class TestDriveController {
    private final TestDriveService testDrives;
    private final VehiculoService vehiculos;
    private final ClienteService clientes;
    private final VendedorService vendedores;

    /**
     * @param testDrives casos de uso de pruebas de manejo
     * @param vehiculos consulta de vehiculos
     * @param clientes consulta de clientes
     * @param vendedores consulta de vendedores
     */
    public TestDriveController(
            TestDriveService testDrives,
            VehiculoService vehiculos,
            ClienteService clientes,
            VendedorService vendedores) {
        this.testDrives = testDrives;
        this.vehiculos = vehiculos;
        this.clientes = clientes;
        this.vendedores = vendedores;
    }

    /** Historial, filtrable por estado. */
    @GetMapping
    public String lista(@RequestParam(required = false) EstadoTestDrive estado, Model model) {
        model.addAttribute("pruebas", testDrives.listar(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoTestDrive.values());
        return "testdrives/lista";
    }

    /** Formulario de inicio; "vehiculoId" preselecciona el vehiculo. */
    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long vehiculoId, Model model) {
        DatosTestDrive datos = new DatosTestDrive();
        datos.setVehiculoId(vehiculoId);
        return formulario(model, datos);
    }

    /** Inicia la prueba: el vehiculo pasa a EN_TEST_DRIVE. */
    @PostMapping
    public String iniciar(
            @Valid @ModelAttribute("datos") DatosTestDrive datos,
            BindingResult errores,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos);
        try {
            testDrives.iniciar(datos);
            redireccion.addFlashAttribute("mensaje", "Test drive iniciado. El vehículo quedó en prueba.");
            return "redirect:/testdrives";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos);
        }
    }

    /** Finaliza la prueba: el vehiculo vuelve a DISPONIBLE. */
    @PostMapping("/{id}/finalizar")
    public String finalizar(
            @PathVariable long id,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redireccion) {
        try {
            testDrives.finalizar(id, observaciones);
            redireccion.addFlashAttribute("mensaje", "Test drive finalizado. El vehículo está disponible.");
        } catch (ReglaNegocioException ex) {
            redireccion.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/testdrives";
    }

    private String formulario(Model model, DatosTestDrive datos) {
        model.addAttribute("datos", datos);
        model.addAttribute(
                "disponibles",
                vehiculos.listar(
                        new FiltroVehiculos(
                                null, null, EstadoVehiculo.DISPONIBLE, null, null, null, null, null, false)));
        model.addAttribute("clientes", clientes.listar(null, false));
        model.addAttribute("vendedores", vendedores.listar(null, false));
        return "testdrives/form";
    }
}
