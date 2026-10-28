package pe.cruzma.ventas.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Ítem de una venta, copiado del detalle de la cotización.
 * <p>
 * Tabla {@code detalle_venta}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Constructor requerido por JPA.
     */
    protected DetalleVenta() { }

    /**
     * Crea un ítem de venta y calcula su subtotal.
     *
     * @param producto producto o servicio vendido
     * @param cantidad unidades vendidas
     * @param precio   precio unitario con IGV
     */
    public DetalleVenta(Producto producto, int cantidad, BigDecimal precio) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
    }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return venta a la que pertenece} */
    public Venta getVenta() { return venta; }
    /**
     * Asigna venta a la que pertenece.
     *
     * @param venta nuevo valor
     */
    void setVenta(Venta venta) { this.venta = venta; }
    /** {@return producto o servicio} */
    public Producto getProducto() { return producto; }
    /** {@return cantidad de unidades} */
    public int getCantidad() { return cantidad; }
    /** {@return precio unitario pactado con IGV} */
    public BigDecimal getPrecio() { return precio; }
    /** {@return subtotal (cantidad por precio)} */
    public BigDecimal getSubtotal() { return subtotal; }
}
