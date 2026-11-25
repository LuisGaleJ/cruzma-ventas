package pe.cruzma.ventas.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.cruzma.ventas.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facturación electrónica real con la API JSON de <a href="https://www.nubefact.com">Nubefact</a> (PSE/OSE).
 * <p>
 * Se activa con {@code cruzma.ose.proveedor=nubefact} (variable {@code OSE_PROVEEDOR}). La ruta y el token de
 * la cuenta se leen de las variables {@code OSE_URL} y {@code OSE_TOKEN}, nunca del repositorio. Con una cuenta
 * de demostración los comprobantes se generan con las series de prueba ({@code FFF1}, {@code BBB1}) y no llegan a SUNAT.
 * <p>
 * Los importes del sistema ya incluyen IGV: por cada ítem se separan valor sin IGV, IGV y total, y los totales
 * del comprobante se arman sumando los ítems para que cuadren exactamente.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.6.0
 */
@Service
@ConditionalOnProperty(name = "cruzma.ose.proveedor", havingValue = "nubefact")
public class NubefactOseService implements ServicioFacturacionOSE {

    private static final Logger LOG = LoggerFactory.getLogger(NubefactOseService.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final RestClient http;
    private final String token;
    private final BigDecimal tasaIgv;
    private final Map<String, String> series;

    /**
     * Crea el servicio con la configuración de {@code application.properties}.
     *
     * @param url                 RUTA de la cuenta de Nubefact (variable {@code OSE_URL})
     * @param token               TOKEN de la cuenta (variable {@code OSE_TOKEN})
     * @param tasaIgv             tasa del IGV, por ejemplo 0.18
     * @param serieFactura        serie de facturas en Nubefact
     * @param serieBoleta         serie de boletas en Nubefact
     * @param serieNotaFactura    serie de notas de crédito de facturas
     * @param serieNotaBoleta     serie de notas de crédito de boletas
     */
    public NubefactOseService(@Value("${cruzma.ose.url}") String url,
                              @Value("${cruzma.ose.token}") String token,
                              @Value("${cruzma.igv}") BigDecimal tasaIgv,
                              @Value("${cruzma.ose.serie-factura}") String serieFactura,
                              @Value("${cruzma.ose.serie-boleta}") String serieBoleta,
                              @Value("${cruzma.ose.serie-nota-factura}") String serieNotaFactura,
                              @Value("${cruzma.ose.serie-nota-boleta}") String serieNotaBoleta) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(10));
        tiempos.setReadTimeout(Duration.ofSeconds(30));
        this.http = RestClient.builder().baseUrl(url).requestFactory(tiempos).build();
        this.token = token;
        this.tasaIgv = tasaIgv;
        this.series = Map.of("FACTURA", serieFactura, "BOLETA", serieBoleta,
                "NC_FACTURA", serieNotaFactura, "NC_BOLETA", serieNotaBoleta);
    }

    @Override
    public RespuestaOSE enviar(Comprobante comprobante) {
        return publicar(armarComprobante(comprobante, null, null, series, tasaIgv), comprobante);
    }

    @Override
    public RespuestaOSE enviarNotaCredito(Comprobante notaCredito, Comprobante original, String motivo) {
        return publicar(armarComprobante(notaCredito, original, motivo, series, tasaIgv), notaCredito);
    }

    /** Comunicación de baja directa del comprobante (sin nota de crédito). */
    @Override
    public RespuestaOSE comunicarBaja(Comprobante comprobante, String motivo) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("operacion", "generar_anulacion");
        cuerpo.put("tipo_de_comprobante", tipoNubefact(comprobante.getTipo()));
        cuerpo.put("serie", serieNubefact(comprobante, null, series));
        cuerpo.put("numero", comprobante.getCorrelativo());
        cuerpo.put("motivo", motivo);
        return publicar(cuerpo, comprobante);
    }

    private RespuestaOSE publicar(Map<String, Object> cuerpo, Comprobante c) {
        try {
            JsonNode r = http.post()
                    .header("Authorization", "Token token=\"" + token + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(JsonNode.class);
            return leerRespuesta(r, c.getNumero());
        } catch (RestClientResponseException e) {
            // Nubefact responde los errores de validación con código 4xx y {"errors": "...", "codigo": n}
            String detalle = e.getResponseBodyAsString();
            LOG.warn("Nubefact rechazó {}: {} {}", c.getNumero(), e.getStatusCode().value(), detalle);
            return new RespuestaOSE(false, null, "Nubefact: " + mensajeDeError(detalle, e.getStatusCode().value()));
        } catch (RestClientException e) {
            LOG.warn("No se pudo conectar con Nubefact: {}", e.getMessage());
            return new RespuestaOSE(false, null, "No se pudo conectar con el OSE. Intenta emitir de nuevo en unos minutos.");
        }
    }

    /**
     * Arma el JSON de {@code generar_comprobante} para una factura, boleta o nota de crédito.
     *
     * @param c        comprobante a enviar
     * @param original comprobante que modifica la nota de crédito; {@code null} si no es nota
     * @param motivo   motivo de la nota de crédito; {@code null} si no es nota
     * @param series   series de Nubefact por tipo
     * @param tasaIgv  tasa del IGV
     * @return cuerpo de la petición
     */
    @VisibleForTesting
    static Map<String, Object> armarComprobante(Comprobante c, Comprobante original, String motivo,
                                                Map<String, String> series, BigDecimal tasaIgv) {
        Cliente cliente = c.getVenta().getCliente();
        BigDecimal factor = BigDecimal.ONE.add(tasaIgv);
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal gravada = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleVenta d : c.getVenta().getDetalles()) {
            BigDecimal importe = d.getSubtotal();                       // con IGV, con descuentos y metrado
            BigDecimal cantidad = BigDecimal.valueOf(d.getCantidad());
            BigDecimal precio = importe.divide(cantidad, 6, RoundingMode.HALF_UP);
            BigDecimal valor = precio.divide(factor, 6, RoundingMode.HALF_UP);
            BigDecimal subtotal = importe.divide(factor, 2, RoundingMode.HALF_UP);
            BigDecimal igvItem = importe.subtract(subtotal);
            Producto p = d.getProducto();
            Map<String, Object> item = new LinkedHashMap<>();
            // Código de la Tabla 6 de SUNAT del producto; si es una unidad antigua sin equivalencia, NIU o ZZ según el tipo
            String unidad = UnidadMedida.normalizar(p.getUnidad());
            if (!UnidadMedida.existe(unidad)) unidad = p.getTipo() == TipoProducto.SERVICIO ? UnidadMedida.SERVICIO : UnidadMedida.UNIDAD;
            item.put("unidad_de_medida", unidad);
            item.put("codigo", p.getCodigo());
            item.put("descripcion", p.getDescripcion());
            item.put("cantidad", cantidad);
            item.put("valor_unitario", valor);
            item.put("precio_unitario", precio);
            item.put("subtotal", subtotal);
            item.put("tipo_de_igv", 1);                                  // gravado, operación onerosa
            item.put("igv", igvItem);
            item.put("total", importe);
            item.put("anticipo_regularizacion", false);
            items.add(item);
            gravada = gravada.add(subtotal);
            igv = igv.add(igvItem);
            total = total.add(importe);
        }
        Map<String, Object> j = new LinkedHashMap<>();
        j.put("operacion", "generar_comprobante");
        j.put("tipo_de_comprobante", tipoNubefact(c.getTipo()));
        j.put("serie", serieNubefact(c, original, series));
        j.put("numero", c.getCorrelativo());
        j.put("sunat_transaction", 1);
        j.put("cliente_tipo_de_documento", cliente.getTipoDoc() == TipoDocumento.RUC ? "6" : "1");
        j.put("cliente_numero_de_documento", cliente.getNumDoc());
        j.put("cliente_denominacion", cliente.getRazonSocial());
        j.put("cliente_direccion", cliente.getDireccion() == null || cliente.getDireccion().isBlank() ? "-" : cliente.getDireccion());
        j.put("cliente_email", cliente.getEmail() == null ? "" : cliente.getEmail());
        j.put("fecha_de_emision", c.getFechaEmision().format(FECHA));
        j.put("moneda", 1);                                              // soles
        j.put("porcentaje_de_igv", tasaIgv.movePointRight(2).setScale(2, RoundingMode.HALF_UP));
        j.put("total_gravada", gravada);
        j.put("total_igv", igv);
        j.put("total", total);
        j.put("enviar_automaticamente_a_la_sunat", true);
        j.put("enviar_automaticamente_al_cliente", false);
        if (original != null) {
            j.put("documento_que_se_modifica_tipo", tipoNubefact(original.getTipo()));
            j.put("documento_que_se_modifica_serie", serieNubefact(original, null, series));
            j.put("documento_que_se_modifica_numero", original.getCorrelativo());
            j.put("tipo_de_nota_de_credito", 1);                         // anulación de la operación
            j.put("observaciones", motivo);
        }
        j.put("items", items);
        return j;
    }

    /**
     * Interpreta la respuesta de Nubefact.
     * <p>
     * Sin {@code errors}, el comprobante quedó generado: las facturas suelen volver ya aceptadas por SUNAT y las
     * boletas se informan después en el resumen diario.
     *
     * @param r      respuesta JSON
     * @param numero número del comprobante en el sistema
     * @return respuesta para el sistema
     */
    @VisibleForTesting
    static RespuestaOSE leerRespuesta(JsonNode r, String numero) {
        if (r == null) return new RespuestaOSE(false, null, "Nubefact no devolvió respuesta");
        if (r.hasNonNull("errors")) return new RespuestaOSE(false, null, "Nubefact: " + r.get("errors").asText());
        boolean aceptadaSunat = r.path("aceptada_por_sunat").asBoolean(false);
        String descripcion = r.path("sunat_description").asText("");
        String mensaje = aceptadaSunat
                ? "El comprobante " + numero + " fue aceptado por SUNAT" + (descripcion.isBlank() ? "" : ": " + descripcion)
                : "El comprobante " + numero + " fue generado por el OSE; SUNAT lo confirmará en el envío del día";
        String hash = r.path("codigo_hash").asText(null);
        String pdf = r.path("enlace_del_pdf").asText(null);
        return new RespuestaOSE(true, hash, mensaje, pdf);
    }

    private static String mensajeDeError(String cuerpo, int estado) {
        int i = cuerpo == null ? -1 : cuerpo.indexOf("\"errors\"");
        if (i < 0) return estado == 401 ? "token no válido (revisa OSE_TOKEN)" : "error " + estado;
        String resto = cuerpo.substring(cuerpo.indexOf(':', i) + 1).trim();
        return resto.replaceAll("^\"|\".*$", "");
    }

    /** Códigos de Nubefact: 1 factura, 2 boleta, 3 nota de crédito. */
    private static int tipoNubefact(TipoComprobante tipo) {
        return switch (tipo) {
            case FACTURA -> 1;
            case BOLETA -> 2;
            case NOTA_CREDITO -> 3;
        };
    }

    /** La nota de crédito usa la serie de facturas (F...) o de boletas (B...) según el comprobante que anula. */
    private static String serieNubefact(Comprobante c, Comprobante original, Map<String, String> series) {
        return switch (c.getTipo()) {
            case FACTURA -> series.get("FACTURA");
            case BOLETA -> series.get("BOLETA");
            case NOTA_CREDITO -> original != null && original.getTipo() == TipoComprobante.BOLETA
                    ? series.get("NC_BOLETA") : series.get("NC_FACTURA");
        };
    }
}
