package pe.cruzma.ventas.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.cruzma.ventas.model.Usuario;
import pe.cruzma.ventas.repository.UsuarioRepository;

/**
 * Carga el usuario para Spring Security; el rol se convierte en ROLE_ADMIN, ROLE_VENDEDOR o ROLE_CAJA.
 * <p>
 * Un usuario desactivado o bloqueado por intentos fallidos ({@link IntentosLoginService}) no puede ingresar.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repo;
    private final IntentosLoginService intentos;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param repo     repositorio de usuarios
     * @param intentos control de intentos fallidos
     */
    public UsuarioDetailsService(UsuarioRepository repo, IntentosLoginService intentos) {
        this.repo = repo;
        this.intentos = intentos;
    }

    /**
     * Carga el usuario para el inicio de sesión (RF01).
     *
     * @param username nombre de usuario ingresado en el login
     * @return datos de autenticación con su rol
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario o contraseña incorrectos"));
        return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRol().getNombre())
                .disabled(!u.isActivo())
                .accountLocked(intentos.estaBloqueado(username))
                .build();
    }
}
