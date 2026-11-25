package pe.cruzma.ventas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un ítem vendido en el período, para la hoja «Detalle por producto» del Excel.
 * <p>
 * Muestra cuánto costaba el ítem sin descuentos, cuánto se descontó (descuento del ítem más su parte del
 * descuento global de la cotización) y cuánto se cobró finalmente.
 *
 * @param fecha          fecha de la venta
 * @param venta          número de la venta
 * @param cotizacion     número de la cotización de origen
 * @param cliente        razón social o nombre del cliente
 * @param producto       descripción del producto o servicio
 * @param unidad         código de unidad de la Tabla 6 de SUNAT
 * @param cantidad       cantidad vendida
 * @param medidas        medidas indicadas en la cotización (vacío si no aplica)
 * @param precio         precio unitario con IGV
 * @param montoOriginal  importe sin descuentos (precio x cantidad x metrado)
 * @param descuentoItem  porcentaje de descuento del ítem
 * @param descuentoGlobal porcentaje de descuento global de la cotización
 * @param montoDescuento descuento total del ítem en soles
 * @param montoCobrado   importe cobrado del ítem, después de descuentos
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.7.0
 */
public record DetalleReporte(LocalDate fecha, Long venta, String cotizacion, String cliente, String producto,
                             String unidad, int cantidad, String medidas, BigDecimal precio, BigDecimal montoOriginal,
                             BigDecimal descuentoItem, BigDecimal descuentoGlobal, BigDecimal montoDescuento,
                             BigDecimal montoCobrado) {
}
