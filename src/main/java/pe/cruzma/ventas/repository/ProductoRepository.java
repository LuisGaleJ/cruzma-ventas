package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;

import java.util.List;

/**
 * Acceso a la tabla {@code producto}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    /**
     * Lista todo el catálogo en orden alfabético.
     *
     * @return productos y servicios
     */
    List<Producto> findAllByOrderByDescripcionAsc();
    /**
     * Lista el catálogo de un tipo en orden alfabético.
     *
     * @param tipo producto o servicio
     * @return ítems de ese tipo
     */
    List<Producto> findByTipoOrderByDescripcionAsc(TipoProducto tipo);
    /**
     * Lista los ítems cuyo código empieza con un prefijo, para calcular el siguiente correlativo.
     *
     * @param prefijo por ejemplo {@code "PRD-"} o {@code "SRV-"}
     * @return ítems con ese prefijo
     * @since 0.4.1
     */
    List<Producto> findByCodigoStartingWith(String prefijo);
}
