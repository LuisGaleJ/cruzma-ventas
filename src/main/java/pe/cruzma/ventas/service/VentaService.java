package pe.cruzma.ventas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.MedioPago;
import pe.cruzma.ventas.model.Pago;
import pe.cruzma.ventas.model.Venta;
import pe.cruzma.ventas.repository.VentaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Consulta de ventas y registro de pagos (RF08).
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.1.0
 */
@Service
public class VentaService {
    private final VentaRepository repo;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param repo repositorio de ventas
     */
    public VentaService(VentaRepository repo) { this.repo = repo; }

    /** {@return ventas, de la más reciente a la más antigua} */
    public List<Venta> listar() { return repo.findAllByOrderByFechaDesc(); }

    /**
     * Obtiene una venta.
     *
     * @param id id de la venta
     * @return la venta
     * @throws IllegalArgumentException si no existe
     */
    public Venta obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
    }

    /**
     * Registra un adelanto o pago; el saldo y el estado se recalculan en la entidad (RF08).
     *
     * @param idVenta venta que se cobra
     * @param monto   monto pagado, mayor a cero y no mayor al saldo
     * @param medio   medio de pago
     * @return la venta actualizada
     * @throws IllegalArgumentException si el monto no es válido o supera el saldo
     * @throws IllegalStateException    si la venta está anulada
     */
    @Transactional
    public Venta registrarPago(Long idVenta, BigDecimal monto, MedioPago medio) {
        Venta v = obtener(idVenta);
        v.registrarPago(new Pago(monto, medio));
        return v;
    }
}
