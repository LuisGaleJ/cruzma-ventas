package pe.cruzma.ventas.service;

import com.google.common.base.Ticker;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del bloqueo por intentos fallidos (Google Guava Cache). */
class IntentosLoginServiceTest {

    /** Reloj manual para simular el paso del tiempo sin esperar. */
    private static final class RelojFalso extends Ticker {
        private long nanos;
        @Override public long read() { return nanos; }
        void avanzarMinutos(long m) { nanos += TimeUnit.MINUTES.toNanos(m); }
    }

    @Test
    void bloqueaAlLlegarAlMaximoDeIntentos() {
        IntentosLoginService s = new IntentosLoginService(3, 15, new RelojFalso());
        s.registrarFallo("caja");
        s.registrarFallo("caja");
        assertFalse(s.estaBloqueado("caja"));
        s.registrarFallo("CAJA ");
        assertTrue(s.estaBloqueado("caja"), "el usuario se compara sin mayúsculas ni espacios");
    }

    @Test
    void elBloqueoExpiraConElTiempo() {
        RelojFalso reloj = new RelojFalso();
        IntentosLoginService s = new IntentosLoginService(2, 15, reloj);
        s.registrarFallo("vendedor");
        s.registrarFallo("vendedor");
        assertTrue(s.estaBloqueado("vendedor"));
        reloj.avanzarMinutos(16);
        assertFalse(s.estaBloqueado("vendedor"));
    }

    @Test
    void unIngresoCorrectoReiniciaElContador() {
        IntentosLoginService s = new IntentosLoginService(2, 15, new RelojFalso());
        s.registrarFallo("admin");
        s.registrarExito("admin");
        s.registrarFallo("admin");
        assertFalse(s.estaBloqueado("admin"));
    }
}
