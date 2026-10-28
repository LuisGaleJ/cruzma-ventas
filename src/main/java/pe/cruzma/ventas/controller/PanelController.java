package pe.cruzma.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.cruzma.ventas.service.CotizacionService;
import pe.cruzma.ventas.service.ReporteService;
import pe.cruzma.ventas.service.VentaService;

/** P2 Panel principal. */
@Controller
public class PanelController {
    private final ReporteService reportes;
    private final VentaService ventas;
    private final CotizacionService cotizaciones;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param reportes     servicio de reportes
     * @param ventas       servicio de ventas
     * @param cotizaciones servicio de cotizaciones
     */
    public PanelController(ReporteService reportes, VentaService ventas, CotizacionService cotizaciones) {
        this.reportes = reportes;
        this.ventas = ventas;
        this.cotizaciones = cotizaciones;
    }

    /**
     * GET {@code /}: panel con indicadores, últimas ventas y cotizaciones pendientes.
     *
     * @param model modelo de la vista
     * @return vista {@code panel}
     */
    @GetMapping("/")
    public String panel(Model model) {
        model.addAttribute("resumen", reportes.resumen());
        model.addAttribute("ultimasVentas", ventas.listar().stream().limit(5).toList());
        model.addAttribute("pendientes", cotizaciones.listar().stream()
                .filter(c -> c.getEstado().name().equals("PENDIENTE")).limit(5).toList());
        return "panel";
    }
}
