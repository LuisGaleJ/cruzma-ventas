package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.service.Fixtures;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Cotizaciones por vencer: días restantes, texto del aviso y recordatorio por WhatsApp. */
class CotizacionVencimientoTest {

    private static Cotizacion venceEn(int dias) {
        Cotizacion c = Fixtures.cotizacion(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"), 15);
        c.setFechaVencimiento(LocalDate.now().plusDays(dias));
        return c;
    }

    @Test
    void estaPorVencerSoloSiSiguePendienteYVenceDentroDelAviso() {
        assertTrue(venceEn(0).porVencer(3), "vence hoy");
        assertTrue(venceEn(3).porVencer(3));
        assertFalse(venceEn(4).porVencer(3), "aún tiene tiempo");
        assertFalse(venceEn(-1).porVencer(3), "ya venció");
        Cotizacion aprobada = venceEn(1);
        aprobada.setEstado(EstadoCotizacion.APROBADA);
        assertFalse(aprobada.porVencer(3), "ya se convirtió en venta");
    }

    @Test
    void textoDelVencimiento() {
        assertEquals("vence hoy", venceEn(0).getTextoVencimiento());
        assertEquals("vence mañana", venceEn(1).getTextoVencimiento());
        assertEquals("vence en 3 días", venceEn(3).getTextoVencimiento());
        assertEquals("venció ayer", venceEn(-1).getTextoVencimiento());
        assertEquals("venció hace 4 días", venceEn(-4).getTextoVencimiento());
    }

    @Test
    void recordatorioPorWhatsAppSoloConCelular() {
        Cotizacion c = venceEn(2);
        c.getCliente().setTelefono("987654321");
        String enlace = c.getEnlaceWhatsApp();
        assertTrue(enlace.startsWith("https://wa.me/51987654321?text="));
        assertTrue(enlace.contains("Mar%C3%ADa%20Castillo%20Ruiz"));
        assertTrue(enlace.contains("vence%20el"));

        c.getCliente().setTelefono("073321654");                  // fijo
        assertNull(c.getEnlaceWhatsApp());
    }
}
