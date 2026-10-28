package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableList;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.FilaReporte;
import pe.cruzma.ventas.dto.ResumenPanel;
import pe.cruzma.ventas.model.DetalleVenta;
import pe.cruzma.ventas.model.EstadoCotizacion;
import pe.cruzma.ventas.model.EstadoVenta;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.repository.CotizacionRepository;
import pe.cruzma.ventas.repository.VentaRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Cálculo de los indicadores del panel y de los reportes R1, R2 y R3.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class ReporteService {
    private final VentaRepository ventas;
    private final CotizacionRepository cotizaciones;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param ventas       repositorio de ventas
     * @param cotizaciones repositorio de cotizaciones
     */
    public ReporteService(VentaRepository ventas, CotizacionRepository cotizaciones) {
        this.ventas = ventas;
        this.cotizaciones = cotizaciones;
    }

    /**
     * Indicadores del panel principal (P2).
     *
     * @return ventas de hoy y del mes, cotizaciones pendientes y saldo por cobrar
     */
    public ResumenPanel resumen() {
        LocalDate hoy = LocalDate.now();
        return new ResumenPanel(
                ventas.totalVendido(hoy.atStartOfDay(), hoy.atTime(LocalTime.MAX)),
                ventas.totalVendido(hoy.withDayOfMonth(1).atStartOfDay(), hoy.atTime(LocalTime.MAX)),
                cotizaciones.countByEstado(EstadoCotizacion.PENDIENTE),
                ventas.saldoPorCobrar());
    }

    /**
     * Ventas no anuladas de un período.
     *
     * @param desde primer día, inclusive
     * @param hasta último día, inclusive
     * @return ventas en orden cronológico
     */
    public List<Venta> ventasDelPeriodo(LocalDate desde, LocalDate hasta) {
        return ventas.findByFechaBetweenAndEstadoNotOrderByFechaAsc(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), EstadoVenta.ANULADA);
    }

    /**
     * Arma todos los reportes de un período en un solo objeto inmutable.
     *
     * @param desde primer día, inclusive
     * @param hasta último día, inclusive
     * @return datos de R1, R2 y R3
     * @throws IllegalArgumentException si {@code desde} es posterior a {@code hasta}
     */
    public DatosReporte armar(LocalDate desde, LocalDate hasta) {
        checkArgument(!desde.isAfter(hasta),
                "La fecha inicial no puede ser posterior a la final");
        List<Venta> lista = ImmutableList.copyOf(ventasDelPeriodo(desde, hasta));
        BigDecimal total = lista.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticket = lista.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP);
        return new DatosReporte(desde, hasta, lista, total, ticket,
                ImmutableList.copyOf(porDia(lista)), ImmutableList.copyOf(porCliente(lista)),
                ImmutableList.copyOf(porProducto(lista)), ImmutableList.copyOf(cuentasPorCobrar()));
    }

    /**
     * R1 Ventas por período, agrupadas por día (RF12).
     *
     * @param lista ventas del período
     * @return una fila por día, en orden cronológico
     */
    public List<FilaReporte> porDia(List<Venta> lista) {
        return agrupar(lista, v -> v.getFecha().toLocalDate().toString(), false);
    }

    /**
     * R2 Ranking de clientes por monto comprado (RF12).
     *
     * @param lista ventas del período
     * @return una fila por cliente, de mayor a menor monto
     */
    public List<FilaReporte> porCliente(List<Venta> lista) {
        return agrupar(lista, v -> v.getCliente().getRazonSocial(), true);
    }

    /**
     * R2 Ranking de productos por monto vendido (RF12).
     *
     * @param lista ventas del período
     * @return una fila por producto con unidades y monto, de mayor a menor
     */
    public List<FilaReporte> porProducto(List<Venta> lista) {
        Map<String, BigDecimal> monto = new HashMap<>();
        Map<String, Long> unidades = new HashMap<>();
        for (Venta v : lista) {
            for (DetalleVenta d : v.getDetalles()) {
                String k = d.getProducto().getDescripcion();
                monto.merge(k, d.getSubtotal(), BigDecimal::add);
                unidades.merge(k, (long) d.getCantidad(), Long::sum);
            }
        }
        return monto.entrySet().stream()
                .map(e -> new FilaReporte(e.getKey(), unidades.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(FilaReporte::monto).reversed())
                .toList();
    }

    /** {@return R3 Cuentas por cobrar: ventas con saldo pendiente (RF13)} */
    public List<Venta> cuentasPorCobrar() { return ventas.conSaldoPendiente(); }

    /**
     * Agrupa ventas por una clave y suma montos y cantidades.
     *
     * @param lista           ventas a agrupar
     * @param clave           función que obtiene la clave de cada venta
     * @param ordenarPorMonto {@code true} para ordenar de mayor a menor monto
     * @return una fila por clave
     */
    private List<FilaReporte> agrupar(List<Venta> lista, Function<Venta, String> clave, boolean ordenarPorMonto) {
        Map<String, BigDecimal> monto = new LinkedHashMap<>();
        Map<String, Long> cantidad = new HashMap<>();
        for (Venta v : lista) {
            String k = clave.apply(v);
            monto.merge(k, v.getTotal(), BigDecimal::add);
            cantidad.merge(k, 1L, Long::sum);
        }
        List<FilaReporte> filas = new ArrayList<>(monto.entrySet().stream()
                .map(e -> new FilaReporte(e.getKey(), cantidad.get(e.getKey()), e.getValue())).toList());
        if (ordenarPorMonto) filas.sort(Comparator.comparing(FilaReporte::monto).reversed());
        return filas;
    }
}
