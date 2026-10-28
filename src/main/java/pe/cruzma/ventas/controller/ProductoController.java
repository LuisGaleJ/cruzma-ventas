package pe.cruzma.ventas.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.service.ProductoService;

/** P5 Catálogo de productos y servicios (RF04). Todos lo consultan; solo el administrador lo modifica. */
@Controller
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService service;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service servicio del catálogo
     */
    public ProductoController(ProductoService service) { this.service = service; }

    /**
     * GET {@code /productos}: catálogo con filtro por tipo.
     *
     * @param tipo   tipo a filtrar
     * @param editar id del ítem a editar
     * @param model  modelo de la vista
     * @return vista {@code productos/lista}
     */
    @GetMapping
    public String listar(@RequestParam(required = false) TipoProducto tipo, @RequestParam(required = false) Long editar, Model model) {
        model.addAttribute("productos", service.listar(tipo));
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("producto", editar != null ? service.obtener(editar) : new Producto());
        model.addAttribute("tipos", TipoProducto.values());
        return "productos/lista";
    }

    /**
     * POST {@code /productos/guardar}: registra o actualiza un ítem (solo administrador).
     *
     * @param producto datos del formulario
     * @param br       errores de validación
     * @param model    modelo de la vista
     * @param ra       mensajes para la siguiente vista
     * @return redirección al catálogo, o la misma vista si hay errores
     */
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("producto") Producto producto, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("productos", service.listar(null));
            model.addAttribute("tipos", TipoProducto.values());
            return "productos/lista";
        }
        try {
            service.guardar(producto);
            ra.addFlashAttribute("ok", "Producto " + producto.getCodigo() + " guardado");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", "No se pudo guardar: revisa que el código no esté repetido");
        }
        return "redirect:/productos";
    }
}
