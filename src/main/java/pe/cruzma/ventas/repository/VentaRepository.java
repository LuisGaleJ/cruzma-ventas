package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cruzma.ventas.model.EstadoVenta;
import pe.cruzma.ventas.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a la tabla {@code venta}, con las consultas de los reportes.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface VentaRepository extends JpaRepository<Venta, Long> {
    /**
     * Lista las ventas, de la más reciente a la más antigua.
     *
     * @return ventas
     */
    List<Venta> findAllByOrderByFechaDesc();

    /**
     * Lista las ventas de un período excluyendo un estado.
     *
     * @param desde    inicio del período
     * @param hasta    fin del período
     * @param excluido estado que no se incluye, normalmente {@code ANULADA}
     * @return ventas del período en orden cronológico
     */
    List<Venta> findByFechaBetweenAndEstadoNotOrderByFechaAsc(LocalDateTime desde, LocalDateTime hasta, EstadoVenta excluido);

    /**
     * Indica si una cotización ya se convirtió en venta.
     *
     * @param idCotizacion id de la cotización
     * @return {@code true} si ya tiene venta
     */
    boolean existsByCotizacionId(Long idCotizacion);

    /**
     * Suma el total vendido en un período, sin contar ventas anuladas.
     *
     * @param desde inicio del período
     * @param hasta fin del período
     * @return total en soles; cero si no hay ventas
     */
    @Query("select coalesce(sum(v.total), 0) from Venta v where v.fecha between :desde and :hasta and v.estado <> pe.cruzma.ventas.model.EstadoVenta.ANULADA")
    BigDecimal totalVendido(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /**
     * Ventas con saldo pendiente, de la más antigua a la más reciente (RF13).
     *
     * @return ventas por cobrar
     */
    @Query("select v from Venta v where v.saldo > 0 and v.estado <> pe.cruzma.ventas.model.EstadoVenta.ANULADA order by v.fecha asc")
    List<Venta> conSaldoPendiente();

    /**
     * Suma los saldos pendientes de todas las ventas no anuladas.
     *
     * @return saldo total por cobrar
     */
    @Query("select coalesce(sum(v.saldo), 0) from Venta v where v.estado <> pe.cruzma.ventas.model.EstadoVenta.ANULADA")
    BigDecimal saldoPorCobrar();
}
