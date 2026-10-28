package pe.cruzma.ventas.model;

/**
 * Estado de cobro de una venta.
 * <ul>
 * <li>{@code PENDIENTE}: tiene saldo por cobrar.</li>
 * <li>{@code PAGADA}: el saldo llegó a cero.</li>
 * <li>{@code ANULADA}: su comprobante fue anulado con nota de crédito.</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum EstadoVenta {
    /** Tiene saldo por cobrar. */
    PENDIENTE,

    /** El saldo llegó a cero. */
    PAGADA,

    /** Su comprobante se anuló con nota de crédito. */
    ANULADA
}
