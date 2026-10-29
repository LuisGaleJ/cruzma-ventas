package pe.cruzma.ventas.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** La revisión guarda el usuario de la sesión, o "sistema" si no hay sesión. */
class RevisionUsuarioListenerTest {

    @AfterEach
    void limpiar() { SecurityContextHolder.clearContext(); }

    @Test
    void usaElUsuarioAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("caja", null, List.of()));
        Revision r = new Revision();
        new RevisionUsuarioListener().newRevision(r);
        assertEquals("caja", r.getUsuario());
    }

    @Test
    void sinSesionRegistraSistema() {
        SecurityContextHolder.clearContext();
        Revision r = new Revision();
        new RevisionUsuarioListener().newRevision(r);
        assertEquals("sistema", r.getUsuario());
    }
}
