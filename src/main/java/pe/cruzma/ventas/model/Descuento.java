package pe.cruzma.ventas.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Descuentos de la cotización: por ítem y global, en porcentaje, con un tope según el rol.
 * <p>
 * Regla del negocio: el vendedor puede descontar hasta {@value #MAXIMO_VENDEDOR} % y el
 * administrador hasta {@value #MAXIMO_ADMINISTRADOR} %. El tope se aplica a cada porcentaje y
 * también al descuento total efectivo (por ejemplo, 10 % por ítem más 10 % global es 19 % efectivo).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.6.0
 */
public final class Descuento {

    /** Descuento máximo que puede aplicar un vendedor, en porcentaje. */
    public static final int MAXIMO_VENDEDOR = 10;
    /** Descuento máximo que puede aplicar un administrador, en porcentaje. */
    public static final int MAXIMO_ADMINISTRADOR = 30;

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private Descuento() { }

    /**
     * Descuento máximo permitido para un usuario.
     *
     * @param usuario usuario que cotiza
     * @return porcentaje máximo; 0 si su rol no puede dar descuentos
     */
    public static BigDecimal maximo(Usuario usuario) {
        String rol = usuario == null || usuario.getRol() == null ? "" : usuario.getRol().getNombre();
        return BigDecimal.valueOf(switch (rol) {
            case Rol.ADMIN -> MAXIMO_ADMINISTRADOR;
            case Rol.VENDEDOR -> MAXIMO_VENDEDOR;
            default -> 0;
        });
    }

    /**
     * Normaliza un porcentaje: {@code null} se toma como 0 y se dejan 2 decimales.
     *
     * @param porcentaje porcentaje escrito
     * @return porcentaje entre 0 y 100
     * @throws IllegalArgumentException si es negativo o mayor que 100
     */
    public static BigDecimal porcentaje(BigDecimal porcentaje) {
        BigDecimal p = porcentaje == null ? BigDecimal.ZERO : porcentaje.setScale(2, RoundingMode.HALF_UP);
        if (p.signum() < 0 || p.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 % y 100 %");
        }
        return p;
    }

    /**
     * Aplica un porcentaje de descuento a un monto.
     *
     * @param monto      monto con IGV
     * @param porcentaje porcentaje de descuento (0 a 100)
     * @return monto con descuento, redondeado a 2 decimales
     */
    public static BigDecimal aplicar(BigDecimal monto, BigDecimal porcentaje) {
        BigDecimal factor = CIEN.subtract(porcentaje(porcentaje)).divide(CIEN, 6, RoundingMode.HALF_UP);
        return monto.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Porcentaje efectivo de descuento entre un monto sin descuento y el monto final.
     *
     * @param bruto monto sin descuentos
     * @param neto  monto con descuentos
     * @return porcentaje efectivo con 2 decimales; 0 si el bruto es 0
     */
    public static BigDecimal efectivo(BigDecimal bruto, BigDecimal neto) {
        if (bruto.signum() == 0) return BigDecimal.ZERO;
        return bruto.subtract(neto).multiply(CIEN).divide(bruto, 2, RoundingMode.HALF_UP);
    }
}
