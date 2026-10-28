package pe.cruzma.ventas.dto;

import java.math.BigDecimal;

/**
 * Indicadores del panel principal (P2).
 *
 * @param ventasHoy              total vendido hoy
 * @param ventasMes              total vendido en el mes
 * @param cotizacionesPendientes cotizaciones sin respuesta del cliente
 * @param saldoPorCobrar         suma de saldos pendientes
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public record ResumenPanel(BigDecimal ventasHoy, BigDecimal ventasMes, long cotizacionesPendientes, BigDecimal saldoPorCobrar) { }
