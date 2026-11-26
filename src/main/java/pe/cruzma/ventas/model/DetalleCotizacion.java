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

    /** Descuento del ítem en porcentaje (0 a 100). */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Constructor requerido por JPA.
     */
    protected DetalleCotizacion() { }

    /**
     * Crea un ítem y calcula su subtotal: precio x cantidad x metrado.
     * <p>
     * Si el producto se cobra por medida (M, ML, M2, M3), el metrado sale de las medidas;
     * por ejemplo, 1 baranda de 3.20 x 0.90 m2 a S/ 280 = 280 x 1 x 2.88 = S/ 806.40.
     *
     * @param producto producto o servicio cotizado
     * @param cantidad unidades, mayor a cero
     * @param medidas medidas del trabajo; obligatorias si la unidad es M, ML, M2 o M3
     * @param precio  precio unitario con IGV (por metro o metro cuadrado si corresponde), no negativo
     * @throws IllegalArgumentException si la cantidad, el precio o las medidas no son válidos
     */
    public DetalleCotizacion(Producto producto, int cantidad, String medidas, BigDecimal precio) {
        this(producto, cantidad, medidas, precio, BigDecimal.ZERO);
    }

    /**
     * Crea un ítem con descuento: subtotal = precio x cantidad x metrado x (1 - descuento / 100).
     *
     * @param producto  producto o servicio cotizado
     * @param cantidad  unidades, mayor a cero
     * @param medidas   medidas del trabajo; obligatorias si la unidad es M, ML, M2 o M3
     * @param precio    precio unitario con IGV, no negativo
     * @param descuento porcentaje de descuento del ítem (0 a 100); {@code null} es 0
     * @throws IllegalArgumentException si algún dato no es válido
     * @since 0.6.0
     */
    public DetalleCotizacion(Producto producto, int cantidad, String medidas, BigDecimal precio, BigDecimal descuento) {
        checkArgument(cantidad > 0, "La cantidad debe ser mayor a cero");
        checkArgument(precio != null && precio.signum() >= 0, "El precio no puede ser negativo");
        this.producto = producto;
        this.cantidad = cantidad;
        this.medidas = medidas;
        this.precio = precio;
        BigDecimal metrado;
        try {
            metrado = Metrado.calcular(producto.getUnidad(), medidas);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(producto.getDescripcion() + ": " + e.getMessage());
        }
        this.descuento = Descuento.porcentaje(descuento);
        BigDecimal bruto = precio.multiply(BigDecimal.valueOf(cantidad)).multiply(metrado)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        this.subtotal = Descuento.aplicar(bruto, this.descuento);
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
    /** {@return {@code true} si el producto se cobra por medida (M, ML, M2, M3)} */
    public boolean isPorMedida() { return Metrado.seCobraPorMedida(producto.getUnidad()); }
    /**
     * Metrado para mostrar en la cotización. No lanza error: los ítems antiguos pueden tener
     * medidas escritas antes de que existiera el cálculo.
     *
     * @return metros o metros cuadrados por unidad; 1 si no se cobra por medida; {@code null} si no se puede calcular
     */
    public BigDecimal getMetrado() {
        try {
            return Metrado.calcular(producto.getUnidad(), medidas);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    /** {@return descuento del ítem en porcentaje} */
    public BigDecimal getDescuento() { return descuento; }
    /**
     * Importe del ítem antes de su descuento (precio x cantidad x metrado), obtenido de lo que quedó guardado.
     * <p>
     * No se recalcula con la unidad actual del producto, porque la unidad puede haber cambiado después de cotizar
     * (por ejemplo, un servicio que se cobraba por metro cuadrado y ahora usa ZZ): eso daría un monto original
     * menor que lo cobrado. Sin descuento, el monto original es el subtotal guardado. Con descuento, se busca el
     * metrado con el que se cotizó (el de la unidad actual, 1, o el de las medidas como metro, metro cuadrado o
     * metro cúbico) cuyo importe con descuento coincide exactamente con el subtotal guardado; si ninguno coincide,
     * se reconstruye a partir del subtotal y el porcentaje.
     *
     * @return monto original del ítem en soles
     * @since 0.7.0
     */
    public BigDecimal getBruto() {
        if (descuento == null || descuento.signum() == 0) return subtotal;
        BigDecimal base = precio.multiply(BigDecimal.valueOf(cantidad));
        for (BigDecimal m : metradosPosibles()) {
            BigDecimal bruto = base.multiply(m).setScale(2, java.math.RoundingMode.HALF_UP);
            if (Descuento.aplicar(bruto, descuento).compareTo(subtotal) == 0) return bruto;
        }
        BigDecimal cien = BigDecimal.valueOf(100);
        if (descuento.compareTo(cien) >= 0) return subtotal;
        return subtotal.multiply(cien).divide(cien.subtract(descuento), 2, java.math.RoundingMode.HALF_UP);
    }

    /** Metrados con los que se pudo cotizar el ítem: el de la unidad actual, 1 y el de las medidas en m, m2 y m3. */
    private java.util.List<BigDecimal> metradosPosibles() {
        java.util.List<BigDecimal> posibles = new java.util.ArrayList<>();
        BigDecimal actual = getMetrado();
        if (actual != null) posibles.add(actual);
        posibles.add(BigDecimal.ONE);
        for (String unidad : java.util.List.of(UnidadMedida.METRO, UnidadMedida.METRO_CUADRADO, UnidadMedida.METRO_CUBICO)) {
            try {
                posibles.add(Metrado.calcular(unidad, medidas));
            } catch (IllegalArgumentException e) {
                // las medidas no sirven para esa unidad: se descarta
            }
        }
        return posibles;
    }
    /** {@return subtotal con el descuento del ítem (precio por cantidad por metrado, menos descuento)} */
    public BigDecimal getSubtotal() { return subtotal; }
}
