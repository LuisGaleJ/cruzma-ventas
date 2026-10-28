package pe.cruzma.ventas.integration;

import pe.cruzma.ventas.model.Comprobante;

/**
 * Contrato con el proveedor de facturación electrónica (OSE/PSE).
 * El sistema depende de esta interfaz y no del proveedor concreto,
 * así se puede cambiar de proveedor sin tocar la lógica de ventas.
 */
public interface ServicioFacturacionOSE {
    /**
     * Envía un comprobante al OSE para su validación ante SUNAT.
     *
     * @param comprobante comprobante ya numerado
     * @return la respuesta del OSE
     */
    RespuestaOSE enviar(Comprobante comprobante);

    /**
     * Comunica la baja de un comprobante aceptado.
     *
     * @param comprobante comprobante a dar de baja
     * @param motivo      motivo de la anulación
     * @return la respuesta del OSE
     */
    RespuestaOSE comunicarBaja(Comprobante comprobante, String motivo);
}
