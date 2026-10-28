package pe.cruzma.ventas.model;

import jakarta.persistence.*;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;

/**
 * Ítem de una cotización: producto, cantidad, medidas y precio pactado.
 * <p>
 * Tabla {@code detalle_cotizacion}. El precio puede ser menor al del catálogo
 * para reflejar un descuento.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "detalle_cotizacion")
public class DetalleCotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cotizacion")
    private Cotizacion cotizacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    /** Medidas del trabajo a medida, por ejemplo "3.20 m x 0.90 m". */
    @Column(length = 100)
    private String medidas;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Constructor requerido por JPA.
     */
    protected DetalleCotizacion() { }

    /**
     * Crea un ítem y calcula su subtotal.
     *
     * @param producto producto o servicio cotizado
     * @param cantidad unidades, mayor a cero
     * @param medidas medidas del trabajo a medida; puede ser {@code null}
     * @param precio  precio unitario con IGV, no negativo
     * @throws IllegalArgumentException si la cantidad o el precio no son válidos
     */
    public DetalleCotizacion(Producto producto, int cantidad, String medidas, BigDecimal precio) {
        checkArgument(cantidad > 0, "La cantidad debe ser mayor a cero");
        checkArgument(precio != null && precio.signum() >= 0, "El precio no puede ser negativo");
        this.producto = producto;
        this.cantidad = cantidad;
        this.medidas = medidas;
        this.precio = precio;
        this.subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
    }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return cotización a la que pertenece} */
    public Cotizacion getCotizacion() { return cotizacion; }
    /**
     * Asigna cotización a la que pertenece.
     *
     * @param cotizacion nuevo valor
     */
    void setCotizacion(Cotizacion cotizacion) { this.cotizacion = cotizacion; }
    /** {@return producto o servicio} */
    public Producto getProducto() { return producto; }
    /** {@return cantidad de unidades} */
    public int getCantidad() { return cantidad; }
    /** {@return medidas del trabajo} */
    public String getMedidas() { return medidas; }
    /** {@return precio unitario pactado con IGV} */
    public BigDecimal getPrecio() { return precio; }
    /** {@return subtotal (cantidad por precio)} */
    public BigDecimal getSubtotal() { return subtotal; }
}
