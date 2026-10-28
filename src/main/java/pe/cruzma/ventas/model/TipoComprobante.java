package pe.cruzma.ventas.model;

/** Tipos de comprobante con su código del catálogo 01 de SUNAT. */
public enum TipoComprobante {
    /** Factura electrónica (01), serie F001; solo para clientes con RUC. */
    FACTURA("01", "F001"),

    /** Boleta de venta electrónica (03), serie B001. */
    BOLETA("03", "B001"),

    /** Nota de crédito electrónica (07), serie FC01; anula un comprobante. */
    NOTA_CREDITO("07", "FC01");

    private final String codigo;
    private final String serie;

    TipoComprobante(String codigo, String serie) {
        this.codigo = codigo;
        this.serie = serie;
    }

    /** {@return código interno} */
    public String getCodigo() { return codigo; }
    /** {@return serie (F001, B001 o FC01)} */
    public String getSerie() { return serie; }

    /**
     * Obtiene el tipo a partir de su código de SUNAT.
     *
     * @param codigo {@code 01}, {@code 03} o {@code 07}
     * @return el tipo correspondiente
     * @throws IllegalArgumentException si el código no existe
     */
    public static TipoComprobante desdeCodigo(String codigo) {
        for (TipoComprobante t : values()) {
            if (t.codigo.equals(codigo)) return t;
        }
        throw new IllegalArgumentException("Código de comprobante desconocido: " + codigo);
    }
}
