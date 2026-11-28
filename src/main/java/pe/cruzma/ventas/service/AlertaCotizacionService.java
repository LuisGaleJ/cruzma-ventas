package pe.cruzma.ventas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.Cotizacion;
import pe.cruzma.ventas.model.EstadoCotizacion;
import pe.cruzma.ventas.repository.CotizacionRepository;
import pe.cruzma.ventas.util.Auditoria;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Alertas de cotizaciones por vencer y vencimiento automático.
 * <ul>
 *   <li>«Por vencer»: pendientes cuya fecha de vencimiento está entre hoy y los próximos
 *       {@code cruzma.cotizacion.dias-aviso} días (3 por defecto). Se muestran en el Dashboard, en el menú y en la
 *       lista de cotizaciones.</li>
 *   <li>Cada día a las 00:05 (hora de Lima) las pendientes cuya fecha ya pasó se marcan como VENCIDA.</li>
 * </ul>
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.7.0
 */
@Service
public class AlertaCotizacionService {
    private static final Logger log = LoggerFactory.getLogger(AlertaCotizacionService.class);

    private final CotizacionRepository cotizaciones;
    private final int diasAviso;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param cotizaciones repositorio de cotizaciones
     * @param diasAviso    días de anticipación del aviso, propiedad {@code cruzma.cotizacion.dias-aviso}
     */
    public AlertaCotizacionService(CotizacionRepository cotizaciones,
                                   @Value("${cruzma.cotizacion.dias-aviso:3}") int diasAviso) {
        checkArgument(diasAviso >= 0 && diasAviso <= CotizacionService.DIAS_VALIDEZ,
                "cruzma.cotizacion.dias-aviso debe estar entre 0 y %s", CotizacionService.DIAS_VALIDEZ);
        this.cotizaciones = cotizaciones;
        this.diasAviso = diasAviso;
    }

    /** {@return con cuántos días de anticipación se avisa} */
    public int diasAviso() { return diasAviso; }

    /** {@return cotizaciones pendientes que vencen entre hoy y los próximos días de aviso, la más urgente primero} */
    public List<Cotizacion> porVencer() {
        LocalDate hoy = LocalDate.now();
        return cotizaciones.findByEstadoAndFechaVencimientoBetweenOrderByFechaVencimientoAsc(
                EstadoCotizacion.PENDIENTE, hoy, hoy.plusDays(diasAviso));
    }

    /** {@return cantidad de cotizaciones por vencer, para el contador del menú} */
    public long contarPorVencer() {
        LocalDate hoy = LocalDate.now();
        return cotizaciones.countByEstadoAndFechaVencimientoBetween(EstadoCotizacion.PENDIENTE, hoy, hoy.plusDays(diasAviso));
    }

    /**
     * Marca como VENCIDA las cotizaciones pendientes cuya fecha de vencimiento ya pasó.
     * Se ejecuta sola cada día a las 00:05 (hora de Lima).
     *
     * @return cantidad de cotizaciones marcadas
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Lima")
    @Transactional
    public int marcarVencidas() {
        List<Cotizacion> vencidas = cotizaciones.findByEstadoAndFechaVencimientoBefore(EstadoCotizacion.PENDIENTE, LocalDate.now());
        vencidas.forEach(c -> c.setEstado(EstadoCotizacion.VENCIDA));
        if (!vencidas.isEmpty()) {
            log.info("Cotizaciones marcadas como vencidas: {}", vencidas.size());
            Auditoria.registrar("sistema", "VENCER_COTIZACIONES",
                    vencidas.stream().map(Cotizacion::getNumero).toList().toString());
        }
        return vencidas.size();
    }
}
