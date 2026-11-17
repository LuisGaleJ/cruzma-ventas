package pe.cruzma.ventas.integration;

/**
 * Respuesta del OSE/PSE a un envío.
 *
 * @param aceptado true si SUNAT aceptó el comprobante
 * @param hashCdr  identificador de la constancia de recepción (CDR)
 * @param mensaje  descripción devuelta por el OSE
 * @param enlacePdf enlace al PDF oficial generado por el OSE; {@code null} si no hay
 */
public record RespuestaOSE(boolean aceptado, String hashCdr, String mensaje, String enlacePdf) {

    /**
     * Respuesta sin enlace al PDF oficial (OSE simulado).
     *
     * @param aceptado {@code true} si el comprobante fue aceptado
     * @param hashCdr  hash o código de la constancia
     * @param mensaje  mensaje del OSE
     */
    public RespuestaOSE(boolean aceptado, String hashCdr, String mensaje) {
        this(aceptado, hashCdr, mensaje, null);
    }
}
