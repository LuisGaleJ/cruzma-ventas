package pe.cruzma.ventas.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fila de la pantalla Historial de cambios: un registro en una revisión.
 *
 * @param revision  número de revisión ({@code revinfo.id_revision})
 * @param fecha     fecha y hora del cambio
 * @param usuario   usuario que hizo el cambio
 * @param entidad   tipo de registro, por ejemplo {@code Producto}
 * @param idRegistro clave primaria del registro afectado
 * @param titulo    texto que identifica al registro, por ejemplo su código
 * @param operacion {@code Creación}, {@code Modificación} o {@code Eliminación}
 * @param detalles  campos con su valor anterior y nuevo
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.0
 */
public record CambioAuditado(Long revision, LocalDateTime fecha, String usuario, String entidad,
                             Object idRegistro, String titulo, String operacion, List<String> detalles) {
}
