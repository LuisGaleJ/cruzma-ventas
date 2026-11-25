package pe.cruzma.ventas.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.cruzma.ventas.dto.ClienteDTO;

/**
 * Prueba de integración del DAO contra una base de datos real (H2 en memoria, modo MySQL):
 * recorre el CRUD completo con JDBC.
 */
@SpringBootTest(properties = "cruzma.demo.datos=false")
class ClienteDAOImplTest {

    @Autowired
    private ClienteDAO clienteDAO;

    @Test
    void recorreElCrudCompleto() {
        ClienteDTO nuevo = new ClienteDTO("RUC", "20612410390", "Constructora Prueba SAC");
        nuevo.setTelefono("073456789");

        Long id = clienteDAO.create(nuevo);
        assertNotNull(id);

        ClienteDTO leido = clienteDAO.read(id).orElseThrow();
        assertEquals("Constructora Prueba SAC", leido.getRazonSocial());
        assertTrue(clienteDAO.buscarPorDocumento("20612410390").isPresent());

        leido.setDireccion("Av. Grau 123, Piura");
        assertTrue(clienteDAO.update(leido));
        assertEquals("Av. Grau 123, Piura", clienteDAO.read(id).orElseThrow().getDireccion());

        assertTrue(clienteDAO.contarPorTipoDocumento("RUC") >= 1);
        assertTrue(clienteDAO.delete(id));
        assertFalse(clienteDAO.read(id).isPresent());
    }
}
