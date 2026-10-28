package pe.cruzma.ventas.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.cruzma.ventas.service.UsuarioService;

/** P3 Gestión de usuarios (RF02). Solo administrador. */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param service servicio de usuarios
     */
    public UsuarioController(UsuarioService service) { this.service = service; }

    /**
     * GET {@code /usuarios}: lista de usuarios y formulario de alta.
     *
     * @param model modelo de la vista
     * @return vista {@code usuarios/lista}
     */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", service.listar());
        model.addAttribute("roles", service.roles());
        return "usuarios/lista";
    }

    /**
     * POST {@code /usuarios}: crea un usuario.
     *
     * @param username nombre de usuario
     * @param nombres  nombre completo
     * @param password contraseña
     * @param idRol    rol asignado
     * @param ra       mensajes para la siguiente vista
     * @return redirección a la lista
     */
    @PostMapping
    public String crear(@RequestParam String username, @RequestParam String nombres,
                        @RequestParam String password, @RequestParam Long idRol, RedirectAttributes ra) {
        try {
            service.crear(username.trim().toLowerCase(), nombres.trim(), password, idRol);
            ra.addFlashAttribute("ok", "Usuario " + username + " creado");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    /**
     * POST {@code /usuarios/{id}/estado}: activa o desactiva un usuario.
     *
     * @param id     id del usuario
     * @param actual usuario que hace el cambio
     * @param ra     mensajes para la siguiente vista
     * @return redirección a la lista
     */
    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @AuthenticationPrincipal UserDetails actual, RedirectAttributes ra) {
        try {
            service.cambiarEstado(id, actual.getUsername());
            ra.addFlashAttribute("ok", "Estado del usuario actualizado");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
