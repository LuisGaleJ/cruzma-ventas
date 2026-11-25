package pe.cruzma.ventas.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pe.cruzma.ventas.dao.ClienteDAO;
import pe.cruzma.ventas.dto.ClienteDTO;

/**
 * Clase <b>cliente</b> del patrón DAO: usa el {@link ClienteDAO} a través de su interfaz,
 * sin saber si los datos vienen de MySQL, de H2 o de otra fuente.
 * <p>
 * Aplica las reglas del caso de uso Gestionar Clientes (CUS02) antes de pedir al DAO
 * que guarde: documento válido según su tipo y documento no registrado.
 *
 * @author Grupo Y - ADSI
 */
@Service
public class CarteraClientesService {

    private final ClienteDAO clienteDAO;

    /**
     * Crea el servicio con el DAO de clientes (inyección por constructor).
     *
     * @param clienteDAO DAO de la tabla cliente
     */
    public CarteraClientesService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    /**
     * Registra un cliente nuevo.
     *
     * @param dto datos del cliente
     * @return identificador asignado
     * @throws IllegalArgumentException si el documento no es válido o ya existe
     */
    public Long registrar(ClienteDTO dto) {
        if (!documentoValido(dto.getTipoDoc(), dto.getNumDoc())) {
            throw new IllegalArgumentException("El DNI tiene 8 dígitos y el RUC 11 (empieza en 10, 15, 17 o 20)");
        }
        if (clienteDAO.buscarPorDocumento(dto.getNumDoc()).isPresent()) {
            throw new IllegalArgumentException("El cliente ya existe");
        }
        return clienteDAO.create(dto);
    }

    /**
     * Busca un cliente por su documento (CUS13 Buscar Cliente).
     *
     * @param numDoc número de DNI o RUC
     * @return el cliente si está registrado
     */
    public Optional<ClienteDTO> buscar(String numDoc) {
        return clienteDAO.buscarPorDocumento(numDoc);
    }

    /** {@return la cartera completa de clientes} */
    public List<ClienteDTO> listarCartera() {
        return clienteDAO.findAll();
    }

    /**
     * Actualiza los datos de contacto de un cliente.
     *
     * @param dto datos con el identificador
     * @return {@code true} si se actualizó
     */
    public boolean actualizar(ClienteDTO dto) {
        return clienteDAO.update(dto);
    }

    /** {@return resumen de la cartera: clientes con DNI y con RUC} */
    public String resumen() {
        return "Personas (DNI): " + clienteDAO.contarPorTipoDocumento("DNI")
                + " | Empresas (RUC): " + clienteDAO.contarPorTipoDocumento("RUC");
    }

    private static boolean documentoValido(String tipo, String numero) {
        if (tipo == null || numero == null) return false;
        return "DNI".equals(tipo) ? numero.matches("\\d{8}") : numero.matches("(10|15|17|20)\\d{9}");
    }
}
