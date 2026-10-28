package pe.cruzma.ventas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.integration.RespuestaOSE;
import pe.cruzma.ventas.integration.ServicioFacturacionOSE;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.ComprobanteRepository;
import pe.cruzma.ventas.repository.VentaRepository;
import pe.cruzma.ventas.util.Auditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Emisión y anulación de comprobantes electrónicos (CU08, CU09).
 * <p>
 * Asigna serie y correlativo, separa el IGV, envía el comprobante al OSE a
 * través de {@link pe.cruzma.ventas.integration.ServicioFacturacionOSE} y
 * guarda su respuesta.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class ComprobanteService {
    private static final Logger LOG = LoggerFactory.getLogger(ComprobanteService.class);
    private final ComprobanteRepository comprobantes;
    private final VentaRepository ventas;
    private final ServicioFacturacionOSE ose;
    private final BigDecimal tasaIgv;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param comprobantes repositorio de comprobantes
     * @param ventas       repositorio de ventas
     * @param ose          proveedor de facturación electrónica
     * @param tasaIgv      tasa del IGV, propiedad {@code cruzma.igv} (0.18)
     */
    public ComprobanteService(ComprobanteRepository comprobantes, VentaRepository ventas,
                              ServicioFacturacionOSE ose, @Value("${cruzma.igv}") BigDecimal tasaIgv) {
        this.comprobantes = comprobantes;
        this.ventas = ventas;
        this.ose = ose;
        this.tasaIgv = tasaIgv;
    }

    /** {@return comprobantes, del más reciente al más antiguo} */
    public List<Comprobante> listar() { return comprobantes.findAllByOrderByFechaEmisionDesc(); }

    /**
     * Obtiene un comprobante.
     *
     * @param id id del comprobante
     * @return el comprobante
     * @throws IllegalArgumentException si no existe
     */
    public Comprobante obtener(Long id) {
        return comprobantes.findById(id).orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
    }

    /**
     * CU08 Emitir comprobante electrónico (RF09, RF11).
     * <p>
     * Precondición: la venta tiene al menos un pago y no tiene otro comprobante vigente.
     *
     * @param idVenta venta a facturar
     * @param tipo    {@code FACTURA} (solo clientes con RUC) o {@code BOLETA}
     * @return la respuesta del OSE
     * @throws IllegalArgumentException si la venta no existe o el tipo no corresponde al cliente
     * @throws IllegalStateException    si la venta está anulada, no tiene pagos o ya tiene comprobante
     */
    @Transactional
    public RespuestaOSE emitir(Long idVenta, TipoComprobante tipo) {
        Venta v = ventas.findById(idVenta).orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        if (tipo == TipoComprobante.NOTA_CREDITO) throw new IllegalArgumentException("La nota de crédito se genera al anular");
        if (v.getEstado() == EstadoVenta.ANULADA) throw new IllegalStateException("La venta está anulada");
        if (v.getPagos().isEmpty()) throw new IllegalStateException("Registra al menos un adelanto antes de emitir el comprobante");
        if (v.tieneComprobanteVigente()) throw new IllegalStateException("La venta ya tiene un comprobante aceptado");
        if (tipo == TipoComprobante.FACTURA && v.getCliente().getTipoDoc() != TipoDocumento.RUC) {
            throw new IllegalArgumentException("Para emitir factura el cliente debe tener RUC; emite una boleta");
        }

        Comprobante c = new Comprobante(v, tipo, comprobantes.ultimoCorrelativo(tipo.getSerie()) + 1, v.getTotal(), tasaIgv);
        comprobantes.save(c);
        RespuestaOSE r = ose.enviar(c);
        c.marcarRespuesta(r.aceptado(), r.hashCdr());
        v.getComprobantes().add(c);
        if (r.aceptado()) {
            LOG.info("Comprobante {} aceptado por el OSE", c.getNumero());
        } else {
            LOG.warn("Comprobante {} rechazado: {}", c.getNumero(), r.mensaje());
        }
        Auditoria.registrar("EMITIR_COMPROBANTE", c.getNumero() + " venta=" + idVenta + " estado=" + c.getEstadoSunat());
        return r;
    }

    /**
     * CU09 Anular comprobante con nota de crédito (RF10).
     * <p>
     * El comprobante no se borra: cambia a {@code ANULADO} y la venta a {@code ANULADA}.
     *
     * @param idComprobante factura o boleta aceptada
     * @param motivo        motivo de la anulación; si está vacío se usa uno genérico
     * @return la respuesta del OSE a la baja
     * @throws IllegalStateException si el comprobante no es una factura o boleta aceptada
     */
    @Transactional
    public RespuestaOSE anular(Long idComprobante, String motivo) {
        Comprobante original = obtener(idComprobante);
        if (original.getEstadoSunat() != EstadoSunat.ACEPTADO || original.getTipo() == TipoComprobante.NOTA_CREDITO) {
            throw new IllegalStateException("Solo se anulan facturas o boletas aceptadas");
        }
        Venta v = original.getVenta();
        Comprobante nc = new Comprobante(v, TipoComprobante.NOTA_CREDITO,
                comprobantes.ultimoCorrelativo(TipoComprobante.NOTA_CREDITO.getSerie()) + 1, original.getTotal(), tasaIgv);
        comprobantes.save(nc);
        RespuestaOSE r = ose.comunicarBaja(original, motivo == null || motivo.isBlank() ? "Anulación de la operación" : motivo);
        nc.marcarRespuesta(r.aceptado(), r.hashCdr());
        if (r.aceptado()) {
            original.anular();
            v.setEstado(EstadoVenta.ANULADA);
        }
        v.getComprobantes().add(nc);
        Auditoria.registrar("ANULAR_COMPROBANTE", original.getNumero() + " con " + nc.getNumero() + " aceptado=" + r.aceptado());
        return r;
    }
}
