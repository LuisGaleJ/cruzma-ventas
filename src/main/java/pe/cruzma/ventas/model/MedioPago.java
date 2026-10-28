package pe.cruzma.ventas.model;

/**
 * Medios de pago aceptados por Grupo Cruzma para adelantos y saldos.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum MedioPago {
    /** Pago en efectivo en caja. */
    EFECTIVO,

    /** Transferencia bancaria. */
    TRANSFERENCIA,

    /** Pago con Yape. */
    YAPE,

    /** Pago con Plin. */
    PLIN,

    /** Tarjeta de débito o crédito. */
    TARJETA
}
