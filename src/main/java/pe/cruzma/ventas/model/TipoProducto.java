package pe.cruzma.ventas.model;

/**
 * Clasifica los ítems del catálogo.
 * <ul>
 * <li>{@code PRODUCTO}: bien fabricado en el taller (baranda, mesa, puerta).</li>
 * <li>{@code SERVICIO}: trabajo sin bien físico (instalación, mantenimiento).</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public enum TipoProducto {
    /** Bien fabricado en el taller. */
    PRODUCTO,

    /** Trabajo sin bien físico, como la instalación. */
    SERVICIO
}
