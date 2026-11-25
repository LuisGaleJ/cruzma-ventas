package pe.cruzma.ventas.dto;

import pe.cruzma.ventas.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Todos los datos de los reportes R1, R2 y R3 de un período.
 * <p>
 * Lo arma {@link pe.cruzma.ventas.service.ReporteService#armar(LocalDate, LocalDate)} y lo usan
 * tanto la pantalla de reportes como los exportadores a Excel y CSV, así ambos muestran lo mismo.
 *
 * @param desde          primer día del período
 * @param hasta          último día del período
 * @param ventas         ventas no anuladas del período
 * @param total          total vendido
 * @param ticketPromedio total entre número de ventas
 * @param porDia         R1 ventas por día
 * @param porCliente     R2 ranking de clientes
 * @param porProducto    R2 ranking de productos
 * @param porCobrar      R3 ventas con saldo pendiente
 * @param detalle        ítems vendidos, con monto original, descuento y monto cobrado de cada uno
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
public record DatosReporte(LocalDate desde, LocalDate hasta, List<Venta> ventas, BigDecimal total,
                           BigDecimal ticketPromedio, List<FilaReporte> porDia, List<FilaReporte> porCliente,
                           List<FilaReporte> porProducto, List<Venta> porCobrar, List<DetalleReporte> detalle) {

    /**
     * Datos sin el detalle por producto.
     *
     * @param desde          primer día del período
     * @param hasta          último día del período
     * @param ventas         ventas no anuladas del período
     * @param total          total vendido
     * @param ticketPromedio total entre número de ventas
     * @param porDia         R1 ventas por día
     * @param porCliente     R2 ranking de clientes
     * @param porProducto    R2 ranking de productos
     * @param porCobrar      R3 ventas con saldo pendiente
     */
    public DatosReporte(LocalDate desde, LocalDate hasta, List<Venta> ventas, BigDecimal total, BigDecimal ticketPromedio,
                        List<FilaReporte> porDia, List<FilaReporte> porCliente, List<FilaReporte> porProducto,
                        List<Venta> porCobrar) {
        this(desde, hasta, ventas, total, ticketPromedio, porDia, porCliente, porProducto, porCobrar, List.of());
    }

    /** {@return monto original del período, antes de descuentos} */
    public BigDecimal totalOriginal() {
        return ventas.stream().map(Venta::getMontoOriginal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** {@return descuentos concedidos en el período, en soles} */
    public BigDecimal totalDescuento() {
        return totalOriginal().subtract(total);
    }


    /** {@return número de ventas del período} */
    public int numeroVentas() {
        return ventas.size();
    }
}
