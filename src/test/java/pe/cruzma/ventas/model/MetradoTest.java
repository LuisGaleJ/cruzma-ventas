package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.service.Fixtures;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Metrado de productos que se cobran por M, M2 o M3. */
class MetradoTest {

    private static void igual(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " y fue " + real);
    }

    @Test
    void m2AceptaAreaDirectaOLargoPorAlto() {
        igual("5", Metrado.calcular("M2", "5"));
        igual("2.88", Metrado.calcular("M2", "3.20 x 0.90"));
        igual("2.88", Metrado.calcular("M2", "3,20 m x 0,90 m"));
        igual("2.88", Metrado.calcular("m2", "3.2x0.9 m2"));
    }

    @Test
    void lineales() {
        igual("4.5", Metrado.calcular("ML", "4.5 m"));
        igual("12", Metrado.calcular("M", "12"));
        assertThrows(IllegalArgumentException.class, () -> Metrado.calcular("M", "3 x 2"));
    }

    @Test
    void otrasUnidadesNoMultiplican() {
        igual("1", Metrado.calcular("UND", "color negro, 2 hojas"));
        igual("1", Metrado.calcular("SERV", null));
        assertFalse(Metrado.seCobraPorMedida("UND"));
        assertTrue(Metrado.seCobraPorMedida("M2"));
    }

    @Test
    void medidasFaltantesOInvalidasSeRechazan() {
        assertThrows(IllegalArgumentException.class, () -> Metrado.calcular("M2", ""));
        assertThrows(IllegalArgumentException.class, () -> Metrado.calcular("M2", "ancho"));
        assertThrows(IllegalArgumentException.class, () -> Metrado.calcular("M2", "0"));
        assertThrows(IllegalArgumentException.class, () -> Metrado.calcular("M2", "1 x 2 x 3"));
    }

    @Test
    void elSubtotalDelItemMultiplicaPrecioCantidadYMetrado() {
        Producto baranda = Fixtures.producto("Baranda escalera", "280.00");
        baranda.setUnidad("M2");
        DetalleCotizacion d = new DetalleCotizacion(baranda, 1, "5", new BigDecimal("280.00"));
        igual("1400.00", d.getSubtotal());

        DetalleCotizacion dos = new DetalleCotizacion(baranda, 2, "3.20 x 0.90", new BigDecimal("280.00"));
        igual("1612.80", dos.getSubtotal());   // 280 x 2 x 2.88

        assertThrows(IllegalArgumentException.class,
                () -> new DetalleCotizacion(baranda, 1, "", new BigDecimal("280.00")));
    }

    @Test
    void reconoceLosCodigosDeLaTabla6DeSunat() {
        igual("2.88", Metrado.calcular("MTK", "3.20 x 0.90"));
        igual("4.5", Metrado.calcular("MTR", "4.5"));
        igual("1", Metrado.calcular("MTQ", "2 x 1 x 0.5"));
        igual("1", Metrado.calcular("NIU", "color negro"));
        assertTrue(Metrado.seCobraPorMedida("MTK"));
        assertFalse(Metrado.seCobraPorMedida("ZZ"));
    }
}
