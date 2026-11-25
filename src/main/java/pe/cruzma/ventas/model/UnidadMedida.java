package pe.cruzma.ventas.model;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

/**
 * Unidades de medida de SUNAT (Tabla 6 del Anexo 3 de los Libros Electrónicos, PLE: «Código de la unidad de medida»)
 * que corresponden al giro de Grupo Cruzma. Los productos guardan el código oficial ({@code NIU}, {@code MTR},
 * {@code MTK}, {@code ZZ}...) elegido de una lista desplegable, de modo que el usuario no pueda escribir una unidad
 * inexistente ni una que la empresa no usa.
 * <p>
 * Reglas: un SERVICIO usa siempre {@code ZZ} (unidad de servicios) y un PRODUCTO cualquier otra unidad de la tabla.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.7.0
 */
public final class UnidadMedida {

    /** Unidad (bienes). */
    public static final String UNIDAD = "NIU";
    /** Unidad (servicios). */
    public static final String SERVICIO = "ZZ";
    /** Metro (lineal). */
    public static final String METRO = "MTR";
    /** Metro cuadrado. */
    public static final String METRO_CUADRADO = "MTK";
    /** Metro cúbico. */
    public static final String METRO_CUBICO = "MTQ";

    /**
     * Unidades de la Tabla 6 que usa Grupo Cruzma, con la descripción oficial de SUNAT. Se eligieron según los giros
     * del RUC: CIIU 2511 (fabricación de productos metálicos para uso estructural) y CIIU 4390 (otras actividades
     * especializadas de construcción). Para agregar otra, copiar su código y descripción de la Tabla 6.
     */
    private static final ImmutableMap<String, String> UNIDADES = ImmutableMap.<String, String>builder()
            .put("NIU", "UNIDAD (BIENES)")   // bienes por pieza terminada: puertas, rejas, mesas, campanas
            .put("MTR", "METRO")   // barandas, pasamanos, tuberías, cercos lineales
            .put("MTK", "METRO CUADRADO")   // rejas, cerramientos, techos, coberturas, planchas instaladas
            .put("MTQ", "METRO CUBICO")   // trabajos de obra civil por volumen
            .put("KGM", "KILOGRAMO")   // estructuras y perfiles metálicos vendidos por peso
            .put("TNE", "TONELADAS")   // estructuras metálicas grandes (naves, tijerales) por peso
            .put("C62", "PIEZAS")   // piezas sueltas: platinas, ángulos, accesorios
            .put("PG", "PLACAS")   // planchas y placas metálicas
            .put("TU", "TUBOS")   // tubos enteros
            .put("SET", "JUEGO")   // juegos: chapas, kits de bisagras, herrajes
            .put("PR", "PAR")   // pares: bisagras, manijas
            .put("KT", "KIT")   // kits de instalación
            .put("BX", "CAJA")   // cajas: pernos, tornillos, electrodos
            .put("PK", "PAQUETE")   // paquetes de accesorios
            .put("GLL", "US GALON (3,7843 L)")   // pintura y anticorrosivo por galón
            .put("LTR", "LITRO")   // pintura, thinner y otros líquidos por litro
            .put("ZZ", "UNIDAD (SERVICIOS)")   // servicios: instalación, mantenimiento, soldadura en obra
            .build();

    /** Abreviaturas para mostrar junto a cantidades y medidas (por ejemplo «2.88 m2 por unidad»). */
    private static final ImmutableMap<String, String> SIMBOLOS = ImmutableMap.<String, String>builder()
            .put(UNIDAD, "und").put(SERVICIO, "serv").put(METRO, "m").put(METRO_CUADRADO, "m2").put(METRO_CUBICO, "m3")
            .put("KGM", "kg").put("TNE", "t").put("C62", "pza").put("PG", "placa").put("TU", "tubo").put("SET", "juego")
            .put("PR", "par").put("KT", "kit").put("BX", "caja").put("PK", "paquete").put("GLL", "gal").put("LTR", "L")
            .build();

    /** Unidades escritas a mano antes de usar la Tabla 6 y su código SUNAT equivalente. */
    private static final ImmutableMap<String, String> ANTERIORES = ImmutableMap.<String, String>builder()
            .put("UND", UNIDAD).put("UNID", UNIDAD).put("M", METRO).put("ML", METRO).put("M2", METRO_CUADRADO)
            .put("M3", METRO_CUBICO).put("SERV", SERVICIO).put("KG", "KGM").put("JGO", "SET").put("JUEGO", "SET").put("PZA", "C62")
            .put("GL", "GLL").put("GAL", "GLL").put("LT", "LTR")
            .build();

    private UnidadMedida() { }

    /**
     * Indica si el código es una de las unidades disponibles.
     *
     * @param codigo código de unidad (ej. {@code NIU})
     * @return {@code true} si está en la lista de unidades de Grupo Cruzma
     */
    public static boolean existe(String codigo) {
        return codigo != null && UNIDADES.containsKey(codigo);
    }

    /**
     * Descripción oficial de la unidad.
     *
     * @param codigo código de unidad
     * @return descripción de SUNAT (ej. «METRO CUADRADO»), o el mismo código si no está en la tabla
     */
    public static String descripcion(String codigo) {
        return codigo == null ? "" : UNIDADES.getOrDefault(codigo, codigo);
    }

    /**
     * Abreviatura legible de la unidad, para textos como «= 2.88 m2 por unidad».
     *
     * @param codigo código de unidad (también acepta las unidades anteriores, como {@code M2})
     * @return abreviatura (ej. {@code m2}); si no tiene, el código en minúsculas
     */
    public static String simbolo(String codigo) {
        if (codigo == null) return "";
        String c = normalizar(codigo);
        return SIMBOLOS.getOrDefault(c, c.toLowerCase());
    }

    /**
     * Convierte una unidad escrita con el formato anterior ({@code UND}, {@code M2}, {@code SERV}...) a su código
     * de la Tabla 6; los códigos que ya son de SUNAT se devuelven igual.
     *
     * @param unidad unidad guardada
     * @return código SUNAT equivalente, o la unidad en mayúsculas si no tiene equivalencia
     */
    public static String normalizar(String unidad) {
        if (unidad == null) return "";
        String u = unidad.trim().toUpperCase().replace("²", "2").replace("³", "3");
        return ANTERIORES.getOrDefault(u, u);
    }

    /**
     * Valida la unidad según el tipo de ítem.
     *
     * @param tipo   PRODUCTO o SERVICIO
     * @param codigo código elegido
     * @return mensaje de error para el usuario, o {@code null} si la unidad es válida
     */
    public static String validar(TipoProducto tipo, String codigo) {
        if (codigo == null || codigo.isBlank()) return "Elige la unidad de medida";
        if (!existe(codigo)) return "Elige una unidad de la lista";
        if (tipo == TipoProducto.SERVICIO && !SERVICIO.equals(codigo)) return "Un servicio usa la unidad ZZ - UNIDAD (SERVICIOS)";
        if (tipo == TipoProducto.PRODUCTO && SERVICIO.equals(codigo)) return "ZZ es solo para servicios: elige la unidad del producto";
        return null;
    }

    /** {@return unidades disponibles (código y descripción de SUNAT), en el orden en que se muestran} */
    public static List<Map.Entry<String, String>> todas() {
        return List.copyOf(UNIDADES.entrySet());
    }

    /** {@return cantidad de unidades del catálogo} */
    public static int total() { return UNIDADES.size(); }
}
