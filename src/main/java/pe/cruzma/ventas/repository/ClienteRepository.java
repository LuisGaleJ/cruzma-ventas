package pe.cruzma.ventas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.cruzma.ventas.model.Cliente;

import java.util.List;

/**
 * Acceso a la tabla {@code cliente}.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    /**
     * Busca clientes por parte del DNI, RUC o nombre, sin distinguir mayúsculas (RF03).
     *
     * @param q texto a buscar
     * @return clientes encontrados, ordenados por nombre
     */
    @Query("select c from Cliente c where c.numDoc like concat('%', :q, '%') "
         + "or lower(c.razonSocial) like lower(concat('%', :q, '%')) order by c.razonSocial")
    List<Cliente> buscar(@Param("q") String q);

    /**
     * Indica si ya hay un cliente con ese DNI o RUC.
     *
     * @param numDoc número de documento
     * @return {@code true} si ya está registrado
     */
    boolean existsByNumDoc(String numDoc);
}
