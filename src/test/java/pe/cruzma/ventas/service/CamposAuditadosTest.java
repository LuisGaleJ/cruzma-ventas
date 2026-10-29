package pe.cruzma.ventas.service;

import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;
import pe.cruzma.ventas.dto.CambioAuditado;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.Revision;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas de la conversión de revisiones de Envers en cambios legibles. */
class CamposAuditadosTest {

    private static Revision rev(long id, String usuario) {
        Revision r = new Revision();
        r.setId(id);
        r.setUsuario(usuario);
        r.setFecha(LocalDateTime.of(2026, 10, 28, 10, 0).plusMinutes(id));
        return r;
    }

    private static Producto producto(String precio) {
        Producto p = Fixtures.producto("Puerta de acero", precio);
        p.setId(5L);
        return p;
    }

    @Test
    void creacionListaValoresInicialesYModificacionSoloLoQueCambio() {
        CamposAuditados.Descriptor d = CamposAuditados.filtrar("Producto").get(0);
        List<CambioAuditado> cambios = CamposAuditados.convertir(d, List.of(
                new Object[]{producto("100.00"), rev(1, "admin"), RevisionType.ADD},
                new Object[]{producto("120.50"), rev(2, "vendedor"), RevisionType.MOD}));

        CambioAuditado alta = cambios.get(0);
        assertEquals("Creación", alta.operacion());
        assertEquals("admin", alta.usuario());
        assertTrue(alta.detalles().contains("Precio unitario: 100.00"));

        CambioAuditado mod = cambios.get(1);
        assertEquals("Modificación", mod.operacion());
        assertEquals("vendedor", mod.usuario());
        assertEquals(List.of("Precio unitario: 100.00 → 120.50"), mod.detalles());
        assertEquals(5L, mod.idRegistro());
    }

    @Test
    void mismoImporteConOtraEscalaNoCuentaComoCambio() {
        CamposAuditados.Descriptor d = CamposAuditados.filtrar("Producto").get(0);
        List<CambioAuditado> cambios = CamposAuditados.convertir(d, List.of(
                new Object[]{producto("100.00"), rev(1, "admin"), RevisionType.ADD},
                new Object[]{producto("100.0"), rev(2, "admin"), RevisionType.MOD}));
        assertEquals(List.of("Cambio en datos relacionados"), cambios.get(1).detalles());
    }

    @Test
    void filtroVacioDevuelveTodasYNombreDesconocidoSeRechaza() {
        assertEquals(7, CamposAuditados.filtrar("").size());
        assertTrue(CamposAuditados.nombres().contains("Comprobante"));
        assertThrows(IllegalArgumentException.class, () -> CamposAuditados.filtrar("Rol"));
    }
}
