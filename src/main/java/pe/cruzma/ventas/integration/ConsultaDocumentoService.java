package pe.cruzma.ventas.integration;

import pe.cruzma.ventas.model.TipoDocumento;

import java.util.Optional;

/**
 * Consulta de datos públicos por DNI (RENIEC) o RUC (SUNAT) para autocompletar clientes.
 * <p>
 * Igual que {@link ServicioFacturacionOSE}, el resto del sistema depende de esta interfaz y no del
 * proveedor: cambiar de proveedor de API es escribir otra implementación.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.5.0
 */
public interface ConsultaDocumentoService {

    /**
     * Busca los datos de un documento.
     *
     * @param tipo   DNI o RUC
     * @param numero número de 8 (DNI) u 11 (RUC) dígitos
     * @return los datos encontrados, o vacío si el documento no existe
     * @throws IllegalArgumentException si el número no tiene el formato correcto
     * @throws IllegalStateException    si el servicio no está configurado o no responde
     */
    Optional<DatosDocumento> consultar(TipoDocumento tipo, String numero);
}
