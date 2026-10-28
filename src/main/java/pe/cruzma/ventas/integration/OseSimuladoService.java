package pe.cruzma.ventas.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.cruzma.ventas.model.Cliente;
import pe.cruzma.ventas.model.Comprobante;
import pe.cruzma.ventas.model.TipoComprobante;
import pe.cruzma.ventas.model.TipoDocumento;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Implementación simulada del OSE para el prototipo.
 * Aplica las validaciones básicas que haría SUNAT y devuelve un CDR ficticio.
 * En producción se reemplaza por un cliente REST del proveedor (por ejemplo Nubefact)
 * usando cruzma.ose.url y cruzma.ose.token.
 */
@Service
public class OseSimuladoService implements ServicioFacturacionOSE {

    private final String urlOse;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param urlOse dirección del proveedor, propiedad {@code cruzma.ose.url}
     */
    public OseSimuladoService(@Value("${cruzma.ose.url}") String urlOse) {
        this.urlOse = urlOse;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Rechaza la factura a un cliente sin RUC, el documento inválido y el total no positivo.
     */
    @Override
    public RespuestaOSE enviar(Comprobante c) {
        Cliente cliente = c.getVenta().getCliente();
        if (c.getTipo() == TipoComprobante.FACTURA && cliente.getTipoDoc() != TipoDocumento.RUC) {
            return new RespuestaOSE(false, null, "2800: la factura requiere que el cliente tenga RUC");
        }
        if (!cliente.documentoValido()) {
            return new RespuestaOSE(false, null, "2017: el número de documento del cliente no es válido");
        }
        if (c.getTotal().signum() <= 0) {
            return new RespuestaOSE(false, null, "2638: el importe total debe ser mayor a cero");
        }
        return new RespuestaOSE(true, hash(c.getNumero() + "|" + c.getTotal()),
                "La " + c.getTipo().name().toLowerCase() + " " + c.getNumero() + " ha sido aceptada (simulado: " + urlOse + ")");
    }

    /**
     * {@inheritDoc}
     * <p>
     * En el prototipo siempre acepta la baja.
     */
    @Override
    public RespuestaOSE comunicarBaja(Comprobante c, String motivo) {
        return new RespuestaOSE(true, hash("BAJA|" + c.getNumero()), "Baja registrada: " + motivo);
    }

    /**
     * Genera un identificador de constancia simulado.
     *
     * @param texto datos del comprobante
     * @return los primeros 40 caracteres del SHA-256 en hexadecimal
     */
    private String hash(String texto) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d).substring(0, 40);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
