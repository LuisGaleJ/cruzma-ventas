package pe.cruzma.ventas.model;

import jakarta.persistence.*;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Adelanto o pago de una venta (RF08).
 * <p>
 * Tabla {@code pago}. Se registra con {@link Venta#registrarPago(Pago)}, que
 * verifica que no supere el saldo.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Table(name = "pago")
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_venta")
    private Venta venta;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MedioPago medio;

    /**
     * Constructor requerido por JPA.
     */
    protected Pago() { }

    /**
     * Crea un pago.
     *
     * @param monto monto en soles, mayor a cero
     * @param medio medio de pago
     * @throws IllegalArgumentException si el monto no es positivo o falta el medio
     */
    public Pago(BigDecimal monto, MedioPago medio) {
        checkArgument(monto != null && monto.signum() > 0, "El monto debe ser mayor a cero");
        checkArgument(medio != null, "Elige el medio de pago");
        this.monto = monto;
        this.medio = medio;
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
    /** {@return fecha de registro} */
    public LocalDateTime getFecha() { return fecha; }
    /** {@return monto pagado} */
    public BigDecimal getMonto() { return monto; }
    /** {@return medio de pago} */
    public MedioPago getMedio() { return medio; }
}
