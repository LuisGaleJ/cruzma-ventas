package pe.cruzma.ventas.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Accesos rápidos del filtro de fechas del Dashboard.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.7.0
 */
public enum Periodo {
    /** Solo el día de hoy. */
    HOY("Hoy"),
    /** El día anterior. */
    AYER("Ayer"),
    /** De lunes a hoy. */
    SEMANA("Esta semana"),
    /** Del 1 del mes a hoy. */
    MES("Este mes"),
    /** Todo el mes anterior. */
    MES_ANTERIOR("Mes anterior"),
    /** Del 1 de enero a hoy. */
    ANIO("Este año");

    private final String etiqueta;

    Periodo(String etiqueta) { this.etiqueta = etiqueta; }

    /** {@return texto del botón} */
    public String getEtiqueta() { return etiqueta; }

    /**
     * Primer día del período.
     *
     * @param hoy fecha de referencia
     * @return primer día, inclusive
     */
    public LocalDate desde(LocalDate hoy) {
        return switch (this) {
            case HOY -> hoy;
            case AYER -> hoy.minusDays(1);
            case SEMANA -> hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> hoy.withDayOfMonth(1);
            case MES_ANTERIOR -> hoy.minusMonths(1).withDayOfMonth(1);
            case ANIO -> hoy.withDayOfYear(1);
        };
    }

    /**
     * Último día del período.
     *
     * @param hoy fecha de referencia
     * @return último día, inclusive
     */
    public LocalDate hasta(LocalDate hoy) {
        return switch (this) {
            case AYER -> hoy.minusDays(1);
            case MES_ANTERIOR -> hoy.withDayOfMonth(1).minusDays(1);
            default -> hoy;
        };
    }
}
