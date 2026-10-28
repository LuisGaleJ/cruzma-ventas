package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas unitarias de las reglas de negocio del dominio (sin base de datos). */
class ReglasNegocioTest {

    private Producto producto(String precio) {
        Producto p = new Producto();
        p.setDescripcion("Baranda inox");
        p.setPrecioUnitario(new BigDecimal(precio));
        return p;
    }

    private Cotizacion cotizacion() {
        Cliente c = new Cliente();
        c.setTipoDoc(TipoDocumento.RUC);
        c.setNumDoc("20601234567");
        Cotizacion cot = new Cotizacion();
        cot.setCliente(c);
        cot.agregarDetalle(new DetalleCotizacion(producto("320.00"), 3, "3 m", new BigDecimal("320.00")));
        cot.agregarDetalle(new DetalleCotizacion(producto("250.00"), 1, null, new BigDecimal("250.00")));
        cot.calcularTotal();
        return cot;
    }

    @Test
    void totalDeCotizacionSumaLosItems() {
        assertEquals(new BigDecimal("1210.00"), cotizacion().getTotal());
    }

    @Test
    void ventaDesdeCotizacionCopiaDetalleYSaldo() {
        Venta v = Venta.desdeCotizacion(cotizacion(), new Usuario());
        assertEquals(2, v.getDetalles().size());
        assertEquals(new BigDecimal("1210.00"), v.getSaldo());
    }

    @Test
    void pagosActualizanSaldoYEstado() {
        Venta v = Venta.desdeCotizacion(cotizacion(), new Usuario());
        v.registrarPago(new Pago(new BigDecimal("605.00"), MedioPago.YAPE));
        assertEquals(new BigDecimal("605.00"), v.getSaldo());
        assertEquals(EstadoVenta.PENDIENTE, v.getEstado());
        v.registrarPago(new Pago(new BigDecimal("605.00"), MedioPago.EFECTIVO));
        assertEquals(EstadoVenta.PAGADA, v.getEstado());
    }

    @Test
    void noSePuedePagarMasQueElSaldo() {
        Venta v = Venta.desdeCotizacion(cotizacion(), new Usuario());
        assertThrows(IllegalArgumentException.class, () -> v.registrarPago(new Pago(new BigDecimal("2000"), MedioPago.EFECTIVO)));
    }

    @Test
    void comprobanteSeparaIgvDelTotal() {
        Venta v = Venta.desdeCotizacion(cotizacion(), new Usuario());
        Comprobante c = new Comprobante(v, TipoComprobante.FACTURA, 12, v.getTotal(), new BigDecimal("0.18"));
        assertEquals(new BigDecimal("1025.42"), c.getSubtotal());
        assertEquals(new BigDecimal("184.58"), c.getIgv());
        assertEquals("F001-00000012", c.getNumero());
    }

    @Test
    void validaDniYRuc() {
        Cliente c = new Cliente();
        c.setTipoDoc(TipoDocumento.DNI);
        c.setNumDoc("45871236");
        assertTrue(c.documentoValido());
        c.setTipoDoc(TipoDocumento.RUC);
        assertFalse(c.documentoValido());
    }
}
