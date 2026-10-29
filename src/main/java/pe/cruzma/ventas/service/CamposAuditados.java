package pe.cruzma.ventas.service;

import com.google.common.collect.ImmutableList;
import org.hibernate.envers.RevisionType;
import pe.cruzma.ventas.dto.CambioAuditado;
import pe.cruzma.ventas.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Catálogo de entidades auditadas y comparación de versiones.
 * <p>
 * Indica, para cada entidad con {@code @Audited}, qué campos se muestran en el historial
 * y convierte las filas que devuelve Envers en {@link CambioAuditado} con el detalle
 * "campo: anterior → nuevo". No accede a la base de datos, por eso se prueba con
 * pruebas unitarias simples. Nunca muestra contraseñas: {@code password_hash} no se audita.
 *
 * @author Luis Osmin Galecio Jaramillo
 * @since 0.4.0
 */
public final class CamposAuditados {

    /** Campo mostrado en el historial: etiqueta y forma de leer su valor. */
    record Campo(String etiqueta, Function<Object, Object> valor) { }

    /**
     * Describe una entidad auditada.
     *
     * @param nombre nombre mostrado y usado en el filtro
     * @param clase  clase JPA auditada
     * @param id     lectura de la clave primaria
     * @param titulo texto que identifica al registro
     * @param campos campos que se comparan entre versiones
     */
    public record Descriptor(String nombre, Class<?> clase, Function<Object, Object> id,
                             Function<Object, String> titulo, List<Campo> campos) { }

    private static final List<Descriptor> ENTIDADES = ImmutableList.of(
            de("Cliente", Cliente.class, Cliente::getId, Cliente::getRazonSocial,
                    campo("Tipo doc.", Cliente.class, Cliente::getTipoDoc),
                    campo("Nro. doc.", Cliente.class, Cliente::getNumDoc),
                    campo("Razón social", Cliente.class, Cliente::getRazonSocial),
                    campo("Dirección", Cliente.class, Cliente::getDireccion),
                    campo("Teléfono", Cliente.class, Cliente::getTelefono),
                    campo("Email", Cliente.class, Cliente::getEmail)),
            de("Producto", Producto.class, Producto::getId, p -> p.getCodigo() + " " + p.getDescripcion(),
                    campo("Código", Producto.class, Producto::getCodigo),
                    campo("Descripción", Producto.class, Producto::getDescripcion),
                    campo("Unidad", Producto.class, Producto::getUnidad),
                    campo("Tipo", Producto.class, Producto::getTipo),
                    campo("Precio unitario", Producto.class, Producto::getPrecioUnitario)),
            de("Cotizacion", Cotizacion.class, Cotizacion::getId, Cotizacion::getNumero,
                    campo("Estado", Cotizacion.class, Cotizacion::getEstado),
                    campo("Fecha", Cotizacion.class, Cotizacion::getFecha),
                    campo("Vencimiento", Cotizacion.class, Cotizacion::getFechaVencimiento),
                    campo("Total", Cotizacion.class, Cotizacion::getTotal)),
            de("Venta", Venta.class, Venta::getId, v -> "Venta N.° " + v.getId(),
                    campo("Estado", Venta.class, Venta::getEstado),
                    campo("Total", Venta.class, Venta::getTotal),
                    campo("Saldo", Venta.class, Venta::getSaldo)),
            de("Pago", Pago.class, Pago::getId, p -> "Pago N.° " + p.getId(),
                    campo("Monto", Pago.class, Pago::getMonto),
                    campo("Medio", Pago.class, Pago::getMedio),
                    campo("Fecha", Pago.class, Pago::getFecha)),
            de("Comprobante", Comprobante.class, Comprobante::getId, Comprobante::getNumero,
                    campo("Tipo", Comprobante.class, Comprobante::getTipo),
                    campo("Estado SUNAT", Comprobante.class, Comprobante::getEstadoSunat),
                    campo("Total", Comprobante.class, Comprobante::getTotal),
                    campo("Hash CDR", Comprobante.class, Comprobante::getHashCdr)),
            de("Usuario", Usuario.class, Usuario::getId, Usuario::getUsername,
                    campo("Usuario", Usuario.class, Usuario::getUsername),
                    campo("Nombres", Usuario.class, Usuario::getNombres),
                    campo("Activo", Usuario.class, u -> u.isActivo() ? "Sí" : "No")));

