package pe.cruzma.ventas.integration;

/**
 * Respuesta del OSE/PSE a un envío.
 *
 * @param aceptado true si SUNAT aceptó el comprobante
 * @param hashCdr  identificador de la constancia de recepción (CDR)
 * @param mensaje  descripción devuelta por el OSE
 */
public record RespuestaOSE(boolean aceptado, String hashCdr, String mensaje) { }
