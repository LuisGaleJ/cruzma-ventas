package pe.cruzma.ventas.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Enlaces de WhatsApp ({@code https://wa.me/...}) con un mensaje ya escrito.
 * <p>
 * El enlace abre WhatsApp (en la PC o en el celular) con el chat del cliente y el texto listo; el vendedor solo
 * presiona Enviar. No usa ninguna API ni cuenta pagada: es el enlace público de WhatsApp.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.7.0
 */
public final class WhatsApp {
    /** Código de país de Perú. */
    private static final String PERU = "51";

    private WhatsApp() { }

    /**
     * Número en formato internacional para WhatsApp.
     *
     * @param telefono teléfono del cliente (solo se usan sus dígitos)
     * @return {@code 51} + celular de 9 dígitos (que empieza con 9), o {@code null} si no es un celular peruano
     */
    public static String numero(String telefono) {
        if (telefono == null) return null;
        String d = telefono.replaceAll("\\D", "");
        if (d.length() == 11 && d.startsWith(PERU + "9")) return d;
        if (d.length() == 9 && d.startsWith("9")) return PERU + d;
        return null;                                   // fijo o incompleto: WhatsApp no aplica
    }

    /**
     * Enlace {@code wa.me} con el mensaje.
     *
     * @param telefono teléfono del cliente
     * @param mensaje  texto del mensaje
     * @return enlace, o {@code null} si el teléfono no es un celular peruano
     */
    public static String enlace(String telefono, String mensaje) {
        String n = numero(telefono);
        if (n == null) return null;
        String texto = URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + n + "?text=" + texto;
    }
}
