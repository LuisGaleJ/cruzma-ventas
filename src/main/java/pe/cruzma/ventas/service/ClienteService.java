package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.Cliente;
import pe.cruzma.ventas.repository.ClienteRepository;
import pe.cruzma.ventas.util.DatosSeguros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Reglas de negocio del registro de clientes (RF03).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class ClienteService {
    private static final Logger LOG = LoggerFactory.getLogger(ClienteService.class);
    private final ClienteRepository repo;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param repo repositorio de clientes
     */
    public ClienteService(ClienteRepository repo) { this.repo = repo; }

    /**
     * Busca clientes por DNI, RUC o nombre.
     *
     * @param q texto a buscar; vacío o {@code null} devuelve todos
     * @return clientes encontrados
     */
    public List<Cliente> buscar(String q) {
        return (q == null || q.isBlank()) ? repo.findAll() : repo.buscar(q.trim());
    }

    /**
     * Obtiene un cliente por su id.
     *
     * @param id id del cliente
     * @return el cliente
     * @throws IllegalArgumentException si no existe
     */
    public Cliente obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    /**
     * Registra o actualiza un cliente.
     *
     * @param c cliente del formulario
     * @return el cliente guardado
     * @throws IllegalArgumentException si el documento no es válido para su tipo
     *         o ya existe otro cliente con el mismo documento
     */
    @Transactional
    public Cliente guardar(Cliente c) {
        c.setNumDoc(DatosSeguros.normalizar(c.getNumDoc()));
        c.setRazonSocial(DatosSeguros.normalizar(c.getRazonSocial()));
        c.setDireccion(DatosSeguros.normalizar(c.getDireccion()));
        c.setTelefono(DatosSeguros.normalizar(c.getTelefono()));
        c.setEmail(DatosSeguros.normalizar(c.getEmail()));
        if (!c.documentoValido()) {
            throw new IllegalArgumentException("El documento no coincide con el tipo: DNI de 8 dígitos o RUC de 11 que empiece en 10, 15, 17 o 20");
        }
        if (c.getId() == null && repo.existsByNumDoc(c.getNumDoc())) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento " + c.getNumDoc());
        }
        Cliente guardado = repo.save(c);
        LOG.info("Cliente guardado: {} {}", c.getTipoDoc(), DatosSeguros.enmascararDocumento(c.getNumDoc()));
        return guardado;
    }
}
