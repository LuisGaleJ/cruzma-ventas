package pe.cruzma.ventas.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.dto.ItemCotizacionForm;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** P6 Cotizaciones (RF05, RF06, RF07). */
@Controller
@RequestMapping("/cotizaciones")
public class CotizacionController {
    private final CotizacionService service;
    private final ClienteService clientes;
    private final ProductoService productos;
    private final UsuarioService usuarios;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service   servicio de cotizaciones
     * @param clientes  servicio de clientes
     * @param productos servicio del catálogo
     * @param usuarios  servicio de usuarios
     */
    public CotizacionController(CotizacionService service, ClienteService clientes,
                                ProductoService productos, UsuarioService usuarios) {
        this.service = service;
        this.clientes = clientes;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /**
     * GET {@code /cotizaciones}: lista de cotizaciones.
     *
     * @param model modelo de la vista
     * @return vista {@code cotizaciones/lista}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cotizaciones", service.listar());
        return "cotizaciones/lista";
    }

    /**
     * GET {@code /cotizaciones/nueva}: formulario de nueva cotización (P6).
     *
     * @param clienteId cliente preseleccionado, opcional
     * @param model     modelo de la vista
     * @return vista {@code cotizaciones/form}
     */
    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Long clienteId, Model model) {
        model.addAttribute("clientes", clientes.buscar(null));
        model.addAttribute("productos", productos.listar(null));
        model.addAttribute("clienteId", clienteId);
        model.addAttribute("diasValidez", CotizacionService.DIAS_VALIDEZ);
        return "cotizaciones/form";
    }

    /**
     * POST {@code /cotizaciones}: guarda una cotización con sus ítems.
     * <p>
     * Cada lista trae un valor por fila del formulario, en el mismo orden.
     *
     * @param clienteId   cliente de la cotización
     * @param productoIds productos de cada fila
     * @param cantidades  cantidades de cada fila
     * @param medidas     medidas de cada fila
     * @param precios     precios de cada fila
     * @param user        usuario que cotiza
     * @param ra          mensajes para la siguiente vista
     * @return redirección al detalle, o al formulario si hay errores
     */
    @PostMapping
    public String crear(@RequestParam(required = false) Long clienteId,
                        @RequestParam(name = "productoId", required = false) List<Long> productoIds,
                        @RequestParam(name = "cantidad", required = false) List<Integer> cantidades,
                        @RequestParam(name = "medidas", required = false) List<String> medidas,
                        @RequestParam(name = "precio", required = false) List<BigDecimal> precios,
                        @AuthenticationPrincipal UserDetails user, RedirectAttributes ra) {
        List<ItemCotizacionForm> items = new ArrayList<>();
        if (productoIds != null) {
            for (int i = 0; i < productoIds.size(); i++) {
                items.add(new ItemCotizacionForm(productoIds.get(i), valor(cantidades, i), valor(medidas, i), valor(precios, i)));
            }
        }
        try {
            var cot = service.crear(clienteId, items, usuarios.porUsername(user.getUsername()));
            ra.addFlashAttribute("ok", "Cotización " + cot.getNumero() + " registrada");
            return "redirect:/cotizaciones/" + cot.getId();
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cotizaciones/nueva";
        }
    }

    /**
     * GET {@code /cotizaciones/{id}}: vista imprimible de la cotización; el navegador la guarda como PDF (RF06).
     *
     * @param id    id de la cotización
     * @param model modelo de la vista
     * @return vista {@code cotizaciones/detalle}
     */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("cot", service.obtener(id));
        return "cotizaciones/detalle";
    }

    /**
     * POST {@code /cotizaciones/{id}/rechazar}: marca la cotización como rechazada.
     *
     * @param id id de la cotización
     * @param ra mensajes para la siguiente vista
     * @return redirección al detalle
     */
    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.rechazar(id);
            ra.addFlashAttribute("ok", "Cotización marcada como rechazada");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cotizaciones/" + id;
    }

    /**
     * POST {@code /cotizaciones/{id}/convertir}: convierte la cotización en venta (RF07).
     *
     * @param id   id de la cotización
     * @param user usuario que registra la venta
     * @param ra   mensajes para la siguiente vista
     * @return redirección a la venta creada, o al detalle si no se pudo
     */
    @PostMapping("/{id}/convertir")
    public String convertir(@PathVariable Long id, @AuthenticationPrincipal UserDetails user, RedirectAttributes ra) {
        try {
            Venta v = service.convertirAVenta(id, usuarios.porUsername(user.getUsername()));
            ra.addFlashAttribute("ok", "Venta registrada. Registra el adelanto para emitir el comprobante.");
            return "redirect:/ventas/" + v.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cotizaciones/" + id;
        }
    }

    /**
     * Devuelve el elemento i de una lista o {@code null} si no existe.
     *
     * @param lista lista de valores del formulario
     * @param i     posición
     * @param <T>   tipo de los elementos
     * @return el valor o {@code null}
     */
    private static <T> T valor(List<T> lista, int i) {
        return (lista != null && i < lista.size()) ? lista.get(i) : null;
    }
}
