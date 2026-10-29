package pe.cruzma.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.cruzma.ventas.service.HistorialCambiosService;

/** P9 Historial de cambios en la base de datos (auditoría de datos). Solo administrador. */
@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {
    private final HistorialCambiosService service;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service servicio de historial
     */
    public AuditoriaController(HistorialCambiosService service) { this.service = service; }

    /**
     * GET {@code /auditoria}: quién creó o modificó cada registro y cuándo.
     *
     * @param entidad filtro opcional por tipo de registro
     * @param id      filtro opcional por clave del registro
     * @param model   modelo de la vista
     * @return vista {@code auditoria/historial}
     */
    @GetMapping
    public String historial(@RequestParam(required = false) String entidad,
                            @RequestParam(required = false) Long id, Model model) {
        model.addAttribute("entidades", service.entidades());
        model.addAttribute("entidad", entidad);
        model.addAttribute("id", id);
        try {
            model.addAttribute("cambios", service.historial(entidad, id));
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cambios", java.util.List.of());
        }
        return "auditoria/historial";
    }
}
