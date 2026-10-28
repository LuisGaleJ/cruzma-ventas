package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cruzma.ventas.model.Cotizacion;
import pe.cruzma.ventas.model.EstadoCotizacion;

import java.util.List;

/**
 * Acceso a la tabla {@code cotizacion}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    /**
     * Lista las cotizaciones, de la más reciente a la más antigua.
     *
     * @return cotizaciones
     */
    List<Cotizacion> findAllByOrderByIdDesc();
    /**
     * Cuenta las cotizaciones en un estado.
     *
     * @param estado estado a contar
     * @return cantidad de cotizaciones
     */
    long countByEstado(EstadoCotizacion estado);
}
