package pe.cruzma.ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** P1 Inicio de sesión (RF01). El POST /login lo procesa Spring Security. */
@Controller
public class LoginController {

    /** Constructor usado por Spring. */
    public LoginController() { }

    /**
     * Muestra el formulario de inicio de sesión.
     *
     * @return vista {@code login}
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
