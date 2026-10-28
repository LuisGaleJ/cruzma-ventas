package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cruzma.ventas.model.Rol;

import java.util.Optional;

/**
 * Acceso a la tabla {@code rol}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface RolRepository extends JpaRepository<Rol, Long> {
    /**
     * Busca un rol por su nombre.
     *
     * @param nombre nombre del rol, por ejemplo {@code ADMIN}
     * @return el rol, si existe
     */
    Optional<Rol> findByNombre(String nombre);
}
