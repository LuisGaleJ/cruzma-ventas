package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Comprobante electrónico: factura (01), boleta (03) o nota de crédito (07) (RF09).
 * <p>
 * Tabla {@code comprobante}. La pareja serie y correlativo es única, como exige
 * SUNAT. Un comprobante aceptado nunca se borra: se anula con
 * {@link #anular()} y una nota de crédito.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Audited
@Table(name = "comprobante", uniqueConstraints = @UniqueConstraint(columnNames = {"serie", "correlativo"}))
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @Convert(converter = TipoComprobanteConverter.class)
    @Column(nullable = false, length = 2)
    private TipoComprobante tipo;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private int correlativo;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sunat", nullable = false, length = 10)
    private EstadoSunat estadoSunat = EstadoSunat.PENDIENTE;

    @Column(name = "hash_cdr", length = 100)
    private String hashCdr;

    @Column(name = "ruta_pdf")
    private String rutaPdf;

    /**
     * Constructor requerido por JPA.
     */
    protected Comprobante() { }

    /**
     * Crea el comprobante separando el IGV de un total que ya lo incluye.
     * <p>
     * Op. gravada = total / (1 + tasa); IGV = total - op. gravada.
     *
     * @param venta       venta que se factura
     * @param tipo        factura, boleta o nota de crédito; define la serie
     * @param correlativo número siguiente de la serie
     * @param total       importe total con IGV
     * @param tasaIgv     tasa del IGV, por ejemplo 0.18
     */
    public Comprobante(Venta venta, TipoComprobante tipo, int correlativo, BigDecimal total, BigDecimal tasaIgv) {
        this.venta = venta;
        this.tipo = tipo;
        this.serie = tipo.getSerie();
        this.correlativo = correlativo;
        this.total = total.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = this.total.divide(BigDecimal.ONE.add(tasaIgv), 2, RoundingMode.HALF_UP);
        this.igv = this.total.subtract(this.subtotal);
    }

    /**
     * Número completo del comprobante.
     *
     * @return serie y correlativo de 8 dígitos, por ejemplo {@code F001-00000012}
     */
    public String getNumero() {
        return serie + "-" + String.format("%08d", correlativo);
    }

    /**
     * Guarda la respuesta del OSE.
     *
     * @param aceptado {@code true} si SUNAT aceptó el comprobante
     * @param hash     identificador de la constancia de recepción (CDR); puede ser {@code null}
     */
    public void marcarRespuesta(boolean aceptado, String hash) {
        this.estadoSunat = aceptado ? EstadoSunat.ACEPTADO : EstadoSunat.RECHAZADO;
        this.hashCdr = hash;
        this.rutaPdf = aceptado ? "/comprobantes/" + id : null;
    }

    /**
     * Marca el comprobante como anulado. Lo invoca el servicio al emitir la nota de crédito.
     */
    public void anular() { this.estadoSunat = EstadoSunat.ANULADO; }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return venta a la que pertenece} */
    public Venta getVenta() { return venta; }
    /** {@return tipo} */
    public TipoComprobante getTipo() { return tipo; }
    /** {@return serie (F001, B001 o FC01)} */
    public String getSerie() { return serie; }
    /** {@return número correlativo dentro de la serie} */
    public int getCorrelativo() { return correlativo; }
    /** {@return fecha y hora de emisión} */
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    /** {@return subtotal (cantidad por precio)} */
    public BigDecimal getSubtotal() { return subtotal; }
    /** {@return IGV (18 %)} */
    public BigDecimal getIgv() { return igv; }
    /** {@return importe total con IGV} */
    public BigDecimal getTotal() { return total; }
    /** {@return estado ante SUNAT} */
    public EstadoSunat getEstadoSunat() { return estadoSunat; }
    /** {@return identificador de la constancia de recepción (CDR)} */
    public String getHashCdr() { return hashCdr; }
    /** {@return ruta de la representación impresa} */
    public String getRutaPdf() { return rutaPdf; }
}
