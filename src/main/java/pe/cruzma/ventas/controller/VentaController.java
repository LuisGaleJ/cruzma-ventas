package pe.cruzma.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.integration.RespuestaOSE;
import pe.cruzma.ventas.model.MedioPago;
import pe.cruzma.ventas.model.TipoComprobante;
import pe.cruzma.ventas.service.ComprobanteService;
import pe.cruzma.ventas.service.VentaService;

import java.math.BigDecimal;

/** P7 Venta y comprobante (RF08, RF09, RF11). */
@Controller
@RequestMapping("/ventas")
public class VentaController {
    private final VentaService ventas;
    private final ComprobanteService comprobantes;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param ventas       servicio de ventas
     * @param comprobantes servicio de comprobantes
     */
    public VentaController(VentaService ventas, ComprobanteService comprobantes) {
        this.ventas = ventas;
        this.comprobantes = comprobantes;
    }

    /**
     * GET {@code /ventas}: lista de ventas.
     *
     * @param model modelo de la vista
     * @return vista {@code ventas/lista}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventas.listar());
        return "ventas/lista";
    }

    /**
     * GET {@code /ventas/{id}}: detalle de la venta, pagos y comprobantes (P7).
     *
     * @param id    id de la venta
     * @param model modelo de la vista
     * @return vista {@code ventas/detalle}
     */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventas.obtener(id));
        model.addAttribute("medios", MedioPago.values());
        return "ventas/detalle";
    }

    /**
     * POST {@code /ventas/{id}/pagos}: registra un adelanto o pago (RF08).
     *
     * @param id    id de la venta
     * @param monto monto pagado
     * @param medio medio de pago
     * @param ra    mensajes para la siguiente vista
     * @return redirección a la venta
     */
    @PostMapping("/{id}/pagos")
    public String pagar(@PathVariable Long id, @RequestParam(required = false) BigDecimal monto,
                        @RequestParam MedioPago medio, RedirectAttributes ra) {
        try {
            ventas.registrarPago(id, monto, medio);
            ra.addFlashAttribute("ok", "Pago de S/ " + monto + " registrado");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }

    /**
     * POST {@code /ventas/{id}/emitir}: emite la boleta o factura y la envía al OSE (RF09).
     *
     * @param id   id de la venta
     * @param tipo boleta o factura
     * @param ra   mensajes para la siguiente vista
     * @return redirección a la venta
     */
    @PostMapping("/{id}/emitir")
    public String emitir(@PathVariable Long id, @RequestParam TipoComprobante tipo, RedirectAttributes ra) {
        try {
            RespuestaOSE r = comprobantes.emitir(id, tipo);
            ra.addFlashAttribute(r.aceptado() ? "ok" : "error", r.mensaje());
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/" + id;
    }
}
