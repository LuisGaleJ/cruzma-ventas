package pe.cruzma.ventas.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Rangos de los accesos rápidos del Dashboard (referencia: sábado 28/11/2026). */
class PeriodoTest {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 28);

    @Test
    void hoyYAyerSonUnSoloDia() {
        assertEquals(HOY, Periodo.HOY.desde(HOY));
        assertEquals(HOY, Periodo.HOY.hasta(HOY));
        assertEquals(LocalDate.of(2026, 11, 27), Periodo.AYER.desde(HOY));
        assertEquals(LocalDate.of(2026, 11, 27), Periodo.AYER.hasta(HOY));
    }

    @Test
    void semanaEmpiezaElLunesYMesElDiaUno() {
        assertEquals(LocalDate.of(2026, 11, 23), Periodo.SEMANA.desde(HOY));
        assertEquals(LocalDate.of(2026, 11, 1), Periodo.MES.desde(HOY));
        assertEquals(HOY, Periodo.MES.hasta(HOY));
        assertEquals(LocalDate.of(2026, 1, 1), Periodo.ANIO.desde(HOY));
    }

    @Test
    void mesAnteriorEsElMesCompleto() {
        assertEquals(LocalDate.of(2026, 10, 1), Periodo.MES_ANTERIOR.desde(HOY));
        assertEquals(LocalDate.of(2026, 10, 31), Periodo.MES_ANTERIOR.hasta(HOY));
        LocalDate marzo = LocalDate.of(2028, 3, 15);                 // febrero bisiesto
        assertEquals(LocalDate.of(2028, 2, 29), Periodo.MES_ANTERIOR.hasta(marzo));
    }
}
