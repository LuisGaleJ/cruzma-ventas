package pe.cruzma.ventas.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import pe.cruzma.ventas.integration.ConsultaDocumentoService;
import pe.cruzma.ventas.model.Cliente;
import pe.cruzma.ventas.model.TipoDocumento;
import pe.cruzma.ventas.service.ClienteService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * P4 Clientes (RF03): búsqueda por DNI, RUC o nombre; registro y edición en una ventana emergente.
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService service;
    private final ConsultaDocumentoService consulta;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service  servicio de clientes
     * @param consulta consulta de DNI y RUC
     */
    public ClienteController(ClienteService service, ConsultaDocumentoService consulta) {
        this.service = service;
        this.consulta = consulta;
    }

    /**
     * GET {@code /clientes}: búsqueda, lista y formulario de clientes.
     *
     * @param q      texto a buscar
     * @param editar id del cliente a editar: abre la ventana con sus datos (enlace directo)
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
        model.addAttribute("abrirFormulario", editar != null);
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
        model.addAttribute("abrirFormulario", true);   // vuelve a abrir la ventana con los errores
        return "clientes/lista";
    }

    /**
     * GET {@code /clientes/consulta?tipo=DNI&numero=...}: busca el nombre (y la dirección, si es RUC)
     * en RENIEC o SUNAT para autocompletar el formulario. Responde JSON; lo llama la ventana de cliente.
     *
     * @param tipo   DNI o RUC
     * @param numero número del documento
     * @return 200 con los datos, 404 si no existe, 400 si el número es inválido, 503 si el servicio no está disponible
     */
    @GetMapping(value = "/consulta", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> consultar(@RequestParam TipoDocumento tipo, @RequestParam String numero) {
        Map<String, Object> r = new LinkedHashMap<>();
        try {
            var datos = consulta.consultar(tipo, numero.trim());
            if (datos.isEmpty()) {
                r.put("mensaje", "No se encontró el " + tipo + " " + numero);
                return ResponseEntity.status(404).body(r);
            }
            r.put("nombre", datos.get().nombre());
            r.put("direccion", datos.get().direccion());
            return ResponseEntity.ok(r);
        } catch (IllegalArgumentException e) {
            r.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(r);
        } catch (IllegalStateException e) {
            r.put("mensaje", e.getMessage());
            return ResponseEntity.status(503).body(r);
        }
    }
}
