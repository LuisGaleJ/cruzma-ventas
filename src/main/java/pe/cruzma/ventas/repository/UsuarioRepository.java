package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cruzma.ventas.model.Usuario;

import java.util.Optional;

/**
 * Acceso a la tabla {@code usuario}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return el usuario, si existe
     */
    Optional<Usuario> findByUsername(String username);
    /**
     * Indica si ya existe el nombre de usuario.
     *
     * @param username nombre de usuario
     * @return {@code true} si está ocupado
     */
    boolean existsByUsername(String username);
}
