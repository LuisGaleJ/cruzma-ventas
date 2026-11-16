package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.service.Fixtures;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Cálculo de descuentos por ítem y global, y reparto del descuento global en la venta. */
class DescuentoTest {

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " y fue " + real);
    }

    @Test
    void aplicaPorcentajeYRedondeaADosDecimales() {
        igual("90.00", Descuento.aplicar(new BigDecimal("100.00"), BigDecimal.TEN));
        igual("33.33", Descuento.aplicar(new BigDecimal("37.03"), new BigDecimal("10")));
        igual("100.00", Descuento.aplicar(new BigDecimal("100.00"), null));
        assertThrows(IllegalArgumentException.class, () -> Descuento.porcentaje(new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> Descuento.porcentaje(new BigDecimal("101")));
    }

    @Test
    void maximoSegunRol() {
        Usuario u = Fixtures.usuario("x");
        igual("0", Descuento.maximo(u));
        u.setRol(new Rol(Rol.VENDEDOR));
        igual("10", Descuento.maximo(u));
        u.setRol(new Rol(Rol.ADMIN));
        igual("30", Descuento.maximo(u));
        u.setRol(new Rol(Rol.CAJA));
        igual("0", Descuento.maximo(u));
    }

    @Test
    void laVentaRepartaElDescuentoGlobalYSumaElTotal() {
        Producto reja = Fixtures.producto("Reja", "33.33");
        Cotizacion cot = new Cotizacion();
        cot.agregarDetalle(new DetalleCotizacion(reja, 1, null, new BigDecimal("33.33")));
        cot.agregarDetalle(new DetalleCotizacion(reja, 1, null, new BigDecimal("33.33")));
        cot.agregarDetalle(new DetalleCotizacion(reja, 1, null, new BigDecimal("33.34"), new BigDecimal("10")));
        cot.setDescuento(new BigDecimal("7"));
        cot.calcularTotal();
        // ítems: 33.33 + 33.33 + 30.01 = 96.67; menos 7 % = 89.90
        igual("96.67", cot.getSubtotalItems());
        igual("89.90", cot.getTotal());
        igual("6.77", cot.getMontoDescuentoGlobal());

        Venta v = Venta.desdeCotizacion(cot, Fixtures.usuario("caja"));
        BigDecimal suma = v.getDetalles().stream().map(DetalleVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        igual("89.90", suma);
        igual("89.90", v.getTotal());
    }
}
