package pe.cruzma.ventas.model;

/**
 * Tipo de documento de identidad del cliente.
 * <ul>
 * <li>{@code DNI}: persona natural, 8 dígitos; recibe boleta.</li>
 * <li>{@code RUC}: contribuyente, 11 dígitos; puede recibir factura.</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum TipoDocumento {
    /** Documento Nacional de Identidad, 8 dígitos. */
    DNI,

    /** Registro Único de Contribuyentes, 11 dígitos. */
    RUC
}
