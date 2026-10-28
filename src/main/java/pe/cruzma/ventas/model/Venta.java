package pe.cruzma.ventas.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Venta registrada a partir de una cotización aceptada (RF07, RF08).
 * <p>
 * Tabla {@code venta}. Controla los pagos y el saldo: cuando el saldo llega a
 * cero pasa a {@link EstadoVenta#PAGADA}. Sus detalles se copian de la
 * cotización para no volver a digitarlos.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    /** Cotización de origen; es única porque una cotización genera como máximo una venta. */
    @OneToOne
    @JoinColumn(name = "id_cotizacion", unique = true)
    private Cotizacion cotizacion;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    @OrderBy("fecha ASC")
    private List<Pago> pagos = new ArrayList<>();

    @OneToMany(mappedBy = "venta")
    @OrderBy("fechaEmision ASC")
    private List<Comprobante> comprobantes = new ArrayList<>();

    /** Constructor requerido por JPA; las ventas se crean con {@link #desdeCotizacion(Cotizacion, Usuario)}. */
    protected Venta() { }

    /**
     * Crea una venta copiando el cliente y los ítems de una cotización aceptada (RF07).
     *
     * @param cot     cotización aceptada por el cliente
     * @param usuario usuario que registra la venta
     * @return la venta nueva, con saldo igual al total
     */
    public static Venta desdeCotizacion(Cotizacion cot, Usuario usuario) {
        Venta v = new Venta();
        v.cliente = cot.getCliente();
        v.usuario = usuario;
        v.cotizacion = cot;
        for (DetalleCotizacion d : cot.getDetalles()) {
            v.agregarDetalle(new DetalleVenta(d.getProducto(), d.getCantidad(), d.getPrecio()));
        }
        v.total = cot.getTotal();
        v.saldo = v.total;
        return v;
    }

    /**
     * Agrega un ítem y enlaza la relación en ambos sentidos.
     *
     * @param detalle ítem a agregar
     */
    public void agregarDetalle(DetalleVenta detalle) {
        detalle.setVenta(this);
        detalles.add(detalle);
    }

    /**
     * Registra un adelanto o pago y actualiza saldo y estado (RF08).
     *
     * @param pago pago a registrar
     * @throws IllegalStateException    si la venta está anulada
     * @throws IllegalArgumentException si el pago supera el saldo
     */
    public void registrarPago(Pago pago) {
        if (estado == EstadoVenta.ANULADA) throw new IllegalStateException("La venta está anulada");
        if (pago.getMonto().compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente de S/ " + saldo);
        }
        pago.setVenta(this);
        pagos.add(pago);
        calcularSaldo();
    }

    /**
     * Recalcula el saldo como total menos la suma de pagos y actualiza el estado.
     *
     * @return el saldo pendiente
     */
    public BigDecimal calcularSaldo() {
        BigDecimal pagado = pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        saldo = total.subtract(pagado);
        if (saldo.signum() == 0 && estado != EstadoVenta.ANULADA) estado = EstadoVenta.PAGADA;
        return saldo;
    }

    /**
     * Monto ya cobrado de la venta.
     *
     * @return total menos saldo
     */
    public BigDecimal getPagado() { return total.subtract(saldo); }

    /**
     * Días transcurridos desde la venta, para el reporte de cuentas por cobrar.
     *
     * @return días desde la fecha de la venta hasta hoy
     */
    public long getDiasDesdeVenta() {
        return java.time.temporal.ChronoUnit.DAYS.between(fecha.toLocalDate(), java.time.LocalDate.now());
    }

    /**
     * Indica si la venta ya tiene una factura o boleta aceptada por SUNAT.
     *
     * @return {@code true} si no se debe emitir otro comprobante
     */
    public boolean tieneComprobanteVigente() {
        return comprobantes.stream().anyMatch(c -> c.getTipo() != TipoComprobante.NOTA_CREDITO
                && c.getEstadoSunat() == EstadoSunat.ACEPTADO);
    }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return fecha de registro} */
    public LocalDateTime getFecha() { return fecha; }
    /** {@return importe total con IGV} */
    public BigDecimal getTotal() { return total; }
    /** {@return saldo pendiente de cobro} */
    public BigDecimal getSaldo() { return saldo; }
    /** {@return estado actual} */
    public EstadoVenta getEstado() { return estado; }
    /**
     * Asigna estado actual.
     *
     * @param estado nuevo valor
     */
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
    /** {@return cliente} */
    public Cliente getCliente() { return cliente; }
    /** {@return usuario que la registró} */
    public Usuario getUsuario() { return usuario; }
    /** {@return cotización a la que pertenece} */
    public Cotizacion getCotizacion() { return cotizacion; }
    /** {@return ítems} */
    public List<DetalleVenta> getDetalles() { return detalles; }
    /** {@return pagos registrados} */
    public List<Pago> getPagos() { return pagos; }
    /** {@return comprobantes emitidos} */
    public List<Comprobante> getComprobantes() { return comprobantes; }
}
