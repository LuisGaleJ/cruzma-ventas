package pe.cruzma.ventas.service.exportacion;

import pe.cruzma.ventas.dto.DatosReporte;

/**
 * Formato de exportación de los reportes (RF14).
 * <p>
 * Aplica el principio abierto/cerrado (SOLID): para agregar un formato nuevo, por ejemplo PDF,
 * se crea otra clase que implemente esta interfaz y Spring la registra sola; el controlador no cambia.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
public interface ExportadorReporte {

    /** {@return identificador del formato en la URL, por ejemplo {@code xlsx} o {@code csv}} */
    String formato();

    /** {@return tipo de contenido HTTP del archivo generado} */
    String tipoContenido();

    /**
     * Genera el archivo.
     *
     * @param datos reportes del período
     * @return contenido del archivo
     */
    byte[] exportar(DatosReporte datos);
}
