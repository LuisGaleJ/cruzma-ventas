package pe.cruzma.ventas.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.model.Cliente;
import pe.cruzma.ventas.model.TipoDocumento;
import pe.cruzma.ventas.service.ClienteService;

/** P4 Clientes (RF03): búsqueda por DNI, RUC o nombre, registro y edición. */
@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service servicio de clientes
     */
    public ClienteController(ClienteService service) { this.service = service; }

    /**
     * GET {@code /clientes}: búsqueda, lista y formulario de clientes.
     *
     * @param q      texto a buscar
     * @param editar id del cliente a editar; {@code null} para uno nuevo
     * @param model  modelo de la vista
     * @return vista {@code clientes/lista}
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String q, @RequestParam(required = false) Long editar, Model model) {
        model.addAttribute("clientes", service.buscar(q));
        model.addAttribute("q", q);
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", editar != null ? service.obtener(editar) : new Cliente());
        }
        model.addAttribute("tipos", TipoDocumento.values());
        return "clientes/lista";
    }

    /**
     * POST {@code /clientes/guardar}: registra o actualiza un cliente.
     *
     * @param cliente datos del formulario
     * @param br      errores de validación
     * @param model   modelo de la vista
     * @param ra      mensajes para la siguiente vista
     * @return redirección a la lista, o la misma vista si hay errores
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult br, Model model, RedirectAttributes ra) {
        if (!br.hasErrors()) {
            try {
                service.guardar(cliente);
                ra.addFlashAttribute("ok", "Cliente " + cliente.getRazonSocial() + " guardado");
                return "redirect:/clientes";
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("clientes", service.buscar(null));
        model.addAttribute("tipos", TipoDocumento.values());
        return "clientes/lista";
    }
}
