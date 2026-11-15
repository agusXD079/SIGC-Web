package com.sigc.controlador;

import com.sigc.dominio.EstadoVehiculo;
import com.sigc.dominio.TipoVehiculo;
import com.sigc.servicio.VehiculoService;
import com.sigc.servicio.vista.FiltroVehiculos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Vista de solo lectura del inventario: estadisticas y listado filtrable por estado y tipo. */
@Controller
@RequestMapping("/inventario")
public class InventarioController {
    private final VehiculoService servicio;

    /**
     * @param servicio casos de uso de vehiculos
     */
    public InventarioController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    /** Inventario con estadisticas; por defecto ordenado por precio ascendente. */
    @GetMapping
    public String inventario(
            @RequestParam(required = false) EstadoVehiculo estado,
            @RequestParam(required = false) TipoVehiculo tipo,
            @RequestParam(defaultValue = "PRECIO_ASC") String orden,
            Model model) {
        FiltroVehiculos filtro =
                new FiltroVehiculos(null, tipo, estado, null, null, null, null, orden, false);
        model.addAttribute("vehiculos", servicio.listar(filtro));
        model.addAttribute("resumen", servicio.resumen());
        model.addAttribute("filtro", filtro);
        model.addAttribute("estados", EstadoVehiculo.values());
        model.addAttribute("tipos", TipoVehiculo.values());
        return "inventario";
    }
}
