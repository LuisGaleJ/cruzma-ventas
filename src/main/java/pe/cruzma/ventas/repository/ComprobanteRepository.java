package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cruzma.ventas.model.Comprobante;

import java.util.List;

/**
 * Acceso a la tabla {@code comprobante}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    /**
     * Devuelve el último correlativo usado en una serie.
     *
     * @param serie serie del comprobante, por ejemplo {@code F001}
     * @return el mayor correlativo, o 0 si la serie no se ha usado
     */
    @Query("select coalesce(max(c.correlativo), 0) from Comprobante c where c.serie = :serie")
    int ultimoCorrelativo(@Param("serie") String serie);

    /**
     * Lista los comprobantes, del más reciente al más antiguo.
     *
     * @return comprobantes
     */
    List<Comprobante> findAllByOrderByFechaEmisionDesc();
}
