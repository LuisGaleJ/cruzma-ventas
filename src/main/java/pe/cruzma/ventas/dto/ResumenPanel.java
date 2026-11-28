package pe.cruzma.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Indicadores del Dashboard (P2) para un rango de fechas.
 * <p>
 * Las ventas, lo cobrado y las cotizaciones son del período elegido; el saldo por cobrar y las cotizaciones
 * pendientes son a la fecha (no dependen del filtro), porque muestran lo que falta gestionar hoy.
 *
 * @param desde                  primer día del período
 * @param hasta                  último día del período
 * @param ventasPeriodo          total vendido en el período (sin anuladas)
 * @param numeroVentas           cantidad de ventas del período
 * @param ticketPromedio         total vendido entre número de ventas
 * @param cobradoPeriodo         pagos registrados en el período
 * @param cotizacionesPeriodo    cotizaciones emitidas en el período
 * @param cotizacionesPendientes cotizaciones sin respuesta del cliente (a la fecha)
 * @param saldoPorCobrar         suma de saldos pendientes (a la fecha)
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public record ResumenPanel(LocalDate desde, LocalDate hasta, BigDecimal ventasPeriodo, long numeroVentas,
                           BigDecimal ticketPromedio, BigDecimal cobradoPeriodo, long cotizacionesPeriodo,
                           long cotizacionesPendientes, BigDecimal saldoPorCobrar) { }
