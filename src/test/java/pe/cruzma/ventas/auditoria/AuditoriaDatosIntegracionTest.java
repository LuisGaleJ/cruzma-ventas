package pe.cruzma.ventas.auditoria;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.cruzma.ventas.dto.CambioAuditado;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.repository.ProductoRepository;
import pe.cruzma.ventas.service.HistorialCambiosService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración con H2: Envers guarda quién creó y modificó un producto,
 * cuándo y qué cambió. Valida también el mapeo de las tablas {@code *_aud}.
 */
@SpringBootTest
class AuditoriaDatosIntegracionTest {

    @Autowired ProductoRepository productos;
    @Autowired HistorialCambiosService historial;

    private static void sesion(String usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    @AfterEach
    void cerrarSesion() { SecurityContextHolder.clearContext(); }

    @Test
    void registraUsuarioFechaYValoresDeCadaCambio() {
        sesion("admin");
        Producto p = new Producto();
        p.setCodigo("AUD-001");
        p.setDescripcion("Reja de prueba de auditoría");
        p.setUnidad("UND");
        p.setTipo(TipoProducto.PRODUCTO);
        p.setPrecioUnitario(new BigDecimal("250.00"));
        p = productos.save(p);

        sesion("vendedor");
        p.setPrecioUnitario(new BigDecimal("275.00"));
        productos.save(p);

        List<CambioAuditado> cambios = historial.historial("Producto", p.getId());
        assertEquals(2, cambios.size());

        CambioAuditado ultimo = cambios.get(0);
        assertEquals("Modificación", ultimo.operacion());
        assertEquals("vendedor", ultimo.usuario());
        assertNotNull(ultimo.fecha());
        assertEquals(List.of("Precio unitario: 250.00 → 275.00"), ultimo.detalles());

        CambioAuditado primero = cambios.get(1);
        assertEquals("Creación", primero.operacion());
        assertEquals("admin", primero.usuario());
        assertTrue(ultimo.revision() > primero.revision());
    }
}
