package pe.cruzma.ventas.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.cruzma.ventas.model.TipoDocumento;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Consulta DNI y RUC con la API REST de <a href="https://decolecta.com">Decolecta</a>.
 * <p>
 * El token se lee de la variable de entorno {@code DECOLECTA_TOKEN}, nunca del repositorio.
 * Las respuestas se guardan un día en una caché de Guava para no gastar consultas del plan
 * cuando se busca dos veces el mismo documento.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.5.0
 */
@Service
public class DecolectaConsultaService implements ConsultaDocumentoService {

    private static final Logger LOG = LoggerFactory.getLogger(DecolectaConsultaService.class);

    private final RestClient http;
    private final String token;
    private final String rutaDni;
    private final String rutaRuc;
    private final Cache<String, DatosDocumento> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS).maximumSize(1_000).build();

    /**
     * Crea el servicio con la configuración de {@code application.properties}.
     *
     * @param url     URL base de la API
     * @param token   token del plan (variable de entorno {@code DECOLECTA_TOKEN})
     * @param rutaDni ruta de la consulta de DNI
     * @param rutaRuc ruta de la consulta de RUC
     */
    public DecolectaConsultaService(@Value("${cruzma.consulta.url}") String url,
                                    @Value("${cruzma.consulta.token:}") String token,
                                    @Value("${cruzma.consulta.ruta-dni}") String rutaDni,
                                    @Value("${cruzma.consulta.ruta-ruc}") String rutaRuc) {
        SimpleClientHttpRequestFactory tiempos = new SimpleClientHttpRequestFactory();
        tiempos.setConnectTimeout(Duration.ofSeconds(5));
        tiempos.setReadTimeout(Duration.ofSeconds(8));
        this.http = RestClient.builder().baseUrl(url).requestFactory(tiempos).build();
        this.token = token;
        this.rutaDni = rutaDni;
        this.rutaRuc = rutaRuc;
    }

    @Override
    public Optional<DatosDocumento> consultar(TipoDocumento tipo, String numero) {
        validar(tipo, numero);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("La consulta a RENIEC/SUNAT no está configurada (falta la variable DECOLECTA_TOKEN)");
        }
        String clave = tipo + ":" + numero;
        DatosDocumento enCache = cache.getIfPresent(clave);
        if (enCache != null) return Optional.of(enCache);
        try {
            JsonNode json = http.get()
                    .uri(uri -> uri.path(tipo == TipoDocumento.DNI ? rutaDni : rutaRuc).queryParam("numero", numero).build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class);
            Optional<DatosDocumento> datos = Optional.ofNullable(json).map(j -> convertir(tipo, numero, j));
            datos.ifPresent(d -> cache.put(clave, d));
            return datos;
        } catch (RestClientResponseException e) {
            HttpStatusCode estado = e.getStatusCode();
            if (estado.value() == 404 || estado.value() == 422) return Optional.empty();   // documento inexistente
            LOG.warn("Consulta de {} respondió {}", tipo, estado.value());
            throw new IllegalStateException(estado.value() == 401 || estado.value() == 403
                    ? "El token de consulta RENIEC/SUNAT no es válido"
                    : "El servicio de consulta respondió con error " + estado.value());
        } catch (RestClientException e) {
            LOG.warn("No se pudo conectar con el servicio de consulta: {}", e.getMessage());
            throw new IllegalStateException("No se pudo conectar con el servicio de consulta. Completa los datos a mano.");
        }
    }

    /** El número se valida antes de llamar a la API para no gastar consultas en datos mal escritos. */
    private static void validar(TipoDocumento tipo, String numero) {
        boolean ok = numero != null && (tipo == TipoDocumento.DNI
                ? numero.matches("\\d{8}")
                : numero.matches("(10|15|17|20)\\d{9}"));
        if (!ok) {
            throw new IllegalArgumentException(tipo == TipoDocumento.DNI
                    ? "El DNI debe tener 8 dígitos" : "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20");
        }
    }

    /**
     * Convierte la respuesta JSON en {@link DatosDocumento}. Acepta los nombres de campo de la versión
     * actual de la API y los de versiones anteriores, por si el proveedor los cambia.
     *
     * @param tipo   DNI o RUC
     * @param numero número consultado
     * @param json   respuesta de la API
     * @return datos encontrados, o {@code null} si la respuesta no trae nombre
     */
    @VisibleForTesting
    static DatosDocumento convertir(TipoDocumento tipo, String numero, JsonNode json) {
        String nombre;
        String direccion = null;
        if (tipo == TipoDocumento.DNI) {
            nombre = primero(json, "full_name", "nombre_completo", "nombre");
            if (nombre == null) {
                nombre = unir(texto(json, "first_name"), texto(json, "first_last_name"), texto(json, "second_last_name"));
            }
            if (nombre == null) {
                nombre = unir(texto(json, "nombres"), texto(json, "apellidoPaterno"), texto(json, "apellidoMaterno"));
            }
        } else {
            nombre = primero(json, "razon_social", "razonSocial", "nombre");
            direccion = primero(json, "direccion", "direccion_completa", "domicilio_fiscal");
        }
        return nombre == null ? null : new DatosDocumento(numero, nombre, direccion);
    }

    private static String primero(JsonNode json, String... campos) {
        for (String c : campos) {
            String v = texto(json, c);
            if (v != null) return v;
        }
        return null;
    }

    private static String texto(JsonNode json, String campo) {
        JsonNode n = json.get(campo);
        if (n == null || n.isNull()) return null;
        String v = n.asText().trim().replaceAll("\\s+", " ");
        return v.isEmpty() || v.equals("-") ? null : v;
    }

    private static String unir(String... partes) {
        String r = Stream.of(partes).filter(p -> p != null).collect(Collectors.joining(" "));
        return r.isEmpty() ? null : r;
    }
}
