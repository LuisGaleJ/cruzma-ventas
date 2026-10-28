package pe.cruzma.ventas.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fila genérica de los reportes R1 y R2.
 *
 * @param etiqueta día, cliente o producto que agrupa la fila
 * @param cantidad número de ventas o unidades
 * @param monto    importe total en soles
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public record FilaReporte(String etiqueta, long cantidad, BigDecimal monto) {
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
