package pe.cruzma.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.integration.RespuestaOSE;
import pe.cruzma.ventas.model.Comprobante;
import pe.cruzma.ventas.service.ComprobanteService;

/** Comprobantes emitidos: lista, representación impresa y anulación (RF09, RF10, RF11). */
@Controller
@RequestMapping("/comprobantes")
public class ComprobanteController {
    private final ComprobanteService service;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service servicio de comprobantes
     */
    public ComprobanteController(ComprobanteService service) { this.service = service; }

    /**
     * GET {@code /comprobantes}: lista de comprobantes emitidos.
     *
     * @param model modelo de la vista
     * @return vista {@code comprobantes/lista}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("comprobantes", service.listar());
        return "comprobantes/lista";
    }

    /**
     * GET {@code /comprobantes/{id}}: representación impresa del comprobante.
     *
     * @param id    id del comprobante
     * @param model modelo de la vista
     * @return vista {@code comprobantes/ver}
     */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("c", service.obtener(id));
        return "comprobantes/ver";
    }

    /**
     * POST {@code /comprobantes/{id}/anular}: anula el comprobante con nota de crédito (RF10).
     *
     * @param id     id del comprobante
     * @param motivo motivo de la anulación
     * @param ra     mensajes para la siguiente vista
     * @return redirección a la venta
     */
    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes ra) {
        Comprobante c = service.obtener(id);
        try {
            RespuestaOSE r = service.anular(id, motivo);
            ra.addFlashAttribute(r.aceptado() ? "ok" : "error", r.mensaje());
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/" + c.getVenta().getId();
    }
}
