package pe.cruzma.ventas.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Registro de auditoría de las operaciones sensibles (Logback).
 * <p>
 * Escribe en el logger {@code AUDITORIA}, que {@code logback-spring.xml} envía al archivo
 * {@code logs/auditoria.log}. Se registra quién hizo qué, nunca contraseñas ni documentos completos.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.3.0
 */
public final class Auditoria {

    private static final Logger LOG = LoggerFactory.getLogger("AUDITORIA");

    private Auditoria() { }

    /**
     * Registra una operación.
     *
     * @param usuario usuario que la realiza, o {@code "sistema"}
     * @param accion  acción en mayúsculas, por ejemplo {@code EMITIR_COMPROBANTE}
     * @param detalle detalle sin datos sensibles
     */
    public static void registrar(String usuario, String accion, String detalle) {
        LOG.info("usuario={} accion={} detalle={}", usuario, accion, detalle);
    }

    /**
     * Registra una operación hecha por el usuario que tiene la sesión abierta.
     *
     * @param accion  acción en mayúsculas
     * @param detalle detalle sin datos sensibles
     */
    public static void registrar(String accion, String detalle) {
        registrar(usuarioActual(), accion, detalle);
    }

    /** {@return nombre del usuario autenticado, o {@code "sistema"} si no hay sesión} */
    static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "sistema" : auth.getName();
    }
}
