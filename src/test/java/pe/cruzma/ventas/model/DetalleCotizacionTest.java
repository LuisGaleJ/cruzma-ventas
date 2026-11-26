package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.service.Fixtures;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Monto original de un ítem cuando la unidad del producto cambia después de cotizar
 * (caso real: COT-2026-0009, servicio cotizado por medida que después pasó a ZZ).
 */
class DetalleCotizacionTest {

    @Test
    void sinDescuentoElMontoOriginalEsElSubtotalGuardadoAunqueCambieLaUnidad() {
        Producto mantenimiento = Fixtures.producto("Mantenimiento preventivo de acero inoxidable", "100.00");
        mantenimiento.setUnidad("MTK");
        DetalleCotizacion d = new DetalleCotizacion(mantenimiento, 1, "9", mantenimiento.getPrecioUnitario());
        assertEquals(new BigDecimal("900.00"), d.getSubtotal());

        mantenimiento.setTipo(TipoProducto.SERVICIO);
        mantenimiento.setUnidad("ZZ");                          // la migración le asigna la unidad de servicios
        assertEquals(new BigDecimal("900.00"), d.getBruto(), "no debe recalcularse como 100 x 1");
    }

    @Test
    void conDescuentoRecuperaElMetradoConElQueSeCotizo() {
        Producto reja = Fixtures.producto("Reja de fierro", "100.00");
        reja.setUnidad("MTK");
        DetalleCotizacion d = new DetalleCotizacion(reja, 2, "3.20 x 0.90", reja.getPrecioUnitario(), new BigDecimal("10"));
        assertEquals(new BigDecimal("576.00"), d.getBruto());   // 100 x 2 x 2.88
        assertEquals(new BigDecimal("518.40"), d.getSubtotal());

        reja.setUnidad("NIU");                                   // la unidad cambia después de cotizar
        assertEquals(new BigDecimal("576.00"), d.getBruto());
    }

    @Test
    void conDescuentoYSinMedidasUsaPrecioPorCantidad() {
        Producto puerta = Fixtures.producto("Puerta metálica", "250.00");
        DetalleCotizacion d = new DetalleCotizacion(puerta, 3, null, puerta.getPrecioUnitario(), new BigDecimal("5"));
        assertEquals(new BigDecimal("750.00"), d.getBruto());
        assertEquals(new BigDecimal("712.50"), d.getSubtotal());
    }
}
