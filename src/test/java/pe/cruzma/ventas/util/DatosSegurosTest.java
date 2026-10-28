package pe.cruzma.ventas.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas del tratamiento seguro de datos (Apache Commons Lang). */
class DatosSegurosTest {

    @Test
    void normalizaEspaciosYConvierteVacioEnNull() {
        assertEquals("Constructora Los Algarrobos", DatosSeguros.normalizar("  Constructora   Los  Algarrobos "));
        assertNull(DatosSeguros.normalizar("   "));
        assertNull(DatosSeguros.normalizar(null));
    }

    @Test
    void enmascaraDocumentoDejandoCuatroDigitos() {
        assertEquals("4587****", DatosSeguros.enmascararDocumento("45871236"));
        assertEquals("2061*******", DatosSeguros.enmascararDocumento("20612410390"));
        assertEquals("****", DatosSeguros.enmascararDocumento(null));
    }

    @Test
    void neutralizaFormulasAlExportar() {
        assertEquals("'=HYPERLINK(\"http://x\")", DatosSeguros.celdaSegura("=HYPERLINK(\"http://x\")"));
        assertEquals("'+51 969", DatosSeguros.celdaSegura("+51 969"));
        assertEquals("'@SUM(A1)", DatosSeguros.celdaSegura("@SUM(A1)"));
        assertEquals("María Castillo", DatosSeguros.celdaSegura("María Castillo"));
        assertEquals("", DatosSeguros.celdaSegura(null));
    }
}
