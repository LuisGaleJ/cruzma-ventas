package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cotización enviada a un cliente (RF05).
 * <p>
 * Tabla {@code cotizacion}. Es dueña de sus ítems ({@link DetalleCotizacion}):
 * si se elimina la cotización, se eliminan sus detalles (composición). Una
 * cotización aceptada se convierte en una {@link Venta} con
 * {@link Venta#desdeCotizacion(Cotizacion, Usuario)}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Entity
@Audited
@Table(name = "cotizacion")
public class Cotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String numero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoCotizacion estado = EstadoCotizacion.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @NotAudited
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCotizacion> detalles = new ArrayList<>();

    /**
     * Agrega un ítem y enlaza la relación en ambos sentidos.
     *
     * @param detalle ítem a agregar
     */
    public void agregarDetalle(DetalleCotizacion detalle) {
        detalle.setCotizacion(this);
        detalles.add(detalle);
    }

    /**
     * Recalcula el total como la suma de los subtotales de sus ítems.
     *
     * @return el total actualizado, con IGV incluido
     */
    public BigDecimal calcularTotal() {
        total = detalles.stream().map(DetalleCotizacion::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    /**
     * Indica si la cotización sigue pendiente después de su fecha de vencimiento.
     *
     * @return {@code true} si ya no se puede convertir en venta
     */
    public boolean estaVencida() {
        return estado == EstadoCotizacion.PENDIENTE && LocalDate.now().isAfter(fechaVencimiento);
    }

    /** Crea una cotización vacía en estado {@code PENDIENTE}. */
    public Cotizacion() { }

    /** {@return identificador} */
    public Long getId() { return id; }
    /** {@return número correlativo de la cotización} */
    public String getNumero() { return numero; }
    /**
     * Asigna número correlativo de la cotización.
     *
     * @param numero nuevo valor
     */
    public void setNumero(String numero) { this.numero = numero; }
    /** {@return fecha de registro} */
    public LocalDate getFecha() { return fecha; }
    /**
     * Asigna fecha de registro.
     *
     * @param fecha nuevo valor
     */
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    /** {@return fecha hasta la que es válida} */
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    /**
     * Asigna fecha hasta la que es válida.
     *
     * @param fechaVencimiento nuevo valor
     */
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    /** {@return estado actual} */
    public EstadoCotizacion getEstado() { return estado; }
    /**
     * Asigna estado actual.
     *
     * @param estado nuevo valor
     */
    public void setEstado(EstadoCotizacion estado) { this.estado = estado; }
    /** {@return importe total con IGV} */
    public BigDecimal getTotal() { return total; }
    /** {@return cliente} */
    public Cliente getCliente() { return cliente; }
    /**
     * Asigna cliente.
     *
     * @param cliente nuevo valor
     */
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    /** {@return usuario que la registró} */
    public Usuario getUsuario() { return usuario; }
    /**
     * Asigna usuario que la registró.
     *
     * @param usuario nuevo valor
     */
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    /** {@return ítems} */
    public List<DetalleCotizacion> getDetalles() { return detalles; }
}
