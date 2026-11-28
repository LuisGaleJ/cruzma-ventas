package pe.cruzma.ventas.controller;

import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.dto.Periodo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Texto del período que se muestra bajo el título del Dashboard. */
class PanelControllerTest {

    @Test
    void etiquetaDelPeriodo() {
        LocalDate hoy = LocalDate.of(2026, 11, 28);
        assertEquals("Hoy · 28/11/2026", PanelController.etiqueta(Periodo.HOY, hoy, hoy));
        assertEquals("Este mes · 01/11/2026 – 28/11/2026",
                PanelController.etiqueta(Periodo.MES, LocalDate.of(2026, 11, 1), hoy));
        assertEquals("15/11/2026 – 20/11/2026",
                PanelController.etiqueta(null, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 20)));
    }
}
