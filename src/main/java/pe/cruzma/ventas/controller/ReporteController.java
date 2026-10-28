package pe.cruzma.ventas.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.service.ReporteService;
import pe.cruzma.ventas.service.exportacion.ExportadorReporte;
import pe.cruzma.ventas.util.Auditoria;

import java.time.LocalDate;
import java.util.List;

/**
 * P8 Reportes R1, R2 y R3 y su exportación a Excel y CSV (RF12, RF13, RF14).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    private final ReporteService service;
    private final ImmutableMap<String, ExportadorReporte> exportadores;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     * <p>
     * Spring entrega todos los {@link ExportadorReporte} registrados; Guava los indexa por formato.
     *
     * @param service      servicio de reportes
     * @param exportadores formatos de exportación disponibles
     */
    public ReporteController(ReporteService service, List<ExportadorReporte> exportadores) {
        this.service = service;
        this.exportadores = Maps.uniqueIndex(exportadores, ExportadorReporte::formato);
    }

    /**
     * GET {@code /reportes}: reportes del período; por defecto, el mes actual.
     *
     * @param desde primer día del período
     * @param hasta último día del período
     * @param model modelo de la vista
     * @return vista {@code reportes/index}
     */
    @GetMapping
    public String ver(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                      Model model) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);
        if (inicio.isAfter(fin)) {
            model.addAttribute("error", "La fecha inicial no puede ser posterior a la final; se muestra el mes actual");
            fin = LocalDate.now();
            inicio = fin.withDayOfMonth(1);
        }
        DatosReporte d = service.armar(inicio, fin);
        model.addAttribute("desde", d.desde());
        model.addAttribute("hasta", d.hasta());
        model.addAttribute("total", d.total());
        model.addAttribute("numeroVentas", d.numeroVentas());
        model.addAttribute("ticketPromedio", d.ticketPromedio());
        model.addAttribute("porDia", d.porDia());
        model.addAttribute("porCliente", d.porCliente());
        model.addAttribute("porProducto", d.porProducto());
        model.addAttribute("porCobrar", d.porCobrar());
        model.addAttribute("formatos", exportadores.keySet());
        return "reportes/index";
    }

    /**
     * GET {@code /reportes/exportar}: descarga los reportes del período (RF14).
     *
     * @param formato {@code xlsx} (Excel) o {@code csv}
     * @param desde   primer día del período
     * @param hasta   último día del período
     * @return el archivo como descarga
     * @throws ResponseStatusException 404 si el formato no existe, 400 si las fechas no son válidas
     */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam String formato,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        ExportadorReporte exportador = exportadores.get(formato);
        if (exportador == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formato no disponible");
        if (desde.isAfter(hasta)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rango de fechas no válido");

        byte[] archivo = exportador.exportar(service.armar(desde, hasta));
        Auditoria.registrar("EXPORTAR_REPORTE", formato + " " + desde + " a " + hasta);
        String nombre = "ventas_" + desde + "_" + hasta + "." + exportador.formato();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportador.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(archivo);
    }
}
