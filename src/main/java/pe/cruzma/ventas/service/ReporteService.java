package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import com.google.common.collect.ImmutableList;
import pe.cruzma.ventas.dto.DatosReporte;
import pe.cruzma.ventas.dto.DetalleReporte;
import pe.cruzma.ventas.dto.FilaReporte;
import pe.cruzma.ventas.dto.ResumenPanel;
import pe.cruzma.ventas.model.Cotizacion;
import pe.cruzma.ventas.model.DetalleCotizacion;
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
     * Indicadores del Dashboard (P2) para un rango de fechas.
     *
     * @param desde primer día, inclusive
     * @param hasta último día, inclusive
     * @return ventas, cobros y cotizaciones del período; saldo por cobrar y cotizaciones pendientes a la fecha
     * @throws IllegalArgumentException si {@code desde} es posterior a {@code hasta}
     */
    public ResumenPanel resumen(LocalDate desde, LocalDate hasta) {
        checkArgument(!desde.isAfter(hasta), "La fecha inicial no puede ser posterior a la final");
        List<Venta> lista = ventasDelPeriodo(desde, hasta);
        BigDecimal total = lista.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticket = lista.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(lista.size()), 2, RoundingMode.HALF_UP);
        BigDecimal cobrado = ventas.findAllByOrderByFechaDesc().stream()
                .filter(v -> v.getEstado() != EstadoVenta.ANULADA)
                .flatMap(v -> v.getPagos().stream())
                .filter(p -> enRango(p.getFecha().toLocalDate(), desde, hasta))
                .map(p -> p.getMonto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cotizacionesPeriodo = cotizaciones.findAllByOrderByIdDesc().stream()
                .filter(c -> c.getFecha() != null && enRango(c.getFecha(), desde, hasta))
                .count();
        return new ResumenPanel(desde, hasta, total, lista.size(), ticket, cobrado, cotizacionesPeriodo,
                cotizaciones.countByEstado(EstadoCotizacion.PENDIENTE), ventas.saldoPorCobrar());
    }

    private static boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
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
        List<DetalleReporte> detalle = ImmutableList.copyOf(detalle(lista));
        return new DatosReporte(desde, hasta, lista, total, ticket,
                ImmutableList.copyOf(porDia(lista)), ImmutableList.copyOf(porCliente(lista)),
                ImmutableList.copyOf(porProducto(detalle)), ImmutableList.copyOf(cuentasPorCobrar()), detalle);
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
     * R2 Ranking de productos por monto vendido (RF12), con monto original y descuento.
     *
     * @param detalle ítems vendidos del período (ver {@link #detalle(List)})
     * @return una fila por producto con unidades, monto cobrado y monto original, de mayor a menor
     */
    public List<FilaReporte> porProducto(List<DetalleReporte> detalle) {
        Map<String, BigDecimal> monto = new HashMap<>();
        Map<String, BigDecimal> original = new HashMap<>();
        Map<String, Long> unidades = new HashMap<>();
        for (DetalleReporte d : detalle) {
            monto.merge(d.producto(), d.montoCobrado(), BigDecimal::add);
            original.merge(d.producto(), d.montoOriginal(), BigDecimal::add);
            unidades.merge(d.producto(), (long) d.cantidad(), Long::sum);
        }
        return monto.entrySet().stream()
                .map(e -> new FilaReporte(e.getKey(), unidades.get(e.getKey()), e.getValue(), original.get(e.getKey())))
                .sorted(Comparator.comparing(FilaReporte::monto).reversed())
                .toList();
    }

    /**
     * Ítems vendidos en el período con su monto original, el descuento y lo cobrado.
     * <p>
     * El monto original sale de la cotización (precio x cantidad x metrado) y lo cobrado del ítem de la venta,
     * que ya incluye su parte del descuento global. Los ítems de la venta están en el mismo orden que los de la
     * cotización, porque se copian de ella al convertirla.
     *
     * @param lista ventas del período
     * @return una fila por ítem vendido, en orden cronológico
     */
    public List<DetalleReporte> detalle(List<Venta> lista) {
        List<DetalleReporte> filas = new ArrayList<>();
        for (Venta v : lista) {
            Cotizacion cot = v.getCotizacion();
            List<DetalleCotizacion> itemsCot = cot == null ? List.of() : cot.getDetalles();
            List<DetalleVenta> itemsVenta = v.getDetalles();
            for (int i = 0; i < itemsVenta.size(); i++) {
                DetalleVenta d = itemsVenta.get(i);
                DetalleCotizacion dc = i < itemsCot.size() ? itemsCot.get(i) : null;
                BigDecimal original = dc == null ? d.getSubtotal() : dc.getBruto();
                filas.add(new DetalleReporte(v.getFecha().toLocalDate(), v.getId(), cot == null ? "" : cot.getNumero(),
                        v.getCliente().getRazonSocial(), d.getProducto().getDescripcion(), d.getProducto().getUnidadSunat(),
                        d.getCantidad(), dc == null || dc.getMedidas() == null ? "" : dc.getMedidas(), d.getPrecio(),
                        original, dc == null ? BigDecimal.ZERO : dc.getDescuento(),
                        cot == null ? BigDecimal.ZERO : cot.getDescuento(),
                        original.subtract(d.getSubtotal()), d.getSubtotal()));
            }
        }
        return filas;
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
        Map<String, BigDecimal> original = new HashMap<>();
        Map<String, Long> cantidad = new HashMap<>();
        for (Venta v : lista) {
            String k = clave.apply(v);
            monto.merge(k, v.getTotal(), BigDecimal::add);
            original.merge(k, v.getMontoOriginal(), BigDecimal::add);
            cantidad.merge(k, 1L, Long::sum);
        }
        List<FilaReporte> filas = new ArrayList<>(monto.entrySet().stream()
                .map(e -> new FilaReporte(e.getKey(), cantidad.get(e.getKey()), e.getValue(), original.get(e.getKey()))).toList());
        if (ordenarPorMonto) filas.sort(Comparator.comparing(FilaReporte::monto).reversed());
        return filas;
    }
}
