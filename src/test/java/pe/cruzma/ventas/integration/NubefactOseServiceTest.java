package pe.cruzma.ventas.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.service.Fixtures;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** JSON de Nubefact: armado del comprobante y lectura de la respuesta (sin llamar a internet). */
class NubefactOseServiceTest {

    private static final BigDecimal IGV = new BigDecimal("0.18");
    private static final Map<String, String> SERIES =
            Map.of("FACTURA", "FFF1", "BOLETA", "BBB1", "NC_FACTURA", "FFF1", "NC_BOLETA", "BBB1");

    private static Comprobante boleta() {
        Venta v = Fixtures.venta(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"));
        return new Comprobante(v, TipoComprobante.BOLETA, 7, v.getTotal(), IGV);
    }

    @Test
    @SuppressWarnings("unchecked")
    void armaLaBoletaConTotalesQueCuadranConLosItems() {
        Map<String, Object> j = NubefactOseService.armarComprobante(boleta(), null, null, SERIES, IGV);

        assertEquals("generar_comprobante", j.get("operacion"));
        assertEquals(2, j.get("tipo_de_comprobante"));
        assertEquals("BBB1", j.get("serie"));
        assertEquals(7, j.get("numero"));
        assertEquals("1", j.get("cliente_tipo_de_documento"));
        assertEquals("-", j.get("cliente_direccion"));
        // Fixture: 3 m de baranda a S/ 320 = S/ 960 con IGV -> gravada 813.56 + IGV 146.44
        assertEquals(0, new BigDecimal("960.00").compareTo((BigDecimal) j.get("total")));
        assertEquals(0, new BigDecimal("813.56").compareTo((BigDecimal) j.get("total_gravada")));
        assertEquals(0, new BigDecimal("146.44").compareTo((BigDecimal) j.get("total_igv")));
        List<Map<String, Object>> items = (List<Map<String, Object>>) j.get("items");
        assertEquals(1, items.size());
        assertEquals("NIU", items.get(0).get("unidad_de_medida"));
        assertFalse(j.containsKey("documento_que_se_modifica_tipo"));
    }

    @Test
    void laNotaDeCreditoReferenciaAlComprobanteQueAnula() {
        Comprobante boleta = boleta();
        Comprobante nc = new Comprobante(boleta.getVenta(), TipoComprobante.NOTA_CREDITO, 2, boleta.getTotal(), IGV);
        Map<String, Object> j = NubefactOseService.armarComprobante(nc, boleta, "Cliente desistió", SERIES, IGV);

        assertEquals(3, j.get("tipo_de_comprobante"));
        assertEquals("BBB1", j.get("serie"));                       // nota de una boleta: serie B
        assertEquals(2, j.get("documento_que_se_modifica_tipo"));
        assertEquals(7, j.get("documento_que_se_modifica_numero"));
        assertEquals(1, j.get("tipo_de_nota_de_credito"));
    }

    @Test
    void leeRespuestaAceptadaConPdfYRespuestaConError() throws Exception {
        ObjectMapper om = new ObjectMapper();
        RespuestaOSE ok = NubefactOseService.leerRespuesta(om.readTree(
                "{\"aceptada_por_sunat\":true,\"sunat_description\":\"La Boleta ha sido aceptada\","
                        + "\"codigo_hash\":\"abc123\",\"enlace_del_pdf\":\"https://www.nubefact.com/cpe/x.pdf\"}"), "B001-00000007");
        assertTrue(ok.aceptado());
        assertEquals("abc123", ok.hashCdr());
        assertEquals("https://www.nubefact.com/cpe/x.pdf", ok.enlacePdf());

        RespuestaOSE error = NubefactOseService.leerRespuesta(om.readTree(
                "{\"errors\":\"Este documento ya existe en NubeFacT\",\"codigo\":23}"), "B001-00000007");
        assertFalse(error.aceptado());
        assertTrue(error.mensaje().contains("ya existe"));
    }
}
