package pe.cruzma.ventas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.cruzma.ventas.model.Rol;
import pe.cruzma.ventas.model.Usuario;
import pe.cruzma.ventas.repository.RolRepository;
import pe.cruzma.ventas.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Edición de usuarios desde la ventana emergente (RF02). */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock RolRepository roles;
    @Mock PasswordEncoder encoder;
    UsuarioService service;

    private final Rol vendedor = new Rol(Rol.VENDEDOR);
    private final Rol caja = new Rol(Rol.CAJA);
    private Usuario jperez;

    @BeforeEach
    void preparar() {
        service = new UsuarioService(usuarios, roles, encoder);
        jperez = Fixtures.usuario("jperez");
        jperez.setRol(vendedor);
        jperez.setPasswordHash("hash-anterior");
    }

    @Test
    void cambiaNombresYRolSinTocarLaContrasenaSiVieneVacia() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(jperez));
        when(roles.findById(3L)).thenReturn(Optional.of(caja));
        when(usuarios.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario u = service.actualizar(5L, "  Juan Pérez  ", 3L, "", "admin");

        assertEquals("Juan Pérez", u.getNombres());
        assertEquals(Rol.CAJA, u.getRol().getNombre());
        assertEquals("hash-anterior", u.getPasswordHash());
        verify(encoder, never()).encode(any());
    }

    @Test
    void cifraLaContrasenaNuevaYRechazaUnaCorta() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(jperez));
        when(roles.findById(2L)).thenReturn(Optional.of(vendedor));
        when(encoder.encode("Nueva2026")).thenReturn("hash-nuevo");
        when(usuarios.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("hash-nuevo", service.actualizar(5L, "Juan", 2L, "Nueva2026", "admin").getPasswordHash());
        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, "Juan", 2L, "corta", "admin"));
    }

    @Test
    void nadiePuedeCambiarSuPropioRol() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(jperez));
        when(roles.findById(3L)).thenReturn(Optional.of(caja));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5L, "Juan", 3L, "", "jperez"));
        verify(usuarios, never()).save(any());
    }
}