    private CamposAuditados() { }

    /** {@return nombres de las entidades auditadas, para el filtro de la pantalla} */
    public static List<String> nombres() {
        return ENTIDADES.stream().map(Descriptor::nombre).toList();
    }

    /**
     * Devuelve las entidades a consultar según el filtro.
     *
     * @param entidad nombre de una entidad, o vacío/{@code null} para todas
     * @return descriptores que coinciden
     * @throws IllegalArgumentException si el nombre no corresponde a una entidad auditada
     */
    public static List<Descriptor> filtrar(String entidad) {
        if (entidad == null || entidad.isBlank()) return ENTIDADES;
        return ENTIDADES.stream().filter(d -> d.nombre().equals(entidad)).findFirst()
                .map(List::of)
                .orElseThrow(() -> new IllegalArgumentException("Entidad no auditada: " + entidad));
    }

    /**
     * Convierte las filas de Envers de una entidad en cambios legibles.
     * <p>
     * Cada fila es {@code [entidad en esa revisión, Revision, RevisionType]} y deben venir
     * ordenadas por revisión ascendente, para comparar cada versión con la anterior del mismo registro.
     *
     * @param d     descriptor de la entidad
     * @param filas filas devueltas por {@code AuditQuery.forRevisionsOfEntity(clase, false, true)}
     * @return un cambio por fila, en el mismo orden
     */
    public static List<CambioAuditado> convertir(Descriptor d, List<Object[]> filas) {
        Map<Object, Object> anteriores = new HashMap<>();
        List<CambioAuditado> cambios = new ArrayList<>(filas.size());
        for (Object[] fila : filas) {
            Object actual = fila[0];
            Revision rev = (Revision) fila[1];
            RevisionType tipo = (RevisionType) fila[2];
            Object id = d.id().apply(actual);
            Object anterior = anteriores.put(id, actual);
            cambios.add(new CambioAuditado(rev.getId(), rev.getFecha(), rev.getUsuario(), d.nombre(), id,
                    d.titulo().apply(actual), operacion(tipo), detalles(d, tipo, anterior, actual)));
        }
        return cambios;
    }

    static List<String> detalles(Descriptor d, RevisionType tipo, Object anterior, Object actual) {
        List<String> lineas = new ArrayList<>();
        for (Campo c : d.campos()) {
            Object nuevo = c.valor().apply(actual);
            if (tipo == RevisionType.ADD || anterior == null) {
                if (nuevo != null) lineas.add(c.etiqueta() + ": " + texto(nuevo));
            } else if (tipo == RevisionType.MOD) {
                Object viejo = c.valor().apply(anterior);
                if (!iguales(viejo, nuevo)) lineas.add(c.etiqueta() + ": " + texto(viejo) + " → " + texto(nuevo));
            }
        }
        if (tipo == RevisionType.MOD && lineas.isEmpty()) lineas.add("Cambio en datos relacionados");
        return lineas;
    }

    static boolean iguales(Object a, Object b) {
        if (a instanceof BigDecimal x && b instanceof BigDecimal y) return x.compareTo(y) == 0;
        return Objects.equals(a, b);
    }

    private static String texto(Object v) {
        return v == null ? "(vacío)" : v.toString();
    }

    private static String operacion(RevisionType tipo) {
        return switch (tipo) {
            case ADD -> "Creación";
            case MOD -> "Modificación";
            case DEL -> "Eliminación";
        };
    }

    private static <T> Campo campo(String etiqueta, Class<T> clase, Function<T, ?> f) {
        return new Campo(etiqueta, o -> f.apply(clase.cast(o)));
    }

    private static <T> Descriptor de(String nombre, Class<T> clase, Function<T, ?> id,
                                     Function<T, String> titulo, Campo... campos) {
        return new Descriptor(nombre, clase, o -> id.apply(clase.cast(o)),
                o -> titulo.apply(clase.cast(o)), List.of(campos));
    }
}
