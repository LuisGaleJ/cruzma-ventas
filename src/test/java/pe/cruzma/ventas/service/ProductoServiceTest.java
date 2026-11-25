package pe.cruzma.ventas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.cruzma.ventas.model.Producto;
import pe.cruzma.ventas.model.TipoProducto;
import pe.cruzma.ventas.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Código automático del catálogo: PRD-### para productos y SRV-### para servicios. */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock ProductoRepository repo;
    ProductoService service;

    @BeforeEach
    void preparar() { service = new ProductoService(repo); }

    private static Producto item(Long id, String codigo, TipoProducto tipo) {
        Producto p = Fixtures.producto("Ítem " + codigo, "100.00");
        p.setId(id);
        p.setCodigo(codigo);
        p.setTipo(tipo);
        p.setUnidad(tipo == TipoProducto.SERVICIO ? "ZZ" : "NIU");   // unidades válidas de la Tabla 6
        return p;
    }

    @Test
    void siguienteCodigoEsElMayorMasUnoConTresDigitos() {
        when(repo.findByCodigoStartingWith("PRD-")).thenReturn(List.of(
                item(1L, "PRD-001", TipoProducto.PRODUCTO), item(2L, "PRD-005", TipoProducto.PRODUCTO),
                item(3L, "PRD-X", TipoProducto.PRODUCTO)));
        when(repo.findByCodigoStartingWith("SRV-")).thenReturn(List.of());

        assertEquals("PRD-006", service.siguienteCodigo(TipoProducto.PRODUCTO));
        assertEquals("SRV-001", service.siguienteCodigo(TipoProducto.SERVICIO));
    }

    @Test
    void unItemNuevoRecibeElCodigoDelServidorAunqueElFormularioEnvieOtro() {
        when(repo.findByCodigoStartingWith("SRV-")).thenReturn(List.of(item(1L, "SRV-002", TipoProducto.SERVICIO)));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Producto nuevo = item(null, "INVENTADO", TipoProducto.SERVICIO);
        assertEquals("SRV-003", service.guardar(nuevo).getCodigo());
    }

    @Test
    void alEditarSeConservaElCodigoSalvoQueCambieElTipo() {
        when(repo.findById(7L)).thenReturn(Optional.of(item(7L, "PRD-004", TipoProducto.PRODUCTO)));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("PRD-004", service.guardar(item(7L, "OTRO", TipoProducto.PRODUCTO)).getCodigo());

        when(repo.findByCodigoStartingWith("SRV-")).thenReturn(List.of(item(1L, "SRV-002", TipoProducto.SERVICIO)));
        assertEquals("SRV-003", service.guardar(item(7L, "PRD-004", TipoProducto.SERVICIO)).getCodigo());
    }

    @Test
    void noGuardaUnaUnidadFueraDeLaListaNiUnServicioSinZZ() {
        Producto conUnidadLibre = item(null, "X", TipoProducto.PRODUCTO);
        conUnidadLibre.setUnidad("UND");
        assertThrows(IllegalArgumentException.class, () -> service.guardar(conUnidadLibre));

        Producto servicioEnMetros = item(null, "X", TipoProducto.SERVICIO);
        servicioEnMetros.setUnidad("MTR");
        assertThrows(IllegalArgumentException.class, () -> service.guardar(servicioEnMetros));
        verify(repo, never()).save(any());
    }
}
