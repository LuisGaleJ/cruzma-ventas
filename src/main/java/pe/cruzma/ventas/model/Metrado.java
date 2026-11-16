package pe.cruzma.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metrado de un ítem cotizado: cuántos metros o metros cuadrados se cobran por unidad.
 * <p>
 * Según la unidad del producto:
 * <ul>
 *   <li>{@code M2}: el área, escrita directa ({@code 5}) o como largo x alto ({@code 3.20 x 0.90}).</li>
 *   <li>{@code M} o {@code ML}: la longitud ({@code 4.5}).</li>
 *   <li>{@code M3}: el volumen, directo o como largo x ancho x alto.</li>
 *   <li>Cualquier otra unidad ({@code UND}, {@code SERV}...): las medidas son solo descriptivas y el metrado es 1.</li>
 * </ul>
 * El subtotal del ítem es precio x cantidad x metrado.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.1
 */
public final class Metrado {

    private static final Set<String> LINEALES = Set.of("M", "ML");
    private static final Pattern NUMERO = Pattern.compile("\\d+(?:[.,]\\d+)?");

    private Metrado() { }

    /**
     * Indica si la unidad se cobra por medida.
     *
     * @param unidad unidad del producto
     * @return {@code true} para M, ML, M2 y M3
     */
    public static boolean seCobraPorMedida(String unidad) {
        return dimensiones(unidad) > 0;
    }

    /**
     * Calcula el metrado a partir de las medidas escritas.
     *
     * @param unidad  unidad del producto (M2, M, ML, M3, UND...)
     * @param medidas texto escrito en la cotización, por ejemplo {@code "3.20 x 0.90"}
     * @return metrado mayor a cero, con hasta 4 decimales; 1 si la unidad no se cobra por medida
     * @throws IllegalArgumentException si la unidad se cobra por medida y las medidas no son válidas
     */
    public static BigDecimal calcular(String unidad, String medidas) {
        int dims = dimensiones(unidad);
        if (dims == 0) return BigDecimal.ONE;
        List<BigDecimal> numeros = numeros(medidas);
        if (numeros.isEmpty() || (numeros.size() != 1 && numeros.size() != dims)) {
            throw new IllegalArgumentException(ayuda(unidad));
        }
        BigDecimal resultado = BigDecimal.ONE;
        for (BigDecimal n : numeros) resultado = resultado.multiply(n);
        if (resultado.signum() <= 0) throw new IllegalArgumentException(ayuda(unidad));
        return resultado.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /**
     * Texto de ayuda con el formato esperado para una unidad.
     *
     * @param unidad unidad del producto
     * @return ejemplo de cómo escribir las medidas
     */
    public static String ayuda(String unidad) {
        return switch (dimensiones(unidad)) {
            case 1 -> "Para unidad " + normalizar(unidad) + " indica la longitud en metros (ej. 4.5)";
            case 2 -> "Para unidad M2 indica el área (ej. 5) o largo x alto en metros (ej. 3.20 x 0.90)";
            case 3 -> "Para unidad M3 indica el volumen (ej. 2) o largo x ancho x alto en metros (ej. 2 x 1 x 0.5)";
            default -> "Las medidas son opcionales para esta unidad";
        };
    }

    /** Cantidad de medidas que se multiplican: 0 si la unidad no se cobra por medida. */
    private static int dimensiones(String unidad) {
        String u = normalizar(unidad);
        if (LINEALES.contains(u)) return 1;
        if (u.equals("M2")) return 2;
        if (u.equals("M3")) return 3;
        return 0;
    }

    private static String normalizar(String unidad) {
        return unidad == null ? "" : unidad.trim().toUpperCase().replace("²", "2").replace("³", "3");
    }

    /** Extrae los números del texto (acepta coma o punto decimal) ignorando "m", "x", "por", etc. */
    private static List<BigDecimal> numeros(String medidas) {
        List<BigDecimal> lista = new ArrayList<>();
        if (medidas == null) return lista;
        // "m2" y "m3" son unidades, no medidas: se quitan antes de buscar números
        String limpio = medidas.toLowerCase().replaceAll("m[23²³]", " ");
        Matcher m = NUMERO.matcher(limpio);
        while (m.find()) lista.add(new BigDecimal(m.group().replace(',', '.')));
        return lista;
    }
}
