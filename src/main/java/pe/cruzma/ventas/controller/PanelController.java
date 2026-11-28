package pe.cruzma.ventas.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.cruzma.ventas.dto.Periodo;
import pe.cruzma.ventas.model.EstadoCotizacion;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.service.AlertaCotizacionService;
import pe.cruzma.ventas.service.CotizacionService;
import pe.cruzma.ventas.service.ReporteService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** P2 Dashboard: indicadores del período elegido, ventas del período y cotizaciones por responder. */
@Controller
public class PanelController {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReporteService reportes;
    private final CotizacionService cotizaciones;
    private final AlertaCotizacionService alertas;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param reportes     servicio de reportes
     * @param cotizaciones servicio de cotizaciones
     * @param alertas      alertas de cotizaciones por vencer
     */
    public PanelController(ReporteService reportes, CotizacionService cotizaciones, AlertaCotizacionService alertas) {
        this.reportes = reportes;
        this.cotizaciones = cotizaciones;
        this.alertas = alertas;
    }

    /**
     * GET {@code /}: Dashboard filtrado por fechas.
     * <p>
     * Se elige un acceso rápido ({@code periodo=HOY}, {@code MES}...) o un rango libre ({@code desde} y
     * {@code hasta}). Sin parámetros muestra el día de hoy. Si las fechas vienen invertidas se intercambian.
     *
     * @param periodo acceso rápido; tiene prioridad sobre el rango libre
     * @param desde   primer día del rango libre
     * @param hasta   último día del rango libre
     * @param model   modelo de la vista
     * @return vista {@code panel}
     */
    @GetMapping("/")
    public String panel(@RequestParam(required = false) Periodo periodo,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model model) {
        LocalDate hoy = LocalDate.now();
        if (periodo == null && desde == null && hasta == null) periodo = Periodo.HOY;
        if (periodo != null) {
            desde = periodo.desde(hoy);
            hasta = periodo.hasta(hoy);
        } else {
            if (desde == null) desde = hasta;
            if (hasta == null) hasta = desde;
            if (desde.isAfter(hasta)) { LocalDate t = desde; desde = hasta; hasta = t; }
        }
        model.addAttribute("periodos", Periodo.values());
        model.addAttribute("periodo", periodo);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("etiquetaPeriodo", etiqueta(periodo, desde, hasta));
        model.addAttribute("resumen", reportes.resumen(desde, hasta));
        List<Venta> ventas = new ArrayList<>(reportes.ventasDelPeriodo(desde, hasta));
        Collections.reverse(ventas);                                  // primero las más recientes (Java 17: sin reversed())
        model.addAttribute("ventasPeriodo", ventas.stream().limit(10).toList());
        model.addAttribute("porVencer", alertas.porVencer());
        model.addAttribute("pendientes", cotizaciones.listar().stream()
                .filter(c -> c.getEstado() == EstadoCotizacion.PENDIENTE).limit(5).toList());
        return "panel";
    }

    /** Texto del período, por ejemplo «Hoy · 28/11/2026» o «01/11/2026 – 28/11/2026». */
    static String etiqueta(Periodo periodo, LocalDate desde, LocalDate hasta) {
        String fechas = desde.equals(hasta) ? FECHA.format(desde) : FECHA.format(desde) + " – " + FECHA.format(hasta);
        return periodo == null ? fechas : periodo.getEtiqueta() + " · " + fechas;
    }
}
