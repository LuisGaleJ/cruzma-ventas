package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.repository.ProductoRepository;

import java.util.List;

/**
 * Gestión del catálogo de productos y servicios (RF04).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class ProductoService {
    private final ProductoRepository repo;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param repo repositorio de productos
     */
    public ProductoService(ProductoRepository repo) { this.repo = repo; }

    /**
     * Lista el catálogo, opcionalmente filtrado por tipo.
     *
     * @param tipo tipo a filtrar; {@code null} lista todo
     * @return ítems del catálogo
     */
    public List<Producto> listar(TipoProducto tipo) {
        return tipo == null ? repo.findAllByOrderByDescripcionAsc() : repo.findByTipoOrderByDescripcionAsc(tipo);
    }

    /**
     * Obtiene un ítem del catálogo.
     *
     * @param id id del producto
     * @return el producto
     * @throws IllegalArgumentException si no existe
     */
    public Producto obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    /**
     * Registra o actualiza un ítem del catálogo.
     *
     * @param p producto del formulario
     * @return el producto guardado
     */
    @Transactional
    public Producto guardar(Producto p) { return repo.save(p); }
}
