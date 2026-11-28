package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cruzma.ventas.model.Cotizacion;
import pe.cruzma.ventas.model.EstadoCotizacion;

import java.time.LocalDate;
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

    /**
     * Cotizaciones en un estado que vencen dentro de un rango de fechas.
     *
     * @param estado estado (normalmente PENDIENTE)
     * @param desde  primer día, inclusive
     * @param hasta  último día, inclusive
     * @return cotizaciones, de la que vence primero a la última
     */
    List<Cotizacion> findByEstadoAndFechaVencimientoBetweenOrderByFechaVencimientoAsc(EstadoCotizacion estado,
                                                                                     LocalDate desde, LocalDate hasta);

    /**
     * Cuenta las cotizaciones en un estado que vencen dentro de un rango de fechas.
     *
     * @param estado estado (normalmente PENDIENTE)
     * @param desde  primer día, inclusive
     * @param hasta  último día, inclusive
     * @return cantidad
     */
    long countByEstadoAndFechaVencimientoBetween(EstadoCotizacion estado, LocalDate desde, LocalDate hasta);

    /**
     * Cotizaciones en un estado cuya fecha de vencimiento ya pasó.
     *
     * @param estado estado (normalmente PENDIENTE)
     * @param fecha  fecha de referencia (hoy)
     * @return cotizaciones vencidas
     */
    List<Cotizacion> findByEstadoAndFechaVencimientoBefore(EstadoCotizacion estado, LocalDate fecha);
}
