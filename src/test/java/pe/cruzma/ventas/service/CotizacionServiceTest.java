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
