package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.dto.ItemCotizacionForm;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.ClienteRepository;
import pe.cruzma.ventas.repository.CotizacionRepository;
import pe.cruzma.ventas.repository.ProductoRepository;
import pe.cruzma.ventas.repository.VentaRepository;
import pe.cruzma.ventas.util.Auditoria;

import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio de las cotizaciones (RF05, RF07).
 * <p>
 * Crea cotizaciones, las marca como rechazadas y las convierte en venta.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class CotizacionService {
    /** Días de validez de una cotización, por la variación del precio del acero. */
    public static final int DIAS_VALIDEZ = 15;

    private final CotizacionRepository cotizaciones;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final VentaRepository ventas;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param cotizaciones repositorio de cotizaciones
     * @param clientes     repositorio de clientes
     * @param productos    repositorio de productos
     * @param ventas       repositorio de ventas
     */
    public CotizacionService(CotizacionRepository cotizaciones, ClienteRepository clientes,
                             ProductoRepository productos, VentaRepository ventas) {
        this.cotizaciones = cotizaciones;
        this.clientes = clientes;
        this.productos = productos;
        this.ventas = ventas;
    }

    /**
     * Lista las cotizaciones y marca como vencidas las que pasaron su fecha.
     *
     * @return cotizaciones, de la más reciente a la más antigua
     */
    @Transactional
    public List<Cotizacion> listar() {
        List<Cotizacion> lista = cotizaciones.findAllByOrderByIdDesc();
        lista.stream().filter(Cotizacion::estaVencida).forEach(c -> c.setEstado(EstadoCotizacion.VENCIDA));
        return lista;
    }

    /**
     * Obtiene una cotización.
     *
     * @param id id de la cotización
     * @return la cotización
     * @throws IllegalArgumentException si no existe
     */
    public Cotizacion obtener(Long id) {
        return cotizaciones.findById(id).orElseThrow(() -> new IllegalArgumentException("Cotización no encontrada"));
    }

    /**
     * Registra una cotización con sus ítems (RF05).
     * <p>
     * El precio de cada ítem puede ajustarse para aplicar un descuento; si viene
     * vacío se usa el del catálogo. La cotización vence a los {@link #DIAS_VALIDEZ} días.
     *
     * @param idCliente cliente que solicita la cotización
     * @param items     filas del formulario; las que no tienen producto se ignoran
     * @param vendedor  usuario que cotiza
     * @return la cotización guardada
     * @throws IllegalArgumentException si falta el cliente, no hay ítems o un dato no es válido
     */
    @Transactional
    public Cotizacion crear(Long idCliente, List<ItemCotizacionForm> items, Usuario vendedor) {
        if (idCliente == null) throw new IllegalArgumentException("Elige un cliente");
        List<ItemCotizacionForm> validos = items.stream().filter(i -> i.productoId() != null).toList();
        if (validos.isEmpty()) throw new IllegalArgumentException("Agrega al menos un producto o servicio");

        Cotizacion c = new Cotizacion();
        c.setCliente(clientes.findById(idCliente).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado")));
        c.setUsuario(vendedor);
        c.setFecha(LocalDate.now());
        c.setFechaVencimiento(LocalDate.now().plusDays(DIAS_VALIDEZ));
        for (ItemCotizacionForm i : validos) {
            Producto p = productos.findById(i.productoId()).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            int cantidad = i.cantidad() == null ? 1 : i.cantidad();
            c.agregarDetalle(new DetalleCotizacion(p, cantidad, i.medidas(), i.precio() != null ? i.precio() : p.getPrecioUnitario()));
        }
        c.calcularTotal();
        c.setNumero("COT-" + LocalDate.now().getYear() + "-" + String.format("%04d", cotizaciones.count() + 1));
        return cotizaciones.save(c);
    }

    /**
     * Marca como rechazada una cotización pendiente.
     *
     * @param id id de la cotización
     * @throws IllegalStateException si no está pendiente
     */
    @Transactional
    public void rechazar(Long id) {
        Cotizacion c = obtener(id);
        if (c.getEstado() != EstadoCotizacion.PENDIENTE) throw new IllegalStateException("Solo se rechazan cotizaciones pendientes");
        c.setEstado(EstadoCotizacion.RECHAZADA);
    }

    /**
     * Convierte la cotización aceptada por el cliente en una venta, sin volver a digitar datos (RF07).
     *
     * @param id      cotización aceptada
     * @param usuario usuario que registra la venta
     * @return la venta creada
     * @throws IllegalStateException si la cotización venció, fue rechazada o ya tiene venta
     */
    @Transactional
    public Venta convertirAVenta(Long id, Usuario usuario) {
        Cotizacion c = obtener(id);
        if (c.estaVencida()) {
            c.setEstado(EstadoCotizacion.VENCIDA);
            throw new IllegalStateException("La cotización venció el " + c.getFechaVencimiento() + "; genera una nueva con precios actuales");
        }
        if (c.getEstado() == EstadoCotizacion.RECHAZADA || c.getEstado() == EstadoCotizacion.VENCIDA) {
            throw new IllegalStateException("La cotización está " + c.getEstado().name().toLowerCase());
        }
        if (ventas.existsByCotizacionId(id)) throw new IllegalStateException("Esta cotización ya se convirtió en venta");
        c.setEstado(EstadoCotizacion.APROBADA);
        Venta venta = ventas.save(Venta.desdeCotizacion(c, usuario));
        Auditoria.registrar(usuario.getUsername(), "CONVERTIR_EN_VENTA", c.getNumero() + " total=" + c.getTotal());
        return venta;
    }
}
