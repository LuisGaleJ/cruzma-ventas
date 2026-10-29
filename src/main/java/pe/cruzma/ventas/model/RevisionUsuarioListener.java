package pe.cruzma.ventas.model;

import org.hibernate.envers.RevisionListener;
import pe.cruzma.ventas.util.Auditoria;

/**
 * Completa cada {@link Revision} con el usuario que tiene la sesión abierta.
 * <p>
 * Envers lo invoca al crear la revisión, dentro de la misma transacción del cambio,
 * así que el usuario y los datos modificados se guardan juntos o no se guardan.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.0
 */
public class RevisionUsuarioListener implements RevisionListener {

    /** Constructor usado por Hibernate Envers. */
    public RevisionUsuarioListener() { }

    /**
     * Registra el usuario actual en la revisión nueva.
     *
     * @param revisionEntity revisión que Envers está por guardar
     */
    @Override
    public void newRevision(Object revisionEntity) {
        ((Revision) revisionEntity).setUsuario(Auditoria.usuarioActual());
    }
}
