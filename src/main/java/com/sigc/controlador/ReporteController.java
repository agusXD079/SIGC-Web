package com.sigc.controlador;

import com.sigc.dominio.ReglaNegocioException;
import com.sigc.servicio.ReporteService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Reportes de ventas por vendedor, periodo y tipo de vehiculo. */
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService reportes;

    /**
     * @param reportes servicio de reportes
     */
    public ReporteController(ReporteService reportes) {
        this.reportes = reportes;
    }

    /** Reporte del rango pedido; sin fechas abarca todo el historial. */
    @GetMapping
    public String ver(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {
        try {
            model.addAttribute("reporte", reportes.generar(desde, hasta));
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("reporte", reportes.generar(null, null));
            desde = null;
            hasta = null;
        }
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "reportes";
    }
}
