package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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

    /** Descuento global en porcentaje, aplicado sobre la suma de los ítems (0 a 100). */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

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
     * Calcula el total: suma de los ítems (ya con su descuento) menos el descuento global.
     *
     * @return el total calculado
     */
    public BigDecimal calcularTotal() {
        total = Descuento.aplicar(getSubtotalItems(), descuento);
        return total;
    }

    /** {@return suma de los ítems con su descuento, antes del descuento global} */
    public BigDecimal getSubtotalItems() {
        return detalles.stream().map(DetalleCotizacion::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** {@return suma de los ítems sin ningún descuento} */
    public BigDecimal getBruto() {
        return detalles.stream().map(DetalleCotizacion::getBruto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** {@return monto del descuento global en soles} */
    public BigDecimal getMontoDescuentoGlobal() { return getSubtotalItems().subtract(total); }

    /** {@return {@code true} si algún ítem tiene descuento} */
    public boolean isTieneDescuentoPorItem() {
        return detalles.stream().anyMatch(d -> d.getDescuento().signum() > 0);
    }

    /** {@return descuento total efectivo en porcentaje (ítems y global juntos)} */
    public BigDecimal getDescuentoEfectivo() { return Descuento.efectivo(getBruto(), total); }

    /**
     * Indica si la cotización sigue pendiente después de su fecha de vencimiento.
     *
     * @return {@code true} si ya no se puede convertir en venta
     */
    public boolean estaVencida() {
        return estado == EstadoCotizacion.PENDIENTE && LocalDate.now().isAfter(fechaVencimiento);
    }

    /** {@return días que faltan para la fecha de vencimiento (0 = vence hoy; negativo = ya venció)} */
    public long getDiasParaVencer() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /**
     * Indica si la cotización está pendiente y vence dentro de los próximos días.
     *
     * @param diasAviso con cuántos días de anticipación se avisa
     * @return {@code true} si sigue pendiente y vence entre hoy y {@code diasAviso} días
     * @since 0.7.0
     */
    public boolean porVencer(int diasAviso) {
        long dias = getDiasParaVencer();
        return estado == EstadoCotizacion.PENDIENTE && dias >= 0 && dias <= diasAviso;
    }

    /** {@return texto del vencimiento: «vence hoy», «vence mañana», «vence en 3 días» o «venció hace 2 días»} */
    public String getTextoVencimiento() {
        long dias = getDiasParaVencer();
        if (dias == 0) return "vence hoy";
        if (dias == 1) return "vence mañana";
        if (dias > 1) return "vence en " + dias + " días";
        return dias == -1 ? "venció ayer" : "venció hace " + (-dias) + " días";
    }

    /**
     * Enlace de WhatsApp para recordarle al cliente que su cotización está por vencer.
     *
     * @return enlace {@code wa.me} con el mensaje listo, o {@code null} si el cliente no tiene celular registrado
     * @since 0.7.0
     */
    public String getEnlaceWhatsApp() {
        if (cliente == null) return null;
        String fecha = fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String mensaje = "Hola " + cliente.getRazonSocial() + ", le saluda Grupo Cruzma. Le recordamos que su cotización "
                + numero + " por S/ " + String.format(java.util.Locale.US, "%,.2f", total)
                + (getDiasParaVencer() < 0 ? " venció el " : " vence el ") + fecha
                + ". ¿Desea confirmarla o que le enviemos una actualizada? Quedamos atentos.";
        return pe.cruzma.ventas.util.WhatsApp.enlace(cliente.getTelefono(), mensaje);
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
    /** {@return descuento global en porcentaje} */
    public BigDecimal getDescuento() { return descuento; }
    /**
     * Asigna el descuento global.
     *
     * @param descuento porcentaje (0 a 100); {@code null} es 0
     * @since 0.6.0
     */
    public void setDescuento(BigDecimal descuento) { this.descuento = Descuento.porcentaje(descuento); }
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
