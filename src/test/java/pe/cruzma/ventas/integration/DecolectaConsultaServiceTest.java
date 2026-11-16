package pe.cruzma.ventas.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.model.TipoDocumento;

import static org.junit.jupiter.api.Assertions.*;

/** Consulta de DNI/RUC: lectura de la respuesta y validaciones previas (sin llamar a internet). */
class DecolectaConsultaServiceTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static JsonNode json(String texto) throws Exception { return JSON.readTree(texto); }

    @Test
    void leeElNombreDeUnDniConNombreCompletoOPorPartes() throws Exception {
        DatosDocumento a = DecolectaConsultaService.convertir(TipoDocumento.DNI, "45871236",
                json("{\"full_name\":\"CASTILLO RUIZ MARIA\",\"document_number\":\"45871236\"}"));
        assertEquals("CASTILLO RUIZ MARIA", a.nombre());
        assertNull(a.direccion());

        DatosDocumento b = DecolectaConsultaService.convertir(TipoDocumento.DNI, "45871236",
                json("{\"first_name\":\"MARIA\",\"first_last_name\":\"CASTILLO\",\"second_last_name\":\"RUIZ\"}"));
        assertEquals("MARIA CASTILLO RUIZ", b.nombre());
    }

    @Test
    void leeRazonSocialYDireccionDeUnRuc() throws Exception {
        DatosDocumento d = DecolectaConsultaService.convertir(TipoDocumento.RUC, "20601234567",
                json("{\"razon_social\":\"CONSTRUCTORA LOS ALGARROBOS S.A.\",\"direccion\":\"AV.  GRAU 123  PIURA\"}"));
        assertEquals("CONSTRUCTORA LOS ALGARROBOS S.A.", d.nombre());
        assertEquals("AV. GRAU 123 PIURA", d.direccion());
    }

    @Test
    void respuestaSinNombreNoDevuelveDatos() throws Exception {
        assertNull(DecolectaConsultaService.convertir(TipoDocumento.RUC, "20601234567", json("{\"direccion\":\"-\"}")));
    }

    @Test
    void validaElNumeroYExigeTokenAntesDeLlamarALaApi() {
        var sinToken = new DecolectaConsultaService("https://api.decolecta.com", "", "/v1/reniec/dni", "/v1/sunat/ruc");
        assertThrows(IllegalArgumentException.class, () -> sinToken.consultar(TipoDocumento.DNI, "1234567"));
        assertThrows(IllegalArgumentException.class, () -> sinToken.consultar(TipoDocumento.RUC, "30601234567"));
        assertThrows(IllegalStateException.class, () -> sinToken.consultar(TipoDocumento.DNI, "45871236"));
    }
}
