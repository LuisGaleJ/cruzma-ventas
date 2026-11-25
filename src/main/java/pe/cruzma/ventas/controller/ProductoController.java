package pe.cruzma.ventas.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.model.UnidadMedida;
import pe.cruzma.ventas.service.ProductoService;

/**
 * P5 Catálogo de productos y servicios (RF04). Todos lo consultan; solo el administrador lo modifica,
 * en una ventana emergente.
 */
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
     * @param editar id del ítem a editar: abre la ventana con sus datos (enlace directo)
     * @param model  modelo de la vista
     * @return vista {@code productos/lista}
     */
    @GetMapping
    public String listar(@RequestParam(required = false) TipoProducto tipo, @RequestParam(required = false) Long editar, Model model) {
        model.addAttribute("productos", service.listar(tipo));
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("producto", editar != null ? service.obtener(editar) : new Producto());
        model.addAttribute("tipos", TipoProducto.values());
        agregarUnidades(model);
        model.addAttribute("siguientesCodigos", service.siguientesCodigos());
        model.addAttribute("abrirFormulario", editar != null);
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
        // Unidad: debe ser de la Tabla 6 de SUNAT; ZZ solo para servicios
        String errorUnidad = UnidadMedida.validar(producto.getTipo(), producto.getUnidad());
        if (errorUnidad != null && !br.hasFieldErrors("unidad")) br.rejectValue("unidad", "unidad.invalida", errorUnidad);
        if (!br.hasErrors()) {
            try {
                service.guardar(producto);
                ra.addFlashAttribute("ok", "Producto " + producto.getCodigo() + " guardado");
                return "redirect:/productos";
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", e.getMessage());
            } catch (RuntimeException e) {
                model.addAttribute("error", "No se pudo guardar: intenta de nuevo");
            }
        }
        // Errores: se vuelve a mostrar el catálogo con la ventana abierta, los datos escritos y los mensajes
        model.addAttribute("productos", service.listar(null));
        model.addAttribute("tipos", TipoProducto.values());
        agregarUnidades(model);
        model.addAttribute("siguientesCodigos", service.siguientesCodigos());
        model.addAttribute("abrirFormulario", true);
        return "productos/lista";
    }

    /** Lista desplegable de unidades: las de la Tabla 6 de SUNAT que corresponden al giro de la empresa. */
    private static void agregarUnidades(Model model) {
        model.addAttribute("unidades", UnidadMedida.todas());
    }
}
