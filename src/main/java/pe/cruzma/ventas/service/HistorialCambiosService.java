package pe.cruzma.ventas.service;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.dto.CambioAuditado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Consulta el historial de cambios de la base de datos (auditoría de datos).
 * <p>
 * Lee con Hibernate Envers las tablas {@code revinfo} y {@code <tabla>_aud}, y responde
 * quién creó o modificó cada registro, cuándo y qué valores cambiaron. Solo lectura.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.0
 */
@Service
public class HistorialCambiosService {

    /** Máximo de filas que muestra la pantalla. */
    static final int LIMITE = 200;

    private final EntityManager em;

    /**
     * Crea el servicio.
     *
     * @param em gestor de entidades JPA
     */
    public HistorialCambiosService(EntityManager em) { this.em = em; }

    /**
     * Devuelve los cambios más recientes primero.
     *
     * @param entidad nombre de la entidad ({@code Producto}, {@code Cliente}...), o vacío para todas
     * @param id      clave del registro, o {@code null} para todos
     * @return hasta {@value #LIMITE} cambios, del más reciente al más antiguo
     * @throws IllegalArgumentException si la entidad no está auditada
     */
    @Transactional(readOnly = true)
    public List<CambioAuditado> historial(String entidad, Long id) {
        AuditReader reader = AuditReaderFactory.get(em);
        List<CambioAuditado> cambios = new ArrayList<>();
        for (CamposAuditados.Descriptor d : CamposAuditados.filtrar(entidad)) {
            AuditQuery q = reader.createQuery().forRevisionsOfEntity(d.clase(), false, true)
                    .addOrder(AuditEntity.revisionNumber().asc());
            if (id != null) q.add(AuditEntity.id().eq(id));
            @SuppressWarnings("unchecked")
            List<Object[]> filas = q.getResultList();
            cambios.addAll(CamposAuditados.convertir(d, filas));
        }
        return cambios.stream()
                .sorted(Comparator.comparing(CambioAuditado::revision).reversed())
                .limit(LIMITE)
                .toList();
    }

    /** {@return nombres de las entidades auditadas} */
    public List<String> entidades() { return CamposAuditados.nombres(); }
}
