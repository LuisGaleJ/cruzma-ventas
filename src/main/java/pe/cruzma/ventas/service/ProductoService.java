package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.model.UnidadMedida;
import pe.cruzma.ventas.repository.ProductoRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Gestión del catálogo de productos y servicios (RF04).
 * <p>
 * El código es automático: {@code PRD-001}, {@code PRD-002}... para productos y
 * {@code SRV-001}, {@code SRV-002}... para servicios. Se asigna al registrar y solo cambia si se
 * cambia el tipo del ítem.
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
     * Calcula el siguiente código libre para un tipo: el mayor correlativo existente más uno.
     *
     * @param tipo producto o servicio
     * @return por ejemplo {@code PRD-006} o {@code SRV-003}
     * @since 0.4.1
     */
    public String siguienteCodigo(TipoProducto tipo) {
        String prefijo = prefijo(tipo);
        int mayor = repo.findByCodigoStartingWith(prefijo).stream()
                .map(p -> p.getCodigo().substring(prefijo.length()))
                .filter(n -> n.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        return prefijo + String.format("%03d", mayor + 1);
    }

    /**
     * Códigos que tendría un ítem nuevo de cada tipo, para mostrarlos en el formulario.
     *
     * @return código siguiente por tipo
     * @since 0.4.1
     */
    public Map<TipoProducto, String> siguientesCodigos() {
        Map<TipoProducto, String> codigos = new EnumMap<>(TipoProducto.class);
        for (TipoProducto t : TipoProducto.values()) codigos.put(t, siguienteCodigo(t));
        return codigos;
    }

    /**
     * Registra o actualiza un ítem del catálogo.
     * <p>
     * Un ítem nuevo recibe el siguiente código de su tipo, aunque el formulario envíe otro.
     * Al editar se conserva el código que ya tenía, salvo que cambie el tipo: entonces recibe
     * el siguiente código del nuevo tipo (el historial de auditoría guarda el anterior).
     *
     * @param p producto del formulario
     * @return el producto guardado
     */
    @Transactional
    public Producto guardar(Producto p) {
        String errorUnidad = UnidadMedida.validar(p.getTipo(), p.getUnidad());
        if (errorUnidad != null) throw new IllegalArgumentException(errorUnidad);
        Producto actual = p.getId() == null ? null : obtener(p.getId());
        boolean conservar = actual != null && actual.getTipo() == p.getTipo();
        p.setCodigo(conservar ? actual.getCodigo() : siguienteCodigo(p.getTipo()));
        return repo.save(p);
    }

    private static String prefijo(TipoProducto tipo) {
        return tipo == TipoProducto.SERVICIO ? "SRV-" : "PRD-";
    }
}
