package pe.cruzma.ventas.dto;

import java.math.BigDecimal;

/**
 * Fila que el vendedor agrega en la pantalla de nueva cotización (P6).
 *
 * @param productoId producto o servicio elegido
 * @param cantidad   cantidad; si viene vacía se toma 1
 * @param medidas    medidas del trabajo a medida
 * @param precio     precio con IGV; si viene vacío se toma el del catálogo
 * @param descuento  descuento del ítem en porcentaje; vacío es 0
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public record ItemCotizacionForm(Long productoId, Integer cantidad, String medidas, BigDecimal precio, BigDecimal descuento) {

    /**
     * Fila sin descuento.
     *
     * @param productoId producto elegido
     * @param cantidad   cantidad
     * @param medidas    medidas
     * @param precio     precio unitario
     */
    public ItemCotizacionForm(Long productoId, Integer cantidad, String medidas, BigDecimal precio) {
        this(productoId, cantidad, medidas, precio, null);
    }
}
