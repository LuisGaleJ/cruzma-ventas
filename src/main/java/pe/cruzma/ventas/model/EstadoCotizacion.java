package pe.cruzma.ventas.model;

/**
 * Ciclo de vida de una cotización.
 * <ul>
 * <li>{@code PENDIENTE}: enviada al cliente, sin respuesta.</li>
 * <li>{@code APROBADA}: el cliente aceptó y se convirtió en venta.</li>
 * <li>{@code RECHAZADA}: el cliente no aceptó.</li>
 * <li>{@code VENCIDA}: pasaron los días de validez sin respuesta.</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum EstadoCotizacion {
    /** Enviada al cliente, sin respuesta. */
    PENDIENTE,

    /** Aceptada y convertida en venta. */
    APROBADA,

    /** El cliente no la aceptó. */
    RECHAZADA,

    /** Pasó su fecha de validez sin respuesta. */
    VENCIDA
}
