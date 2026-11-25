package pe.cruzma.ventas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.cruzma.ventas.model.*;
import pe.cruzma.ventas.repository.*;

import java.math.BigDecimal;

/**
 * Carga los datos iniciales la primera vez que arranca el sistema (base sin roles):
 * roles y usuario {@code admin}; si {@code cruzma.demo.datos=true}, también los usuarios
 * {@code vendedor} y {@code caja}, clientes y el catálogo de productos de Grupo Cruzma.
 * <p>
 * La contraseña inicial viene de {@code ADMIN_PASSWORD}. En local, si no se define, es la de demostración
 * ({@value #CLAVE_DEMO}). Cuando el sistema está publicado en internet ({@code CRUZMA_PUBLICO=true})
 * no arranca con esa clave pública ni con una de menos de {@value #LARGO_MINIMO} caracteres.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    /** Contraseña de demostración publicada en el README; solo para uso local. */
    public static final String CLAVE_DEMO = "Cruzma2026";
    /** Largo mínimo de la contraseña inicial cuando el sistema está en internet. */
    public static final int LARGO_MINIMO = 10;
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ProductoRepository productos;
    private final PasswordEncoder encoder;
    private final String claveInicial;
    private final boolean datosDemo;
    private final boolean publico;

    /**
     * Crea la instancia con sus dependencias (inyección por constructor).
     *
     * @param roles     repositorio de roles
     * @param usuarios  repositorio de usuarios
     * @param clientes  repositorio de clientes
     * @param productos repositorio de productos
     * @param encoder   codificador BCrypt
     * @param claveInicial contraseña inicial de los usuarios ({@code ADMIN_PASSWORD})
     * @param datosDemo    si se cargan usuarios vendedor/caja, clientes y productos de ejemplo
     * @param publico      si el sistema está publicado en internet
     */
    public DataInitializer(RolRepository roles, UsuarioRepository usuarios, ClienteRepository clientes,
                           ProductoRepository productos, PasswordEncoder encoder,
                           @Value("${cruzma.inicial.password:" + CLAVE_DEMO + "}") String claveInicial,
                           @Value("${cruzma.demo.datos:true}") boolean datosDemo,
                           @Value("${cruzma.publico:false}") boolean publico) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.productos = productos;
        this.encoder = encoder;
        this.claveInicial = claveInicial;
        this.datosDemo = datosDemo;
        this.publico = publico;
    }

    /**
     * Verifica que la contraseña inicial sea aceptable: en internet no puede ser la de demostración
     * ni tener menos de {@value #LARGO_MINIMO} caracteres.
     *
     * @param publico si el sistema está publicado en internet
     * @param clave   contraseña inicial
     * @throws IllegalStateException si la contraseña no es aceptable para internet
     */
    static void validarClaveInicial(boolean publico, String clave) {
        if (!publico) return;
        if (clave == null || clave.isBlank() || CLAVE_DEMO.equals(clave) || clave.length() < LARGO_MINIMO) {
            throw new IllegalStateException("El sistema está publicado en internet (CRUZMA_PUBLICO=true): define la variable "
                    + "ADMIN_PASSWORD con una contraseña propia de al menos " + LARGO_MINIMO
                    + " caracteres (no la de demostración del README)");
        }
    }

    /**
     * Carga los datos iniciales si la base está vacía.
     *
     * @param args argumentos de arranque, no se usan
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (roles.count() > 0) return;
        validarClaveInicial(publico, claveInicial);
        Rol admin = roles.save(new Rol(Rol.ADMIN));
        Rol vendedor = roles.save(new Rol(Rol.VENDEDOR));
        Rol caja = roles.save(new Rol(Rol.CAJA));

        usuario("admin", "Gerente General", admin);
        if (!datosDemo) {
            log.info("Datos iniciales: roles y usuario admin (sin datos de ejemplo)");
            return;
        }
        usuario("vendedor", "Vendedor de Taller", vendedor);
        usuario("caja", "Caja y Administración", caja);

        cliente(TipoDocumento.DNI, "45871236", "María Castillo Ruiz", "Av. Grau 845, Piura", "969458712");
        cliente(TipoDocumento.RUC, "20601234567", "Restaurante El Chifa del Norte S.A.C.", "Jr. Tacna 320, Piura", "073305511");
        cliente(TipoDocumento.RUC, "20487654321", "Constructora Los Algarrobos S.A.", "Urb. Miraflores Mz B Lt 4, Castilla", "073348870");

        producto("PRD-001", "Baranda de acero inoxidable 304", "MTR", TipoProducto.PRODUCTO, "320.00");
        producto("PRD-002", "Mesa de trabajo inox para cocina", "NIU", TipoProducto.PRODUCTO, "1450.00");
        producto("PRD-003", "Campana extractora industrial inox", "NIU", TipoProducto.PRODUCTO, "2800.00");
        producto("PRD-004", "Puerta metálica de fierro con pintura", "NIU", TipoProducto.PRODUCTO, "1200.00");
        producto("PRD-005", "Reja de fierro para ventana", "MTK", TipoProducto.PRODUCTO, "180.00");
        producto("SRV-001", "Instalación en obra", "ZZ", TipoProducto.SERVICIO, "250.00");
        producto("SRV-002", "Mantenimiento y pulido de acero inoxidable", "ZZ", TipoProducto.SERVICIO, "150.00");
        log.info("Datos iniciales: roles, usuarios admin, vendedor y caja, clientes y productos de ejemplo");
    }

    private void usuario(String username, String nombres, Rol rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombres(nombres);
        u.setPasswordHash(encoder.encode(claveInicial));
        u.setRol(rol);
        usuarios.save(u);
    }

    private void cliente(TipoDocumento tipo, String doc, String nombre, String direccion, String telefono) {
        Cliente c = new Cliente();
        c.setTipoDoc(tipo);
        c.setNumDoc(doc);
        c.setRazonSocial(nombre);
        c.setDireccion(direccion);
        c.setTelefono(telefono);
        clientes.save(c);
    }

    private void producto(String codigo, String descripcion, String unidad, TipoProducto tipo, String precio) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setDescripcion(descripcion);
        p.setUnidad(unidad);
        p.setTipo(tipo);
        p.setPrecioUnitario(new BigDecimal(precio));
        productos.save(p);
    }
}
