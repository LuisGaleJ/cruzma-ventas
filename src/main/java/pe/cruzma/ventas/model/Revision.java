package pe.cruzma.ventas.model;

import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Revisión de auditoría de datos (Hibernate Envers).
 * <p>
 * Tabla {@code revinfo}. Cada transacción que crea, modifica o elimina una entidad
 * auditada genera una fila aquí con <b>quién</b> hizo el cambio y <b>cuándo</b>; los
 * valores de cada registro en esa revisión quedan en las tablas {@code <tabla>_aud}.
 * El usuario lo completa {@link RevisionUsuarioListener}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.0
 */
@Entity
@Table(name = "revinfo")
@RevisionEntity(RevisionUsuarioListener.class)
public class Revision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "id_revision")
    private Long id;

    /** java.util.Date es el tipo de marca de tiempo que Envers admite en todas sus versiones. */
    @RevisionTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date fecha;

    @Column(nullable = false, length = 50)
    private String usuario;

    /** Constructor requerido por JPA. */
    public Revision() { }

    /** {@return número de revisión, correlativo y creciente} */
    public Long getId() { return id; }

    /**
     * Asigna el número de revisión.
     *
     * @param id número de revisión
     */
    public void setId(Long id) { this.id = id; }

    /** {@return fecha y hora en que se confirmó el cambio} */
    public LocalDateTime getFecha() {
        return fecha == null ? null : LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Asigna la fecha del cambio (Envers la completa automáticamente).
     *
     * @param fecha fecha y hora del cambio
     */
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha == null ? null : Date.from(fecha.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** {@return usuario que hizo el cambio, o {@code "sistema"} para procesos internos} */
    public String getUsuario() { return usuario; }

    /**
     * Asigna el usuario que hizo el cambio.
     *
     * @param usuario nombre de usuario
     */
    public void setUsuario(String usuario) { this.usuario = usuario; }
}
