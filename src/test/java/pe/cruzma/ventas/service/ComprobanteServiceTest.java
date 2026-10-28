package pe.cruzma.ventas.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.cruzma.ventas.integration.RespuestaOSE;
import pe.cruzma.ventas.integration.ServicioFacturacionOSE;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.ComprobanteRepository;
import pe.cruzma.ventas.repository.VentaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas del CU08 Emitir comprobante y CU09 Anular (TDD).
 * <p>
 * Los repositorios y el OSE se simulan con Mockito: la prueba verifica solo las reglas del servicio.
 */
@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {

    @Mock ComprobanteRepository comprobantes;
    @Mock VentaRepository ventas;
    @Mock ServicioFacturacionOSE ose;
    ComprobanteService service;

    @BeforeEach
    void preparar() {
        service = new ComprobanteService(comprobantes, ventas, ose, new BigDecimal("0.18"));
    }

    private Venta ventaConPago(Cliente cliente) {
        Venta v = Fixtures.venta(cliente);
        v.registrarPago(new Pago(new BigDecimal("480.00"), MedioPago.YAPE));
        when(ventas.findById(1L)).thenReturn(Optional.of(v));
        return v;
    }

    @Test
    void emiteBoletaConElSiguienteCorrelativo() {
        Venta v = ventaConPago(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"));
        when(comprobantes.ultimoCorrelativo("B001")).thenReturn(4);
        when(ose.enviar(any())).thenReturn(new RespuestaOSE(true, "cdr123", "aceptada"));

        RespuestaOSE r = service.emitir(1L, TipoComprobante.BOLETA);

        assertTrue(r.aceptado());
        Comprobante c = v.getComprobantes().get(0);
        assertEquals("B001-00000005", c.getNumero());
        assertEquals(EstadoSunat.ACEPTADO, c.getEstadoSunat());
        assertEquals(new BigDecimal("146.44"), c.getIgv());
        verify(comprobantes).save(c);
    }

    @Test
    void noEmiteFacturaAClienteConDni() {
        ventaConPago(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"));
        assertThrows(IllegalArgumentException.class, () -> service.emitir(1L, TipoComprobante.FACTURA));
        verifyNoInteractions(ose);
    }

    @Test
    void noEmiteSinPagoRegistrado() {
        Venta v = Fixtures.venta(Fixtures.cliente(TipoDocumento.RUC, "20601234567", "Restaurante El Chifa del Norte"));
        when(ventas.findById(1L)).thenReturn(Optional.of(v));
        assertThrows(IllegalStateException.class, () -> service.emitir(1L, TipoComprobante.FACTURA));
        verifyNoInteractions(ose);
    }

    @Test
    void comprobanteRechazadoQuedaRechazadoYSePuedeReintentar() {
        Venta v = ventaConPago(Fixtures.cliente(TipoDocumento.RUC, "20601234567", "Restaurante El Chifa del Norte"));
        when(ose.enviar(any())).thenReturn(new RespuestaOSE(false, null, "2017: documento no válido"));

        assertFalse(service.emitir(1L, TipoComprobante.FACTURA).aceptado());
        assertEquals(EstadoSunat.RECHAZADO, v.getComprobantes().get(0).getEstadoSunat());
        assertFalse(v.tieneComprobanteVigente());
    }

    @Test
    void anularEmiteNotaDeCreditoYAnulaLaVenta() {
        Venta v = ventaConPago(Fixtures.cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz"));
        when(ose.enviar(any())).thenReturn(new RespuestaOSE(true, "cdr1", "ok"));
        service.emitir(1L, TipoComprobante.BOLETA);
        Comprobante boleta = v.getComprobantes().get(0);
        when(comprobantes.findById(7L)).thenReturn(Optional.of(boleta));
        when(ose.comunicarBaja(any(), any())).thenReturn(new RespuestaOSE(true, "baja1", "ok"));

        service.anular(7L, "Cliente desistió");

        assertEquals(EstadoSunat.ANULADO, boleta.getEstadoSunat());
        assertEquals(EstadoVenta.ANULADA, v.getEstado());
        assertEquals(TipoComprobante.NOTA_CREDITO, v.getComprobantes().get(1).getTipo());
    }
}
