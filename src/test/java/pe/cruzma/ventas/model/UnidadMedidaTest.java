package pe.cruzma.ventas.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unidades de medida de la Tabla 6 de SUNAT según el giro de Grupo Cruzma. */
class UnidadMedidaTest {

    @Test
    void contieneLasUnidadesDelGiroConSuDescripcionOficial() {
        assertEquals("METRO CUADRADO", UnidadMedida.descripcion("MTK"));
        assertEquals("UNIDAD (SERVICIOS)", UnidadMedida.descripcion("ZZ"));
        assertTrue(UnidadMedida.existe("KGM"));
        assertFalse(UnidadMedida.existe("YRD"), "yarda no corresponde al giro");
        assertFalse(UnidadMedida.existe("UND"), "UND no es un código de SUNAT");
        assertEquals(UnidadMedida.total(), UnidadMedida.todas().size());
    }

    @Test
    void elServicioUsaZZYElProductoCualquierOtra() {
        assertNull(UnidadMedida.validar(TipoProducto.SERVICIO, "ZZ"));
        assertNull(UnidadMedida.validar(TipoProducto.PRODUCTO, "MTR"));
        assertNotNull(UnidadMedida.validar(TipoProducto.SERVICIO, "NIU"));
        assertNotNull(UnidadMedida.validar(TipoProducto.PRODUCTO, "ZZ"));
        assertNotNull(UnidadMedida.validar(TipoProducto.PRODUCTO, ""));
        assertNotNull(UnidadMedida.validar(TipoProducto.PRODUCTO, "M2"));
    }

    @Test
    void convierteLasUnidadesAnterioresYMuestraSuAbreviatura() {
        assertEquals("NIU", UnidadMedida.normalizar("und"));
        assertEquals("MTK", UnidadMedida.normalizar("M²"));
        assertEquals("ZZ", UnidadMedida.normalizar("SERV"));
        assertEquals("MTR", UnidadMedida.normalizar("MTR"));
        assertEquals("m2", UnidadMedida.simbolo("MTK"));
        assertEquals("m2", UnidadMedida.simbolo("M2"));
        assertEquals("kg", UnidadMedida.simbolo("KGM"));
    }
}
