package com.sigc.controlador;

import com.sigc.servicio.DashboardService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Panel principal con indicadores reales de inventario, personas, ventas y operaciones. */
@Controller
public class InicioController {
    private final DashboardService dashboard;

    /**
     * @param dashboard arma los datos del panel
     */
    public InicioController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    /** Dashboard. */
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("panel", dashboard.panel());
        return "inicio";
    }
}
