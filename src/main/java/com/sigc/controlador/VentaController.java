package com.sigc.controlador;

import com.sigc.dominio.EstadoVehiculo;
import com.sigc.dominio.ModalidadPago;
import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.ClienteService;
import com.sigc.servicio.VehiculoService;
import com.sigc.servicio.VendedorService;
import com.sigc.servicio.VentaService;
import com.sigc.servicio.vista.DatosVenta;
import com.sigc.servicio.vista.FiltroVehiculos;
import com.sigc.servicio.vista.FiltroVentas;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Rutas HTTP de ventas: historial con filtros, registro y comprobante imprimible. */
@Controller
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService ventas;
    private final VehiculoService vehiculos;
    private final ClienteService clientes;
    private final VendedorService vendedores;

    /**
     * @param ventas casos de uso de ventas
     * @param vehiculos consulta de vehiculos
     * @param clientes consulta de clientes
     * @param vendedores consulta de vendedores
     */
    public VentaController(
            VentaService ventas,
            VehiculoService vehiculos,
            ClienteService clientes,
            VendedorService vendedores) {
        this.ventas = ventas;
        this.vehiculos = vehiculos;
        this.clientes = clientes;
        this.vendedores = vendedores;
    }

    /** Historial de ventas, filtrable por cliente, vendedor y rango de fechas. */
    @GetMapping
    public String lista(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            model.addAttribute("error", "La fecha final no puede ser anterior a la inicial.");
            desde = null;
            hasta = null;
        }
        model.addAttribute("ventas", ventas.listar(new FiltroVentas(clienteId, vendedorId, desde, hasta)));
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("vendedorId", vendedorId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("clientes", clientes.listar(null, true));
        model.addAttribute("vendedores", vendedores.listar(null, true));
        return "ventas/lista";
    }

    /** Formulario de venta; "vehiculoId" preselecciona el vehiculo. */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long vehiculoId, Model model) {
        DatosVenta datos = new DatosVenta();
        datos.setVehiculoId(vehiculoId);
        datos.setClave(ventas.nuevaClave());
        return formulario(model, datos);
    }

    /** Registra la venta. Un reenvio con la misma clave lleva al comprobante ya existente. */
    @PostMapping
    public String registrar(
            @Valid @ModelAttribute("datos") DatosVenta datos,
            BindingResult errores,
            Model model,
            RedirectAttributes redireccion) {
        if (errores.hasErrors()) return formulario(model, datos);
        try {
            long id = ventas.registrar(datos);
            redireccion.addFlashAttribute("mensaje", "Venta registrada correctamente.");
            return "redirect:/ventas/" + id;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return formulario(model, datos);
        }
    }

    /** Comprobante de la venta, apto para imprimir. */
    @GetMapping("/{id}")
    public String comprobante(@PathVariable long id, Model model) {
        model.addAttribute("venta", ventas.obtener(id));
        return "ventas/comprobante";
    }

    private String formulario(Model model, DatosVenta datos) {
        model.addAttribute("datos", datos);
        model.addAttribute(
                "disponibles",
                vehiculos.listar(
                        new FiltroVehiculos(
                                null, null, EstadoVehiculo.DISPONIBLE, null, null, null, null, null, false)));
        model.addAttribute("clientes", clientes.listar(null, false));
        model.addAttribute("vendedores", vendedores.listar(null, false));
        model.addAttribute("modalidades", ModalidadPago.values());
        return "ventas/form";
    }
}
