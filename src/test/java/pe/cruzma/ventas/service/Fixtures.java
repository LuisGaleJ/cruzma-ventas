package pe.cruzma.ventas.service;

import pe.cruzma.ventas.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de prueba compartidos por las pruebas de servicios y exportadores. */
public final class Fixtures {
    private Fixtures() { }

    public static Cliente cliente(TipoDocumento tipo, String doc, String nombre) {
        Cliente c = new Cliente();
        c.setTipoDoc(tipo);
        c.setNumDoc(doc);
        c.setRazonSocial(nombre);
        return c;
    }

    public static Usuario usuario(String username) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombres(username);
        return u;
    }

    public static Producto producto(String descripcion, String precio) {
        Producto p = new Producto();
        p.setCodigo("PRD-T");
        p.setDescripcion(descripcion);
        p.setUnidad("UND");
        p.setTipo(TipoProducto.PRODUCTO);
        p.setPrecioUnitario(new BigDecimal(precio));
        return p;
    }

    /** Cotización pendiente de S/ 960 (3 barandas de S/ 320) que vence en {@code diasParaVencer} días. */
    public static Cotizacion cotizacion(Cliente cliente, int diasParaVencer) {
        Cotizacion cot = new Cotizacion();
        cot.setNumero("COT-2026-0001");
        cot.setCliente(cliente);
        cot.setUsuario(usuario("vendedor"));
        cot.setFecha(LocalDate.now().minusDays(15 - diasParaVencer));
        cot.setFechaVencimiento(LocalDate.now().plusDays(diasParaVencer));
        Producto baranda = producto("Baranda de acero inoxidable 304", "320.00");
        cot.agregarDetalle(new DetalleCotizacion(baranda, 3, "3 m", baranda.getPrecioUnitario()));
        cot.calcularTotal();
        return cot;
    }

    public static Venta venta(Cliente cliente) {
        return Venta.desdeCotizacion(cotizacion(cliente, 10), usuario("vendedor"));
    }
}
