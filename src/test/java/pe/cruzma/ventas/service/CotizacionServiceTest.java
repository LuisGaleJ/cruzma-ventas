package pe.cruzma.ventas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.cruzma.ventas.dto.ItemCotizacionForm;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.ClienteRepository;
import pe.cruzma.ventas.repository.CotizacionRepository;
import pe.cruzma.ventas.repository.ProductoRepository;
import pe.cruzma.ventas.repository.VentaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas de RF05 (registrar cotización) y RF07 (convertir en venta) con repositorios simulados. */
@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock CotizacionRepository cotizaciones;
    @Mock ClienteRepository clientes;
    @Mock ProductoRepository productos;
    @Mock VentaRepository ventas;
    CotizacionService service;
    final Cliente cliente = Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz");

    @BeforeEach
    void preparar() {
        service = new CotizacionService(cotizaciones, clientes, productos, ventas);
    }

    @Test
    void creaCotizacionConPrecioDeCatalogoYVence15DiasDespues() {
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(productos.findById(5L)).thenReturn(Optional.of(Fixtures.producto("Reja de fierro", "180.00")));
        when(cotizaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cotizacion c = service.crear(1L, List.of(new ItemCotizacionForm(5L, 2, "1 x 1 m", null)), Fixtures.usuario("vendedor"));

        assertEquals(new BigDecimal("360.00"), c.getTotal());
        assertEquals(c.getFecha().plusDays(CotizacionService.DIAS_VALIDEZ), c.getFechaVencimiento());
        assertEquals(EstadoCotizacion.PENDIENTE, c.getEstado());
    }

    private static Usuario conRol(String username, String rol) {
        Usuario u = Fixtures.usuario(username);
        u.setRol(new Rol(rol));
        return u;
    }

    @Test
    void aplicaDescuentoPorItemYGlobalDentroDelMaximoDelRol() {
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(productos.findById(5L)).thenReturn(Optional.of(Fixtures.producto("Reja de fierro", "100.00")));
        when(cotizaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // 2 x 100 = 200; 5 % en el ítem = 190; 5 % global = 180.50 (efectivo 9.75 %)
        Cotizacion c = service.crear(1L, List.of(new ItemCotizacionForm(5L, 2, null, null, new BigDecimal("5"))),
                conRol("vendedor", Rol.VENDEDOR), new BigDecimal("5"));

        assertEquals(0, new BigDecimal("180.50").compareTo(c.getTotal()));
        assertEquals(0, new BigDecimal("9.75").compareTo(c.getDescuentoEfectivo()));
    }

    @Test
    void elVendedorNoPasaDel10PorCientoNiSumandoItemYGlobal() {
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(productos.findById(5L)).thenReturn(Optional.of(Fixtures.producto("Reja de fierro", "100.00")));
        Usuario vendedor = conRol("vendedor", Rol.VENDEDOR);

        assertThrows(IllegalArgumentException.class, () -> service.crear(1L,
                List.of(new ItemCotizacionForm(5L, 1, null, null, new BigDecimal("15"))), vendedor, null));
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L,
                List.of(new ItemCotizacionForm(5L, 1, null, null)), vendedor, new BigDecimal("12")));
        // 10 % + 10 % = 19 % efectivo: supera el 10 %
        assertThrows(IllegalArgumentException.class, () -> service.crear(1L,
                List.of(new ItemCotizacionForm(5L, 1, null, null, BigDecimal.TEN)), vendedor, BigDecimal.TEN));
        verify(cotizaciones, never()).save(any());
    }

    @Test
    void elAdministradorPuedeDarHasta30PorCiento() {
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(productos.findById(5L)).thenReturn(Optional.of(Fixtures.producto("Reja de fierro", "100.00")));
        when(cotizaciones.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Cotizacion c = service.crear(1L, List.of(new ItemCotizacionForm(5L, 1, null, null)),
                conRol("admin", Rol.ADMIN), new BigDecimal("25"));
        assertEquals(0, new BigDecimal("75.00").compareTo(c.getTotal()));
    }

    @Test
    void rechazaCotizacionSinItems() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(1L, List.of(new ItemCotizacionForm(null, 1, null, null)), Fixtures.usuario("vendedor")));
    }

    @Test
    void convierteCotizacionVigenteEnVenta() {
        Cotizacion cot = Fixtures.cotizacion(cliente, 5);
        when(cotizaciones.findById(3L)).thenReturn(Optional.of(cot));
        when(ventas.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Venta v = service.convertirAVenta(3L, Fixtures.usuario("vendedor"));

        assertEquals(EstadoCotizacion.APROBADA, cot.getEstado());
        assertEquals(cot.getTotal(), v.getSaldo());
        assertEquals(1, v.getDetalles().size());
    }

    @Test
    void noConvierteCotizacionVencida() {
        when(cotizaciones.findById(3L)).thenReturn(Optional.of(Fixtures.cotizacion(cliente, -1)));
        assertThrows(IllegalStateException.class, () -> service.convertirAVenta(3L, Fixtures.usuario("vendedor")));
        verify(ventas, never()).save(any());
    }

    @Test
    void noConvierteDosVecesLaMismaCotizacion() {
        when(cotizaciones.findById(3L)).thenReturn(Optional.of(Fixtures.cotizacion(cliente, 5)));
        when(ventas.existsByCotizacionId(3L)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.convertirAVenta(3L, Fixtures.usuario("vendedor")));
    }
}
