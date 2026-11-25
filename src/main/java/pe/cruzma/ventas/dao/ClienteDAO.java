package pe.cruzma.ventas.dao;

import java.util.List;
import java.util.Optional;
import pe.cruzma.ventas.dto.ClienteDTO;

/**
 * DAO de la tabla {@code cliente}: CRUD genérico más consultas propias del negocio.
 *
 * @author Grupo Y - ADSI
 */
public interface ClienteDAO extends DataAccessObject<ClienteDTO, Long> {

    /** {@return todos los clientes ordenados por razón social} */
    List<ClienteDTO> findAll();

    /**
     * Busca un cliente por su número de DNI o RUC (CUS13 Buscar Cliente).
     *
     * @param numDoc número de documento
     * @return el cliente, o vacío si no está registrado
     */
    Optional<ClienteDTO> buscarPorDocumento(String numDoc);

    /**
     * Cuenta los clientes de un tipo de documento (para el reporte de cartera).
     *
     * @param tipoDoc {@code DNI} o {@code RUC}
     * @return cantidad de clientes
     */
    int contarPorTipoDocumento(String tipoDoc);
}
