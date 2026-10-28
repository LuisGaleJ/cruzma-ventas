package pe.cruzma.ventas.model;

/**
 * Estado de un comprobante electrónico frente a SUNAT.
 * <ul>
 * <li>{@code PENDIENTE}: generado, aún sin respuesta del OSE.</li>
 * <li>{@code ACEPTADO}: el OSE devolvió la constancia (CDR).</li>
 * <li>{@code RECHAZADO}: el OSE lo observó; se corrige y se vuelve a emitir.</li>
 * <li>{@code ANULADO}: dado de baja mediante nota de crédito.</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum EstadoSunat {
    /** Generado, sin respuesta del OSE. */
    PENDIENTE,

    /** Aceptado; tiene constancia (CDR). */
    ACEPTADO,

    /** Observado por el OSE. */
    RECHAZADO,

    /** Dado de baja con nota de crédito. */
    ANULADO
}
