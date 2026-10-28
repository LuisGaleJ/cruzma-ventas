package pe.cruzma.ventas.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utilidades para tratar con seguridad los datos personales y los textos que ingresa el usuario.
 * <p>
 * Usa Apache Commons Lang. Se aplica en tres lugares: al guardar datos (normalizar),
 * al escribir logs (enmascarar DNI/RUC) y al exportar a Excel o CSV (evitar inyección de fórmulas).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
public final class DatosSeguros {

    /** Caracteres con los que Excel interpreta una celda como fórmula. */
    private static final String INICIO_DE_FORMULA = "=+-@\t\r";

    private DatosSeguros() { }

    /**
     * Quita espacios al inicio y al final y reduce los espacios repetidos a uno.
     *
     * @param texto texto ingresado por el usuario; puede ser {@code null}
     * @return texto normalizado, o {@code null} si queda vacío
     */
    public static String normalizar(String texto) {
        String limpio = StringUtils.normalizeSpace(texto);
        return StringUtils.isBlank(limpio) ? null : limpio;
    }

    /**
     * Enmascara un DNI o RUC para escribirlo en los logs sin exponer el dato completo.
     * <p>
     * Ejemplo: {@code 45871236} se registra como {@code 4587****}.
     *
     * @param documento número de documento; puede ser {@code null}
     * @return los primeros 4 dígitos y el resto con asteriscos
     */
    public static String enmascararDocumento(String documento) {
        if (StringUtils.isBlank(documento)) return "****";
        String limpio = documento.trim();
        int visibles = Math.min(4, limpio.length());
        return StringUtils.rightPad(StringUtils.left(limpio, visibles), limpio.length(), '*');
    }

    /**
     * Neutraliza un valor que se va a exportar a Excel o CSV (prevención de "CSV injection").
     * <p>
     * Si el texto empieza con {@code = + - @}, Excel lo ejecutaría como fórmula. Se le
     * antepone un apóstrofo para que se muestre como texto.
     *
     * @param valor texto a exportar; puede ser {@code null}
     * @return el valor seguro, o cadena vacía si era {@code null}
     */
    public static String celdaSegura(String valor) {
        if (valor == null) return "";
        return pareceFormula(valor) ? "'" + valor : valor;
    }

    /**
     * Indica si un texto empieza con un carácter que Excel interpretaría como fórmula.
     *
     * @param valor texto a revisar; puede ser {@code null}
     * @return {@code true} si empieza con {@code = + - @}, tabulación o retorno de carro
     */
    public static boolean pareceFormula(String valor) {
        return StringUtils.isNotEmpty(valor) && StringUtils.containsAny(valor.substring(0, 1), INICIO_DE_FORMULA);
    }
}
