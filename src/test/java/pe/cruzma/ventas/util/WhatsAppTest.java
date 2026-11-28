package pe.cruzma.ventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Enlaces wa.me para recordatorios por WhatsApp. */
class WhatsAppTest {

    @Test
    void soloAceptaCelularesPeruanos() {
        assertEquals("51987654321", WhatsApp.numero("987654321"));
        assertEquals("51987654321", WhatsApp.numero("+51 987 654 321"));
        assertNull(WhatsApp.numero("073321654"), "teléfono fijo de Piura");
        assertNull(WhatsApp.numero("98765"), "incompleto");
        assertNull(WhatsApp.numero(null));
        assertNull(WhatsApp.numero(""));
    }

    @Test
    void codificaElMensajeEnElEnlace() {
        assertEquals("https://wa.me/51987654321?text=Hola%20Mar%C3%ADa%2C%20%C2%BFconfirma%3F",
                WhatsApp.enlace("987654321", "Hola María, ¿confirma?"));
        assertNull(WhatsApp.enlace("073321654", "Hola"));
    }
}
