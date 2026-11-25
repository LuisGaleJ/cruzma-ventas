package pe.cruzma.ventas.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fila genérica de los reportes R1 y R2.
 *
 * @param etiqueta día, cliente o producto que agrupa la fila
 * @param cantidad número de ventas o unidades
 * @param monto    importe cobrado en soles (después de descuentos)
 * @param montoOriginal importe antes de descuentos, en soles
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public record FilaReporte(String etiqueta, long cantidad, BigDecimal monto, BigDecimal montoOriginal) {

    /**
     * Fila sin descuentos: el monto original es igual al cobrado.
     *
     * @param etiqueta día, cliente o producto
     * @param cantidad número de ventas o unidades
     * @param monto    importe en soles
     */
    public FilaReporte(String etiqueta, long cantidad, BigDecimal monto) {
        this(etiqueta, cantidad, monto, monto);
    }

    /** {@return descuento concedido en soles: monto original menos monto cobrado} */
    public BigDecimal descuento() {
        return montoOriginal.subtract(monto);
    }

    /**
     * Porcentaje de esta fila sobre el total, para dibujar la barra del reporte.
     *
     * @param total total del período
     * @return porcentaje entero de 0 a 100; 0 si el total es cero
     */
    public int porcentajeDe(BigDecimal total) {
        if (total == null || total.signum() == 0) return 0;
        return monto.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP).intValue();
    }
}
